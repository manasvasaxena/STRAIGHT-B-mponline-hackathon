package com.learnquest.mp.data.repository

import android.content.Context
import com.learnquest.mp.data.network.NetworkConnectivityObserver
import com.learnquest.mp.data.model.*

/**
 * Legacy sample repository. Its content is still sample data, but connectivity
 * is sourced from the real Android network observer rather than a fake flag.
 */
interface LearningRepository {
    fun getHomeData(): HomeData
    fun isOffline(): Boolean
}

class MockLearningRepository(context: Context) : LearningRepository, AutoCloseable {

    private val connectivityObserver = NetworkConnectivityObserver(context)

    override fun isOffline(): Boolean = !connectivityObserver.isOnline.value

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

    override fun close() {
        connectivityObserver.close()
    }
}
