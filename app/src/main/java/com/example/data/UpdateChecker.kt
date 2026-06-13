package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

// JSON response model
data class UpdateConfigResponse(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val updateUrl: String,
    val updateMessage: String,
    val isMandatory: Boolean = true
)

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    object NoInternet : UpdateState
    data class UpdateRequired(
        val latestVersionCode: Int,
        val latestVersionName: String,
        val updateUrl: String,
        val updateMessage: String
    ) : UpdateState
    object UpToDate : UpdateState
    data class Error(val message: String) : UpdateState
}

class UpdateChecker(private val context: Context) {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    // Default configuration: Official destination path
    private val _githubOwner = MutableStateFlow("mohndng")
    val githubOwner: StateFlow<String> = _githubOwner.asStateFlow()

    private val _githubRepo = MutableStateFlow("gmail-extension")
    val githubRepo: StateFlow<String> = _githubRepo.asStateFlow()

    // Current local version info
    val localVersionCode: Int = 1
    val localVersionName: String = "1.0"

    var isMockedOutdated: Boolean = false
        set(value) {
            field = value
            if (value) {
                _updateState.value = UpdateState.UpdateRequired(
                    latestVersionCode = 2,
                    latestVersionName = "2.0-SIMULATED",
                    updateUrl = "https://www.github.com/mohndng/gmail-extension/",
                    updateMessage = "MANDATORY UPDATE DEMONSTRATION: As requested, when this repository on GitHub is updated (or simulated here), this pop-up appears in the app with absolutely no close button since it is mandatory."
                )
            } else {
                _updateState.value = UpdateState.UpToDate
            }
        }

    private val okHttpClient = OkHttpClient.Builder().build()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(UpdateConfigResponse::class.java)

    fun setGithubConfig(owner: String, repo: String) {
        _githubOwner.value = owner.trim()
        _githubRepo.value = repo.trim()
    }

    /**
     * Checks if internet connectivity is active on the device.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    /**
     * Performs an asynchronous update check using OkHttp.
     */
    suspend fun runUpdateCheck(force: Boolean = false) {
        if (_updateState.value is UpdateState.UpdateRequired && !force) {
            // Already identified that an update is mandatory, stay in that blocking state
            return
        }

        val previousState = _updateState.value
        _updateState.value = UpdateState.Checking

        // 1. Check Internet Connection first!
        if (!isNetworkAvailable()) {
            if (previousState == UpdateState.Idle || force) {
                _updateState.value = UpdateState.NoInternet
            } else {
                _updateState.value = previousState
            }
            return
        }

        val url = "https://raw.githubusercontent.com/${_githubOwner.value}/${_githubRepo.value}/main/update_config.json"
        
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                // Disable response caching to ensure we always get the updated raw page from Github
                .header("Cache-Control", "no-cache")
                .build()

            try {
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        // Fallback check: Some repositories use 'master' as their default branch instead of 'main'
                        val fallbackUrl = "https://raw.githubusercontent.com/${_githubOwner.value}/${_githubRepo.value}/master/update_config.json"
                        val fallbackRequest = Request.Builder()
                            .url(fallbackUrl)
                            .header("Cache-Control", "no-cache")
                            .build()
                        
                        okHttpClient.newCall(fallbackRequest).execute().use { fbResponse ->
                            if (!fbResponse.isSuccessful) {
                                throw IOException("Failed to load configuration from primary 'main' or fallback 'master' branches of Github.")
                            }
                            processResponseBody(fbResponse.body?.string())
                        }
                    } else {
                        processResponseBody(response.body?.string())
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateChecker", "Error checking for updates: ${e.message}", e)
                if (previousState == UpdateState.Idle || force) {
                    _updateState.value = UpdateState.Error(e.localizedMessage ?: "Unknown connection error")
                } else {
                    _updateState.value = previousState
                }
            }
        }
    }

    private fun processResponseBody(jsonString: String?) {
        if (jsonString.isNullOrEmpty()) {
            _updateState.value = UpdateState.Error("Empty update configurations received")
            return
        }

        try {
            val responseObj = adapter.fromJson(jsonString)
            if (responseObj != null) {
                if (responseObj.latestVersionCode > localVersionCode) {
                    _updateState.value = UpdateState.UpdateRequired(
                        latestVersionCode = responseObj.latestVersionCode,
                        latestVersionName = responseObj.latestVersionName,
                        updateUrl = "https://www.github.com/mohndng/gmail-extension/",
                        updateMessage = responseObj.updateMessage
                    )
                } else {
                    _updateState.value = UpdateState.UpToDate
                }
            } else {
                _updateState.value = UpdateState.Error("Failed to parse update payload")
            }
        } catch (e: Exception) {
            Log.e("UpdateChecker", "Parsing error: ${e.message}", e)
            _updateState.value = UpdateState.Error("Parsing error: ${e.message}")
        }
    }
}
