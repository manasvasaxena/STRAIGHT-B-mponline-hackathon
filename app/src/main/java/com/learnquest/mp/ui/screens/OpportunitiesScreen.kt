package com.learnquest.mp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.CareerPathway
import com.learnquest.mp.model.Language
import com.learnquest.mp.model.Scholarship
import com.learnquest.mp.ui.appStrings
import com.learnquest.mp.ui.theme.ForestGreen
import com.learnquest.mp.ui.theme.SaffronPrimary

/**
 * Screen showcasing Verified Scholarship Discovery and Rule-based Career Pathways with dynamic language support.
 */
@Composable
fun OpportunitiesScreen(
    scholarships: List<Scholarship>,
    careers: List<CareerPathway>,
    appLanguage: Language = Language.HINDI
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isHindi = appLanguage == Language.HINDI
    val strings = appLanguage.appStrings()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = SaffronPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(strings.t("🎓 Scholarships", "🎓 छात्रवृत्ति", "🎓 Scholarships"), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(strings.t("🚀 Career Guidance", "🚀 करियर मार्ग", "🚀 Career Guidance"), fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(scholarships) { scholarship ->
                    ScholarshipCard(scholarship = scholarship, isHindi = isHindi, language = appLanguage)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(careers) { career ->
                    CareerCard(career = career, isHindi = isHindi, language = appLanguage)
                }
            }
        }
    }
}

@Composable
fun ScholarshipCard(scholarship: Scholarship, isHindi: Boolean, language: Language = if (isHindi) Language.HINDI else Language.ENGLISH) {
    val strings = language.appStrings()
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scholarship.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                if (scholarship.isVerified) {
                    Surface(
                        color = ForestGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = strings.t("✓ Verified", "✓ सत्यापित", "✓ Verified"),
                            color = ForestGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Text(
                text = "${strings.t("Provider", "प्रदाता", "Provider")}: ${scholarship.provider} | ${strings.t("Category", "श्रेणी", "Category")}: ${scholarship.category}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${strings.t("Eligibility", "पात्रता", "Eligibility")}: ${scholarship.eligibility}",
                fontSize = 13.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.t("Amount", "राशि", "Amount")}: ${scholarship.amount}",
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${strings.t("Deadline", "अंतिम तिथि", "Deadline")}: ${scholarship.deadline}",
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CareerCard(career: CareerPathway, isHindi: Boolean, language: Language = if (isHindi) Language.HINDI else Language.ENGLISH) {
    val strings = language.appStrings()
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = career.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = "${strings.t("Sector", "क्षेत्र", "Sector")}: ${career.sector} | ${strings.t("Education", "आवश्यक योग्यता", "Education")}: ${career.requiredEducation}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = career.description,
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${strings.t("Recommended Subjects", "अनुशंसित विषय", "Recommended Subjects")}: ${career.recommendedSubjects.joinToString(", ")}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronPrimary
            )
        }
    }
}
