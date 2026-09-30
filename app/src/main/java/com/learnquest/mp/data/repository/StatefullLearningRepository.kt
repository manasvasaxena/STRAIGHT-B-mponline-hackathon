package com.learnquest.mp.data.repository

import android.content.Context
import com.learnquest.mp.data.model.*
import com.learnquest.mp.data.network.NetworkConnectivityObserver
import com.learnquest.mp.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * State-aware Repository providing real in-memory reactive state and offline sync queue logic.
 */
class StatefullLearningRepository(context: Context) : LearningRepository, AutoCloseable {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectivityObserver = NetworkConnectivityObserver(context)

    private val _isOffline = MutableStateFlow(!connectivityObserver.isOnline.value)
    val isOfflineFlow: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _appLanguage = MutableStateFlow(Language.HINDI)
    val appLanguageFlow: StateFlow<Language> = _appLanguage.asStateFlow()

    init {
        connectivityObserver.isOnline
            .onEach { isOnline -> _isOffline.value = !isOnline }
            .launchIn(repositoryScope)
    }

    fun setAppLanguage(language: Language) {
        _appLanguage.value = language
    }

    override fun isOffline(): Boolean = _isOffline.value

    private val _profiles = MutableStateFlow<List<StudentProfile>>(
        listOf(
            StudentProfile(
                id = "1",
                name = "Aarav Sharma",
                grade = "Class 8",
                preferredLanguage = Language.HINDI,
                totalXp = 1250,
                streakDays = 5,
                isStreakProtected = true
            ),
            StudentProfile(
                id = "2",
                name = "Priya Verma",
                grade = "Class 10",
                preferredLanguage = Language.HINDI,
                totalXp = 2100,
                streakDays = 12,
                isStreakProtected = true
            )
        )
    )
    val profilesFlow: StateFlow<List<StudentProfile>> = _profiles.asStateFlow()

    private val _activeProfileId = MutableStateFlow("1")
    val activeProfileIdFlow: StateFlow<String> = _activeProfileId.asStateFlow()

    private val _syncQueue = MutableStateFlow<List<SyncQueueItem>>(
        listOf(
            SyncQueueItem(
                actionType = "XP_ADD",
                payload = "Completed Math Level 3 Quiz (+50 XP)",
                status = SyncStatus.PENDING
            ),
            SyncQueueItem(
                actionType = "DOUBT_ESCALATE",
                payload = "Escalated Doubt: What is Photosynthesis?",
                status = SyncStatus.PENDING
            )
        )
    )
    val syncQueueFlow: StateFlow<List<SyncQueueItem>> = _syncQueue.asStateFlow()

    private val _scholarships = MutableStateFlow<List<Scholarship>>(
        listOf(
            Scholarship(
                id = "sch-1",
                title = "Mukhyamantri Medhavi Chhatra Yojna (MMCY)",
                provider = "MP State Govt",
                category = "Merit Based",
                eligibility = "≥ 70% in MP Board / ≥ 85% in CBSE (12th)",
                deadline = "30 Nov 2026",
                amount = "Full Tuition Fee Waiver",
                isVerified = true,
                lastVerifiedDate = "2026-09-01"
            ),
            Scholarship(
                id = "sch-2",
                title = "NMMS - National Means-cum-Merit Scholarship",
                provider = "Central & MP Govt",
                category = "Class 8 Students",
                eligibility = "Class 8 passed with ≥ 55% & income < ₹3.5 Lakh",
                deadline = "15 Dec 2026",
                amount = "₹12,000 / year",
                isVerified = true,
                lastVerifiedDate = "2026-09-15"
            )
        )
    )
    val scholarshipsFlow: StateFlow<List<Scholarship>> = _scholarships.asStateFlow()

