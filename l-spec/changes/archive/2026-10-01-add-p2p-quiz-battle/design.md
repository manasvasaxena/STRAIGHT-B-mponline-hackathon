## Context

See proposal.md for background and specs/p2p-quiz-battle/spec.md for requirements.

LearnQuest MP is an offline-first Android application built with Jetpack Compose, Kotlin Flow, and a stateful repository (`StatefullLearningRepository`). To introduce P2P Quiz Battles without internet or external routers, native Android `WifiP2pManager` is used for discovery and direct TCP socket communication (`ServerSocket` on Host port 8888, `Socket` on Client).

## Goals / Non-Goals

**Goals:**
- Provide zero-internet P2P device discovery, pairing, and real-time socket communication.
- Implement host/client session architecture with structured JSON message protocols.
- Reuse existing `QuizQuestion` models and `StatefullLearningRepository` XP/Profile state management.
- Provide a responsive Jetpack Compose UI matching the SUTRA/LearnQuest MP design system.

**Non-Goals:**
- Cloud-based multiplayer or webRTC signaling over WAN.
- Multi-device (>2 player) battle support for MVP.

## Decisions

### 1. Networking Layer Architecture (`com.learnquest.mp.p2p`)
- **`WifiDirectManager`**: Wraps `WifiP2pManager` and `BroadcastReceiver` for peer discovery and socket connection establishment. Handles Android 13+ `NEARBY_WIFI_DEVICES` vs legacy location permissions.
- **`P2PConnectionManager`**: Manages low-level TCP socket I/O, writing and reading line-based structured JSON messages (`BattleMessage`).
- **`BattleHostSession` & `BattleClientSession`**: Holds session state (`BattleState`: SEARCHING, LOBBY, READY, QUESTION, QUESTION_RESULT, FINISHED, DISCONNECTED). The host generates question packages from local repo and validates client answers.

### 2. Message Protocol
`BattleMessage` structure:
- `type`: `HELLO`, `LOBBY_CONFIG`, `READY`, `QUESTION_PKG`, `SUBMIT_ANSWER`, `QUESTION_RESULT`, `BATTLE_FINISHED`, `CANCEL`
- `payload`: Structured JSON payload (e.g. `QuestionPackage`, `AnswerSubmission`, `BattleResult`).

### 3. Scoring & Security
- Host validates all answers. Correct answer = 10 points + max 5 speed bonus (based on remaining timer seconds).
- XP allocation: Win (+50 XP), Draw (+25 XP), Loss (+10 XP). Anti-farming logic limits battle XP per session.

## Risks / Trade-offs

- **[Risk] Wi-Fi Direct Permission / Location State on Android Devices** → **Mitigation**: Provide clear runtime permission check & request UI flow in `QuizBattleScreen`.
- **[Risk] Unexpected Disconnection Mid-Quiz** → **Mitigation**: Handle socket disconnect events gracefully, show "Connection Interrupted" banner, and allow safe exit to Home without invalid score state.
