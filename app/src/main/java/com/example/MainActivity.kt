package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.call.CallScreen
import com.example.ui.screens.main.MainTabScreen
import com.example.ui.screens.security.AppLockScreen
import com.example.ui.theme.MVABusinessChatTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val isLoggedIn by viewModel.repository.isLoggedIn.collectAsState()
            val isAppLocked by viewModel.repository.isAppLocked.collectAsState()
            val pinCode by viewModel.repository.pinCode.collectAsState()
            val isBiometricEnabled by viewModel.repository.biometricEnabled.collectAsState()
            val activeCall by viewModel.repository.activeCall.collectAsState()

            MVABusinessChatTheme(darkTheme = isDarkMode) {
                if (!isLoggedIn) {
                    LoginScreen(
                        onGoogleSignInSuccess = { name, email, photo ->
                            viewModel.repository.loginWithGoogle(name, email, photo)
                        },
                        onWorkAccountSignInSuccess = { email, name, dept, role ->
                            viewModel.repository.loginWithWorkAccount(email, name, dept, role)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Crossfade(targetState = isAppLocked, label = "lock_fade") { locked ->
                        if (locked) {
                            AppLockScreen(
                                correctPin = pinCode,
                                isBiometricEnabled = isBiometricEnabled,
                                onUnlocked = { viewModel.repository.setAppLocked(false) }
                            )
                        } else {
                            if (activeCall != null) {
                                CallScreen(
                                    callSession = activeCall!!,
                                    onEndCall = { viewModel.repository.endCall() },
                                    onToggleMute = { viewModel.repository.toggleCallMute() },
                                    onToggleVideo = { viewModel.repository.toggleCallVideo() },
                                    onToggleSpeaker = { viewModel.repository.toggleCallSpeaker() },
                                    onFlipCamera = { viewModel.repository.toggleCameraFacing() }
                                )
                            } else {
                                MainTabScreen(
                                    viewModel = viewModel,
                                    onLockAppNow = { viewModel.repository.setAppLocked(true) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
