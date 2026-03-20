package com.cricut.androidassessment.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cricut.androidassessment.model.Answer
import com.cricut.androidassessment.model.Question
import com.cricut.androidassessment.ui.AssessmentViewModel
import com.cricut.androidassessment.ui.theme.AndroidAssessmentTheme

@Composable
fun AssessmentScreen(
    modifier: Modifier = Modifier,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.isQuizComplete -> {
                QuizCompleteScreen(
                    onRestart = viewModel::onRestart,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                val currentQuestion = uiState.currentQuestion
                if (currentQuestion != null) {
                    QuestionContent(
                        question = currentQuestion,
                        answer = uiState.currentAnswer,
                        progress = uiState.progress,
                        onAnswer = viewModel::onAnswer,
                        onNext = viewModel::onNext,
                        onPrevious = viewModel::onPrevious,
                        hasPrevious = uiState.hasPrevious,
                        hasNext = uiState.hasNext,
                        canProceed = uiState.canProceed
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuestionContent(
    question: Question,
    answer: Answer?,
    progress: Float,
    onAnswer: (Answer) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
    canProceed: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        AnimatedContent(
            targetState = question,
            transitionSpec = {
                fadeIn() with fadeOut()
            },
            modifier = Modifier.weight(1f)
        ) { targetQuestion ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = targetQuestion.text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    when (targetQuestion) {
                        is Question.TrueFalse -> {
                            TrueFalseQuestion(
                                selected = (answer as? Answer.TrueFalse)?.value,
                                onSelected = { onAnswer(Answer.TrueFalse(it)) }
                            )
                        }
                        is Question.MultipleChoice -> {
                            MultipleChoiceQuestion(
                                options = targetQuestion.options,
                                selected = (answer as? Answer.MultipleChoice)?.index,
                                onSelected = { onAnswer(Answer.MultipleChoice(it)) }
                            )
                        }
                        is Question.MultipleSelection -> {
                            MultipleSelectionQuestion(
                                options = targetQuestion.options,
                                selectedIndices = (answer as? Answer.MultipleSelection)?.indices ?: emptySet(),
                                onToggle = { index ->
                                    val current = (answer as? Answer.MultipleSelection)?.indices ?: emptySet()
                                    val next = if (current.contains(index)) current - index else current + index
                                    onAnswer(Answer.MultipleSelection(next))
                                }
                            )
                        }
                        is Question.OpenEnded -> {
                            OpenEndedQuestion(
                                text = (answer as? Answer.OpenEnded)?.text ?: "",
                                onTextChanged = { onAnswer(Answer.OpenEnded(it)) }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onPrevious,
                enabled = hasPrevious
            ) {
                Text("Previous")
            }

            Button(
                onClick = onNext,
                enabled = canProceed
            ) {
                Text(if (hasNext) "Next" else "Finish")
            }
        }
    }
}

@Composable
fun TrueFalseQuestion(
    selected: Boolean?,
    onSelected: (Boolean) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        listOf(true, false).forEach { value ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = selected == value,
                        onClick = { onSelected(value) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == value,
                    onClick = null
                )
                Text(
                    text = if (value) "True" else "False",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun MultipleChoiceQuestion(
    options: List<String>,
    selected: Int?,
    onSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = selected == index,
                        onClick = { onSelected(index) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == index,
                    onClick = null
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun MultipleSelectionQuestion(
    options: List<String>,
    selectedIndices: Set<Int>,
    onToggle: (Int) -> Unit
) {
    Column {
        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = selectedIndices.contains(index),
                        onClick = { onToggle(index) },
                        role = Role.Checkbox
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedIndices.contains(index),
                    onCheckedChange = null
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun OpenEndedQuestion(
    text: String,
    onTextChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChanged,
        label = { Text("Your Answer") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
}

@Composable
fun QuizCompleteScreen(
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRestart) {
            Text("Restart Quiz")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAssessmentScreen() {
    AndroidAssessmentTheme {
        AssessmentScreen()
    }
}
