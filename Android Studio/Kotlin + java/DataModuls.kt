package com.example.deftesisar

import java.util.Date

data class User(
    val user_name: String,
    val user_lastname: String,
    val mail: String,
    val age: Int,
    val password: String
)

data class LoginRequest(
    val mail: String,
    val password_us: String
)

data class LoginResponse(
    val token: String,
    val user: UserInfo
)

data class UserInfo(
    val id: Int,
    val email: String,
    val name: String
)

data class SignUpResponse(
    val message: String,
    val userId: Int
)

data class SymptomsRequest(
    val symptoms: List<String>
)

data class ProgressData(
    val id_user: Int,
    val date_time_attempted: Date,
    val detection_time: Double,
    val use_assistance: Boolean,
    val answer1: Boolean,
    val answer2: Boolean
)

data class ProgressResponse(
    val message: String,
    val progressId: Int
)

data class SearchResult(
    val symptom: String? = null,
    val id_Failure: Int? = null,
    val fault_name: String? = null,
    val description: String? = null,
    val name_Component: String? = null
)

data class UserProgressResponse(
    val attempts: List<Attempt>,
    val performanceCategories: List<PerformanceCategory>,
    val question1Performance: Map<String, Float>,
    val question2Performance: Map<String, Float>,
    val assistanceUsage: Map<String, Float>,
    val totalAttempts: Int,
    val averageDetectionTime: Double,
    val bestDetectionTime: Double,
    val question1SuccessRate: Double,
    val question2SuccessRate: Double,
    val assistanceUsageRate: Double
)

data class Attempt(
    val detectionTime: Float,
    val dateTimeAttempted: Date
)

data class PerformanceCategory(
    val index: Int,
    val value: Float
)

data class case(
    val id_questions: Int,
    val practice_case: String,
    val fault: String,
    val component: String
)

