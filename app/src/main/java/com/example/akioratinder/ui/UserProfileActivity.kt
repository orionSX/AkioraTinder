package com.example.akioratinder.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.akioratinder.localization.LocaleHelper
import com.example.akioratinder.services.GlobalSettingsManager
import com.example.akioratinder.services.ProfilesManager
import com.example.akioratinder.storage.ThemeLanguageStore
import com.example.akioratinder.storage.UserProfileStore
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.R
import com.example.akioratinder.data.UserProfile
import com.example.akioratinder.viewmodels.ThemeViewModel
import com.example.akioratinder.viewmodels.UserProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class UserProfileActivity : ComponentActivity() {
    private lateinit var themeStore: ThemeLanguageStore
    private lateinit var userProfileStore: UserProfileStore

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeStore)
    }

    private val userProfileViewModel: UserProfileViewModel by viewModels {
        UserProfileViewModelFactory(userProfileStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalSettingsManager.initialize(this)
        ProfilesManager.initialize(this) // Инициализируем менеджер профилей
        themeStore = ThemeLanguageStore(this)
        userProfileStore = UserProfileStore(this)

        super.onCreate(savedInstanceState)

        setContent {
            val darkTheme by themeViewModel.darkTheme.collectAsState()
            GlobalSettingsManager.ObserveSettings()

            AkioraTinderTheme(darkTheme = darkTheme) {
                Scaffold(
                    topBar = {
                        TopBar(

                        )
                    },
                    bottomBar = { BottomNav(current = 1) }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        EnhancedUserProfileScreen(
                            userProfileViewModel = userProfileViewModel
                        )
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val s = ThemeLanguageStore(newBase)
        val lang = runBlocking { s.langFlow.first() }
        val ctx = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(ctx)
    }
}

@Composable
fun EnhancedUserProfileScreen(userProfileViewModel: UserProfileViewModel) {
    val context = LocalContext.current
    val userProfile by userProfileViewModel.getCurrentUserProfileFlow(context).collectAsState()
    val selectionOptions = remember { userProfileViewModel.getSelectionOptions(context) }


    var summonerName by remember { mutableStateOf(userProfile.summonerName) }
    var server by remember { mutableStateOf(userProfile.server) }
    var role by remember { mutableStateOf(userProfile.role) }
    var rankTier by remember { mutableStateOf(userProfile.rankTier) }
    var rankDivision by remember { mutableStateOf(userProfile.rankDivision) }
    var bio by remember { mutableStateOf(userProfile.bio) }
    var age by remember { mutableStateOf(userProfile.age) }
    var gender by remember { mutableStateOf(userProfile.gender) }
    var playStyle by remember { mutableStateOf(userProfile.playStyle) }
    var microphone by remember { mutableStateOf(userProfile.microphone) }
    var goals by remember { mutableStateOf(userProfile.goals) }
    var playSchedule by remember { mutableStateOf(userProfile.playSchedule) }
    var isSaving by remember { mutableStateOf(false) }


    LaunchedEffect(userProfile) {
        summonerName = userProfile.summonerName
        server = userProfile.server
        role = userProfile.role
        rankTier = userProfile.rankTier
        rankDivision = userProfile.rankDivision
        bio = userProfile.bio
        age = userProfile.age
        gender = userProfile.gender
        playStyle = userProfile.playStyle
        microphone = userProfile.microphone
        goals = userProfile.goals
        playSchedule = userProfile.playSchedule
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        ProfileHeader()

        Spacer(modifier = Modifier.height(24.dp))


        BasicInfoCard(
            summonerName = summonerName,
            onSummonerNameChange = { summonerName = it },
            server = server,
            onServerChange = { server = it },
            role = role,
            onRoleChange = { role = it },
            selectionOptions = selectionOptions
        )

        Spacer(modifier = Modifier.height(16.dp))


        RankInfoCard(
            rankTier = rankTier,
            onRankTierChange = { rankTier = it },
            rankDivision = rankDivision,
            onRankDivisionChange = { rankDivision = it },
            showDivision = rankTier in listOf("Iron", "Bronze", "Silver", "Gold", "Platinum", "Diamond"),
            selectionOptions = selectionOptions
        )

        Spacer(modifier = Modifier.height(16.dp))


        PersonalInfoCard(
            age = age,
            onAgeChange = { age = it },
            gender = gender,
            onGenderChange = { gender = it },
            bio = bio,
            onBioChange = { bio = it },
            selectionOptions = selectionOptions
        )

        Spacer(modifier = Modifier.height(16.dp))


        GamingPreferencesCard(
            playStyle = playStyle,
            onPlayStyleChange = { playStyle = it },
            microphone = microphone,
            onMicrophoneChange = { microphone = it },
            goals = goals,
            onGoalsChange = { goals = it },
            playSchedule = playSchedule,
            onPlayScheduleChange = { playSchedule = it },
            selectionOptions = selectionOptions
        )

        Spacer(modifier = Modifier.height(24.dp))


        SaveProfileButton(
            isSaving = isSaving,
            onSaveClick = {
                isSaving = true
                userProfileViewModel.updateProfile(
                    context,
                    summonerName, server, role, rankTier, rankDivision, bio,
                    age, gender, playStyle, microphone, goals, playSchedule
                )
                isSaving = false

            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Preview
        if (!isSaving) {
            EnhancedProfilePreview(userProfile)
        }
    }
}

@Composable
fun ProfileHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.your_profile),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun BasicInfoCard(
    summonerName: String,
    onSummonerNameChange: (String) -> Unit,
    server: String,
    onServerChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    selectionOptions: com.example.akioratinder.viewmodels.SelectionOptions
) {
    ProfileCard(title = stringResource(R.string.basic_information)) {
        // Summoner Name
        OutlinedTextField(
            value = summonerName,
            onValueChange = onSummonerNameChange,
            label = { Text(stringResource(R.string.summoner_name)) },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Server
        LabeledDropdown(
            label = stringResource(R.string.server),
            selectedValue = server,
            onValueSelected = onServerChange,
            options = selectionOptions.servers,
            leadingIcon = Icons.Default.Public
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Role
        LabeledDropdown(
            label = stringResource(R.string.role),
            selectedValue = role,
            onValueSelected = onRoleChange,
            options = selectionOptions.roles,
            leadingIcon = Icons.Default.SportsEsports
        )
    }
}

@Composable
fun RankInfoCard(
    rankTier: String,
    onRankTierChange: (String) -> Unit,
    rankDivision: String,
    onRankDivisionChange: (String) -> Unit,
    showDivision: Boolean,
    selectionOptions: com.example.akioratinder.viewmodels.SelectionOptions
) {
    ProfileCard(title = stringResource(R.string.rank_information)) {
        // Rank Tier
        LabeledDropdown(
            label = stringResource(R.string.rank_tier),
            selectedValue = rankTier,
            onValueSelected = onRankTierChange,
            options = selectionOptions.rankTiers,
            leadingIcon = Icons.Default.Leaderboard
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Rank Division (only show for tiers that have divisions)
        if (showDivision) {
            LabeledDropdown(
                label = stringResource(R.string.rank_division),
                selectedValue = rankDivision,
                onValueSelected = onRankDivisionChange,
                options = selectionOptions.rankDivisions,
                leadingIcon = Icons.Default.Numbers
            )
        }
    }
}

@Composable
fun PersonalInfoCard(
    age: String,
    onAgeChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    selectionOptions: com.example.akioratinder.viewmodels.SelectionOptions
) {
    ProfileCard(title = stringResource(R.string.personal_information)) {
        // Age
        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            label = { Text(stringResource(R.string.age)) },
            leadingIcon = { Icon(Icons.Default.Cake, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gender
        LabeledDropdown(
            label = stringResource(R.string.gender),
            selectedValue = gender,
            onValueSelected = onGenderChange,
            options = selectionOptions.genders,
            leadingIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bio
        OutlinedTextField(
            value = bio,
            onValueChange = onBioChange,
            label = { Text(stringResource(R.string.bio)) },
            leadingIcon = { Icon(Icons.Default.Edit, null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            minLines = 4,
            singleLine = false
        )
    }
}

@Composable
fun GamingPreferencesCard(
    playStyle: String,
    onPlayStyleChange: (String) -> Unit,
    microphone: String,
    onMicrophoneChange: (String) -> Unit,
    goals: String,
    onGoalsChange: (String) -> Unit,
    playSchedule: String,
    onPlayScheduleChange: (String) -> Unit,
    selectionOptions: com.example.akioratinder.viewmodels.SelectionOptions
) {
    ProfileCard(title = stringResource(R.string.gaming_preferences)) {
        // Play Style
        LabeledDropdown(
            label = stringResource(R.string.play_style),
            selectedValue = playStyle,
            onValueSelected = onPlayStyleChange,
            options = selectionOptions.playStyles,
            leadingIcon = Icons.Default.PlayArrow
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Microphone
        LabeledDropdown(
            label = stringResource(R.string.microphone),
            selectedValue = microphone,
            onValueSelected = onMicrophoneChange,
            options = selectionOptions.microphoneOptions,
            leadingIcon = Icons.Default.Mic
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Goals
        LabeledDropdown(
            label = stringResource(R.string.goals),
            selectedValue = goals,
            onValueSelected = onGoalsChange,
            options = selectionOptions.goals,
            leadingIcon = Icons.Default.Flag
        )

        Spacer(modifier = Modifier.height(16.dp))


        LabeledDropdown(
            label = stringResource(R.string.play_schedule),
            selectedValue = playSchedule,
            onValueSelected = onPlayScheduleChange,
            options = selectionOptions.playSchedules,
            leadingIcon = Icons.Default.Schedule
        )
    }
}

@Composable
fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledDropdown(
    label: String,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    options: List<String>,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedValue.ifEmpty { "Select $label" },
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(leadingIcon, null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SaveProfileButton(isSaving: Boolean, onSaveClick: () -> Unit) {
    Button(
        onClick = onSaveClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isSaving,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Saving...")
        } else {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.save),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun EnhancedProfilePreview(profile: UserProfile) {
    val fullRank = if (profile.rankTier in listOf("Master", "Grandmaster", "Challenger")) {
        profile.rankTier
    } else {
        "${profile.rankTier} ${profile.rankDivision}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.profile_preview),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Basic info
            if (profile.summonerName.isNotBlank()) {
                ProfilePreviewRow("Summoner", profile.summonerName)
            }
            ProfilePreviewRow("Server", profile.server)
            ProfilePreviewRow("Role", profile.role)
            ProfilePreviewRow("Rank", fullRank)

            // Personal info
            if (profile.age.isNotBlank()) {
                ProfilePreviewRow("Age", profile.age)
            }
            if (profile.gender.isNotBlank()) {
                ProfilePreviewRow("Gender", profile.gender)
            }
            if (profile.bio.isNotBlank()) {
                ProfilePreviewRow("About", profile.bio)
            }

            // Gaming preferences
            if (profile.playStyle.isNotBlank()) {
                ProfilePreviewRow("Play Style", profile.playStyle)
            }
            if (profile.microphone.isNotBlank()) {
                ProfilePreviewRow("Microphone", profile.microphone)
            }
            if (profile.goals.isNotBlank()) {
                ProfilePreviewRow("Goals", profile.goals)
            }
            if (profile.playSchedule.isNotBlank()) {
                ProfilePreviewRow("Play Time", profile.playSchedule)
            }
        }
    }
}

@Composable
fun ProfilePreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

// Factory classes
class UserProfileViewModelFactory(
    private val userProfileStore: UserProfileStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(userProfileStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ThemeViewModelFactory(private val store: ThemeLanguageStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(store) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}