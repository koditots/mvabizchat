package com.example.data.model

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val commitSha: String,
    val commitMessage: String,
    val authorName: String,
    val authorAvatar: String = "",
    val dateString: String,
    val releaseNotes: List<String> = emptyList(),
    val downloadSizeBytes: Long = 18_450_000L, // ~18.4 MB
    val downloadUrl: String = "",
    val isMandatory: Boolean = false
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val update: AppUpdateInfo) : UpdateState()
    data class Downloading(
        val update: AppUpdateInfo,
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateState()
    data class ReadyToInstall(val update: AppUpdateInfo) : UpdateState()
    data class AutoUpdatedSilently(val update: AppUpdateInfo, val timestamp: Long) : UpdateState()
    data class UpToDate(val lastChecked: Long) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

data class UpdaterConfig(
    val repoOwner: String = "merversaus",
    val repoName: String = "mva-business-chat",
    val branch: String = "main",
    val autoUpdateInBackground: Boolean = true,
    val notifyOnPush: Boolean = true,
    val checkFrequencyMinutes: Int = 15
)
