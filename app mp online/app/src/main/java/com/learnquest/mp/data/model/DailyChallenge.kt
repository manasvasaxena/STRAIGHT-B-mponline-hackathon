package com.learnquest.mp.data.model

data class DailyChallenge(
    val id: String,
    val question: String,   // not shown on the home screen yet; used by the future challenge screen
    val rewardXp: Int
)
