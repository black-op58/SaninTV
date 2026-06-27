package com.sanin.tv.feature_settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanin.tv.ui.components.NavigationPill
import kotlinx.coroutines.delay
@Composable
fun FocusEffectsPreviewScreen(    onBack: () -> Unit,    viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()    
val effects = listOf(        "glow"    to "Glow \u2014 Soft icy-blue halo with border brightening",        "scale"   to "Scale \u2014 1.08x smooth scale increase",        "pulse"   to "Pulse \u2014 1.08x \u2194 1.12x oscillating rhythm",        "breathe" to "Breathe \u2014 Slow organic opacity and scale breathing",        "none"    to "None \u2014 No focus animation"    )    Column(        modifier = Modifier            .fillMaxSize()            .background(Color.Black)            .verticalScroll(rememberScrollState())            .padding(24.dp)    ) {        Row(            verticalAlignment = Alignment.CenterVertically,            modifier = Modifier.padding(bottom = 24.dp)        ) {            IconButton(onClick = onBack) {                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)            }            Text("Focus Effects Preview", style = MaterialTheme.typography.headlineSmall, color = Color.White)        }        Text(            "Select a focus effect to apply it to all navigation pills and interactive elements.",            color = Color.White.copy(alpha = 0.7f),            modifier = Modifier.padding(bottom = 24.dp)        )        effects.forEach { (effectKey, description) ->            
val isSelected = settings.focusEffect == effectKey            Column(modifier = Modifier.padding(bottom = 16.dp)) {                Text(description, color = if (isSelected) Color(0xFF87CEEB) else Color.White.copy(alpha = 0.8f))                Spacer(Modifier.height(8.dp))                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {                    NavigationPill(                        glyph = "\u25EC", label = "Home",                        isActive = false, isExpanded = true,                        focusEffect = effectKey, onClick = {}                    )                    Button(                        onClick = { viewModel.updateFocusEffect(effectKey) },                        colors = ButtonDefaults.buttonColors(                            containerColor = if (isSelected) Color(0xFF87CEEB) else Color.White.copy(alpha = 0.15f)                        )                    ) { Text(if (isSelected) "Applied" else "Apply", color = if (isSelected) Color.Black else Color.White) }                }            }        }    }}
