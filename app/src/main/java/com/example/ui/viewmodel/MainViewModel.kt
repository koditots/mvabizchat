package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MainTab {
    CHATS, STORIES, CALLS, ADMIN, SETTINGS
}

enum class SearchFilter {
    ALL, CHATS, MESSAGES, MEDIA, DOCS, TEAM
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppRepository(application)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(MainTab.CHATS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Active Chat Thread
    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    // Global Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // Google Sign-In registration state
    private val _isGoogleRegistered = MutableStateFlow(true)
    val isGoogleRegistered: StateFlow<Boolean> = _isGoogleRegistered.asStateFlow()

    // Dark Mode Override (true = dark mode, false = light mode)
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Active story viewer
    private val _viewingStory = MutableStateFlow<Story?>(null)
    val viewingStory: StateFlow<Story?> = _viewingStory.asStateFlow()

    // Active Safety Numbers verification dialog
    private val _verifyingConversation = MutableStateFlow<Conversation?>(null)
    val verifyingConversation: StateFlow<Conversation?> = _verifyingConversation.asStateFlow()

    // Quick popup message dialog on screen
    private val _popupMessageEvent = MutableStateFlow<AppRepository.NotificationEvent?>(null)
    val popupMessageEvent: StateFlow<AppRepository.NotificationEvent?> = _popupMessageEvent.asStateFlow()

    // Call duration timer job
    private var callTimerJob: Job? = null

    init {
        // Observe in-app notification events to trigger on-screen popups if enabled
        viewModelScope.launch {
            repository.activeNotification.collect { notif ->
                if (notif != null && repository.popupRepliesEnabled.value) {
                    _popupMessageEvent.value = notif
                }
            }
        }

        // Observe active call to tick timer
        viewModelScope.launch {
            repository.activeCall.collect { call ->
                if (call != null && call.isConnected) {
                    startCallTimer()
                } else {
                    callTimerJob?.cancel()
                    callTimerJob = null
                }
            }
        }
    }

    private fun startCallTimer() {
        if (callTimerJob != null) return
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                repository.incrementCallDuration()
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun openConversation(conversationId: String) {
        _activeConversationId.value = conversationId
        repository.markConversationRead(conversationId)
        _isSearchActive.value = false
    }

    fun closeConversation() {
        _activeConversationId.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    fun toggleSearch(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun dismissPopup() {
        _popupMessageEvent.value = null
        repository.dismissActiveNotification()
    }

    fun sendQuickReply(conversationId: String, replyText: String) {
        repository.sendMessage(conversationId, replyText)
        dismissPopup()
    }

    fun openStory(story: Story) {
        _viewingStory.value = story
    }

    fun closeStory() {
        _viewingStory.value = null
    }

    fun openVerificationDialog(conversation: Conversation) {
        _verifyingConversation.value = conversation
    }

    fun closeVerificationDialog() {
        _verifyingConversation.value = null
    }

    fun triggerGoogleAccountSwitch(name: String, email: String) {
        val derivedUsername = email.substringBefore("@")
        repository.updateCurrentUser(
            name = name,
            email = email,
            username = derivedUsername,
            status = "Leading Enterprise Engineering 🚀",
            department = "Engineering"
        )
    }
}
