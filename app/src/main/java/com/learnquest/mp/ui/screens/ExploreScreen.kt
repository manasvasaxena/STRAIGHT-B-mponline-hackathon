package com.learnquest.mp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import android.os.Environment
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.learnquest.mp.data.repository.PkgsRepository
import com.learnquest.mp.data.repository.RemotePackageFile
import java.io.File
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.appStrings
import com.learnquest.mp.ui.theme.SaffronPrimary

enum class EducationBoard(val displayNameHindi: String, val displayNameEnglish: String, val badgeHindi: String, val badgeEnglish: String) {
    MP_BOARD("एमपी बोर्ड", "MP State Board", "MPBSE पाठ्यक्रम", "MPBSE Syllabus"),
    NCERT("एनसीईआरटी / सीबीएसई", "NCERT / CBSE", "केंद्रीय पाठ्यक्रम", "Central Curriculum")
}

data class SubjectCategory(
    val titleHindi: String,
    val titleEnglish: String,
    val icon: ImageVector,
    val color: Color,
    val totalLessons: Int,
    val tagHindi: String,
    val tagEnglish: String
)

data class ResourceBranch(
    val titleHindi: String,
    val titleEnglish: String,
    val descriptionHindi: String,
    val descriptionEnglish: String,
    val icon: ImageVector,
    val actionTextHindi: String,
    val actionTextEnglish: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    appLanguage: Language = Language.HINDI,
    isOffline: Boolean = false,
    onCategoryClick: (String) -> Unit = {}
) {
    var selectedBoard by remember { mutableStateOf(EducationBoard.MP_BOARD) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("Class 6") }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var remotePackages by remember { mutableStateOf<List<RemotePackageFile>>(emptyList()) }
    var isLoadingPackages by remember { mutableStateOf(false) }
    var packageError by remember { mutableStateOf<String?>(null) }
    var downloadingPackageName by remember { mutableStateOf<String?>(null) }
    var downloadedFilesVersion by remember { mutableStateOf(0) }
    var selectedDownloadedFile by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pkgsRepository = remember(context) { PkgsRepository(context) }

    val strings = appLanguage.appStrings()
    val isHindi = appLanguage == Language.HINDI
    fun localized(hindi: String, english: String): String = strings.t(english, hindi, "$english / $hindi")

    fun refreshPackages() {
        if (isOffline || isLoadingPackages) return
        scope.launch {
            isLoadingPackages = true
            packageError = null
            pkgsRepository.listPackageFiles()
                .onSuccess { remotePackages = it }
                .onFailure { packageError = it.localizedMessage ?: strings.t("Unable to load study files", "स्टडी फ़ाइलें लोड नहीं हो सकीं", "Study files load nahi ho paayi") }
            isLoadingPackages = false
        }
    }

    fun openPackageBrowser(subject: String) {
        selectedSubject = subject
        showNoteDialog = true
        refreshPackages()
    }

    LaunchedEffect(isOffline) {
        if (!isOffline) refreshPackages()
    }

    val locallyDownloadedPackages = remember(downloadedFilesVersion) {
        listLocalPackageFiles(context)
    }

    if (selectedDownloadedFile != null) {
        val file = selectedDownloadedFile!!
        BackHandler { selectedDownloadedFile = null }
        MarkdownReaderScreen(
            title = file.name,
            markdown = runCatching { file.readText() }.getOrDefault(""),
            isHindi = isHindi,
            onBack = { selectedDownloadedFile = null },
            appLanguage = appLanguage
        )
        return
    }

    val grades = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12")

    val coreSubjects = listOf(
        SubjectCategory("गणित", "Mathematics", Icons.Default.Calculate, Color(0xFF3B82F6), 42, "सूत्र एवं अभ्यास", "Formulae & Practice"),
        SubjectCategory("विज्ञान", "Science", Icons.Default.Science, Color(0xFF10B981), 38, "प्रयोग एवं अवधारणाएं", "Experiments & Concepts"),
        SubjectCategory("सामाजिक विज्ञान", "Social Science", Icons.Default.Public, Color(0xFFF59E0B), 30, "इतिहास, नागरिक शास्त्र, भूगोल", "History, Civics, Geography"),
        SubjectCategory("पर्यावरण अध्ययन", "Environmental Studies", Icons.Default.Eco, Color(0xFF8B5CF6), 24, "पारिस्थितिकी एवं जीव जगत", "Ecology & Living")
    )

    val languagesAndLiterature = listOf(
        SubjectCategory("हिन्दी", "Hindi", Icons.Default.MenuBook, Color(0xFFEF4444), 35, "व्याकरण एवं गद्य-पद्य", "Grammar & Literature"),
        SubjectCategory("अंग्रेजी", "English", Icons.Default.Language, Color(0xFF06B6D4), 32, "व्याकरण एवं समझ", "Grammar & Comprehension"),
        SubjectCategory("संस्कृत", "Sanskrit", Icons.Default.HistoryEdu, Color(0xFFEC4899), 20, "श्लोक एवं व्याकरण", "Shlokas & Grammar")
    )

    val mpSpecialBranches = listOf(
        ResourceBranch(
            titleHindi = "एमपी बोर्ड मॉडल प्रश्न पत्र",
            titleEnglish = "MP Board Model Papers",
            descriptionHindi = "समाधान सहित पिछले 5 वर्षों के प्रश्न पत्र।",
            descriptionEnglish = "Previous 5 years question papers with solutions.",
            icon = Icons.Default.Description,
            actionTextHindi = "पेपर देखें",
            actionTextEnglish = "View Papers"
        ),
        ResourceBranch(
            titleHindi = "छात्रवृत्ति एवं करियर मार्ग",
            titleEnglish = "Scholarships & Careers",
            descriptionHindi = "मेधावी छात्र योजना, NMMS एवं करियर विकल्प।",
            descriptionEnglish = "Medhavi Chhatra Yojna, NMMS, & career pathways.",
            icon = Icons.Default.School,
            actionTextHindi = "मार्ग देखें",
            actionTextEnglish = "Explore"
        ),
        ResourceBranch(
            titleHindi = "वसायिक एवं कंप्यूटर कौशल",
            titleEnglish = "Vocational & IT Skills",
            descriptionHindi = "बुनियादी कंप्यूटर ज्ञान एवं डिजिटल साक्षरता।",
            descriptionEnglish = "Basic Computer Skills & Digital Literacy.",
            icon = Icons.Default.Computer,
            actionTextHindi = "सीखना शुरू करें",
            actionTextEnglish = "Start Learning"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(localized("विषय या नोट्स खोजें...", "Search subjects, topics, notes...")) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Column {
                Text(
                    text = localized("1. बोर्ड चुनें", "1. Select Board"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EducationBoard.entries.forEach { board ->
                        val isSelected = selectedBoard == board
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFFF7ED) else Color.White
                            ),
                            border = CardDefaults.outlinedCardBorder(
                                enabled = true
                            ).copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isSelected) SaffronPrimary else Color.LightGray
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedBoard = board },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = localized(board.displayNameHindi, board.displayNameEnglish),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) SaffronPrimary else Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = localized(board.badgeHindi, board.badgeEnglish),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = localized("2. कक्षा चुनें", "2. Select Class"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(grades) { grade ->
                        val isSelected = selectedGrade == grade
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = localized("📚 मुख्य विषय", "📚 Core Subjects"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = localized(selectedBoard.displayNameHindi, selectedBoard.displayNameEnglish),
                        fontSize = 12.sp,
                        color = SaffronPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    coreSubjects.forEach { subject ->
                        SubjectCard(
                            subject = subject,
                            isHindi = isHindi,
                            language = appLanguage,
                            onClick = {
                                val title = localized(subject.titleHindi, subject.titleEnglish)
                                openPackageBrowser(subject.titleEnglish)
                                onCategoryClick(title)
                            }
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = localized("🗣️ भाषाएँ", "🗣️ Languages"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    languagesAndLiterature.forEach { subject ->
                        SubjectCard(
                            subject = subject,
                            isHindi = isHindi,
                            language = appLanguage,
                            onClick = {
                                openPackageBrowser(subject.titleEnglish)
                                onCategoryClick(localized(subject.titleHindi, subject.titleEnglish))
                            }
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = localized("🌟 विशेष संसाधन", "🌟 Special Resources"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    mpSpecialBranches.forEach { branch ->
                        BranchCard(branch = branch, isHindi = isHindi, language = appLanguage)
                    }
                }
            }
        }
    }

    if (showNoteDialog) {
        val availablePackages = (remotePackages + locallyDownloadedPackages).distinctBy { it.name }
        val matchingPackages = availablePackages.filter {
            packageMatchesSelection(it.name, selectedGrade, selectedSubject.orEmpty())
        }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    text = selectedSubject?.let { subject ->
                        localized("$subject की पाठ सामग्री", "$subject study material")
                    } ?: localized("उपलब्ध पाठ सामग्री", "Available Study Material"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = localized("$selectedGrade के GitHub PKGS से फ़ाइलें:", "Files from the GitHub PKGS folder for $selectedGrade:"),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoadingPackages) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(localized("GitHub से फ़ाइलें लोड हो रही हैं...", "Loading files from GitHub..."))
                        }
                    } else if (matchingPackages.isEmpty()) {
                        Text(
                            text = packageError
                                ?: if (isOffline) {
                                    localized("ऑफ़लाइन हैं। पहले से डाउनलोड की गई फ़ाइलें Downloads में उपलब्ध हैं।", "You are offline. Previously downloaded files are available in Downloads.")
                                } else {
                                    localized("इस कक्षा और विषय के लिए GitHub पर कोई फ़ाइल नहीं मिली।", "No matching files were found in GitHub for this class and subject.")
                                },
                            fontSize = 13.sp,
                            color = if (packageError != null) Color(0xFFDC2626) else Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(matchingPackages, key = { it.name }) { packageFile ->
                                val downloadedFile = findLocalPackageFile(context, packageFile.name)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = SaffronPrimary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                         Column(modifier = Modifier.weight(1f)) {
                                            Text(packageFile.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                packageSizeText(packageFile.sizeBytes),
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                         }
                                        if (downloadedFile != null) {
                                            TextButton(
                                                onClick = {
                                                    showNoteDialog = false
                                                    selectedDownloadedFile = downloadedFile
                                                }
                                            ) {
                                                Text(localized("देखें", "View"), fontSize = 12.sp)
                                            }
                                        }
                                        if (packageFile.downloadUrl.isNotBlank()) {
                                            TextButton(
                                                enabled = !isOffline && downloadingPackageName == null,
                                                onClick = {
                                                    scope.launch {
                                                        downloadingPackageName = packageFile.name
                                                        pkgsRepository.downloadPackage(packageFile)
                                                            .onSuccess {
                                                                Toast.makeText(
                                                                    context,
                                                                    localized("${packageFile.name} डाउनलोड हो गई", "Downloaded ${packageFile.name}"),
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                                showNoteDialog = false
                                                                downloadedFilesVersion++
                                                            }
                                                            .onFailure {
                                                                Toast.makeText(
                                                                    context,
                                                    strings.t("Download failed: ${it.localizedMessage ?: "network error"}", "डाउनलोड विफल: ${it.localizedMessage ?: "नेटवर्क त्रुटि"}", "Download failed: ${it.localizedMessage ?: "network error"}"),
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        downloadingPackageName = null
                                                    }
                                                }
                                            ) {
                                                Text(
                                                    if (downloadingPackageName == packageFile.name) {
                                                        localized("हो रहा है…", "Saving…")
                                                    } else {
                                                        localized("डाउनलोड", "Download")
                                                    },
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (isOffline) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = localized("⚠️ डाउनलोड के लिए इंटरनेट कनेक्शन आवश्यक है।", "⚠️ Downloads require an active internet connection."),
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { refreshPackages() }, enabled = !isOffline && !isLoadingPackages) {
                    Text(localized("पुनः लोड करें", "Refresh"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text(localized("बंद करें", "Close"))
                }
            }
        )
    }
}

private fun packageDirectories(context: android.content.Context): List<File> = listOfNotNull(
    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
    File(context.filesDir, "downloads"),
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
).distinctBy { it.absolutePath }

private fun listLocalPackageFiles(context: android.content.Context): List<RemotePackageFile> =
    packageDirectories(context)
        .flatMap { directory ->
            directory.listFiles()
                ?.filter { it.isFile && it.extension.lowercase() in listOf("md", "txt") }
                .orEmpty()
        }
        .map { file -> RemotePackageFile(file.name, "", file.length()) }
        .distinctBy { it.name }

private fun findLocalPackageFile(context: android.content.Context, name: String): File? {
    val safeName = name.substringAfterLast('/')
    return packageDirectories(context)
        .asSequence()
        .map { File(it, safeName) }
        .firstOrNull { it.isFile }
}

private fun packageMatchesSelection(name: String, grade: String, subject: String): Boolean {
    val lowerName = name.lowercase()
    val gradeNumber = Regex("(?:class|cls)[ _-]*(\\d+)").find(lowerName)?.groupValues?.get(1)
    if (gradeNumber != null && gradeNumber != grade.filter(Char::isDigit)) return false

    val subjectTokens = when (subject.lowercase()) {
        "mathematics" -> listOf("math", "maths")
        "science" -> listOf("science")
        "social science" -> listOf("social", "history", "civics", "geography", "sst")
        "environmental studies" -> listOf("evs", "environment")
        "hindi" -> listOf("hindi")
        "english" -> listOf("english")
        "sanskrit" -> listOf("sanskrit")
        else -> listOf(subject.lowercase())
    }
    return subjectTokens.any { token -> lowerName.contains(token) }
}

private fun packageSizeText(sizeBytes: Long): String = when {
    sizeBytes <= 0L -> "Study notes"
    sizeBytes < 1024L -> "$sizeBytes B"
    sizeBytes < 1024L * 1024L -> "${sizeBytes / 1024.0} KB"
    else -> "${sizeBytes / (1024.0 * 1024.0)} MB"
}

@Composable
fun SubjectCard(
    subject: SubjectCategory,
    isHindi: Boolean,
    onClick: () -> Unit,
    language: Language = if (isHindi) Language.HINDI else Language.ENGLISH
) {
    val strings = language.appStrings()
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(subject.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subject.icon,
                    contentDescription = null,
                    tint = subject.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.t(subject.titleEnglish, subject.titleHindi, "${subject.titleEnglish} / ${subject.titleHindi}"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${strings.t(subject.tagEnglish, subject.tagHindi, "${subject.tagEnglish} / ${subject.tagHindi}")} • ${subject.totalLessons} ${strings.t("Lessons", "पाठ", "Lessons")}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun BranchCard(branch: ResourceBranch, isHindi: Boolean, language: Language = if (isHindi) Language.HINDI else Language.ENGLISH) {
    val strings = language.appStrings()
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBBF7D0))
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = branch.icon,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.t(branch.titleEnglish, branch.titleHindi, "${branch.titleEnglish} / ${branch.titleHindi}"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF14532D)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.t(branch.descriptionEnglish, branch.descriptionHindi, "${branch.descriptionEnglish} / ${branch.descriptionHindi}"),
                    fontSize = 11.sp,
                    color = Color(0xFF166534)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = {}) {
                Text(
                    text = strings.t(branch.actionTextEnglish, branch.actionTextHindi, "${branch.actionTextEnglish} / ${branch.actionTextHindi}"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }
        }
    }
}
