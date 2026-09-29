package com.learnquest.mp.data.model

/** Everything the Home screen needs, bundled into one object. */
data class HomeData(
    val progress: StudentProgress,
    val continueTopic: LearningTopic,
    val zones: List<LearningZone>,
    val dailyChallenge: DailyChallenge,
    val quickActions: List<QuickAction>
)
