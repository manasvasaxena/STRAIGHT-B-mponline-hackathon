package com.learnquest.mp.data.model

data class LearningTopic(
    val id: String,
    val subject: String,
    val topic: String,
    val level: Int,
    val progressPercent: Int,
    val emoji: String
)
