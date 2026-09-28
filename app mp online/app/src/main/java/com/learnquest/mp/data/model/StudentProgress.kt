package com.learnquest.mp.data.model

data class StudentProgress(
    val studentName: String,
    val level: Int,
    val currentXp: Int,
    val xpForNextLevel: Int,
    val streakDays: Int
) {
    /** 0f..1f value used by the progress bar. */
    val progressFraction: Float
        get() = (currentXp.toFloat() / xpForNextLevel).coerceIn(0f, 1f)
}
