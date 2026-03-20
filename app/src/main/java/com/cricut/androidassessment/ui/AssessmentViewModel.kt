package com.cricut.androidassessment.ui

import androidx.lifecycle.ViewModel
import com.cricut.androidassessment.model.Answer
import com.cricut.androidassessment.model.Question
import com.cricut.androidassessment.model.QuestionProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AssessmentUiState(
    val questions: List<Question> = QuestionProvider.sampleQuestions.take(2),
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, Answer> = emptyMap(),
    val isQuizComplete: Boolean = false
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentQuestionIndex)
    val currentAnswer: Answer? get() = currentQuestion?.let { answers[it.id] }
    val hasPrevious: Boolean get() = currentQuestionIndex > 0
    val hasNext: Boolean get() = currentQuestionIndex < questions.size - 1
    val canProceed: Boolean get() = currentAnswer?.let { answer ->
        when (answer) {
            is Answer.OpenEnded -> answer.text.isNotBlank()
            is Answer.MultipleSelection -> answer.indices.isNotEmpty()
            else -> true
        }
    } ?: false
}

@HiltViewModel
class AssessmentViewModel
@Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentUiState())
    val uiState: StateFlow<AssessmentUiState> = _uiState.asStateFlow()

    fun onAnswer(answer: Answer) {
        val currentQuestionId = _uiState.value.currentQuestion?.id ?: return
        _uiState.update { state ->
            state.copy(
                answers = state.answers + (currentQuestionId to answer)
            )
        }
    }

    fun onNext() {
        _uiState.update { state ->
            if (state.hasNext) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
            } else {
                state.copy(isQuizComplete = true)
            }
        }
    }

    fun onPrevious() {
        _uiState.update { state ->
            if (state.hasPrevious) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex - 1)
            } else {
                state
            }
        }
    }

    fun onRestart() {
        _uiState.update { state ->
            AssessmentUiState()
        }
    }
}
