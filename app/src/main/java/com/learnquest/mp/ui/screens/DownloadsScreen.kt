package com.learnquest.mp.ui.screens

import android.os.Environment
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DownloadDone
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
import com.learnquest.mp.ui.theme.SaffronPrimary
import java.io.File

data class DownloadedFileItem(
    val file: File,
    val name: String,
    val sizeText: String,
    val lastModifiedText: String
)

fun renderMarkdownToMoraHtml(markdown: String, isHindi: Boolean): String {
    var formatted = markdown
        .replace(Regex("(?m)^### (.*)$"), "<h3>$1</h3>")
        .replace(Regex("(?m)^## (.*)$"), "<h2>$1</h2>")
        .replace(Regex("(?m)^# (.*)$"), "<h1>$1</h1>")
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
        .replace(Regex("(?m)^- (.*)$"), "<li>$1</li>")
        .replace("\n\n", "</p><p>")

    return """
        <!doctype html>
        <html>
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
          <style>
            :root { color-scheme: light; }
            * { box-sizing: border-box; }
            html, body { margin: 0; padding: 0; background: #FFFFFF; }
            body {
              color: #1E293B;
              font-family: system-ui, -apple-system, sans-serif;
              font-size: 15px;
              line-height: 1.65;
              padding: 16px;
              -webkit-font-smoothing: antialiased;
            }
            #write { max-width: 720px; margin: 0 auto; }
            h1, h2, h3, h4 {
              color: #0F172A;
              line-height: 1.3;
              font-weight: 700;
            }
            h1 { font-size: 1.5em; border-bottom: 2px solid #E2E8F0; padding-bottom: 6px; margin-top: 0.2em; color: #D97706; }
            h2 { font-size: 1.25em; color: #1E293B; margin-top: 1.2em; border-bottom: 1px solid #F1F5F9; }
            h3 { font-size: 1.08em; color: #334155; margin-top: 1em; }
            p { margin: 0 0 0.8em; }
            strong { font-weight: 700; color: #0F172A; }
            li { margin: 0.3em 0; }
            code {
              font-family: monospace;
              background: #F1F5F9;
              border-radius: 4px;
              padding: 2px 6px;
              font-size: 0.9em;
            }
            blockquote {
              margin: 1em 0;
              padding: 0.5em 1em;
              border-left: 4px solid #F59E0B;
              background: #FFFBEB;
              color: #92400E;
            }
          </style>
        </head>
        <body>
          <main id="write">$formatted</main>
        </body>
        </html>
    """.trimIndent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownReaderScreen(
    title: String,
    markdown: String,
    isHindi: Boolean,
    onBack: () -> Unit
) {
    val htmlContent = remember(markdown, isHindi) {
        renderMarkdownToMoraHtml(markdown, isHindi)
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    appLanguage: Language = Language.HINDI,
    onBack: () -> Unit = {}
) {
    val isHindi = appLanguage == Language.HINDI
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
                    title = { Text(if (isHindi) "📥 डाउनलोड की गई सामग्री" else "📥 Downloaded Material") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    text = if (isHindi) "आपके डिवाइस में सहेजे गए ऑफ़लाइन स्टडी नोट्स:" else "Offline study notes saved on your device:",
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
                            text = if (isHindi) "कोई डाउनलोड फ़ाइल उपलब्ध नहीं है।" else "No downloaded files found.",
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
                                            contentDescription = "Delete File",
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
            onBack = { selectedFileItem = null }
        )
    }

    if (fileToDelete != null) {
        val target = fileToDelete!!
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = {
                Text(
                    text = if (isHindi) "फ़ाइल हटाएँ?" else "Delete File?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isHindi) "क्या आप निश्चित रूप से '${target.name}' को हटाना चाहते हैं?" else "Are you sure you want to delete '${target.name}'?",
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
                                if (isHindi) "'${target.name}' हटा दी गई।" else "Deleted '${target.name}'.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (isHindi) "हटाएँ" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}
