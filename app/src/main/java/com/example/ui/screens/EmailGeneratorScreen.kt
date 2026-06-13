package com.example.ui.screens

import android.widget.Toast
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GeneratedEmail
import com.example.ui.EmailGeneratorViewModel
import com.example.ui.GeneratedEmailItem
import com.example.ui.GenerationType
import com.example.ui.theme.AppThemePreset

// Custom Content Copy Icon
val customContentCopyIcon: ImageVector
    get() = ImageVector.Builder(
        name = "customContentCopyIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f
        ) {
            moveTo(16f, 1f)
            lineTo(4f, 1f)
            quadTo(3f, 1f, 3f, 2f)
            lineTo(3f, 16f)
        }
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f
        ) {
            moveTo(8f, 5f)
            lineTo(19f, 5f)
            quadTo(21f, 5f, 21f, 7f)
            lineTo(21f, 20f)
            quadTo(21f, 22f, 19f, 22f)
            lineTo(8f, 22f)
            quadTo(6f, 22f, 6f, 20f)
            lineTo(6f, 7f)
            quadTo(6f, 5f, 8f, 5f)
        }
    }.build()

// Custom Content Copy All Icon
val customContentCopyAllIcon: ImageVector
    get() = ImageVector.Builder(
        name = "customContentCopyAllIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f
        ) {
            moveTo(4f, 6f)
            lineTo(2f, 6f)
            quadTo(1f, 6f, 1f, 7f)
            lineTo(1f, 21f)
            quadTo(1f, 22f, 2f, 22f)
            lineTo(16f, 22f)
            quadTo(17f, 22f, 17f, 21f)
            lineTo(17f, 20f)
        }
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f
        ) {
            moveTo(8f, 2f)
            lineTo(19f, 2f)
            quadTo(21f, 2f, 21f, 4f)
            lineTo(21f, 15f)
            quadTo(21f, 17f, 19f, 17f)
            lineTo(8f, 17f)
            quadTo(6f, 17f, 6f, 15f)
            lineTo(6f, 4f)
            quadTo(6f, 2f, 8f, 2f)
        }
    }.build()

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EmailGeneratorScreen(
    viewModel: EmailGeneratorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    // State bindings
    val originalEmail by viewModel.originalEmail.collectAsStateWithLifecycle()
    val generationType by viewModel.generationType.collectAsStateWithLifecycle()
    val randomSuffixLength by viewModel.randomSuffixLength.collectAsStateWithLifecycle()
    val sequentialCount by viewModel.sequentialCount.collectAsStateWithLifecycle()
    val customTag by viewModel.customTag.collectAsStateWithLifecycle()
    val generatedVariations by viewModel.generatedVariations.collectAsStateWithLifecycle()
    val savedEmails by viewModel.savedEmails.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentThemePreset by viewModel.currentThemePreset.collectAsStateWithLifecycle()

    // Dialog & UI temporary States
    var showSaveDialog by remember { mutableStateOf(false) }
    var emailToSave by remember { mutableStateOf("") }
    var suggestedTag by remember { mutableStateOf("") }
    var userNotes by remember { mutableStateOf("") }

    // Tab control for results vs. vault
    var selectedTab by remember { mutableStateOf(0) } // 0 = Variations, 1 = Database Vault

    // Error states
    val isEmailValid = remember(originalEmail) {
        val trimmed = originalEmail.trim()
        trimmed.isNotEmpty() && (trimmed.contains("@") || trimmed.length > 2)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Gmail Generator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    var showThemeMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showThemeMenu = true },
                        modifier = Modifier.testTag("theme_selector_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Change Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (showThemeMenu) {
                        SettingsDialog(
                            currentPreset = currentThemePreset,
                            onPresetSelected = { viewModel.setCurrentThemePreset(it) },
                            viewModel = viewModel,
                            onDismiss = { showThemeMenu = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Inputs & Parameters Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Base Gmail Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = originalEmail,
                            onValueChange = { viewModel.setOriginalEmail(it) },
                            label = { Text("Gmail or Username") },
                            placeholder = { Text("e.g. mohn.oaktale1@gmail.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email icon")
                            },
                            trailingIcon = {
                                if (originalEmail.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setOriginalEmail("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear input")
                                    }
                                }
                            },
                            isError = !isEmailValid && originalEmail.isNotEmpty(),
                            supportingText = {
                                if (!isEmailValid && originalEmail.isNotEmpty()) {
                                    Text("Please enter a valid Gmail prefix/username", color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("Prefix can include letters, numbers, and existing dots.")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (isEmailValid) {
                                        viewModel.generateVariations()
                                    }
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("original_email_input")
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        Text(
                            text = "Generation Technique",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Segmented trick selector
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val chips = listOf(
                                GenerationType.PLUS_COMMON_PRESETS to "Presets Tag",
                                GenerationType.DOTS_ONLY to "Dot Permutations",
                                GenerationType.PLUS_RANDOM to "Random Plus",
                                GenerationType.PLUS_SEQUENTIAL to "Seq Numbers",
                                GenerationType.COMBINED_DOT_AND_PLUS to "Dot + Plus"
                            )

                            chips.forEach { (type, label) ->
                                val selected = generationType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        viewModel.setGenerationType(type)
                                        viewModel.generateVariations()
                                    },
                                    label = { Text(label, fontSize = 12.sp) },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        // Advanced controls based on chosen type
                        AnimatedVisibility(
                            visible = generationType == GenerationType.PLUS_RANDOM ||
                                    generationType == GenerationType.PLUS_SEQUENTIAL ||
                                    generationType == GenerationType.COMBINED_DOT_AND_PLUS
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (generationType == GenerationType.PLUS_RANDOM) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Random Suffix Length", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            Text("$randomSuffixLength chars", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = randomSuffixLength.toFloat(),
                                            onValueChange = { viewModel.setRandomSuffixLength(it.toInt()) },
                                            valueRange = 3f..12f,
                                            steps = 8
                                        )
                                    }
                                }

                                if (generationType == GenerationType.PLUS_SEQUENTIAL) {
                                    OutlinedTextField(
                                        value = customTag,
                                        onValueChange = { viewModel.setCustomTag(it) },
                                        label = { Text("Base Number Suffix / Tag (Optional)") },
                                        placeholder = { Text("e.g. signup_test") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (generationType == GenerationType.PLUS_RANDOM || generationType == GenerationType.PLUS_SEQUENTIAL) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Quantity of variations", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            Text("$sequentialCount variations", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = sequentialCount.toFloat(),
                                            onValueChange = { viewModel.setSequentialCount(it.toInt()) },
                                            valueRange = 5f..50f,
                                            steps = 8
                                        )
                                    }
                                }

                                if (generationType == GenerationType.COMBINED_DOT_AND_PLUS) {
                                    Text(
                                        text = "Generates top 5 Dot permutations combined with standard testing tags (+test, +admin, +dev, +qa, +billing) for 25 absolute unique emails.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.generateVariations()
                            },
                            enabled = isEmailValid,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("generate_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Generate Email Variations", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Section 2: Toggle Tabs (Variations vs Saved Ledger)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("New Variations", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                    Text("${generatedVariations.size}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Saved Vault", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text("${savedEmails.size}", color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    )
                }
            }

            // Section 3: List contents based on active Tab selection
            if (selectedTab == 0) {
                // TAB 0: Generated Variations Panel
                if (generatedVariations.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No generated results yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Enter your base Gmail above and generate variations",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Generated Actions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = {
                                        val bulkString = generatedVariations.joinToString("\n") { it.email }
                                        clipboardManager.setText(AnnotatedString(bulkString))
                                        Toast.makeText(context, "Copied all ${generatedVariations.size} to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(customContentCopyAllIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy All", fontSize = 12.sp)
                                }

                                TextButton(
                                    onClick = {
                                        // Save all to database with default tags
                                        generatedVariations.forEach { variation ->
                                            if (!variation.isSaved) {
                                                val cleanTag = when (generationType) {
                                                    GenerationType.DOTS_ONLY -> "Dot trick"
                                                    GenerationType.PLUS_RANDOM -> "Plus-Random"
                                                    GenerationType.PLUS_SEQUENTIAL -> "Plus-Seq"
                                                    GenerationType.PLUS_COMMON_PRESETS -> {
                                                        variation.email.substringAfter("+").substringBefore("@")
                                                    }
                                                    GenerationType.COMBINED_DOT_AND_PLUS -> "Combined"
                                                }
                                                viewModel.saveEmailToHistory(
                                                    generatedEmail = variation.email,
                                                    tag = cleanTag,
                                                    notes = "Saved via bulk generation"
                                                )
                                            }
                                        }
                                        Toast.makeText(context, "Saved all variations to vault!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save All", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(generatedVariations) { item ->
                        VariationItemRow(
                            item = item,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(item.email))
                                Toast.makeText(context, "Copied variation!", Toast.LENGTH_SHORT).show()
                            },
                            onSave = {
                                emailToSave = item.email
                                suggestedTag = when {
                                    item.email.contains("+") -> item.email.substringAfter("+").substringBefore("@")
                                    else -> "Dots Profile"
                                }
                                userNotes = ""
                                showSaveDialog = true
                            }
                        )
                    }
                }
            } else {
                // TAB 1: Database Account Vault Panel
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search saved profiles (tag or email)...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (savedEmails.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Profiles Ledger (${savedEmails.size} total)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                TextButton(
                                    onClick = {
                                        viewModel.clearAllHistory()
                                        Toast.makeText(context, "Cleared saved vault!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear Vault", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (savedEmails.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                searchQuery.let { if (it.isEmpty()) "Your Vault is Empty" else "No matching results" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                searchQuery.let { if (it.isEmpty()) "Save variations from the 'New Variations' tab to track your test profiles." else "Try a different search term" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(savedEmails, key = { it.id }) { saved ->
                        SavedEmailItemRow(
                            saved = saved,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(saved.generatedEmail))
                                Toast.makeText(context, "Copied profile email!", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                viewModel.deleteEmailFromHistory(saved)
                                Toast.makeText(context, "Deleted profile!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal popup to configure tags and notes when saving a profile
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Save Test Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Customize tags and QA details to save this profile in your tracking database ledger.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = emailToSave,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Profile Email") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = suggestedTag,
                        onValueChange = { suggestedTag = it },
                        label = { Text("App Tag / Profile Label") },
                        placeholder = { Text("e.g. Netflix Test, QA Billing") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_dialog_tag_input")
                    )

                    OutlinedTextField(
                        value = userNotes,
                        onValueChange = { userNotes = it },
                        label = { Text("Dev notes / annotations") },
                        placeholder = { Text("e.g. Admin role, trial checkout") },
                        minLines = 2,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveEmailToHistory(
                            emailToSave,
                            suggestedTag,
                            userNotes
                        )
                        showSaveDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Single Row Item view for a generated email variation
@Composable
fun VariationItemRow(
    item: GeneratedEmailItem,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Highlight trick symbols to show dot or plus
                val highlightedEmail = buildAnnotatedString {
                    val text = item.email
                    var index = 0
                    while (index < text.length) {
                        val char = text[index]
                        if (char == '.' && index < text.indexOf('@')) {
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFFA855F7), // violet purple accent
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            ) {
                                append(char)
                            }
                        } else if (char == '+' || (text.indexOf('+') in 0..index && index < text.indexOf('@') && char != '@')) {
                            val plusIndex = text.indexOf('+')
                            val atIndex = text.indexOf('@')
                            if (index in plusIndex until atIndex) {
                                withStyle(
                                    style = SpanStyle(
                                        color = Color(0xFF0EA5E9), // cyan blue accent
                                        fontWeight = FontWeight.Bold,
                                        background = Color(0xFF0EA5E9).copy(alpha = 0.15f)
                                    )
                                ) {
                                    append(char)
                                }
                            } else {
                                append(char)
                            }
                        } else {
                            append(char)
                        }
                        index++
                    }
                }

                Text(
                    text = highlightedEmail,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.5.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val color = when (item.type) {
                        "Dot Trick" -> Color(0xFFA855F7)
                        "Combined" -> Color(0xFFF43F5E)
                        "QA Preset" -> Color(0xFF10B981)
                        else -> Color(0xFF0EA5E9)
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(color)
                    )

                    Text(
                        text = item.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = customContentCopyIcon,
                        contentDescription = "Copy Variation",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (item.isSaved) {
                    IconButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Color(0xFFE6F4EA),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Saved already",
                            tint = Color(0xFF137333),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onSave,
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                CircleShape
                            ).testTag("save_single_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Save Profile",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Single Row Item view for a saved Account profile in ledger database
@Composable
fun SavedEmailItemRow(
    saved: GeneratedEmail,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = saved.tag,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.height(28.dp).weight(1f, fill = false)
                )

                Text(
                    text = formatTimestamp(saved.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = saved.generatedEmail,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { onCopy() }
            )

            if (saved.notes.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = saved.notes,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Base: ${saved.originalEmail}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = customContentCopyIcon,
                            contentDescription = "Copy saved profile",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete saved profile",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Simple time formatter
fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dt = Date(timestamp)
            sdf.format(dt)
        }
    }
}

@Composable
fun SettingsDialog(
    currentPreset: AppThemePreset,
    onPresetSelected: (AppThemePreset) -> Unit,
    viewModel: EmailGeneratorViewModel,
    onDismiss: () -> Unit
) {
    // Collect settings and status from viewmodel
    val githubOwner by viewModel.updateChecker.githubOwner.collectAsStateWithLifecycle()
    val githubRepo by viewModel.updateChecker.githubRepo.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()

    var ownerText by remember { mutableStateOf(githubOwner) }
    var repoText by remember { mutableStateOf(githubRepo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "App Customizer & Updates",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section 1: Themes
                Text(
                    text = "🎨 Aesthetic App Themes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Select a color scheme tailored to your preference.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val themes = listOf(
                    Triple(AppThemePreset.AMETHYST, "Classic Amethyst", Color(0xFF8B5CF6)),
                    Triple(AppThemePreset.COSMIC_OCEAN, "Cosmic Ocean Blue", Color(0xFF0EA5E9)),
                    Triple(AppThemePreset.FOREST_MINT, "Forest Mint Green", Color(0xFF10B981)),
                    Triple(AppThemePreset.SUNSET_RUBY, "Crimson Sunset Ruby", Color(0xFFF43F5E))
                )

                themes.forEach { (preset, label, primaryColor) ->
                    val isSelected = currentPreset == preset
                    Surface(
                        onClick = { onPresetSelected(preset) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        },
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("theme_option_${preset.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(primaryColor, CircleShape)
                                        .border(2.dp, Color.White, CircleShape)
                                )
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Section 2: Github properties
                Text(
                    text = "🌐 GitHub Update Sync",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Configure the source GitHub repository path to verify live update_config.json configurations over the internet.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Owner Field
                OutlinedTextField(
                    value = ownerText,
                    onValueChange = { ownerText = it },
                    label = { Text("GitHub Owner / Name") },
                    placeholder = { Text("e.g. mohn-oaktale1") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    modifier = Modifier.fillMaxWidth().testTag("github_owner_input")
                )

                // Repo Field
                OutlinedTextField(
                    value = repoText,
                    onValueChange = { repoText = it },
                    label = { Text("GitHub Repository") },
                    placeholder = { Text("e.g. gmail-generator") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    modifier = Modifier.fillMaxWidth().testTag("github_repo_input")
                )

                // Update configuration status view
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Status check:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        val statusLabel = when (updateState) {
                            is com.example.data.UpdateState.Checking -> "Checking repository..."
                            is com.example.data.UpdateState.NoInternet -> "System Offline (Blocked)"
                            is com.example.data.UpdateState.UpdateRequired -> "Update Available (Lock active)"
                            is com.example.data.UpdateState.UpToDate -> "Version 1.0 matches latest"
                            is com.example.data.UpdateState.Error -> "Connection error"
                            else -> "Idle"
                        }
                        
                        Text(
                            text = statusLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Interaction controls row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Update settings button
                    Button(
                        onClick = {
                            viewModel.setGithubConfig(ownerText, repoText)
                            viewModel.checkForUpdates(force = true)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp).testTag("save_and_check_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Quick Sync", fontSize = 12.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Section 3: Developer Live Testing simulations
                Text(
                    text = "⚙️ Dev Simulation (Test Mode)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Manually trigger a mandatory update simulation to visually verify the non-dismissible compliance overlay layout instantly.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        viewModel.toggleMandatoryUpdateSimulation(true)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("simulate_update_button")
                ) {
                    Text("Simulate Blocking Update Dialog", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("apply_theme_button")
            ) {
                Text("Close Settings")
            }
        }
    )
}
