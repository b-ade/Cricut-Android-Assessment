package com.cricut.androidassessment.model

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface QuizRepository {
    suspend fun getQuestions(): List<Question>
}

@Singleton
class MockQuizRepository @Inject constructor() : QuizRepository {
    override suspend fun getQuestions(): List<Question> {
        return QuestionProvider.sampleQuestions
    }
}
