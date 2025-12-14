package com.example.mobile_final.ui

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_final.dto.*
import com.example.mobile_final.services.FormManager
import com.example.mobile_final.storage.UserStore
import com.example.mobile_final.ui.theme.Mobile_finalTheme
import com.example.mobile_final.utils.LocaleHelper
import com.example.mobile_final.utils.PreferencesManager
import com.example.mobile_final.viewmodels.FormCreationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateFormActivity : ComponentActivity() {
    private lateinit var formManager: FormManager
    private lateinit var userStore: UserStore
    private lateinit var viewModel: FormCreationViewModel
    override fun attachBaseContext(newBase: Context) {
        val preferencesManager = PreferencesManager(newBase)
        val language = preferencesManager.getLanguage()
        val context = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(context)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        formManager = FormManager(this)
        userStore = UserStore(this)
        viewModel = ViewModelProvider(this)[FormCreationViewModel::class.java]

        setContent {
            Mobile_finalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FormCreationScreen(
                        formManager = formManager,
                        userStore = userStore,
                        viewModel = viewModel,
                        onFormCreated = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun FormCreationScreen(
    formManager: FormManager,
    userStore: UserStore,
    viewModel: FormCreationViewModel,
    onFormCreated: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }
    val maxSteps = 3
    
    val (accountName, setAccountName) = remember { mutableStateOf("") }
    val (accountServer, setAccountServer) = remember { mutableStateOf("") }
    val (accountTag, setAccountTag) = remember { mutableStateOf("") }
    
    val (selectedRoles, setSelectedRoles) = remember { mutableStateOf<List<Role>>(emptyList()) }
    val (selectedRolesLookingFor, setSelectedRolesLookingFor) = remember { mutableStateOf<List<Role>>(emptyList()) }
    
    val (description, setDescription) = remember { mutableStateOf("") }
    val (minAge, setMinAge) = remember { mutableStateOf("") }
    val (maxAge, setMaxAge) = remember { mutableStateOf("") }
    val (selectedGender, setSelectedGender) = remember { mutableStateOf<Gender?>(null) }
    val (voiceRequired, setVoiceRequired) = remember { mutableStateOf(false) }
    val (selectedGameTypes, setSelectedGameTypes) = remember { mutableStateOf<List<GameType>>(emptyList()) }

    val (isLoading, setIsLoading) = remember { mutableStateOf(false) }
    val (showActivationDialog, setShowActivationDialog) = remember { mutableStateOf(false) }
    val (formId, setFormId) = remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create Form - Step $currentStep/$maxSteps",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        when (currentStep) {
            1 -> AccountStep(
                accountName = accountName,
                onAccountNameChange = setAccountName,
                accountServer = accountServer,
                onAccountServerChange = setAccountServer,
                accountTag = accountTag,
                onAccountTagChange = setAccountTag
            )
            2 -> RolesStep(
                selectedRoles = selectedRoles,
                onRolesChange = setSelectedRoles,
                selectedRolesLookingFor = selectedRolesLookingFor,
                onRolesLookingForChange = setSelectedRolesLookingFor
            )
            3 -> DetailsStep(
                description = description,
                onDescriptionChange = setDescription,
                minAge = minAge,
                onMinAgeChange = setMinAge,
                maxAge = maxAge,
                onMaxAgeChange = setMaxAge,
                selectedGender = selectedGender,
                onGenderChange = setSelectedGender,
                voiceRequired = voiceRequired,
                onVoiceRequiredChange = setVoiceRequired,
                selectedGameTypes = selectedGameTypes,
                onGameTypesChange = setSelectedGameTypes
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (currentStep > 1) currentStep--
                },
                enabled = currentStep > 1
            ) {
                Text("Previous")
            }
            
            if (currentStep < maxSteps) {
                Button(
                    onClick = {
                        if (validateCurrentStep(currentStep, 
                                accountName, accountServer, accountTag,
                                selectedRoles, selectedRolesLookingFor,
                                minAge, maxAge)) {
                            currentStep++
                        }
                    }
                ) {
                    Text("Next")
                }
            } else {
                Button(
                    onClick = {
                        // Save data to form manager
                        formManager.updateAccount(Account(
                            name = accountName,
                            server = accountServer,
                            tag = accountTag
                        ))
                        
                        formManager.updateRoles(selectedRoles)
                        formManager.updateRolesLookingFor(selectedRolesLookingFor)
                        
                        val personData = PersonData(
                            minAge = minAge.toIntOrNull(),
                            maxAge = maxAge.toIntOrNull(),
                            gender = selectedGender,
                            voice = voiceRequired
                        )
                        formManager.updatePersonData(personData)
                        formManager.updateDescription(description)
                        formManager.updateGameTypes(selectedGameTypes)
                        
                        // Create the form
                        createForm(formManager, userStore, setIsLoading, setFormId, setShowActivationDialog, context)
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Form")
                    }
                }
            }
        }
    }
    
    if (showActivationDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Activate Form") },
            text = { Text("Your form has been created successfully! Would you like to activate it now?") },
            confirmButton = {
                Button(
                    onClick = {
                        activateForm(formManager, formId, setIsLoading, onFormCreated, context)
                    }
                ) {
                    Text("Activate")
                }
            },
            dismissButton = {
                Button(
                    onClick = { onFormCreated() }
                ) {
                    Text("Skip")
                }
            }
        )
    }
}

