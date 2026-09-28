package com.learnquest.mp.data.repository

import com.learnquest.mp.data.model.*

/**
 * The UI only talks to this interface. Today it is backed by mock data;
 * later you can add a Room/network version without touching any screen.
 */
interface LearningRepository {
    fun getHomeData(): HomeData
    fun isOffline(): Boolean
}

class MockLearningRepository : LearningRepository {

    // Mock connectivity flag. Replace with real network detection later.
    private val isOffline = true

    override fun isOffline(): Boolean = isOffline

    override fun getHomeData() = HomeData(
        progress = StudentProgress(
            studentName = "Aarav",
            level = 5,
            currentXp = 320,
            xpForNextLevel = 500,
            streakDays = 7
        ),
        continueTopic = LearningTopic(
            id = "math-fractions",
            subject = "Mathematics",
            topic = "Fractions",
            level = 3,
            progressPercent = 65,
            emoji = "📐"
        ),
        zones = listOf(
            LearningZone("science", "Science Forest", "🌳", 65),
            LearningZone("math", "Mathematics Mountain", "🏔", 40),
            LearningZone("computer", "Computer City", "💻", 20),
            LearningZone("language", "Language Village", "🗣", 80),
            LearningZone("career", "Career Campus", "🎓", 10),
        ),
        dailyChallenge = DailyChallenge(
            id = "daily-1",
            question = "Which planet is known as the Red Planet?",
            rewardXp = 50
        ),
        quickActions = listOf(
            QuickAction("downloads", "📥", "Downloads"),
            QuickAction("ask_ai", "🤖", "Ask AI"),
            QuickAction("voice", "🎤", "Voice Tutor"),
            QuickAction("career", "🎓", "Career"),
        )
    )
}
