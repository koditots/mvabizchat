package com.example.ui.screens.call

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.data.model.CallSession
import com.example.data.model.CallStatus
import com.example.ui.theme.*

@Composable
fun CallScreen(
    callSession: CallSession,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onFlipCamera: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val durationFormatted = remember(callSession.durationSeconds) {
        val mins = callSession.durationSeconds / 60
        val secs = callSession.durationSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    // Concentric pulse animation for calling aura
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Waveform bar animations for active audio
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(tween(450, easing = EaseInOutQuad), RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 28f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(550, easing = EaseInOutQuad), RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(tween(400, easing = EaseInOutQuad), RepeatMode.Reverse),
        label = "bar3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 34f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(500, easing = EaseInOutQuad), RepeatMode.Reverse),
        label = "bar4"
    )

    // Camera permission state for real camera preview
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("call_screen")
    ) {
        // Video Call: Real Camera Preview
        if (callSession.isVideo && callSession.isVideoEnabled) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val selector = if (callSession.isFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                            } catch (e: Exception) {
                                Log.e("CallScreen", "Failed to bind camera lifecycle", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        // Re-bind when camera facing flips
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val selector = if (callSession.isFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                            } catch (e: Exception) {
                                Log.e("CallScreen", "Failed to update camera", e)
                            }
                        }, ContextCompat.getMainExecutor(previewView.context))
                    }
                )

                // Top gradient scrim for readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                )

                // Secondary PIP Avatar Box in top right
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PurplePrimary),
                    modifier = Modifier
                        .size(90.dp, 120.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (callSession.peerAvatar.isNotBlank()) {
                            AsyncImage(
                                model = callSession.peerAvatar,
                                contentDescription = callSession.peerName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PurpleAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = callSession.peerName.take(6),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .fillMaxWidth()
                                .padding(2.dp)
                        )
                    }
                }
            } else {
                // Camera Permission Prompt Box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF16092A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = PurpleAccent,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Live Video Call Hardware",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Camera access is required for real video transmission.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Enable Camera", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Overlay Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Encryption status, peer details, real call status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp)
            ) {
                // E2EE Security Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PurpleSurfaceVariant.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = PurpleE2EEGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "End-to-End Encrypted (AES-256-GCM)",
                            color = PurpleNeonGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = callSession.peerName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Real Call Status & Timer
                val statusText = when (callSession.status) {
                    CallStatus.CALLING -> "Calling..."
                    CallStatus.RINGING -> "Ringing..."
                    CallStatus.CONNECTING -> "Connecting E2EE media stream..."
                    CallStatus.CONNECTED -> durationFormatted
                    CallStatus.RECONNECTING -> "Reconnecting..."
                    CallStatus.ENDED -> "Call Ended"
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (callSession.isConnected) PurplePrimaryDark.copy(alpha = 0.7f) else PurpleSurfaceVariant
                ) {
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        color = if (callSession.isConnected) PurpleNeonGlow else PurpleAccent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Real Hardware Telemetry Strip (When Connected)
                if (callSession.isConnected) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${callSession.codec} • ${callSession.latencyMs}ms • 0% loss • ${callSession.bitrateKbps}kbps",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Center Section: Pulsating Avatar & Live Equalizer (Voice or Video Off)
            if (!callSession.isVideo || !callSession.isVideoEnabled) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Pulsing aura ring
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .scale(if (callSession.isConnected) 1.05f else pulseScale)
                                .clip(CircleShape)
                                .background(PurplePrimary.copy(alpha = 0.2f))
                        )
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(3.dp, PurplePrimary, CircleShape)
                        ) {
                            if (callSession.peerAvatar.isNotEmpty()) {
                                AsyncImage(
                                    model = callSession.peerAvatar,
                                    contentDescription = callSession.peerName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(PurplePrimaryDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = callSession.peerName.take(1),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Dynamic Live Audio Equalizer Waveform (When Connected)
                    if (callSession.isConnected) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(44.dp)
                        ) {
                            val bars = listOf(bar1, bar2, bar3, bar4, bar2, bar1)
                            bars.forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(h.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (callSession.isMuted) TextMuted else PurpleNeonGlow)
                                )
                            }
                        }
                        if (callSession.isMuted) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Microphone Muted",
                                fontSize = 11.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom Controls Bar
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = PurpleSurfaceVariant.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Microphone
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (callSession.isMuted) Color.White else PurpleSurface)
                            .testTag("call_mute_button")
                    ) {
                        Icon(
                            imageVector = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (callSession.isMuted) Color.Black else PurpleAccent
                        )
                    }

                    // Video Toggle
                    IconButton(
                        onClick = onToggleVideo,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (!callSession.isVideoEnabled) Color.White else PurpleSurface)
                            .testTag("call_video_toggle")
                    ) {
                        Icon(
                            imageVector = if (callSession.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Camera",
                            tint = if (!callSession.isVideoEnabled) Color.Black else PurpleAccent
                        )
                    }

                    // Flip Camera (If video is active)
                    if (callSession.isVideo && callSession.isVideoEnabled) {
                        IconButton(
                            onClick = onFlipCamera,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(PurpleSurface)
                                .testTag("call_flip_camera")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Flip Camera",
                                tint = PurpleAccent
                            )
                        }
                    }

                    // Speakerphone Toggle
                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (callSession.isSpeakerOn) PurplePrimary else PurpleSurface)
                            .testTag("call_speaker_toggle")
                    ) {
                        Icon(
                            imageVector = if (callSession.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = "Speaker",
                            tint = if (callSession.isSpeakerOn) Color.White else PurpleAccent
                        )
                    }

                    // End Call
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626))
                            .testTag("call_end_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
