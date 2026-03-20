package com.cricut.androidassessment.ui

import com.cricut.androidassessment.model.Answer
import com.cricut.androidassessment.model.Question
import com.cricut.androidassessment.model.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AssessmentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: QuizRepository
    private lateinit var viewModel: AssessmentViewModel

    private val sampleQuestions = listOf(
        Question.TrueFalse("1", "Q1"),
        Question.MultipleChoice("2", "Q2", listOf("A", "B")),
        Question.OpenEnded("3", "Q3")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel() = runTest {
        whenever(repository.getQuestions()).thenReturn(sampleQuestions)
        viewModel = AssessmentViewModel(repository)
        advanceUntilIdle()
    }

    @Test
    fun `Given viewModel init, When questions loaded, Then state has questions`() = runTest {
        initViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleQuestions, state.questions)
        assertEquals(0, state.currentQuestionIndex)
        assertNotNull(state.currentQuestion)
        assertEquals("1", state.currentQuestion?.id)
    }

    @Test
    fun `Given viewModel loaded, When answer provided, Then state updates with answer`() = runTest {
        initViewModel()

        val answer = Answer.TrueFalse(true)
        viewModel.onAnswer(answer)

        val state = viewModel.uiState.value
        assertEquals(answer, state.answers["1"])
        assertEquals(answer, state.currentAnswer)
    }

    @Test
    fun `Given viewModel on first question, When onNext called, Then index increments`() = runTest {
        initViewModel()

        viewModel.onNext()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentQuestionIndex)
        assertEquals("2", state.currentQuestion?.id)
        assertFalse(state.isQuizComplete)
    }

    @Test
    fun `Given viewModel on last question, When onNext called, Then quiz completes`() = runTest {
        initViewModel()
        viewModel.onNext() // to index 1
        viewModel.onNext() // to index 2

        viewModel.onNext() // complete

        val state = viewModel.uiState.value
        assertTrue(state.isQuizComplete)
    }

    @Test
    fun `Given viewModel on second question, When onPrevious called, Then index decrements`() = runTest {
        initViewModel()
        viewModel.onNext()

        viewModel.onPrevious()

        val state = viewModel.uiState.value
        assertEquals(0, state.currentQuestionIndex)
    }

    @Test
    fun `Given viewModel with answers, When onRestart called, Then state is reset`() = runTest {
        initViewModel()
        viewModel.onAnswer(Answer.TrueFalse(true))
        viewModel.onNext()
        
        viewModel.onRestart()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.currentQuestionIndex)
        assertTrue(state.answers.isEmpty())
        assertFalse(state.isQuizComplete)
    }

    @Test
    fun `Given state without answer, When checking canProceed, Then returns false`() {
        val state = AssessmentUiState(
            questions = sampleQuestions,
            currentQuestionIndex = 0
        )
        
        assertFalse(state.canProceed)
    }

    @Test
    fun `Given state with answer, When checking canProceed, Then returns true`() {
        val state = AssessmentUiState(
            questions = sampleQuestions,
            currentQuestionIndex = 0,
            answers = mapOf("1" to Answer.TrueFalse(true))
        )
        
        assertTrue(state.canProceed)
    }

    @Test
    fun `Given OpenEnded question, When text is blank or empty, Then canProceed is false`() {
        val openEndedQuestion = Question.OpenEnded("3", "Q3")
        val state = AssessmentUiState(
            questions = listOf(openEndedQuestion),
            currentQuestionIndex = 0
        )

        assertFalse(state.copy(answers = mapOf("3" to Answer.OpenEnded(""))).canProceed)
        assertFalse(state.copy(answers = mapOf("3" to Answer.OpenEnded("  "))).canProceed)
    }

    @Test
    fun `Given OpenEnded question, When text is provided, Then canProceed is true`() {
        val openEndedQuestion = Question.OpenEnded("3", "Q3")
        val state = AssessmentUiState(
            questions = listOf(openEndedQuestion),
            currentQuestionIndex = 0,
            answers = mapOf("3" to Answer.OpenEnded("Answer"))
        )

        assertTrue(state.canProceed)
    }
}
