package com.learnquest.mp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.learnquest.mp.data.repository.StatefullLearningRepository
import com.learnquest.mp.navigation.AppNavigation
import com.learnquest.mp.ui.theme.LearnQuestMPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnQuestMPTheme {
                val context = LocalContext.current.applicationContext
                val repository = remember { StatefullLearningRepository(context) }
                AppNavigation(repository = repository)
            }
        }
    }
}