@Composable
fun AccountStep(
    accountName: String,
    onAccountNameChange: (String) -> Unit,
    accountServer: String,
    onAccountServerChange: (String) -> Unit,
    accountTag: String,
    onAccountTagChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Account Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        OutlinedTextField(
            value = accountName,
            onValueChange = onAccountNameChange,
            label = { Text("Account Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = accountServer,
            onValueChange = onAccountServerChange,
            label = { Text("Server") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = accountTag,
            onValueChange = onAccountTagChange,
            label = { Text("Tag") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RolesStep(
    selectedRoles: List<Role>,
    onRolesChange: (List<Role>) -> Unit,
    selectedRolesLookingFor: List<Role>,
    onRolesLookingForChange: (List<Role>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Your Roles & Looking For",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Text("Your Roles:", fontWeight = FontWeight.Medium)
        RoleSelection(
            selectedRoles = selectedRoles,
            onSelectionChange = onRolesChange
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Looking For:", fontWeight = FontWeight.Medium)
        RoleSelection(
            selectedRoles = selectedRolesLookingFor,
            onSelectionChange = onRolesLookingForChange
        )
    }
}

@Composable
fun DetailsStep(
    description: String,
    onDescriptionChange: (String) -> Unit,
    minAge: String,
    onMinAgeChange: (String) -> Unit,
    maxAge: String,
    onMaxAgeChange: (String) -> Unit,
    selectedGender: Gender?,
    onGenderChange: (Gender?) -> Unit,
    voiceRequired: Boolean,
    onVoiceRequiredChange: (Boolean) -> Unit,
    selectedGameTypes: List<GameType>,
    onGameTypesChange: (List<GameType>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Additional Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = minAge,
                onValueChange = onMinAgeChange,
                label = { Text("Min Age") },
                modifier = Modifier.weight(1f)
            )
            
            OutlinedTextField(
                value = maxAge,
                onValueChange = onMaxAgeChange,
                label = { Text("Max Age") },
                modifier = Modifier.weight(1f)
            )
        }
        
        GenderSelection(
            selectedGender = selectedGender,
            onGenderChange = onGenderChange
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = voiceRequired,
                onCheckedChange = onVoiceRequiredChange
            )
            Text("Voice Required")
        }
        
        Text("Game Types:", fontWeight = FontWeight.Medium)
        GameTypeSelection(
            selectedGameTypes = selectedGameTypes,
            onSelectionChange = onGameTypesChange
        )
    }
}

@Composable
fun RoleSelection(
    selectedRoles: List<Role>,
    onSelectionChange: (List<Role>) -> Unit
) {
    val allRoles = listOf(Role.TOP, Role.JG, Role.MID, Role.ADC, Role.SUP, Role.ANY)
    
    LazyRow {
        items(allRoles.size) { index ->
            val role = allRoles[index]
            val isSelected = selectedRoles.contains(role)
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = if (isSelected) {
                        selectedRoles.filter { it != role }
                    } else {
                        selectedRoles + role
                    }
                    onSelectionChange(newSelection)
                },
                label = { Text(role.name) }
            )
        }
    }
}

@Composable
fun GameTypeSelection(
    selectedGameTypes: List<GameType>,
    onSelectionChange: (List<GameType>) -> Unit
) {
    val allGameTypes = listOf(GameType.NORMAL, GameType.ARAM, GameType.ARENA, GameType.SOLOQ, GameType.FLEX, GameType.ANY)
    
    LazyRow {
        items(allGameTypes.size) { index ->
            val gameType = allGameTypes[index]
            val isSelected = selectedGameTypes.contains(gameType)
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = if (isSelected) {
                        selectedGameTypes.filter { it != gameType }
                    } else {
                        selectedGameTypes + gameType
                    }
                    onSelectionChange(newSelection)
                },
                label = { Text(gameType.name) }
            )
        }
    }
}

