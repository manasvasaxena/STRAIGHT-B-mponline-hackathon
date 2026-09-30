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
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.Language
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
    var selectedSubjectNotes by remember { mutableStateOf<String?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteContent by remember { mutableStateOf("") }
    val context = LocalContext.current

    val isHindi = appLanguage == Language.HINDI

    val class6MathsMdUrl = "https://raw.githubusercontent.com/manasvasaxena/STRAIGHT-B-mponline-hackathon/main/PKGS/Class_6_Maths_Lesson_1_Notes.md"
    val localClass6Notes = """
        # Class 6 Maths - Lesson 1
        ## Knowing Our Numbers

        ### 1. Numbers and Place Value
        A number is made up of digits. The place value of a digit depends on its position.

        Example: 5,43,216
        - 5 Lakhs (5,00,000)
        - 4 Ten Thousands (40,000)
        - 3 Thousands (3,000)
        - 2 Hundreds (200)
        - 1 Tens (10)
        - 6 Ones (6)

        ### 2. Face Value
        The face value of a digit is the digit itself.
        Example: In 72,456, the face value of 2 is 2.

        ### 3. Indian Place Value System
        Ones -> Tens -> Hundreds -> Thousands -> Ten Thousands -> Lakhs -> Ten Lakhs -> Crores

        Example: 12,34,567
        Twelve lakh thirty-four thousand five hundred sixty-seven.

        ### 4. Comparing Numbers
        45,678 > 9,876
        56,432 > 54,321

        ### 5. Ascending and Descending Order
        Ascending: 12, 25, 37, 48, 63
        Descending: 63, 48, 37, 25, 12

        ### 6. Rounding Off Numbers
        47 -> 50, 43 -> 40
        346 -> 300, 378 -> 400
    """.trimIndent()

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
                placeholder = { Text(if (isHindi) "विषय या नोट्स खोजें..." else "Search subjects, topics, notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Column {
                Text(
                    text = if (isHindi) "1. बोर्ड चुनें" else "1. Select Board",
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
                                    text = if (isHindi) board.displayNameHindi else board.displayNameEnglish,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) SaffronPrimary else Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHindi) board.badgeHindi else board.badgeEnglish,
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
                    text = if (isHindi) "2. कक्षा चुनें" else "2. Select Class",
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
                        text = if (isHindi) "📚 मुख्य विषय" else "📚 Core Subjects",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isHindi) selectedBoard.displayNameHindi else selectedBoard.displayNameEnglish,
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
                            onClick = {
                                val title = if (isHindi) subject.titleHindi else subject.titleEnglish
                                if (selectedGrade == "Class 6" && (title == "Mathematics" || title == "गणित")) {
                                    selectedSubjectNotes = "Class_6_Maths_Lesson_1_Notes.md"
                                    noteContent = localClass6Notes
                                    showNoteDialog = true
                                }
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
                    text = if (isHindi) "🗣️ भाषाएँ" else "🗣️ Languages",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    languagesAndLiterature.forEach { subject ->
                        SubjectCard(
                            subject = subject,
                            isHindi = isHindi,
                            onClick = { onCategoryClick(if (isHindi) subject.titleHindi else subject.titleEnglish) }
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = if (isHindi) "🌟 विशेष संसाधन" else "🌟 Special Resources",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    mpSpecialBranches.forEach { branch ->
                        BranchCard(branch = branch, isHindi = isHindi)
                    }
                }
            }
        }
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    text = if (isHindi) "उपलब्ध पाठ सामग्री" else "Available Study Material",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isHindi) "नोट्स फ़ाइल का नाम:" else "Notes File Name:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
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
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedSubjectNotes ?: "Class_6_Maths_Lesson_1_Notes.md",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    if (isOffline) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHindi) "⚠️ डाउनलोड केवल इंटरनेट उपलब्ध होने पर काम करेगा।" else "⚠️ Downloading requires active internet connection.",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isOffline) {
                            Toast.makeText(
                                context,
                                if (isHindi) "इंटरनेट कनेक्शन नहीं है! डाउनलोड करने के लिए ऑनलाइन आएं।" else "No internet connection! Go online to download.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            downloadMarkdownNote(
                                context = context,
                                filename = "Class_6_Maths_Lesson_1_Notes.md",
                                content = noteContent
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOffline) Color.Gray else SaffronPrimary
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isHindi) "डाउनलोड .md" else "Download .md")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text(if (isHindi) "बंद करें" else "Close")
                }
            }
        )
    }
}

fun downloadMarkdownNote(context: Context, filename: String, content: String) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/markdown")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { os ->
                    os.write(content.toByteArray())
                }
                Toast.makeText(context, "Downloaded $filename to Downloads folder!", Toast.LENGTH_LONG).show()
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)
            FileOutputStream(file).use { os ->
                os.write(content.toByteArray())
            }
            Toast.makeText(context, "Saved $filename to Downloads!", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to download note: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SubjectCard(
    subject: SubjectCategory,
    isHindi: Boolean,
    onClick: () -> Unit
) {
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
                    text = if (isHindi) subject.titleHindi else subject.titleEnglish,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${if (isHindi) subject.tagHindi else subject.tagEnglish} • ${subject.totalLessons} ${if (isHindi) "पाठ" else "Lessons"}",
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
fun BranchCard(branch: ResourceBranch, isHindi: Boolean) {
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
                    text = if (isHindi) branch.titleHindi else branch.titleEnglish,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF14532D)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isHindi) branch.descriptionHindi else branch.descriptionEnglish,
                    fontSize = 11.sp,
                    color = Color(0xFF166534)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = {}) {
                Text(
                    text = if (isHindi) branch.actionTextHindi else branch.actionTextEnglish,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }
        }
    }
}
