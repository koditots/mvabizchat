package com.example.ui.screens.call

import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CallSession
import com.example.ui.theme.*

@Composable
fun CallScreen(
    callSession: CallSession,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("call_screen")
    ) {
        // If video call and video is enabled, show simulated camera background preview
        if (callSession.isVideo && callSession.isVideoEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF16092A))
            ) {
                // Secondary small user preview in top right
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PurplePrimary),
                    modifier = Modifier
                        .size(110.dp, 160.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "My Camera",
                            tint = PurpleAccent,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "You",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Encryption status & Peer Details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp)
            ) {
                // Encrypted Call Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PurpleSurfaceVariant,
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
                            text = "End-to-End Encrypted",
                            color = PurpleNeonGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = callSession.peerName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (callSession.isConnected) durationFormatted else "Connecting...",
                    fontSize = 14.sp,
                    color = PurpleAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            // Center: Pulsating Avatar (Voice or Video Off)
            if (!callSession.isVideo || !callSession.isVideoEnabled) {
                Box(contentAlignment = Alignment.Center) {
                    // Pulsing aura ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(pulseScale)
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
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom Controls Bar
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = PurpleSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(52.dp)
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

                    // Video toggle
                    IconButton(
                        onClick = onToggleVideo,
                        modifier = Modifier
                            .size(52.dp)
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

                    // Speaker
                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(52.dp)
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
