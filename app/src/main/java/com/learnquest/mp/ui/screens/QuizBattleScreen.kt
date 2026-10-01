package com.learnquest.mp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.learnquest.mp.data.repository.StatefullLearningRepository
import com.learnquest.mp.model.Language
import com.learnquest.mp.model.QuizQuestion
import com.learnquest.mp.p2p.P2PConnectionManager
import com.learnquest.mp.p2p.WifiDirectManager
import com.learnquest.mp.p2p.models.*
import com.learnquest.mp.ui.appStrings
import com.learnquest.mp.ui.theme.ForestGreen
import com.learnquest.mp.ui.theme.SaffronPrimary
import com.learnquest.mp.ui.theme.WeakRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizBattleScreen(
    repository: StatefullLearningRepository,
    appLanguage: Language,
    onNavigateBack: () -> Unit,
    onNavigateToLearn: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strings = appLanguage.appStrings()

    val wifiDirectManager = remember { WifiDirectManager(context) }
    val connectionManager = remember { P2PConnectionManager() }

    DisposableEffect(Unit) {
        wifiDirectManager.init()
        onDispose {
            wifiDirectManager.close()
            connectionManager.close()
        }
    }

    var hasPermissions by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    val profiles by repository.profilesFlow.collectAsState()
    val activeProfileId by repository.activeProfileIdFlow.collectAsState()
    val activeProfile = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull()

    var battleRole by remember { mutableStateOf<BattleRole?>(null) }
    var battleState by remember { mutableStateOf(BattleState.IDLE) }

    // Host Config
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    var selectedDifficulty by remember { mutableStateOf("Medium") }
    var selectedQuestionCount by remember { mutableStateOf(5) }

    // Players Info
    val hostPlayer = remember(activeProfile) {
        PlayerBattleInfo(
            id = activeProfile?.id ?: "host_1",
            name = activeProfile?.name ?: "Learner 1",
            avatarRes = activeProfile?.avatarRes ?: "student_avatar_1",
            level = 5,
            totalXp = activeProfile?.totalXp ?: 1250
        )
    }

    var opponentPlayer by remember {
        mutableStateOf(
            PlayerBattleInfo(
                id = "op_1",
                name = "Nearby Learner",
                avatarRes = "student_avatar_2",
                level = 4,
                totalXp = 980
            )
        )
    }

    var isPlayerReady by remember { mutableStateOf(false) }
    var isOpponentReady by remember { mutableStateOf(false) }

    // Questions & Quiz Progress
    val allQuestions by repository.quizQuestionsFlow.collectAsState()
    var currentBattleQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var timerSeconds by remember { mutableStateOf(15) }

    var p1SelectedOption by remember { mutableStateOf<Int?>(null) }
    var p2SelectedOption by remember { mutableStateOf<Int?>(null) }

    var p1Score by remember { mutableStateOf(0) }
    var p2Score by remember { mutableStateOf(0) }

    var p1CorrectCount by remember { mutableStateOf(0) }
    var p2CorrectCount by remember { mutableStateOf(0) }

    var questionResultState by remember { mutableStateOf<QuestionResult?>(null) }
    var finalSummary by remember { mutableStateOf<BattleFinalSummary?>(null) }

    val discoveredPeers by wifiDirectManager.discoveredPeers.collectAsState()
    val connectionInfo by wifiDirectManager.connectionInfo.collectAsState()

    // Listen for incoming P2P messages
    val incomingMessage by connectionManager.incomingMessages.collectAsState()

    LaunchedEffect(incomingMessage) {
        val msg = incomingMessage ?: return@LaunchedEffect
        when (msg.type) {
            MessageType.HELLO -> {
                try {
                    val json = JSONObject(msg.payloadJson)
                    opponentPlayer = PlayerBattleInfo(
                        id = json.optString("id", "op_1"),
                        name = json.optString("name", "Nearby Learner"),
                        avatarRes = json.optString("avatarRes", "student_avatar_2"),
                        level = json.optInt("level", 4),
                        totalXp = json.optInt("totalXp", 1000)
                    )
                } catch (_: Exception) {}
            }
            MessageType.READY_TOGGLE -> {
                isOpponentReady = msg.payloadJson.toBooleanStrictOrNull() ?: false
            }
            MessageType.START_BATTLE -> {
                battleState = BattleState.QUESTION
            }
            MessageType.QUESTION_PKG -> {
                try {
                    val array = JSONObject(msg.payloadJson).getJSONArray("questions")
                    val qList = mutableListOf<QuizQuestion>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val opts = mutableListOf<String>()
                        val optsArray = obj.getJSONArray("options")
                        for (j in 0 until optsArray.length()) {
                            opts.add(optsArray.getString(j))
                        }
                        qList.add(
                            QuizQuestion(
                                id = obj.getString("id"),
                                questionText = obj.getString("questionText"),
                                options = opts,
                                correctAnswerIndex = obj.getInt("correctAnswerIndex"),
                                explanation = obj.optString("explanation", "")
                            )
                        )
                    }
                    currentBattleQuestions = qList
                } catch (_: Exception) {}
            }
            MessageType.SUBMIT_ANSWER -> {
                try {
                    val json = JSONObject(msg.payloadJson)
                    val opt = json.getInt("selectedOptionIndex")
                    p2SelectedOption = opt
                } catch (_: Exception) {}
            }
            MessageType.QUESTION_RESULT -> {
                try {
                    val json = JSONObject(msg.payloadJson)
                    val qIndex = json.getInt("questionIndex")
                    val p1Ans = json.getInt("playerAnswerIndex")
                    val p1Corr = json.getBoolean("playerIsCorrect")
                    val p1Pts = json.getInt("playerPointsEarned")
                    val p2Ans = json.getInt("opponentAnswerIndex")
                    val p2Corr = json.getBoolean("opponentIsCorrect")
                    val p2Pts = json.getInt("opponentPointsEarned")

                    questionResultState = QuestionResult(
                        questionIndex = qIndex,
                        playerAnswerIndex = p1Ans,
                        playerIsCorrect = p1Corr,
                        playerPointsEarned = p1Pts,
                        opponentAnswerIndex = p2Ans,
                        opponentIsCorrect = p2Corr,
                        opponentPointsEarned = p2Pts
                    )
                    battleState = BattleState.QUESTION_RESULT
                } catch (_: Exception) {}
            }
            MessageType.NEXT_QUESTION_TRIGGER -> {
                p1SelectedOption = null
                p2SelectedOption = null
                questionResultState = null
                currentQuestionIndex = msg.payloadJson.toIntOrNull() ?: (currentQuestionIndex + 1)
                timerSeconds = 15
                battleState = BattleState.QUESTION
            }
            MessageType.BATTLE_FINISHED -> {
                try {
                    val json = JSONObject(msg.payloadJson)
                    finalSummary = BattleFinalSummary(
                        winnerName = json.getString("winnerName"),
                        player1Score = json.getInt("player1Score"),
                        player1XpEarned = json.getInt("player1XpEarned"),
                        player2Score = json.getInt("player2Score"),
                        player2XpEarned = json.getInt("player2XpEarned"),
                        totalQuestions = json.getInt("totalQuestions"),
                        player1CorrectCount = json.getInt("player1CorrectCount"),
                        isDraw = json.optBoolean("isDraw", false)
                    )
                    battleState = BattleState.FINISHED
                } catch (_: Exception) {}
            }
            else -> {}
        }
    }

    // Host - Socket connection setup once Wi-Fi P2P is established
    LaunchedEffect(connectionInfo) {
        val info = connectionInfo ?: return@LaunchedEffect
        if (info.groupFormed) {
            if (info.isGroupOwner && battleRole == BattleRole.HOST) {
                connectionManager.startHostServer {
                    battleState = BattleState.LOBBY
                    // Send host info
                    val json = JSONObject().apply {
                        put("id", hostPlayer.id)
                        put("name", hostPlayer.name)
                        put("avatarRes", hostPlayer.avatarRes)
                        put("level", hostPlayer.level)
                        put("totalXp", hostPlayer.totalXp)
                    }
                    connectionManager.sendMessage(
                        BattleMessage(
                            battleId = "b1",
                            type = MessageType.HELLO,
                            senderId = hostPlayer.id,
                            payloadJson = json.toString()
                        )
                    )
                }
            } else if (!info.isGroupOwner && battleRole == BattleRole.CLIENT) {
                connectionManager.connectToHost(
                    hostAddress = info.groupOwnerAddress,
                    onConnected = {
                        battleState = BattleState.LOBBY
                        val json = JSONObject().apply {
                            put("id", hostPlayer.id)
                            put("name", hostPlayer.name)
                            put("avatarRes", hostPlayer.avatarRes)
                            put("level", hostPlayer.level)
                            put("totalXp", hostPlayer.totalXp)
                        }
                        connectionManager.sendMessage(
                            BattleMessage(
                                battleId = "b1",
                                type = MessageType.HELLO,
                                senderId = hostPlayer.id,
                                payloadJson = json.toString()
                            )
                        )
                    },
                    onError = {
                        battleState = BattleState.DISCONNECTED
                    }
                )
            }
        }
    }

    // Timer countdown in Question state
    LaunchedEffect(battleState, currentQuestionIndex) {
        if (battleState == BattleState.QUESTION) {
            timerSeconds = 15
            while (timerSeconds > 0 && battleState == BattleState.QUESTION) {
                delay(1000)
                timerSeconds--
            }
            if (battleState == BattleState.QUESTION && timerSeconds <= 0) {
                // Auto lock answers on timeout
                if (battleRole == BattleRole.HOST) {
                    evaluateAndProgressQuestion(
                        currentQuestion = currentBattleQuestions.getOrNull(currentQuestionIndex),
                        p1Ans = p1SelectedOption,
                        p2Ans = p2SelectedOption,
                        timerRemaining = 0,
                        currentIdx = currentQuestionIndex,
                        totalQs = currentBattleQuestions.size,
                        p1Score = p1Score,
                        p2Score = p2Score,
                        p1Correct = p1CorrectCount,
                        p2Correct = p2CorrectCount,
                        hostPlayer = hostPlayer,
                        opponentPlayer = opponentPlayer,
                        repository = repository,
                        selectedSubject = selectedSubject,
                        onResultReady = { res, summary, newP1Score, newP2Score, newP1Corr, newP2Corr ->
                            p1Score = newP1Score
                            p2Score = newP2Score
                            p1CorrectCount = newP1Corr
                            p2CorrectCount = newP2Corr
                            questionResultState = res
                            if (summary != null) {
                                finalSummary = summary
                                battleState = BattleState.FINISHED
                                // Send FINISHED message
                                val json = JSONObject().apply {
                                    put("winnerName", summary.winnerName)
                                    put("player1Score", summary.player1Score)
                                    put("player1XpEarned", summary.player1XpEarned)
                                    put("player2Score", summary.player2Score)
                                    put("player2XpEarned", summary.player2XpEarned)
                                    put("totalQuestions", summary.totalQuestions)
                                    put("player1CorrectCount", summary.player1CorrectCount)
                                    put("isDraw", summary.isDraw)
                                }
                                connectionManager.sendMessage(
                                    BattleMessage(
                                        battleId = "b1",
                                        type = MessageType.BATTLE_FINISHED,
                                        senderId = hostPlayer.id,
                                        payloadJson = json.toString()
                                    )
                                )
                            } else {
                                battleState = BattleState.QUESTION_RESULT
                                // Send QUESTION_RESULT message
                                val json = JSONObject().apply {
                                    put("questionIndex", res.questionIndex)
                                    put("playerAnswerIndex", res.playerAnswerIndex)
                                    put("playerIsCorrect", res.playerIsCorrect)
                                    put("playerPointsEarned", res.playerPointsEarned)
                                    put("opponentAnswerIndex", res.opponentAnswerIndex)
                                    put("opponentIsCorrect", res.opponentIsCorrect)
                                    put("opponentPointsEarned", res.opponentPointsEarned)
                                }
                                connectionManager.sendMessage(
                                    BattleMessage(
                                        battleId = "b1",
                                        type = MessageType.QUESTION_RESULT,
                                        senderId = hostPlayer.id,
                                        payloadJson = json.toString()
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    BackHandler {
        if (battleState != BattleState.IDLE) {
            wifiDirectManager.disconnect()
            connectionManager.disconnect()
            battleState = BattleState.IDLE
            battleRole = null
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚔️ ", fontSize = 20.sp)
                        Text(
                            strings.t("P2P Quiz Battle", "पी2पी क्विज़ बैटल", "P2P Quiz Battle"),
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (battleState != BattleState.IDLE) {
                            wifiDirectManager.disconnect()
                            connectionManager.disconnect()
                            battleState = BattleState.IDLE
                            battleRole = null
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (battleState) {
                BattleState.IDLE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SaffronPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Text(
                            strings.t("Offline Peer-to-Peer Quiz Challenge", "ऑफलाइन पी2पी क्विज़ चुनौती", "Offline P2P Quiz Challenge"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            strings.t(
                                "Connect with a nearby friend using Wi-Fi Direct without internet or a router. Compete in real-time educational quizzes and earn XP!",
                                "बिना इंटरनेट या राउटर के पास के दोस्त से वाई-फाई डायरेक्ट द्वारा जुड़ें। वास्तविक समय में क्विज़ खेलें और XP अर्जित करें!",
                                "Bina internet ya router ke nearby friend se connect karein. Real-time quiz khele aur XP earn karein!"
                            ),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                battleRole = BattleRole.HOST
                                battleState = BattleState.SEARCHING
                                wifiDirectManager.startDiscovery()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                strings.t("CREATE BATTLE (HOST)", "बैटल बनाएं (होस्ट)", "CREATE BATTLE (HOST)"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                battleRole = BattleRole.CLIENT
                                battleState = BattleState.SEARCHING
                                wifiDirectManager.startDiscovery()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                strings.t("JOIN BATTLE (NEARBY)", "बैटल में शामिल हों", "JOIN BATTLE (NEARBY)"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                BattleState.SEARCHING -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (battleRole == BattleRole.HOST) {
                            Text(
                                strings.t("Configure Battle Settings", "बैटल सेटिंग्स चुनें", "Configure Battle Settings"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Subject Selection
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        strings.t("Select Subject", "विषय चुनें", "Select Subject"),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Mathematics", "Science", "Computer", "English").forEach { sub ->
                                            FilterChip(
                                                selected = selectedSubject == sub,
                                                onClick = { selectedSubject = sub },
                                                label = { Text(sub) }
                                            )
                                        }
                                    }
                                }
                            }

                            // Difficulty & Questions
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        strings.t("Select Difficulty", "कठिनाई चुनें", "Select Difficulty"),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Easy", "Medium", "Hard").forEach { diff ->
                                            FilterChip(
                                                selected = selectedDifficulty == diff,
                                                onClick = { selectedDifficulty = diff },
                                                label = { Text(diff) }
                                            )
                                        }
                                    }
                                }
                            }

                            CircularProgressIndicator(color = SaffronPrimary, modifier = Modifier.padding(top = 12.dp))
                            Text(
                                strings.t("Waiting for nearby player to connect...", "पास के खिलाड़ी के जुड़ने का इंतज़ार है...", "Nearby player ke connect hone ka wait..."),
                                color = Color.Gray,
                                fontSize = 13.sp
                            )

                        } else {
                            Text(
                                strings.t("Searching for Nearby Players", "पास के खिलाड़ियों को खोजा जा रहा है", "Searching Nearby Players"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SaffronPrimary)

                            Text(
                                strings.t("Available Devices Nearby:", "पास के उपलब्ध उपकरण:", "Available Devices Nearby:"),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            if (discoveredPeers.isEmpty()) {
                                Text(
                                    strings.t("Scanning for LearnQuest MP devices...", "डिवाइस स्कैन हो रहे हैं...", "Scanning for devices..."),
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 20.dp)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(discoveredPeers) { peer ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    wifiDirectManager.connectToPeer(
                                                        deviceAddress = peer.deviceAddress,
                                                        onSuccess = {
                                                            battleState = BattleState.CONNECTING
                                                        },
                                                        onFailure = {}
                                                    )
                                                },
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = SaffronPrimary.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Person, contentDescription = null, tint = SaffronPrimary)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(peer.name, fontWeight = FontWeight.Bold)
                                                    Text("Wi-Fi Direct • ${peer.deviceAddress}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Button(
                                                    onClick = {
                                                        wifiDirectManager.connectToPeer(
                                                            deviceAddress = peer.deviceAddress,
                                                            onSuccess = { battleState = BattleState.CONNECTING },
                                                            onFailure = {}
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                                                ) {
                                                    Text(strings.t("Connect", "जोड़ें", "Connect"))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                wifiDirectManager.disconnect()
                                battleState = BattleState.IDLE
                                battleRole = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text(strings.t("Cancel", "रद्द करें", "Cancel"))
                        }
                    }
                }

                BattleState.CONNECTING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        CircularProgressIndicator(color = SaffronPrimary)
                        Text(
                            strings.t("Establishing Peer-to-Peer Connection...", "पी2पी कनेक्शन स्थापित हो रहा है...", "Establishing P2P Connection..."),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                BattleState.LOBBY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            strings.t("BATTLE LOBBY", "बैटल लॉबी", "BATTLE LOBBY"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SaffronPrimary
                        )

                        // VS Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Player 1
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SaffronPrimary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(54.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(32.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(hostPlayer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Lvl ${hostPlayer.level} • ${hostPlayer.totalXp} XP", fontSize = 11.sp, color = Color.Gray)
                                    if (isPlayerReady) {
                                        Text(strings.t("✓ READY", "✓ तैयार", "✓ READY"), color = ForestGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                Text("VS", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = WeakRed)

                                // Player 2
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                        modifier = Modifier.size(54.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(32.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(opponentPlayer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Lvl ${opponentPlayer.level} • ${opponentPlayer.totalXp} XP", fontSize = 11.sp, color = Color.Gray)
                                    if (isOpponentReady) {
                                        Text(strings.t("✓ READY", "✓ तैयार", "✓ READY"), color = ForestGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Config Details
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("• Subject: $selectedSubject", fontWeight = FontWeight.SemiBold)
                                Text("• Difficulty: $selectedDifficulty", fontWeight = FontWeight.SemiBold)
                                Text("• Questions: $selectedQuestionCount", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (battleRole == BattleRole.HOST) {
                            Button(
                                onClick = {
                                    isPlayerReady = true
                                    // Generate question package
                                    currentBattleQuestions = allQuestions.take(selectedQuestionCount)
                                    val qPkgJson = JSONObject().apply {
                                        val array = org.json.JSONArray()
                                        currentBattleQuestions.forEach { q ->
                                            val qObj = JSONObject().apply {
                                                put("id", q.id)
                                                put("questionText", q.questionText)
                                                put("correctAnswerIndex", q.correctAnswerIndex)
                                                put("explanation", q.explanation)
                                                val optsArray = org.json.JSONArray()
                                                q.options.forEach { optsArray.put(it) }
                                                put("options", optsArray)
                                            }
                                            array.put(qObj)
                                        }
                                        put("questions", array)
                                    }
                                    // Send question package to client
                                    connectionManager.sendMessage(
                                        BattleMessage(
                                            battleId = "b1",
                                            type = MessageType.QUESTION_PKG,
                                            senderId = hostPlayer.id,
                                            payloadJson = qPkgJson.toString()
                                        )
                                    )
                                    // Trigger start
                                    connectionManager.sendMessage(
                                        BattleMessage(
                                            battleId = "b1",
                                            type = MessageType.START_BATTLE,
                                            senderId = hostPlayer.id,
                                            payloadJson = "true"
                                        )
                                    )
                                    battleState = BattleState.QUESTION
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(strings.t("START BATTLE", "बैटल शुरू करें", "START BATTLE"), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                strings.t("Waiting for host to start the battle...", "होस्ट द्वारा बैटल शुरू करने का इंतज़ार है...", "Waiting for host to start battle..."),
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                BattleState.QUESTION -> {
                    val currentQ = currentBattleQuestions.getOrNull(currentQuestionIndex)
                    if (currentQ != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header: Question index + Timer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    strings.t("Question ${currentQuestionIndex + 1} / ${currentBattleQuestions.size}", "प्रश्न ${currentQuestionIndex + 1} / ${currentBattleQuestions.size}", "Question ${currentQuestionIndex + 1} / ${currentBattleQuestions.size}"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = if (timerSeconds <= 5) WeakRed else SaffronPrimary,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$timerSeconds", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }

                            // Question Text
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    currentQ.questionText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            // Options
                            currentQ.options.forEachIndexed { idx, optText ->
                                val isSelected = p1SelectedOption == idx
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = p1SelectedOption == null) {
                                            p1SelectedOption = idx
                                            val json = JSONObject().apply {
                                                put("questionIndex", currentQuestionIndex)
                                                put("selectedOptionIndex", idx)
                                                put("secondsTaken", 15 - timerSeconds)
                                            }
                                            connectionManager.sendMessage(
                                                BattleMessage(
                                                    battleId = "b1",
                                                    type = MessageType.SUBMIT_ANSWER,
                                                    senderId = hostPlayer.id,
                                                    payloadJson = json.toString()
                                                )
                                            )
                                            // Check if both answered in host mode
                                            if (battleRole == BattleRole.HOST) {
                                                evaluateAndProgressQuestion(
                                                    currentQuestion = currentQ,
                                                    p1Ans = idx,
                                                    p2Ans = p2SelectedOption,
                                                    timerRemaining = timerSeconds,
                                                    currentIdx = currentQuestionIndex,
                                                    totalQs = currentBattleQuestions.size,
                                                    p1Score = p1Score,
                                                    p2Score = p2Score,
                                                    p1Correct = p1CorrectCount,
                                                    p2Correct = p2CorrectCount,
                                                    hostPlayer = hostPlayer,
                                                    opponentPlayer = opponentPlayer,
                                                    repository = repository,
                                                    selectedSubject = selectedSubject,
                                                    onResultReady = { res, summary, newP1Score, newP2Score, newP1Corr, newP2Corr ->
                                                        p1Score = newP1Score
                                                        p2Score = newP2Score
                                                        p1CorrectCount = newP1Corr
                                                        p2CorrectCount = newP2Corr
                                                        questionResultState = res
                                                        if (summary != null) {
                                                            finalSummary = summary
                                                            battleState = BattleState.FINISHED
                                                            val finishedJson = JSONObject().apply {
                                                                put("winnerName", summary.winnerName)
                                                                put("player1Score", summary.player1Score)
                                                                put("player1XpEarned", summary.player1XpEarned)
                                                                put("player2Score", summary.player2Score)
                                                                put("player2XpEarned", summary.player2XpEarned)
                                                                put("totalQuestions", summary.totalQuestions)
                                                                put("player1CorrectCount", summary.player1CorrectCount)
                                                                put("isDraw", summary.isDraw)
                                                            }
                                                            connectionManager.sendMessage(
                                                                BattleMessage(
                                                                    battleId = "b1",
                                                                    type = MessageType.BATTLE_FINISHED,
                                                                    senderId = hostPlayer.id,
                                                                    payloadJson = finishedJson.toString()
                                                                )
                                                            )
                                                        } else {
                                                            battleState = BattleState.QUESTION_RESULT
                                                            val resultJson = JSONObject().apply {
                                                                put("questionIndex", res.questionIndex)
                                                                put("playerAnswerIndex", res.playerAnswerIndex)
                                                                put("playerIsCorrect", res.playerIsCorrect)
                                                                put("playerPointsEarned", res.playerPointsEarned)
                                                                put("opponentAnswerIndex", res.opponentAnswerIndex)
                                                                put("opponentIsCorrect", res.opponentIsCorrect)
                                                                put("opponentPointsEarned", res.opponentPointsEarned)
                                                            }
                                                            connectionManager.sendMessage(
                                                                BattleMessage(
                                                                    battleId = "b1",
                                                                    type = MessageType.QUESTION_RESULT,
                                                                    senderId = hostPlayer.id,
                                                                    payloadJson = resultJson.toString()
                                                                )
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) SaffronPrimary.copy(alpha = 0.2f) else Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, SaffronPrimary) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(optText, fontSize = 15.sp)
                                    }
                                }
                            }

                            if (p1SelectedOption != null) {
                                Text(
                                    strings.t("Answer submitted! Waiting for opponent / timer...", "उत्तर भेजा गया! प्रतिद्वंद्वी / टाइमर का इंतज़ार...", "Answer submitted! Waiting for opponent..."),
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                BattleState.QUESTION_RESULT -> {
                    val res = questionResultState
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            strings.t("QUESTION RESULT", "प्रश्न परिणाम", "QUESTION RESULT"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "You: ${if (res?.playerIsCorrect == true) "✓ Correct (+${res.playerPointsEarned})" else "✗ Incorrect"}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (res?.playerIsCorrect == true) ForestGreen else WeakRed
                                    )
                                    Text(
                                        "Opponent: ${if (res?.opponentIsCorrect == true) "✓ Correct (+${res.opponentPointsEarned})" else "✗ Incorrect"}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (res?.opponentIsCorrect == true) ForestGreen else WeakRed
                                    )
                                }

                                Divider()

                                Text("Current Score:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("You: $p1Score points | Opponent: $p2Score points", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        if (battleRole == BattleRole.HOST) {
                            Button(
                                onClick = {
                                    val nextIdx = currentQuestionIndex + 1
                                    p1SelectedOption = null
                                    p2SelectedOption = null
                                    questionResultState = null
                                    currentQuestionIndex = nextIdx
                                    battleState = BattleState.QUESTION
                                    connectionManager.sendMessage(
                                        BattleMessage(
                                            battleId = "b1",
                                            type = MessageType.NEXT_QUESTION_TRIGGER,
                                            senderId = hostPlayer.id,
                                            payloadJson = nextIdx.toString()
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                            ) {
                                Text(strings.t("NEXT QUESTION", "अगला प्रश्न", "NEXT QUESTION"))
                            }
                        } else {
                            Text(
                                strings.t("Waiting for host to load next question...", "होस्ट का इंतज़ार...", "Waiting for host..."),
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                BattleState.FINISHED -> {
                    val summary = finalSummary
                    if (summary != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🏆 ${strings.t("QUIZ BATTLE COMPLETE", "क्विज़ बैटल पूर्ण", "QUIZ BATTLE COMPLETE")}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SaffronPrimary
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        if (summary.isDraw) "ITS A DRAW!" else "WINNER: ${summary.winnerName.uppercase()}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )

                                    Divider()

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(hostPlayer.name, fontWeight = FontWeight.Bold)
                                            Text("${summary.player1Score} Points", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                            Text("+${summary.player1XpEarned} XP", color = SaffronPrimary, fontWeight = FontWeight.Bold)
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(opponentPlayer.name, fontWeight = FontWeight.Bold)
                                            Text("${summary.player2Score} Points", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                            Text("+${summary.player2XpEarned} XP", color = SaffronPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Educational Recommendation
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        strings.t("Educational Growth Loop", "शैक्षणिक प्रगति", "Educational Growth Loop"),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        strings.t("Want to revise $selectedSubject topics to boost your score in the next battle?", "$selectedSubject विषय का दोहराव करें और अगली बैटल में बेहतर प्रदर्शन करें!", "Revise $selectedSubject topics to improve your next battle!"),
                                        fontSize = 13.sp,
                                        color = Color(0xFF1D4ED8)
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateToLearn() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                            ) {
                                Text(strings.t("REVISE TOPIC", "विषय दोहराएं", "REVISE TOPIC"))
                            }

                            OutlinedButton(
                                onClick = {
                                    wifiDirectManager.disconnect()
                                    connectionManager.disconnect()
                                    battleState = BattleState.IDLE
                                    battleRole = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(strings.t("BACK TO QUIZ BATTLE HOME", "मुख्य पृष्ठ पर वापस", "BACK TO QUIZ BATTLE HOME"))
                            }
                        }
                    }
                }

                BattleState.DISCONNECTED -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = WeakRed, modifier = Modifier.size(54.dp))
                        Text(
                            strings.t("Connection Interrupted", "कनेक्शन टूट गया", "Connection Interrupted"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WeakRed
                        )
                        Button(
                            onClick = {
                                wifiDirectManager.disconnect()
                                connectionManager.disconnect()
                                battleState = BattleState.IDLE
                                battleRole = null
                            }
                        ) {
                            Text(strings.t("Return", "वापस जाएँ", "Return"))
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

private fun evaluateAndProgressQuestion(
    currentQuestion: QuizQuestion?,
    p1Ans: Int?,
    p2Ans: Int?,
    timerRemaining: Int,
    currentIdx: Int,
    totalQs: Int,
    p1Score: Int,
    p2Score: Int,
    p1Correct: Int,
    p2Correct: Int,
    hostPlayer: PlayerBattleInfo,
    opponentPlayer: PlayerBattleInfo,
    repository: StatefullLearningRepository,
    selectedSubject: String,
    onResultReady: (
        res: QuestionResult,
        summary: BattleFinalSummary?,
        newP1Score: Int,
        newP2Score: Int,
        newP1Corr: Int,
        newP2Corr: Int
    ) -> Unit
) {
    val q = currentQuestion ?: return
    val p1IsCorr = (p1Ans == q.correctAnswerIndex)
    val p2IsCorr = (p2Ans == q.correctAnswerIndex)

    val p1Pts = if (p1IsCorr) 10 + (timerRemaining / 3) else 0
    val p2Pts = if (p2IsCorr) 10 + (timerRemaining / 3) else 0

    val newP1Score = p1Score + p1Pts
    val newP2Score = p2Score + p2Pts
    val newP1Corr = p1Correct + if (p1IsCorr) 1 else 0
    val newP2Corr = p2Correct + if (p2IsCorr) 1 else 0

    val res = QuestionResult(
        questionIndex = currentIdx,
        playerAnswerIndex = p1Ans ?: -1,
        playerIsCorrect = p1IsCorr,
        playerPointsEarned = p1Pts,
        opponentAnswerIndex = p2Ans ?: -1,
        opponentIsCorrect = p2IsCorr,
        opponentPointsEarned = p2Pts
    )

    if (currentIdx >= totalQs - 1) {
        val isDraw = (newP1Score == newP2Score)
        val winnerName = when {
            newP1Score > newP2Score -> hostPlayer.name
            newP2Score > newP1Score -> opponentPlayer.name
            else -> "Draw"
        }
        val p1Xp = when {
            newP1Score > newP2Score -> 50
            isDraw -> 25
            else -> 10
        }
        val p2Xp = when {
            newP2Score > newP1Score -> 50
            isDraw -> 25
            else -> 10
        }

        val summary = BattleFinalSummary(
            winnerName = winnerName,
            player1Score = newP1Score,
            player1XpEarned = p1Xp,
            player2Score = newP2Score,
            player2XpEarned = p2Xp,
            totalQuestions = totalQs,
            player1CorrectCount = newP1Corr,
            isDraw = isDraw
        )

        repository.recordBattleCompletion(
            winnerName = winnerName,
            player1Score = newP1Score,
            player1Xp = p1Xp,
            player2Score = newP2Score,
            player2Xp = p2Xp,
            subject = selectedSubject
        )

        onResultReady(res, summary, newP1Score, newP2Score, newP1Corr, newP2Corr)
    } else {
        onResultReady(res, null, newP1Score, newP2Score, newP1Corr, newP2Corr)
    }
}