@Composable
fun GenderSelection(
    selectedGender: Gender?,
    onGenderChange: (Gender?) -> Unit
) {
    val genders = listOf(Gender.MALE, Gender.FEMALE, Gender.ANY)
    
    Row {
        genders.forEach { gender ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                RadioButton(
                    selected = selectedGender == gender,
                    onClick = { onGenderChange(gender) }
                )
                Text(gender.name)
            }
        }
    }
}

fun validateCurrentStep(
    step: Int,
    accountName: String,
    accountServer: String,
    accountTag: String,
    selectedRoles: List<Role>,
    selectedRolesLookingFor: List<Role>,
    minAge: String,
    maxAge: String
): Boolean {
    return when (step) {
        1 -> accountName.isNotBlank() && accountServer.isNotBlank() && accountTag.isNotBlank()
        2 -> selectedRoles.isNotEmpty() && selectedRolesLookingFor.isNotEmpty()
        3 -> {
            // Validate min/max age if provided
            if (minAge.isNotBlank() && minAge.toIntOrNull() == null) return false
            if (maxAge.isNotBlank() && maxAge.toIntOrNull() == null) return false
            if (minAge.isNotBlank() && maxAge.isNotBlank() && minAge.toIntOrNull()!! > maxAge.toIntOrNull()!!) return false
            true
        }
        else -> true
    }
}

fun createForm(
    formManager: FormManager,
    userStore: UserStore,
    setIsLoading: (Boolean) -> Unit,
    setFormId: (String) -> Unit,
    setShowActivationDialog: (Boolean) -> Unit,
    context: android.content.Context
) {
    setIsLoading(true)
    
    // Get current user ID
    val userId = userStore.getUserId()
    if (userId == null) {
        setIsLoading(false)
        Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        return
    }
    
    // Create form in background
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val result = formManager.createForm(userId)
            if (result != null) {
                setFormId(result.id)
                setShowActivationDialog(true)
            } else {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "Failed to create form", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(context, "Error creating form: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } finally {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                setIsLoading(false)
            }
        }
    }
}

fun activateForm(
    formManager: FormManager,
    formId: String,
    setIsLoading: (Boolean) -> Unit,
    onFormCreated: () -> Unit,
    context: android.content.Context
) {
    setIsLoading(true)
    
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val result = formManager.activateForm(formId)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (result) {
                    Toast.makeText(context, "Form activated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to activate form", Toast.LENGTH_SHORT).show()
                }
                onFormCreated()
            }
        } catch (e: Exception) {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(context, "Error activating form: ${e.message}", Toast.LENGTH_SHORT).show()
                onFormCreated()
            }
        } finally {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                setIsLoading(false)
            }
        }
    }
}