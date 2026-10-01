# 🚀 LearnQuest MP (SUTRA)
### *Learning that doesn't wait for network.*

[![MP Board Curriculum](https://img.shields.io/badge/Curriculum-MP%20Board%20%2F%20NCERT-orange.svg)](https://mpbse.nic.in/)
[![Android](https://img.shields.io/badge/Platform-Android%20%7C%20Jetpack%20Compose-green.svg)](https://developer.android.com/)
[![Web Portal](https://img.shields.io/badge/Web%20Portal-HTML5%20%2F%20JS-blue.svg)](#-web-portal--teacher-dashboard)
[![Offline First](https://img.shields.io/badge/Architecture-Offline--First%20%2B%20Wi--Fi%20Direct-brightgreen.svg)](#-key-features--innovations)
[![Languages](https://img.shields.io/badge/Languages-Hindi%20%7C%20Hinglish%20%7C%20English-purple.svg)](#-trilingual--vernacular-support)

---

## 📌 Overview

**LearnQuest MP** (codenamed **SUTRA**) is an **offline-first, AI-powered educational ecosystem** custom-tailored for students in rural, remote, and tribal regions of Madhya Pradesh. 

Designed around intermittent or completely zero connectivity, LearnQuest MP ensures uninterrupted quality education by coupling local-first storage with **P2P Wi-Fi Direct quiz battles**, **curriculum-grounded offline doubt solving**, **verified scholarship matching**, and **local teacher escalation**.

---

## 🌟 Key Features & Innovations

### 📱 1. P2P Offline Quiz Battle (Wi-Fi Direct)
* **Zero Internet Multiplayer**: Engage in live 2-player competitive quizzes with nearby classmates over native Android **Wi-Fi Direct** and direct TCP socket communication—no internet or Wi-Fi router required!
* **Synchronized Gameplay**: Auto host discovery, real-time lobby state, simultaneous question presentation, timed answer submission, and immediate score breakdown.
* **XP & Gamification**: Winner (+50 XP), Draw (+25 XP), and Loss (+10 XP) rewards with anti-farming controls and direct recommendations to study weak topics.

---

### 🛡️ 2. Streak Shield & Offline-First Engine
* **Uninterrupted Learning**: Progress, quiz responses, XP gains, and mastery levels are written instantly to an encrypted local SQLite/Room database.
* **Streak Protection**: Offline activity protects learning streaks even when users remain off the grid for days or weeks.
* **Resumable Downloads & Delta Sync**: Package content level-by-level with versioning and background queueing (`WorkManager`) that syncs deltas seamlessly once network returns.

---

### 👥 3. Multi-Profile Shared Device Support
* Built specifically for households or rural classrooms sharing a single low-end Android phone.
* Multiple students can switch profiles instantly with isolated XP, mastery badges, streak counts, and learning histories.

---

### 🤖 4. Curriculum-Grounded RAG & Offline AI Fallback
* **Online RAG**: Uses Retrieval-Augmented Generation over curated MP Board / NCERT syllabus, textbooks, and notes.
* **Offline AI Solver**: Instant answers powered by cached key concepts, glossaries, local rule matching, and pre-rendered markdown lesson notes.
* **Teacher Escalation Queue**: Low-confidence doubts are automatically queued and escalated to local MP teachers and mentors upon reconnecting.

---

### 🌐 5. Web Portal & Teacher Dashboard
* **Web Portal (`/web`)**: Interactive browser interface for web-based quiz practice, content navigation, and offline status simulation.
* **Teacher Dashboard (`/webdev`)**: Admin & mentor portal to track student mastery, review escalated doubts, manage lesson packages, and publish custom quiz challenges.

---

### 🎓 6. Verified Scholarship & Career Pathway Discovery
* **Curated Opportunities**: Direct filtering of genuine government and institutional scholarships (e.g., *Mukhya Mantri Medhavi Vidyarthi Yojna*, *Post Matric Scholarship for SC/ST/OBC*).
* **Rule-Based Career Guidance**: Matches student interests and academic strengths with relevant vocational and higher education pathways.

---

### 🗣️ 7. Trilingual & Vernacular Support
* Native toggle between **Hindi (हिंदी)**, **Hinglish (हिंग्लिश)**, and **English**.
* Built-in Android Speech-to-Text and Text-to-Speech (TTS) engine integration for voice-assisted offline learning.

---

## 🏗️ Technical Architecture

```text
                                  CLOUD BACKEND / WEB
                   ┌──────────────────────────────────────────────┐
                   │  FastAPI / Python Service                     │
                   │  PostgreSQL Database                         │
                   │  RAG Vector DB (Curated MP Board Syllabus)   │
                   │  Teacher & Mentor Escalation Dashboard       │
                   └──────────────────────┬───────────────────────┘
                                          │
                                Intermittent Network
                                          │
                                 Delta Sync Queue
                                          │
    ┌─────────────────────────────────────┴─────────────────────────────────────┐
    │                              ANDROID CLIENT                               │
    │                      (Kotlin + Jetpack Compose)                           │
    │                                                                           │
    │  ┌────────────────────────┐                   ┌────────────────────────┐  │
    │  │     Local Storage      │                   │   P2P Quiz Engine      │  │
    │  │  • Room / SQLite DB    │                   │  • Wi-Fi Direct        │  │
    │  │  • Student Profiles    │◄─────────────────►│  • TCP Socket Comms    │  │
    │  │  • Offline PKGS Notes  │                   │  • Realtime Lobby      │  │
    │  │  • Sync Queue          │                   │  • Offline Multiplayer │  │
    │  └────────────────────────┘                   └────────────────────────┘  │
    └───────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Domain | Technology / Library |
| :--- | :--- |
| **Android Framework** | Kotlin, Jetpack Compose, Material 3, Coroutines & Flow |
| **Local Persistence** | Room Database, SQLite, DataStore |
| **P2P Connectivity** | Android Wi-Fi Direct (`WifiP2pManager`), Java Socket API, JSON Protocol |
| **Offline Rendering** | Custom Compose Markdown Engine, Embedded MP Board PKGS Bundles |
| **Voice & Speech** | Android Text-To-Speech (TTS Engine), SpeechRecognizer |
| **Web & Teacher Tools** | HTML5, CSS3 (Modern Glassmorphism), JavaScript (ES6+), Python |

---

## 📁 Repository Structure

```text
STRAIGHT-B-mponline-hackathon/
├── app/                        # Android Native App (Kotlin + Compose)
│   └── src/main/java/com/learnquest/mp/
│       ├── data/              # Repositories, Room DB, Models, Connectivity Observers
│       ├── navigation/        # Jetpack Compose Navigation Graph & Top Header
│       ├── p2p/               # Wi-Fi Direct Manager, Socket Comms, Battle Protocol
│       ├── ui/                # Screens (Home, QuizBattle, Doubt, Learn, Explore, Profile)
│       └── markdown/          # Custom Local Markdown Content Renderer
├── PKGS/                       # Bundled MP Board Curriculum Content (Class 6 Math, Science, Hindi, etc.)
├── web/                        # Web Student Portal
├── webdev/                     # Web Teacher / Mentor Dashboard
├── l-spec/                     # L-Spec Change Specifications & Design History
├── tests/                      # Python Automated Test Suite for Features & Quiz Publishing
└── README.md                   # Project Documentation
```

---

## 🎯 Hackathon Demo Flow

1. **Simulate Rural Offline State**: Toggle Airplane Mode on device or enable Offline Mode in app header.
2. **Multi-Profile Switch**: Select or create student profile (*e.g., Rahul - Class 6*).
3. **P2P Quiz Battle**: Open Quiz Battle, pair two phones via Wi-Fi Direct without internet, and play a synchronized live quiz.
4. **Offline Learning & Doubt Solving**: Open Class 6 Science PKGS notes; ask a question to receive instant offline cached answers.
5. **Scholarships & Guidance**: Explore verified MP scholarships filtered by eligibility and view career pathways.
6. **Reconnect & Delta Sync**: Turn off Airplane Mode and trigger Sync Queue to push offline progress, quiz results, and escalated doubts to the backend.

---

> **The internet went away. Learning didn't.**

---

**Made by Straight B**
