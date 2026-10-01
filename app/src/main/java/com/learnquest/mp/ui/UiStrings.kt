package com.learnquest.mp.ui

import com.learnquest.mp.model.Language
import com.learnquest.mp.model.MasteryState

/** Small runtime translator for app chrome and feature labels.
 *
 * Content downloaded from PKGS keeps its original language; this object covers
 * the app's controls, labels, status messages, and navigation consistently.
 */
class AppStrings(val language: Language) {
    fun t(english: String, hindi: String, hinglish: String = "$english / $hindi"): String =
        when (language) {
            Language.ENGLISH -> english
            Language.HINDI -> hindi
            Language.HINGLISH -> hinglish
        }

    fun navHome() = t("Home", "होम", "Home")
    fun navExplore() = t("Explore", "खोजें", "Explore")
    fun navLearn() = t("Learn", "सीखें", "Learn")
    fun navGuidance() = t("Guidance", "मार्गदर्शन", "Guidance")
    fun navProfile() = t("Profile", "प्रोफ़ाइल", "Profile")

    fun level(value: Int) = t("Level $value", "स्तर $value", "Level $value")
    fun complete(percent: Int) = t("$percent% complete", "$percent% पूरा", "$percent% complete")
    fun progressToLevel(percent: Int, level: Int) = t("$percent% to Level $level", "$percent% अगले स्तर तक $level", "$percent% to Level $level")
    fun dayStreak(days: Int) = t("🔥 $days Day Streak", "🔥 $days दिन की स्ट्रीक", "🔥 $days Day Streak")
    fun xp(value: Int) = "$value XP"
    fun mastery(state: MasteryState): String = when (state) {
        MasteryState.WEAK -> t("Weak (< 60%)", "कमज़ोर (< 60%)", "Weak (< 60%)")
        MasteryState.DEVELOPING -> t("Developing (60-84%)", "विकासशील (60-84%)", "Developing (60-84%)")
        MasteryState.MASTERED -> t("Mastered (≥ 85%)", "निपुण (≥ 85%)", "Mastered (≥ 85%)")
    }
    fun masteryAction(state: MasteryState): String = when (state) {
        MasteryState.WEAK -> t("Requires Revision + Extra Practice", "दोहराव और अतिरिक्त अभ्यास ज़रूरी", "Revision + extra practice चाहिए")
        MasteryState.DEVELOPING -> t("Requires Additional Practice", "अतिरिक्त अभ्यास ज़रूरी", "Additional practice चाहिए")
        MasteryState.MASTERED -> t("Unlocks Next Level", "अगला स्तर खुलता है", "Next level unlock होगा")
    }
}

fun Language.appStrings() = AppStrings(this)
