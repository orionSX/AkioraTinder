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
import com.example.mobile_final.dao.QuestionDao
import com.example.mobile_final.database.AppDatabase
import com.example.mobile_final.dto.*
import com.example.mobile_final.model.Question as ModelQuestion
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
    val (showAddTestDialog, setShowAddTestDialog) = remember { mutableStateOf(false) }
    val (showActivationDialog, setShowActivationDialog) = remember { mutableStateOf(false) }
    val (showTestAddingProgress, setShowTestAddingProgress) = remember { mutableStateOf(false) }
    val (formId, setFormId) = remember { mutableStateOf("") }
    val (availableQuestions, setAvailableQuestions) = remember { mutableStateOf<List<ModelQuestion>>(emptyList()) }
    val (selectedQuestions, setSelectedQuestions) = remember { mutableStateOf<List<ModelQuestion>>(emptyList()) }
    val (showQuestionSelectionDialog, setShowQuestionSelectionDialog) = remember { mutableStateOf(false) }

    // Загружаем вопросы из базы данных при первом рендере
    LaunchedEffect(Unit) {
        loadAvailableQuestions(context, setAvailableQuestions)
    }

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
                        // Сохраняем данные в form manager
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

                        // Создаем форму
                        createFormWithTestPrompt(formManager, userStore, setIsLoading, setFormId, setShowAddTestDialog, context)
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

    // Диалог с выбором: добавить тест или нет
    if (showAddTestDialog) {
        AlertDialog(
            onDismissRequest = {
                // Если пользователь закрывает диалог, переходим к активации
                setShowAddTestDialog(false)
                setShowActivationDialog(true)
            },
            title = { Text("Add Test to Form") },
            text = {
                Column {
                    Text("Would you like to add a test to your form?")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (availableQuestions.isEmpty()) {
                        Text("No questions available in your database.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("You have ${availableQuestions.size} questions in your database.")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        setShowAddTestDialog(false)
                        if (availableQuestions.isNotEmpty()) {
                            setShowQuestionSelectionDialog(true)
                        } else {
                            // Если нет вопросов, переходим к активации
                            Toast.makeText(context, "No questions available", Toast.LENGTH_SHORT).show()
                            setShowActivationDialog(true)
                        }
                    },
                    enabled = availableQuestions.isNotEmpty()
                ) {
                    Text("Yes, Select Questions")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        setShowAddTestDialog(false)
                        setShowActivationDialog(true)
                    }
                ) {
                    Text("No, Skip Test")
                }
            }
        )
    }

    // Диалог выбора вопросов
    if (showQuestionSelectionDialog) {
        AlertDialog(
            onDismissRequest = {
                setShowQuestionSelectionDialog(false)
                setShowActivationDialog(true)
            },
            title = { Text("Select 3 Questions") },
            text = {
                QuestionSelectionContent(
                    availableQuestions = availableQuestions,
                    selectedQuestions = selectedQuestions,
                    onQuestionSelected = { question ->
                        val newSelection = if (selectedQuestions.contains(question)) {
                            selectedQuestions.filter { it.id != question.id }
                        } else {
                            if (selectedQuestions.size < 3) {
                                selectedQuestions + question
                            } else {
                                selectedQuestions
                            }
                        }
                        setSelectedQuestions(newSelection)
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedQuestions.size == 3) {
                            setShowQuestionSelectionDialog(false)
                            setShowTestAddingProgress(true)
                            addSelectedQuestionsToForm(
                                formManager,
                                formId,
                                selectedQuestions,
                                context,
                                onTestAdded = {
                                    setShowTestAddingProgress(false)
                                    setShowActivationDialog(true)
                                    setSelectedQuestions(emptyList())
                                },
                                onTestAddFailed = {
                                    setShowTestAddingProgress(false)
                                    setShowActivationDialog(true)
                                    setSelectedQuestions(emptyList())
                                }
                            )
                        } else {
                            Toast.makeText(context, "Please select exactly 3 questions", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = selectedQuestions.size == 3
                ) {
                    Text("Add Selected Questions")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        setShowQuestionSelectionDialog(false)
                        setShowActivationDialog(true)
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Прогресс добавления теста
    if (showTestAddingProgress) {
        AlertDialog(
            onDismissRequest = { }, // Предотвращаем закрытие во время загрузки
            title = { Text("Adding Test") },
            text = {
                Column {
                    Text("Adding selected questions to your form...")
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            },
            confirmButton = { } // Пустая кнопка для удовлетворения требованиям AlertDialog
        )
    }

    // Диалог активации формы
    if (showActivationDialog) {
        AlertDialog(
            onDismissRequest = {
                // Если пользователь закрывает диалог, просто завершаем
                onFormCreated()
            },
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
fun QuestionSelectionContent(
    availableQuestions: List<ModelQuestion>,
    selectedQuestions: List<ModelQuestion>,
    onQuestionSelected: (ModelQuestion) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Text(
            text = "Selected: ${selectedQuestions.size}/3",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (availableQuestions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No questions available in database")
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn {
                items(availableQuestions) { question ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedQuestions.contains(question))
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        onClick = { onQuestionSelected(question) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = question.question,
                                fontWeight = FontWeight.Medium
                            )

                            if (question.answer != null) {

                                Text(
                                    text = "Answer type: ${question.answer.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun loadAvailableQuestions(
    context: Context,
    setAvailableQuestions: (List<ModelQuestion>) -> Unit
) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val database = AppDatabase.getDatabase(context)
            val questionDao = database.questionDao()
            val questions = questionDao.getAllQuestionsList()
            withContext(Dispatchers.Main) {
                setAvailableQuestions(questions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun addSelectedQuestionsToForm(
    formManager: FormManager,
    formId: String,
    selectedQuestions: List<ModelQuestion>,
    context: Context,
    onTestAdded: () -> Unit,
    onTestAddFailed: () -> Unit
) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            // Обновляем форму с выбранными вопросами
            val result = formManager.updateFormWithTest(formId, selectedQuestions, 2) // порог 2

            withContext(Dispatchers.Main) {
                if (result != null) {
                    Toast.makeText(context, "Test added to form successfully!", Toast.LENGTH_SHORT).show()
                    onTestAdded()
                } else {
                    Toast.makeText(context, "Failed to add test to form", Toast.LENGTH_SHORT).show()
                    onTestAddFailed()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error adding test to form: ${e.message}", Toast.LENGTH_SHORT).show()
                onTestAddFailed()
            }
        }
    }
}

// Остальные функции (AccountStep, RolesStep, DetailsStep, RoleSelection, GameTypeSelection, GenderSelection, validateCurrentStep, createFormWithTestPrompt, activateForm)
// остаются без изменений

// ... остальной код без изменений ...




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

fun createFormWithTestPrompt(
    formManager: FormManager,
    userStore: UserStore,
    setIsLoading: (Boolean) -> Unit,
    setFormId: (String) -> Unit,
    setShowAddTestDialog: (Boolean) -> Unit,
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
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    setIsLoading(false)
                    setShowAddTestDialog(true) // Show the test prompt dialog instead of activation
                }
            } else {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    setIsLoading(false)
                    Toast.makeText(context, "Failed to create form", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(context, "Error creating form: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            setIsLoading(false)
        }
    }
}

fun addTestToForm(
    formManager: FormManager,
    formId: String,
    context: android.content.Context,
    onTestAdded: () -> Unit,
    onTestAddFailed: () -> Unit
) {
    // Get the database instance and DAO
    val database = AppDatabase.getDatabase(context)
    val questionDao = database.questionDao()
    
    // Get all questions from the local database
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val allQuestions = questionDao.getAllQuestionsList()
            
            if (allQuestions.isNotEmpty()) {
                // Select 3 random questions from the database
                val selectedQuestions = allQuestions.shuffled().take(3)
                
                // Update the form with the selected questions
                val result = formManager.updateFormWithTest(formId, selectedQuestions, 2) // threshold of 2
                
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (result != null) {
                        Toast.makeText(context, "Test added to form successfully!", Toast.LENGTH_SHORT).show()
                        onTestAdded()
                    } else {
                        Toast.makeText(context, "Failed to add test to form", Toast.LENGTH_SHORT).show()
                        onTestAddFailed()
                    }
                }
            } else {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "No questions available in database", Toast.LENGTH_SHORT).show()
                    onTestAddFailed()
                }
            }
        } catch (e: Exception) {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(context, "Error adding test to form: ${e.message}", Toast.LENGTH_SHORT).show()
                onTestAddFailed()
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