package com.cricut.androidassessment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricut.androidassessment.model.Answer
import com.cricut.androidassessment.model.Question
import com.cricut.androidassessment.model.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssessmentUiState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, Answer> = emptyMap(),
    val isQuizComplete: Boolean = false,
    val isLoading: Boolean = false
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentQuestionIndex)
    val currentAnswer: Answer? get() = currentQuestion?.let { answers[it.id] }
    val hasPrevious: Boolean get() = currentQuestionIndex > 0
    val hasNext: Boolean get() = currentQuestionIndex < questions.size - 1
    val progress: Float get() = if (questions.isEmpty()) 0f else (currentQuestionIndex + 1).toFloat() / questions.size
    
    val canProceed: Boolean get() = currentAnswer?.let { answer ->
        when (answer) {
            is Answer.OpenEnded -> answer.text.isNotBlank()
            is Answer.MultipleSelection -> answer.indices.isNotEmpty()
            else -> true
        }
    } ?: false
}

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentUiState())
    val uiState: StateFlow<AssessmentUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(SIM_LOADING_DELAY_DURATION)
            val questions = repository.getQuestions()
            _uiState.update { it.copy(questions = questions, isLoading = false) }
        }
    }

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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(SIM_LOADING_DELAY_DURATION)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    currentQuestionIndex = 0,
                    answers = emptyMap(),
                    isQuizComplete = false
                )
            }
        }
    }

    companion object {
        private const val SIM_LOADING_DELAY_DURATION = 850L
    }
}