    private val _careerPathways = MutableStateFlow<List<CareerPathway>>(
        listOf(
            CareerPathway(
                id = "car-1",
                title = "Agricultural Officer / Krishi Kendra Specialist",
                sector = "Agriculture & Rural Development",
                requiredEducation = "B.Sc Agriculture / Diploma",
                description = "Manage soil health, modern farming techniques, and rural agricultural expansion in MP districts.",
                recommendedSubjects = listOf("Science", "Biology", "Environment")
            ),
            CareerPathway(
                id = "car-2",
                title = "Software Developer / IT Technician",
                sector = "Technology & Digital MP",
                requiredEducation = "ITI Computer / BCA / B.Tech",
                description = "Build software, support e-governance kiosks (MPOnline), and work in tech hubs.",
                recommendedSubjects = listOf("Mathematics", "Computer Science", "English")
            )
        )
    )
    val careerPathwaysFlow: StateFlow<List<CareerPathway>> = _careerPathways.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(
        listOf(
            QuizQuestion(
                id = "q1",
                questionText = "पौधों में प्रकाश संश्लेषण (Photosynthesis) के लिए कौन सी गैस आवश्यक है?",
                options = listOf("ऑक्सीजन (Oxygen)", "कार्बन डाइऑक्साइड (Carbon Dioxide)", "नाइट्रोजन (Nitrogen)", "हाइड्रोजन (Hydrogen)"),
                correctAnswerIndex = 1,
                explanation = "पौधे कार्बन डाइऑक्साइड (CO2) ग्रहण करते हैं और ऑक्सीजन छोड़ते हैं।"
            ),
            QuizQuestion(
                id = "q2",
                questionText = "मध्य प्रदेश की सबसे लंबी नदी कौन सी है?",
                options = listOf("चंबल (Chambal)", "ताप्ती (Tapti)", "नर्मदा (Narmada)", "बेतवा (Betwa)"),
                correctAnswerIndex = 2,
                explanation = "नर्मदा नदी मध्य प्रदेश की जीवन रेखा मानी जाती है।"
            )
        )
    )
    val quizQuestionsFlow: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    fun selectProfile(id: String) {
        _activeProfileId.value = id
    }

    fun addProfile(name: String, grade: String, language: Language) {
        val newProfile = StudentProfile(
            name = name,
            grade = grade,
            preferredLanguage = language,
            totalXp = 0,
            streakDays = 1,
            isStreakProtected = true
        )
        _profiles.value = _profiles.value + newProfile
        _activeProfileId.value = newProfile.id
    }

    fun addSyncItem(actionType: String, payload: String) {
        val item = SyncQueueItem(
            actionType = actionType,
            payload = payload,
            status = SyncStatus.PENDING
        )
        _syncQueue.value = _syncQueue.value + item
    }

    fun syncAllPending() {
        _syncQueue.value = _syncQueue.value.map { it.copy(status = SyncStatus.SYNCED) }
    }

    override fun getHomeData(): HomeData {
        val activeProfile = _profiles.value.find { it.id == _activeProfileId.value } ?: _profiles.value.first()
        return HomeData(
            progress = StudentProgress(
                studentName = activeProfile.name,
                level = 5,
                currentXp = activeProfile.totalXp,
                xpForNextLevel = activeProfile.totalXp + 300,
                streakDays = activeProfile.streakDays
            ),
            continueTopic = LearningTopic(
                id = "math-fractions",
                subject = "Mathematics",
                topic = "Fractions & Decimals",
                level = 3,
                progressPercent = 65,
                emoji = "📐"
            ),
            zones = listOf(
                LearningZone("science", "Science Forest", "🌳", 65),
                LearningZone("math", "Mathematics Mountain", "🏔", 40),
                LearningZone("computer", "Computer City", "💻", 20),
                LearningZone("language", "Language Village", "🗣", 80),
                LearningZone("career", "Career Campus", "🎓", 10)
            ),
            dailyChallenge = DailyChallenge(
                id = "daily-1",
                question = "प्रकाश संश्लेषण (Photosynthesis) पौधे किस गैस का उपयोग करते हैं?",
                rewardXp = 50
            ),
            quickActions = listOf(
                QuickAction("downloads", "📥", "Downloads"),
                QuickAction("ask_ai", "🤖", "Ask AI"),
                QuickAction("voice", "🎤", "Voice Tutor"),
                QuickAction("career", "🎓", "Career")
            )
        )
    }

    override fun close() {
        connectivityObserver.close()
        repositoryScope.cancel()
    }
}
