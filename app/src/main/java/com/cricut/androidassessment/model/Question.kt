package com.cricut.androidassessment.model

import java.util.UUID

/**
 * Represents a question in the quiz.
 * Using a sealed class allows for exhaustive when expressions and clear sub-typing.
 */
sealed class Question {
    abstract val id: String
    abstract val text: String

    data class TrueFalse(
        override val id: String = UUID.randomUUID().toString(),
        override val text: String
    ) : Question()

    data class MultipleChoice(
        override val id: String = UUID.randomUUID().toString(),
        override val text: String,
        val options: List<String>
    ) : Question()

    data class MultipleSelection(
        override val id: String = UUID.randomUUID().toString(),
        override val text: String,
        val options: List<String>
    ) : Question()

    data class OpenEnded(
        override val id: String = UUID.randomUUID().toString(),
        override val text: String
    ) : Question()
}

/**
 * Represents a user's answer to a question.
 */
sealed class Answer {
    data class TrueFalse(val value: Boolean) : Answer()
    data class MultipleChoice(val index: Int) : Answer()
    data class MultipleSelection(val indices: Set<Int>) : Answer()
    data class OpenEnded(val text: String) : Answer()
}

/**
 * Provides sample questions for the assessment.
 */
object QuestionProvider {
    val sampleQuestions = listOf(
        Question.TrueFalse(
            text = "True or False: A Composable function can return a value?"
        ),
        Question.MultipleChoice(
            text = "Which of the following is used to maintain state across recompositions?",
            options = listOf("remember", "MutableStateFlow", "SharedPreferences", "ViewModel")
        ),
        Question.MultipleSelection(
            text = "Select all the Android lifecycle methods:",
            options = listOf("onCreate", "onResume", "onUpdate", "onRestart")
        ),
        Question.OpenEnded(
            text = "Briefly describe what Jetpack Compose is."
        )
    )
}
