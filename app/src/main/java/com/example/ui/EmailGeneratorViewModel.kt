package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EmailRepository
import com.example.data.GeneratedEmail
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Random
import com.example.ui.theme.AppThemePreset
import com.example.data.UpdateChecker
import com.example.data.UpdateState

enum class GenerationType {
    DOTS_ONLY,
    PLUS_RANDOM,
    PLUS_SEQUENTIAL,
    PLUS_COMMON_PRESETS,
    COMBINED_DOT_AND_PLUS
}

data class GeneratedEmailItem(
    val email: String,
    val type: String,
    val isSaved: Boolean = false
)

class EmailGeneratorViewModel(
    private val repository: EmailRepository,
    val updateChecker: UpdateChecker
) : ViewModel() {

    // Update state flow exposed to screens
    val updateState: StateFlow<UpdateState> = updateChecker.updateState

    fun checkForUpdates(force: Boolean = false) {
        viewModelScope.launch {
            updateChecker.runUpdateCheck(force)
        }
    }

    fun setGithubConfig(owner: String, repo: String) {
        updateChecker.setGithubConfig(owner, repo)
    }

    fun toggleMandatoryUpdateSimulation(enabled: Boolean) {
        updateChecker.isMockedOutdated = enabled
    }

    // Aesthetic Custom Theme Preset State
    private val _currentThemePreset = MutableStateFlow(AppThemePreset.AMETHYST)
    val currentThemePreset: StateFlow<AppThemePreset> = _currentThemePreset.asStateFlow()

    fun setCurrentThemePreset(preset: AppThemePreset) {
        _currentThemePreset.value = preset
    }

    // Inputs
    private val _originalEmail = MutableStateFlow("youremail@gmail.com")
    val originalEmail: StateFlow<String> = _originalEmail.asStateFlow()

    private val _generationType = MutableStateFlow(GenerationType.PLUS_COMMON_PRESETS)
    val generationType: StateFlow<GenerationType> = _generationType.asStateFlow()

    private val _randomSuffixLength = MutableStateFlow(6)
    val randomSuffixLength: StateFlow<Int> = _randomSuffixLength.asStateFlow()

    private val _sequentialCount = MutableStateFlow(10)
    val sequentialCount: StateFlow<Int> = _sequentialCount.asStateFlow()

    private val _customTag = MutableStateFlow("")
    val customTag: StateFlow<String> = _customTag.asStateFlow()

    // Generated variations (Runtime only, visible in results)
    private val _generatedVariations = MutableStateFlow<List<GeneratedEmailItem>>(emptyList())
    val generatedVariations: StateFlow<List<GeneratedEmailItem>> = _generatedVariations.asStateFlow()

    // Database search/filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Saved Emails loaded from Room
    val savedEmails: StateFlow<List<GeneratedEmail>> = _searchQuery
        .debounce(100)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                repository.allEmails
            } else {
                repository.searchEmails(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Run initial generation on load using default email
        generateVariations()
        // Automatically run mandatory update check on startup
        checkForUpdates()
        
        // Setup periodic check every 3 minutes to automatically pop up if an update is found while in-use
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(180_000) // Check every 3 minutes
                try {
                    updateChecker.runUpdateCheck()
                } catch (e: Exception) {
                    // Ignore background periodic check failures gracefully to not disrupt session
                }
            }
        }
    }

    fun setOriginalEmail(email: String) {
        _originalEmail.value = email
    }

    fun setGenerationType(type: GenerationType) {
        _generationType.value = type
    }

    fun setRandomSuffixLength(length: Int) {
        _randomSuffixLength.value = length.coerceIn(3, 15)
    }

    fun setSequentialCount(count: Int) {
        _sequentialCount.value = count.coerceIn(1, 100)
    }

    fun setCustomTag(tag: String) {
        _customTag.value = tag
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Helper to generate the variations
    fun generateVariations() {
        val email = _originalEmail.value.trim()
        if (email.isEmpty()) return

        // Simple validation or fallback to append @gmail.com
        val fullEmail = if (!email.contains("@")) {
            "$email@gmail.com"
        } else {
            email
        }

        val parts = fullEmail.split("@")
        if (parts.size != 2) return
        val localPart = parts[0]
        val domain = parts[1]

        // Keep dots in localPart or strip if wanted? Let's strip dots and plus symbols to find base name
        val baseLocal = localPart.substringBefore("+").replace(".", "")

        val variations = when (_generationType.value) {
            GenerationType.DOTS_ONLY -> {
                generateDotPermutations(baseLocal).map {
                    GeneratedEmailItem("$it@$domain", "Dot Trick")
                }
            }
            GenerationType.PLUS_RANDOM -> {
                List(_sequentialCount.value) {
                    val sfx = generateRandomString(_randomSuffixLength.value)
                    GeneratedEmailItem("$baseLocal+$sfx@$domain", "Plus Random")
                }
            }
            GenerationType.PLUS_SEQUENTIAL -> {
                val pfx = if (_customTag.value.isNotEmpty()) "${_customTag.value}" else ""
                List(_sequentialCount.value) { index ->
                    val sfx = if (pfx.isNotEmpty()) "${pfx}_${index + 1}" else "${index + 1}"
                    GeneratedEmailItem("$baseLocal+$sfx@$domain", "Plus Sequential")
                }
            }
            GenerationType.PLUS_COMMON_PRESETS -> {
                val socialPresets = listOf(
                    "netflix", "youtube", "spotify", "facebook", "instagram", "twitter", "tiktok", "reddit",
                    "disney", "amazon", "google", "apple", "microsoft", "linkedin", "pinterest", "github",
                    "discord", "zoom", "twitch", "steam", "epicgames", "paypal", "stripe"
                )
                val qaPresets = listOf(
                    "test", "admin", "dev", "qa", "billing", "user", "guest", "beta", "sandbox", "owner", "staff"
                )
                val results = mutableListOf<GeneratedEmailItem>()
                socialPresets.forEach { preset ->
                    results.add(GeneratedEmailItem("$baseLocal+$preset@$domain", "Platform Preset"))
                }
                qaPresets.forEach { preset ->
                    results.add(GeneratedEmailItem("$baseLocal+$preset@$domain", "QA Preset"))
                }
                results
            }
            GenerationType.COMBINED_DOT_AND_PLUS -> {
                // Apply a few dots variations (say, up to 5) and combine with random or preset tags
                val dots = generateDotPermutations(baseLocal).take(5)
                val presets = listOf("test", "admin", "dev", "qa", "billing")
                val resultsList = mutableListOf<GeneratedEmailItem>()
                for (dotLocal in dots) {
                    for (preset in presets) {
                        resultsList.add(GeneratedEmailItem("$dotLocal+$preset@$domain", "Combined"))
                    }
                }
                resultsList
            }
        }

        // Compare with database stored to check what is already saved!
        viewModelScope.launch {
            repository.allEmails.collectLatest { savedList ->
                val savedSet = savedList.map { it.generatedEmail }.toSet()
                _generatedVariations.value = variations.map { item ->
                    item.copy(isSaved = savedSet.contains(item.email))
                }
            }
        }
    }

    private fun generateDotPermutations(local: String, limit: Int = 100): List<String> {
        if (local.length <= 1) return listOf(local)
        val n = local.length
        val positions = n - 1
        val list = mutableSetOf<String>()
        list.add(local) // Add original first

        val totalPossible = if (positions < 30) (1L shl positions) else Long.MAX_VALUE

        if (totalPossible <= limit * 2) {
            for (i in 0 until (1L shl positions).toInt()) {
                val sb = StringBuilder()
                for (j in 0 until n) {
                    sb.append(local[j])
                    if (j < positions && ((i shr j) and 1) == 1) {
                        sb.append('.')
                    }
                }
                list.add(sb.toString())
                if (list.size >= limit) break
            }
        } else {
            val random = Random()
            var attempts = 0
            while (list.size < limit && attempts < limit * 5) {
                val sb = StringBuilder()
                for (j in 0 until n) {
                    sb.append(local[j])
                    if (j < positions && random.nextBoolean()) {
                        sb.append('.')
                    }
                }
                list.add(sb.toString())
                attempts++
            }
        }
        return list.toList().sortedBy { it.length }
    }

    private fun generateRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyz1234567890"
        val random = Random()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }

    // DB Operations
    fun saveEmailToHistory(generatedEmail: String, tag: String, notes: String = "") {
        viewModelScope.launch {
            val original = _originalEmail.value
            val typeStr = when (_generationType.value) {
                GenerationType.DOTS_ONLY -> "Dot"
                GenerationType.PLUS_RANDOM -> "Plus-Random"
                GenerationType.PLUS_SEQUENTIAL -> "Plus-Seq"
                GenerationType.PLUS_COMMON_PRESETS -> "QA-Preset"
                GenerationType.COMBINED_DOT_AND_PLUS -> "Combined"
            }
            repository.insertEmail(
                GeneratedEmail(
                    originalEmail = original,
                    generatedEmail = generatedEmail,
                    type = typeStr,
                    tag = tag.ifEmpty { "Testing" },
                    notes = notes
                )
            )
        }
    }

    fun deleteEmailFromHistory(email: GeneratedEmail) {
        viewModelScope.launch {
            repository.deleteEmailById(email.id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}

class EmailGeneratorViewModelFactory(
    private val repository: EmailRepository,
    private val updateChecker: UpdateChecker
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailGeneratorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmailGeneratorViewModel(repository, updateChecker) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
