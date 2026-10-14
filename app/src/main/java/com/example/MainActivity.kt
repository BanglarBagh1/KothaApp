package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.settings.KothaSettings
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.StationScreen
import com.example.ui.screens.TestPadScreen
import com.example.ui.screens.stationIndexList
import com.example.ui.theme.KothaTheme

sealed class Screen {
    object Home : Screen()
    data class Station(val number: String) : Screen()
    object TestPad : Screen()
    object Onboarding : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as KothaApplication
        val settingsRepo = app.settingsRepository

        setContent {
            val settings by settingsRepo.settingsFlow.collectAsState()
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

            KothaTheme(
                appTheme = settings.appTheme,
                accentInk = settings.accentInk
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (val screen = currentScreen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    settings = settings,
                                    settingsRepo = settingsRepo,
                                    onNavigateStation = { num ->
                                        currentScreen = Screen.Station(num)
                                    },
                                    onNavigateTestPad = {
                                        currentScreen = Screen.TestPad
                                    }
                                )
                            }

                            is Screen.Station -> {
                                BackHandler {
                                    currentScreen = Screen.Home
                                }
                                val stationMeta = stationIndexList.find { it.number == screen.number }
                                StationScreen(
                                    stationNumber = screen.number,
                                    stationTitle = stationMeta?.title ?: "Station",
                                    stationSubtitle = stationMeta?.subtitle ?: "",
                                    settings = settings,
                                    settingsRepo = settingsRepo,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }

                            is Screen.TestPad -> {
                                BackHandler {
                                    currentScreen = Screen.Home
                                }
                                TestPadScreen(
                                    settings = settings,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }

                            is Screen.Onboarding -> {
                                BackHandler {
                                    currentScreen = Screen.Home
                                }
                                OnboardingScreen(
                                    onFinished = { currentScreen = Screen.Home }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (application as KothaApplication).accessibilityKeeper.checkPermission()
        com.example.bridge.TextBridgeService.instance?.refreshFocusedNode()
    }
}
