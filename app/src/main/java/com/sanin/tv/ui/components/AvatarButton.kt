package com.sanin.tv.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sanin.tv.settings.SettingsViewModel
@Composable
fun AvatarButton(    avatarUrl: String?,    onClick: () -> Unit) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()    
val settings by settingsViewModel.settings.collectAsState()    
var isFocused by remember { mutableStateOf(false) }    Surface(        modifier = Modifier            .size(40.dp)            .focusable()            .onFocusChanged { isFocused = it.isFocused }            .clickable { onClick() }            .navigationPillFocusEffect(isFocused, settings.focusEffect)            .border(                width = if (isFocused) 2.dp else 0.dp,                color = if (isFocused) Color(0xFF87CEEB) else Color.Transparent,                shape = RoundedCornerShape(50)            ),        shape = RoundedCornerShape(50),        color = Color.Transparent    ) {        AsyncImage(            model = avatarUrl,            contentDescription = "Profile",            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(50))        )    }}