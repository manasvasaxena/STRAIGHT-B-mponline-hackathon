## Why

Rural and tribal students using LearnQuest MP often lack internet access and cellular connectivity, preventing online multiplayer learning. To foster collaborative learning and revision without internet or routers, students need a peer-to-peer local quiz battle capability using Android native Wi-Fi Direct.

## What Changes

- Add Wi-Fi Direct / P2P networking manager and session handling (`com.learnquest.mp.p2p`) for host/client discovery, connection, payload exchange, and session state management.
- Add P2P Quiz Battle UI flow (`QuizBattleScreen.kt`) covering Host setup (Subject, Difficulty, Question count), Nearby Device Discovery / Joining, Battle Lobby, Real-time synchronized 2-player quiz with timer and score calculation, Winner/Result screen, and Post-battle Learning Loop ("Revise Topic" / "Continue Learning").
- Integrate P2P Quiz Battle results into local state and sync queue in `StatefullLearningRepository.kt` to update XP, track battle history, and prevent XP farming while maintaining strict offline operation.
- Update `AppNavigation.kt` and `HomeScreen.kt` to include "Quiz Battle" entry point on Home and Quick Navigation without altering or breaking existing app features.
- Update `AndroidManifest.xml` with required Wi-Fi Direct and location permissions (`NEARBY_WIFI_DEVICES`, `FINE_LOCATION`, `CHANGE_WIFI_STATE`, `ACCESS_WIFI_STATE`, `INTERNET`).

## Capabilities

### New Capabilities
- `p2p-quiz-battle`: Peer-to-peer Wi-Fi Direct device discovery, session state synchronization, offline quiz question package sharing, real-time battle state management, score evaluation, XP rewards, and post-battle educational recommendations.

### Modified Capabilities
None.

## Impact

- `app/src/main/AndroidManifest.xml`: Added Wi-Fi Direct & Location permissions.
- `com.learnquest.mp.p2p.*`: New package for P2P networking and state synchronization.
- `com.learnquest.mp.ui.screens.QuizBattleScreen.kt`: New battle screen UI.
- `com.learnquest.mp.data.repository.StatefullLearningRepository.kt`: Added battle history and XP reward handling.
- `com.learnquest.mp.navigation.AppNavigation.kt` & `HomeScreen.kt`: Added Quiz Battle routes and Home quick action entry point.
