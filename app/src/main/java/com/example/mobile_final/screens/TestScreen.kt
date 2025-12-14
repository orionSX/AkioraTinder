package com.example.mobile_final.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile_final.dto.Answer
import com.example.mobile_final.dto.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    questions: List<Question>,
    onSubmit: (List<Answer>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAnswers by remember { mutableStateOf<Map<Int, Answer>>(emptyMap()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Назад")
            }
            
            Text(
                text = "Тест",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${selectedAnswers.size}/${questions.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(questions) { question ->
                val index = questions.indexOf(question)
                val isSelected = selectedAnswers.containsKey(index)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = question.question,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val yesSelected = selectedAnswers[index] == Answer.YES
                            val noSelected = selectedAnswers[index] == Answer.NO
                            
                            FilterChip(
                                selected = yesSelected,
                                onClick = {
                                    selectedAnswers = selectedAnswers.toMutableMap().apply {
                                        this[index] = Answer.YES
                                    }
                                },
                                label = { Text("Да") }
                            )
                            
                            FilterChip(
                                selected = noSelected,
                                onClick = {
                                    selectedAnswers = selectedAnswers.toMutableMap().apply {
                                        this[index] = Answer.NO
                                    }
                                },
                                label = { Text("Нет") }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val answers = questions.mapIndexed { index, _ ->
                    selectedAnswers[index] ?: Answer.NO // Default to NO if not answered
                }
                onSubmit(answers)
            },
            enabled = selectedAnswers.size == questions.size,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (selectedAnswers.size == questions.size) "Отправить" else "Ответьте на все вопросы",
                fontSize = 16.sp
            )
        }
    }
}