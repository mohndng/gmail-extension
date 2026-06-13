package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.EmailRepository
import com.example.ui.EmailGeneratorViewModel
import com.example.ui.EmailGeneratorViewModelFactory
import com.example.ui.screens.EmailGeneratorScreen
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.screens.WelcomeSplashScreen
import com.example.data.UpdateState
import com.example.ui.screens.MandatoryUpdateScreen
import com.example.ui.screens.NoConnectionScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room local database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EmailRepository(database.emailDao())
        val updateChecker = com.example.data.UpdateChecker(applicationContext)

        // Create ViewModel with Constructor Repository Injection
        val viewModel = ViewModelProvider(
            this,
            EmailGeneratorViewModelFactory(repository, updateChecker)
        )[EmailGeneratorViewModel::class.java]

        setContent {
            val themePreset by viewModel.currentThemePreset.collectAsState()
            val updateState by viewModel.updateState.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            MyApplicationTheme(preset = themePreset) {
                if (showSplash) {
                    WelcomeSplashScreen(
                        onDismiss = { showSplash = false }
                    )
                } else {
                    when (val state = updateState) {
                        is UpdateState.UpdateRequired -> {
                            MandatoryUpdateScreen(
                                latestVersionName = state.latestVersionName,
                                updateUrl = state.updateUrl,
                                updateMessage = state.updateMessage
                            )
                        }
                        is UpdateState.NoInternet -> {
                            NoConnectionScreen(
                                onRetry = { viewModel.checkForUpdates(force = true) }
                            )
                        }
                        else -> {
                            Scaffold(
                                modifier = Modifier.fillMaxSize()
                            ) { innerPadding ->
                                EmailGeneratorScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
