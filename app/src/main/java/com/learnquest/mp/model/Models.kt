package com.learnquest.mp.model

import java.util.UUID

/**
 * Represents student profiles for shared device usage (Key Innovation #3).
 */
data class StudentProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarRes: String = "student_avatar_1",
    val grade: String,
    val preferredLanguage: Language = Language.HINDI,
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val isStreakProtected: Boolean = true, // Streak Shield active
    val isSyncPending: Boolean = false
)

enum class Language(val displayName: String, val code: String) {
    HINDI("हिन्दी (Hindi)", "hi"),
    ENGLISH("English", "en")
}

/**
 * Level-wise content module for chunked downloading (Key Innovation #1 & Low-Bandwidth Design).
 */
data class LearningLevel(
    val id: String,
    val titleHindi: String,
    val titleEnglish: String,
    val subject: String,
    val levelNumber: Int,
    val totalLessons: Int,
    val downloadedLessons: Int,
    val downloadSizeMb: Double,
    val isDownloaded: Boolean = false,
    val masteryState: MasteryState = MasteryState.WEAK
)

/**
 * Rule-based mastery states for adaptive learning (Key Innovation #7).
 */
enum class MasteryState(val label: String, val threshold: String) {
    WEAK("Weak (< 60%)", "Requires Revision + Extra Practice"),
    DEVELOPING("Developing (60-84%)", "Requires Additional Practice"),
    MASTERED("Mastered (≥ 85%)", "Unlocks Next Level")
}

/**
 * Represents quiz questions and scores for adaptive revision.
 */
data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

/**
 * Offline AI fallback doubt resolution query (Key Innovation #5 & Teacher Support).
 */
data class DoubtQuery(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val questionText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolvedOffline: Boolean,
    val offlineAnswer: String?,
    val isEscalatedToTeacher: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
}

/**
 * Verified scholarship entity (Key Innovation #6).
 */
data class Scholarship(
    val id: String,
    val title: String,
    val provider: String,
    val category: String,
    val eligibility: String,
    val deadline: String,
    val amount: String,
    val isVerified: Boolean = true,
    val lastVerifiedDate: String
)

/**
 * Rule-based career pathway.
 */
data class CareerPathway(
    val id: String,
    val title: String,
    val sector: String,
    val requiredEducation: String,
    val description: String,
    val recommendedSubjects: List<String>
)

/**
 * Queue item for Delta Synchronization (Section 182-194).
 */
data class SyncQueueItem(
    val id: String = UUID.randomUUID().toString(),
    val actionType: String, // "XP_ADD", "LESSON_COMPLETE", "QUIZ_RESULT", "DOUBT_ESCALATE"
    val timestamp: Long = System.currentTimeMillis(),
    val payload: String,
    val status: SyncStatus = SyncStatus.PENDING
)
