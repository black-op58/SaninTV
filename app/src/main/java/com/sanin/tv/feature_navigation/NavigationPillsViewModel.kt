package com.sanin.tv.feature_navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanin.tv.settings.Settings
import com.sanin.tv.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class NavigationTab(val route: String, val glyph: String, val label: String)

@HiltViewModel
class NavigationPillsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // ─── Route State ───
    private val _currentRoute = MutableStateFlow("home")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    // ─── Expansion State ───
    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()

    // ─── Navigation Focus State ───
    private val _hasNavFocus = MutableStateFlow(false)
    val hasNavFocus: StateFlow<Boolean> = _hasNavFocus.asStateFlow()

    // ─── Side Rail State ───
    private val _sideRailVisible = MutableStateFlow(false)
    val sideRailVisible: StateFlow<Boolean> = _sideRailVisible.asStateFlow()

    // ─── Notification Count ───
    private val _notificationCount = MutableStateFlow(0)
    val notificationCount: StateFlow<Int> = _notificationCount.asStateFlow()

    // ─── User Info (avatar + username for side rail header) ───
    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    // ─── Settings ───
    val settings: StateFlow<Settings> = settingsRepository.getSettings()
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    // ─── Derived: Is Player Route ───
    val isPlayerRoute: StateFlow<Boolean> = _currentRoute
        .map { it.startsWith("player/") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // ─── Derived: Is Main Tab ───
    val isMainTab: StateFlow<Boolean> = _currentRoute
        .map { it == "home" || it == "anime" || it == "search" || it == "library" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setRoute(route: String) { _currentRoute.value = route }
    fun setExpanded(expanded: Boolean) { _isExpanded.value = expanded }
    fun setNavFocus(focused: Boolean) { _hasNavFocus.value = focused }
    fun toggleExpanded() { _isExpanded.value = !_isExpanded.value }
    fun showSideRail() { _sideRailVisible.value = true }
    fun hideSideRail() { _sideRailVisible.value = false }
    fun updateNotificationCount(count: Int) { _notificationCount.value = count }
    fun updateUserInfo(avatarUrl: String?, username: String?) {
        _avatarUrl.value = avatarUrl
        _username.value = username
    }
}
