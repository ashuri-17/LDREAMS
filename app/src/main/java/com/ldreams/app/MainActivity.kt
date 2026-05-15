package com.ldreams.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.ldreams.app.data.repository.dataStore
import com.ldreams.app.navigation.AppNavigation
import com.ldreams.app.ui.components.DreamBackground
import com.ldreams.app.ui.screens.PrivacyLockScreen
import com.ldreams.app.ui.theme.LDreamsTheme
import com.ldreams.app.ui.theme.NeonCyan
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private val PRIVACY_LOCK_KEY = booleanPreferencesKey("privacy_lock_enabled")
    }

    private var showSplash by mutableStateOf(true)
    private var showLockScreen by mutableStateOf(false)
    private var privacyLockEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Observe privacy lock preference from DataStore
        lifecycleScope.launch {
            dataStore.data.collect { prefs ->
                privacyLockEnabled = prefs[PRIVACY_LOCK_KEY] ?: false
                // Auto-dismiss lock screen when toggle is turned off
                if (!privacyLockEnabled) {
                    showLockScreen = false
                }
            }
        }

        // Auto-dismiss splash after 1.5 seconds
        lifecycleScope.launch {
            delay(1500)
            showSplash = false
        }

        setContent {
            LDreamsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = when {
                            showSplash -> 0
                            showLockScreen -> 1
                            else -> 2
                        },
                        label = "main_transition"
                    ) { state ->
                        when (state) {
                            0 -> SplashScreen()
                            1 -> PrivacyLockScreen(onUnlock = { showLockScreen = false })
                            2 -> AppNavigation()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPrivacyLock()
    }

    private fun checkPrivacyLock() {
        if (!privacyLockEnabled) return
        val prefs = getSharedPreferences("ldreams_privacy", Context.MODE_PRIVATE)
        val unlockTime = prefs.getLong("unlock_time", 0L)
        if (System.currentTimeMillis() - unlockTime > 5000) {
            showLockScreen = true
        }
    }

    @Composable
    private fun SplashScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0B1A)),
            contentAlignment = Alignment.Center
        ) {
            DreamBackground()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "LDREAMS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE8E8F0)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Your Lucid Dreaming Companion",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Explore the world of lucid dreaming",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA0A0B8)
                )
            }
        }
    }

}
