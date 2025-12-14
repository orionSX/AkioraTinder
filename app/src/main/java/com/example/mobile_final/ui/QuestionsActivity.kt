// QuestionsActivity.kt
package com.example.mobile_final.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.mobile_final.database.AppDatabase
import com.example.mobile_final.model.AnswerType
import com.example.mobile_final.model.Question
import com.example.mobile_final.ui.theme.Mobile_finalTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape

class QuestionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Mobile_finalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuestionsContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Состояния
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var selectedQuestion by remember { mutableStateOf<Question?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Загружаем вопросы при запуске
    LaunchedEffect(Unit) {
        loadQuestions(context) { loadedQuestions ->
            questions = loadedQuestions
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление вопросами") },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as QuestionsActivity).finish()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val database = AppDatabase.getDatabase(context)
                                val dao = database.questionDao()
                                dao.deleteAllQuestions()
                                questions = emptyList()
                            }
                        },
                        enabled = questions.isNotEmpty()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Удалить все")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedQuestion = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить вопрос")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (questions.isEmpty()) {
                EmptyQuestionsState {
                    selectedQuestion = null
                    showDialog = true
                }
            } else {
                QuestionsList(
                    questions = questions,
                    onEditClick = { question ->
                        selectedQuestion = question
                        showDialog = true
                    },
                    onDeleteClick = { question ->
                        scope.launch(Dispatchers.IO) {
                            val database = AppDatabase.getDatabase(context)
                            val dao = database.questionDao()
                            dao.deleteQuestion(question)
                            loadQuestions(context) { loadedQuestions ->
                                questions = loadedQuestions
                            }
                        }
                    }
                )
            }
        }
    }

    // Диалог для добавления/редактирования вопроса
    if (showDialog) {
        QuestionDialog(
            question = selectedQuestion,
            onSave = { questionText, answerType ->
                scope.launch(Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(context)
                    val dao = database.questionDao()

                    if (selectedQuestion == null) {
                        // Добавить новый вопрос
                        val question = Question(question = questionText, answer = answerType)
                        dao.insertQuestion(question)
                    } else {
                        // Обновить существующий вопрос
                        val updatedQuestion = selectedQuestion!!.copy(
                            question = questionText,
                            answer = answerType
                        )
                        dao.updateQuestion(updatedQuestion)
                    }

                    loadQuestions(context) { loadedQuestions ->
                        questions = loadedQuestions
                    }
                }
                showDialog = false
                selectedQuestion = null
            },
            onDismiss = {
                showDialog = false
                selectedQuestion = null
            }
        )
    }
}

@Composable
fun EmptyQuestionsState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QuestionAnswer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Нет вопросов",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Нажмите на кнопку +, чтобы добавить первый вопрос",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            modifier = Modifier.width(200.dp)
        ) {
            Text("Добавить вопрос")
        }
    }
}

@Composable
fun QuestionsList(
    questions: List<Question>,
    onEditClick: (Question) -> Unit,
    onDeleteClick: (Question) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(questions, key = { it.id }) { question ->
            QuestionCard(
                question = question,
                onEditClick = { onEditClick(question) },
                onDeleteClick = { onDeleteClick(question) }
            )
        }
    }
}

@Composable
fun QuestionCard(
    question: Question,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Простой Box для отображения ответа
                Box(
                    modifier = Modifier
                        .background(
                            color = if (question.answer == AnswerType.YES)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (question.answer == AnswerType.YES) "ДА" else "НЕТ",
                        fontWeight = FontWeight.Bold,
                        color = if (question.answer == AnswerType.YES)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDialog(
    question: Question?,
    onSave: (String, AnswerType) -> Unit,
    onDismiss: () -> Unit
) {
    var questionText by remember { mutableStateOf(question?.question ?: "") }
    var answerType by remember { mutableStateOf(question?.answer ?: AnswerType.YES) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (question == null) "Добавить вопрос" else "Редактировать вопрос")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Вопрос") },
                    placeholder = { Text("Введите текст вопроса") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilterChip(
                        selected = answerType == AnswerType.YES,
                        onClick = { answerType = AnswerType.YES },
                        label = { Text("Да") }
                    )
                    FilterChip(
                        selected = answerType == AnswerType.NO,
                        onClick = { answerType = AnswerType.NO },
                        label = { Text("Нет") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank()) {
                        onSave(questionText, answerType)
                    }
                },
                enabled = questionText.isNotBlank()
            ) {
                Text(if (question == null) "Добавить" else "Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

// Вспомогательная функция для загрузки вопросов
private fun loadQuestions(
    context: android.content.Context,
    onLoaded: (List<Question>) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        val database = AppDatabase.getDatabase(context)
        val dao = database.questionDao()

        // Flow не поддерживается напрямую, получаем данные сразу
        val questionsList = dao.getAllQuestionsList()
        // Если getAllQuestions возвращает Flow, нужно изменить на:
        // val questionsList = dao.getAllQuestionsSync() - если создадим такой метод
        // или изменить DAO чтобы возвращал List вместо Flow
        onLoaded(questionsList)
    }
}