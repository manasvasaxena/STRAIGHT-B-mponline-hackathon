## Purpose

Provides offline-first peer-to-peer 2-player quiz battle capabilities over native Android Wi-Fi Direct without requiring internet access or routers.

## ADDED Requirements

### Requirement: P2P Device Discovery and Connection
The system SHALL discover nearby Android devices participating in LearnQuest MP Quiz Battle over native Wi-Fi Direct and establish a local direct socket connection without internet or external routers.

#### Scenario: Host creates battle and client joins
- **WHEN** Host initiates "Create Battle" and Client initiates "Join Battle" on nearby devices
- **THEN** Host device appears on Client's device list using friendly player profile name/avatar, and establishing connection links both devices in a shared battle lobby.

### Requirement: Battle Lobby and Synchronization
The system SHALL synchronize battle parameters (Subject, Difficulty, Question Count) and player profiles across host and client, waiting for mutual confirmation before starting.

#### Scenario: Battle start synchronization
- **WHEN** Host configures the quiz battle and clicks "Start Battle"
- **THEN** both Host and Client confirm readiness and simultaneously transition to Question 1 of the battle.

### Requirement: Real-time Synchronized Quiz Play and Scoring
The system SHALL present identical offline questions, options, and timer to both players, evaluating answers independently with timeout mechanisms and synchronized question results.

#### Scenario: Synchronized question answer evaluation
- **WHEN** both players submit answers or the timer expires
- **THEN** the system locks input, evaluates answers (10 pts for correct answer + speed bonus), reveals individual correctness without leaking choices prematurely, and advances to the next question.

### Requirement: Winner Determination, XP Rewards, and Learning Loop
The system SHALL declare the winner on a celebratory result screen, award XP using the existing XP reward framework with anti-farming limits, and suggest targeted study topics based on performance.

#### Scenario: Battle completion and reward allocation
- **WHEN** all battle questions are completed
- **THEN** the system displays final scores and XP (+50 XP for Win, +25 XP for Draw, +10 XP for Loss), updates total profile XP locally, queues sync items, and offers "Revise Topic" or "Continue Learning" action buttons.
