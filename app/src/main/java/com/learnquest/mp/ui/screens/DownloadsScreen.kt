package com.learnquest.mp.ui.screens

import android.os.Environment
import android.speech.tts.TextToSpeech
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.learnquest.mp.model.Language
import com.learnquest.mp.markdown.MarkdownRenderer
import com.learnquest.mp.ui.theme.SaffronPrimary
import com.learnquest.mp.ui.appStrings
import java.io.File
import java.util.Locale

data class DownloadedFileItem(
    val file: File,
    val name: String,
    val sizeText: String,
    val lastModifiedText: String
)

fun renderMarkdownToMoraHtml(markdown: String, isHindi: Boolean): String {
    return MarkdownRenderer.render(markdown, isHindi)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownReaderScreen(
    title: String,
    markdown: String,
    isHindi: Boolean,
    onBack: () -> Unit,
    appLanguage: Language = if (isHindi) Language.HINDI else Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
    val htmlContent = remember(markdown, isHindi) {
        renderMarkdownToMoraHtml(markdown, isHindi)
    }
    val context = LocalContext.current
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var showLessonChatbot by remember(title) { mutableStateOf(false) }
    var chatbotQuestion by remember(title) { mutableStateOf("") }
    var chatbotAnswer by remember(title) { mutableStateOf<String?>(null) }
    val isScienceChapterOne = isScienceChapterOneFile(title)

    DisposableEffect(context, markdown, isHindi) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine = tts
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
            ttsEngine = null
            isSpeaking = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.t("Back", "वापस", "Back"))
                    }
                },
                actions = {
                    IconButton(onClick = { showLessonChatbot = true }) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = if (isScienceChapterOne) strings.t("Ask about this lesson", "इस पाठ के बारे में पूछें", "Is lesson ke बारे में पूछें") else strings.t("Lesson chatbot coming soon", "पाठ चैटबॉट जल्द उपलब्ध होगा", "Lesson chatbot soon"),
                            tint = SaffronPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                ttsEngine?.stop()
                                isSpeaking = false
                            } else {
                                val textToRead = markdown
                                    .replace(Regex("[#*_`>-]"), " ")
                                    .replace(Regex("\\s+"), " ")
                                    .trim()
                                if (textToRead.isBlank()) {
                                    Toast.makeText(context, strings.t("No readable text in this file", "इस फ़ाइल में पढ़ने योग्य टेक्स्ट नहीं है", "Is file mein readable text nahi hai"), Toast.LENGTH_SHORT).show()
                                } else if (ttsEngine == null) {
                                    Toast.makeText(context, strings.t("Text-to-speech is initializing", "टेक्स्ट-टू-स्पीच शुरू हो रहा है", "Text-to-speech start ho raha hai"), Toast.LENGTH_SHORT).show()
                                } else {
                                    val engine = ttsEngine!!
                                    val preferredLocale = if (isHindi) Locale("hi", "IN") else Locale.US
                                    val languageStatus = engine.setLanguage(preferredLocale)
                                    if (languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                                        languageStatus == TextToSpeech.LANG_NOT_SUPPORTED
                                    ) {
                                        engine.setLanguage(Locale.US)
                                    }
                                    engine.setSpeechRate(0.95f)

                                    val chunks = splitTextForSpeech(textToRead)
                                    val didSpeak = chunks.mapIndexed { index, chunk ->
                                        val queueMode = if (index == 0) {
                                            TextToSpeech.QUEUE_FLUSH
                                        } else {
                                            TextToSpeech.QUEUE_ADD
                                        }
                                        engine.speak(chunk, queueMode, null, "MARKDOWN_TTS_ID_$index") == TextToSpeech.SUCCESS
                                    }.any { it }

                                    if (didSpeak) {
                                        isSpeaking = true
                                    } else {
                                        Toast.makeText(context, strings.t("Text-to-speech could not read this file", "टेक्स्ट-टू-स्पीच इस फ़ाइल को नहीं पढ़ सका", "Text-to-speech file read nahi kar saka"), Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) strings.t("Stop text to speech", "टेक्स्ट-टू-स्पीच रोकें", "TTS रोकें") else strings.t("Read aloud", "जोर से पढ़ें", "Read aloud"),
                            tint = if (isSpeaking) SaffronPrimary else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    settings.defaultTextEncodingName = "utf-8"
                    loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }

    if (showLessonChatbot) {
        AlertDialog(
            onDismissRequest = { showLessonChatbot = false },
            title = { Text(strings.t("Lesson chatbot", "पाठ चैटबॉट", "Lesson chatbot")) },
            text = {
                if (!isScienceChapterOne) {
                    Text(strings.t("Chatbot questions for this lesson are coming soon.", "इस पाठ के चैटबॉट प्रश्न जल्द उपलब्ध होंगे।", "Is lesson ke chatbot questions soon available honge."))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            strings.t("Ask only about Class 6 Science Chapter 1. Answers come from this lesson file.", "केवल कक्षा 6 विज्ञान अध्याय 1 के बारे में पूछें। उत्तर इसी पाठ से हैं।", "Sirf Class 6 Science Chapter 1 ke बारे में पूछें. Answers isi lesson file se hain."),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = chatbotQuestion,
                            onValueChange = { chatbotQuestion = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(strings.t("Ask a question", "प्रश्न पूछें", "Question पूछें")) },
                            maxLines = 3
                        )
                        Button(
                            onClick = {
                                chatbotAnswer = answerScienceChapterOneQuestion(chatbotQuestion)
                            },
                            enabled = chatbotQuestion.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.t("Ask chatbot", "चैटबॉट से पूछें", "Chatbot से पूछें"))
                        }
                        chatbotAnswer?.let { answer ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(answer, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                            }
                        }
                        Text(strings.t("Suggested questions", "सुझाए गए प्रश्न", "Suggested questions"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(scienceChapterOneFaqs) { faq ->
                                TextButton(
                                    onClick = {
                                        chatbotQuestion = faq.question
                                        chatbotAnswer = faq.answer
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(faq.question, modifier = Modifier.fillMaxWidth(), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLessonChatbot = false }) {
                    Text(strings.t("Close", "बंद करें", "Close करें"))
                }
            }
        )
    }
}

private fun splitTextForSpeech(text: String, maxChunkLength: Int = 3500): List<String> {
    val chunks = mutableListOf<String>()
    val current = StringBuilder()

    text.split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .forEach { word ->
            if (current.isNotEmpty() && current.length + word.length + 1 > maxChunkLength) {
                chunks += current.toString()
                current.clear()
            }
            if (current.isNotEmpty()) current.append(' ')
            current.append(word)
        }

    if (current.isNotEmpty()) chunks += current.toString()
    return chunks
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    appLanguage: Language = Language.HINDI,
    onBack: () -> Unit = {}
) {
    val strings = appLanguage.appStrings()
    val isHindi = appLanguage == Language.HINDI
    fun localized(hindi: String, english: String): String = strings.t(english, hindi, "$english / $hindi")
    val context = LocalContext.current

    var selectedFileItem by remember { mutableStateOf<DownloadedFileItem?>(null) }
    var noteContent by remember { mutableStateOf("") }
    var refreshKey by remember { mutableStateOf(0) }

    var fileToDelete by remember { mutableStateOf<DownloadedFileItem?>(null) }

    val downloadedFiles = remember(refreshKey) {
        val list = mutableListOf<DownloadedFileItem>()
        try {
            val directories = listOfNotNull(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                File(context.filesDir, "downloads"),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ).distinctBy { it.absolutePath }
            directories.forEach { downloadsDir ->
                if (downloadsDir.exists() && downloadsDir.isDirectory) {
                    downloadsDir.listFiles()
                        ?.filter { it.extension.lowercase() in listOf("md", "txt") }
                        ?.forEach { f ->
                            val kb = f.length() / 1024.0
                            val sizeStr = if (kb < 1024) String.format("%.1f KB", kb) else String.format("%.2f MB", kb / 1024.0)
                            list.add(
                                DownloadedFileItem(
                                    file = f,
                                    name = f.name,
                                    sizeText = sizeStr,
                                    lastModifiedText = "Downloaded"
                                )
                            )
                        }
                }
            }
        } catch (_: Exception) { }
        list.distinctBy { it.file.absolutePath }
    }

    if (selectedFileItem == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(localized("📥 डाउनलोड की गई सामग्री", "📥 Downloaded Material")) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = localized("वापस", "Back"))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    text = localized("आपके डिवाइस में सहेजे गए ऑफ़लाइन स्टडी नोट्स:", "Offline study notes saved on your device:"),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (downloadedFiles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = localized("कोई डाउनलोड फ़ाइल उपलब्ध नहीं है।", "No downloaded files found."),
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(downloadedFiles) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFileItem = item
                                        noteContent = try {
                                            if (item.file.exists()) item.file.readText() else ""
                                        } catch (_: Exception) {
                                            ""
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${item.sizeText} • ${item.lastModifiedText}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.DownloadDone,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { fileToDelete = item }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = localized("फ़ाइल हटाएं", "Delete File"),
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        val selectedFile = selectedFileItem!!
        BackHandler { selectedFileItem = null }
        MarkdownReaderScreen(
            title = selectedFile.name,
            markdown = noteContent,
            isHindi = isHindi,
            onBack = { selectedFileItem = null },
            appLanguage = appLanguage
        )
    }

    if (fileToDelete != null) {
        val target = fileToDelete!!
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = {
                Text(
                    text = localized("फ़ाइल हटाएँ?", "Delete File?"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = localized("क्या आप निश्चित रूप से '${target.name}' को हटाना चाहते हैं?", "Are you sure you want to delete '${target.name}'?"),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            if (target.file.exists()) {
                                target.file.delete()
                            }
                            refreshKey++
                            Toast.makeText(
                                context,
                                localized("'${target.name}' हटा दी गई।", "Deleted '${target.name}'."),
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, strings.t("Error: ${e.localizedMessage}", "त्रुटि: ${e.localizedMessage}", "Error: ${e.localizedMessage}"), Toast.LENGTH_SHORT).show()
                        }
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(localized("हटाएँ", "Delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text(localized("रद्द करें", "Cancel"))
                }
            }
        )
    }
}

private data class LessonChatbotFaq(
    val question: String,
    val answer: String,
    val keywords: Set<String>
)

private val scienceChapterOneFaqs = listOf(
    LessonChatbotFaq(
        question = "What is science?",
        answer = "Science is our natural desire to explore, observe, and ask questions about everything around us.",
        keywords = setOf("science", "meaning", "define")
    ),
    LessonChatbotFaq(
        question = "What are the steps of the scientific method?",
        answer = "The steps are: observe, ask a question, make a hypothesis, test it with an experiment, and analyze the result to reach a conclusion.",
        keywords = setOf("scientific method", "steps", "observe", "hypothesis", "experiment", "conclusion")
    ),
    LessonChatbotFaq(
        question = "Why is collaboration important in science?",
        answer = "Collaboration lets scientists discuss observations, share experiments, cross-check data, reduce personal bias, and understand problems more clearly.",
        keywords = setOf("collaboration", "teamwork", "scientists", "bias", "data")
    ),
    LessonChatbotFaq(
        question = "What is roughage and why is it needed?",
        answer = "Roughage, or dietary fibre, adds bulk to undigested food, helps bowel movements, and prevents constipation even though it does not supply energy.",
        keywords = setOf("roughage", "fibre", "fiber", "constipation", "digestion")
    ),
    LessonChatbotFaq(
        question = "How is starch tested in food?",
        answer = "Add two or three drops of dilute iodine solution. If starch is present, the food changes to a blue-black colour.",
        keywords = setOf("starch", "iodine", "blue-black", "food test")
    ),
    LessonChatbotFaq(
        question = "Which chemicals test for protein?",
        answer = "The protein test uses copper sulphate solution followed by caustic soda solution. A violet or purple colour indicates protein.",
        keywords = setOf("protein", "copper sulphate", "copper", "caustic soda", "violet", "purple")
    )
)

private fun isScienceChapterOneFile(title: String): Boolean {
    val normalized = title.lowercase().replace('-', '_').replace(' ', '_')
    return normalized.contains("science") && normalized.contains("chapter_1")
}

private fun answerScienceChapterOneQuestion(question: String): String {
    val normalized = question.lowercase()
    val bestMatch = scienceChapterOneFaqs
        .map { faq -> faq to faq.keywords.count { keyword -> normalized.contains(keyword) } }
        .maxByOrNull { (_, score) -> score }
        ?.takeIf { (_, score) -> score > 0 }
        ?.first
    return bestMatch?.answer
        ?: "I can only answer questions covered by this Class 6 Science Chapter 1 file. Try one of the suggested questions below."
}
