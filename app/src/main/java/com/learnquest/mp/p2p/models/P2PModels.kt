package com.learnquest.mp.p2p.models

import com.learnquest.mp.model.QuizQuestion

enum class BattleRole {
    HOST,
    CLIENT
}

enum class BattleState {
    IDLE,
    SEARCHING,
    CONNECTING,
    LOBBY,
    READY,
    QUESTION,
    WAITING_FOR_OPPONENT,
    QUESTION_RESULT,
    NEXT_QUESTION,
    FINISHED,
    DISCONNECTED,
    CANCELLED
}

data class PlayerBattleInfo(
    val id: String,
    val name: String,
    val avatarRes: String = "student_avatar_1",
    val level: Int = 1,
    val totalXp: Int = 0,
    val deviceAddress: String = ""
)

data class BattleConfig(
    val battleId: String,
    val subject: String,
    val difficulty: String,
    val questionCount: Int,
    val timerSecondsPerQuestion: Int = 15
)

data class QuestionPackage(
    val questions: List<QuizQuestion>
)

data class AnswerSubmission(
    val questionIndex: Int,
    val selectedOptionIndex: Int,
    val secondsTaken: Int
)

data class QuestionResult(
    val questionIndex: Int,
    val playerAnswerIndex: Int,
    val playerIsCorrect: Boolean,
    val playerPointsEarned: Int,
    val opponentAnswerIndex: Int,
    val opponentIsCorrect: Boolean,
    val opponentPointsEarned: Int
)

data class BattleFinalSummary(
    val winnerName: String,
    val player1Score: Int,
    val player1XpEarned: Int,
    val player2Score: Int,
    val player2XpEarned: Int,
    val totalQuestions: Int,
    val player1CorrectCount: Int,
    val isDraw: Boolean = false
)

enum class MessageType {
    HELLO,
    LOBBY_CONFIG,
    READY_TOGGLE,
    START_BATTLE,
    QUESTION_PKG,
    SUBMIT_ANSWER,
    QUESTION_RESULT,
    NEXT_QUESTION_TRIGGER,
    BATTLE_FINISHED,
    LEAVE_BATTLE
}

data class BattleMessage(
    val messageId: String = java.util.UUID.randomUUID().toString(),
    val battleId: String,
    val type: MessageType,
    val senderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val payloadJson: String = ""
)
