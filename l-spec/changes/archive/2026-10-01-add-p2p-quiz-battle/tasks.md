## 1. Permissions & Data Models

- [x] 1.1 Update `AndroidManifest.xml` with Wi-Fi Direct and Location permissions (`NEARBY_WIFI_DEVICES`, `FINE_LOCATION`, `ACCESS_FINE_LOCATION`, `CHANGE_WIFI_STATE`, `ACCESS_WIFI_STATE`, `INTERNET`) and verify manifest structure.
- [x] 1.2 Define P2P message models, battle session state, and player battle profile models in `com.learnquest.mp.p2p.models` and verify compilation.

## 2. Wi-Fi Direct & Network Engine

- [x] 2.1 Implement `WifiDirectManager` in `com.learnquest.mp.p2p` to handle peer discovery, connection requests, and Wi-Fi Direct p2p state receivers, and verify Wi-Fi P2p listener logic.
- [x] 2.2 Implement `P2PConnectionManager` and Socket Host/Client sessions (`BattleHostSession`, `BattleClientSession`) to send and receive structured JSON messages over direct sockets without internet.

## 3. Battle State & Repository Integration

- [x] 3.1 Update `StatefullLearningRepository.kt` to generate subject question packages, process battle completion XP rewards (+50/25/10), and store battle history in sync queue.

## 4. UI Implementation & Navigation Integration

- [x] 4.1 Create `QuizBattleScreen.kt` featuring Create/Join flow, Nearby Device discovery list, Battle Lobby, Real-time Quiz countdown, Score evaluation, Winner screen, and Post-battle recommendations.
- [x] 4.2 Add "Quiz Battle" navigation route in `AppNavigation.kt` and entry point on `HomeScreen.kt` Quick Actions Hub, and verify navigation works seamlessly.

## 5. Verification & Testing

- [x] 5.1 Run project Gradle build / check and verify zero regressions on existing LearnQuest MP functionality.
