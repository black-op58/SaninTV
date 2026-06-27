package com.sanin.tv.ui.components
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
private val SpringSpec = spring<Float>(dampingRatio = 0.85f, stiffness = Spring.StiffnessVeryLow)
private val FocusSpringSpec = spring<Float>(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
private val BreatheSpec = spring<Float>(dampingRatio = 0.6f, stiffness = Spring.StiffnessVeryLow)
val IcyBlueGlow = Color(0xFF87CEEB)
val IcyBlueSoft = Color(0xFF87CEEB).copy(alpha = 0.3f)
val IcyBlueBorder = Color(0xFF87CEEB).copy(alpha = 0.5f)
@Composable
fun Modifier.glowFocusEffect(isFocused: Boolean): Modifier {
    val glowAlpha by animateFloatAsState(        targetValue = if (isFocused) 0.5f else 0f,        animationSpec = FocusSpringSpec, label = "glowAlpha"    )    
val scale by animateFloatAsState(        targetValue = if (isFocused) 1.05f else 1.0f,        animationSpec = FocusSpringSpec, label = "glowScale"    )
return this.graphicsLayer { scaleX = scale
scaleY = scale }        .then(if (isFocused) Modifier.border(2.dp, IcyBlueBorder, RoundedCornerShape(50)) else Modifier)}

@Composable
fun Modifier.scaleFocusEffect(isFocused: Boolean): Modifier {
    val scale by animateFloatAsState(        targetValue = if (isFocused) 1.08f else 1.0f,        animationSpec = FocusSpringSpec, label = "scaleAnim"    )
return this.graphicsLayer { scaleX = scale
scaleY = scale }}

@Composable
fun Modifier.pulseFocusEffect(isFocused: Boolean): Modifier {
    var pulseScale by remember { mutableFloatStateOf(1.0f) }    LaunchedEffect(isFocused) {
if (isFocused) {
while (true) {                pulseScale = 1.12f
delay(400)                pulseScale = 1.08f
delay(400)            }
} else { pulseScale = 1.0f }    }

val scale by animateFloatAsState(targetValue = pulseScale, animationSpec = SpringSpec, label = "pulse")
return this.graphicsLayer { scaleX = scale
scaleY = scale }}

@Composable
fun Modifier.breatheFocusEffect(isFocused: Boolean): Modifier {
    var target by remember { mutableFloatStateOf(1.0f) }    LaunchedEffect(isFocused) {
if (isFocused) {
while (true) { target = 1.08f
delay(800)
target = 1.0f
delay(800) }
} else { target = 1.0f }    }

val scale by animateFloatAsState(targetValue = target, animationSpec = BreatheSpec, label = "breathe")
return this.graphicsLayer { scaleX = scale
scaleY = scale }}

@Composable
fun Modifier.navigationPillFocusEffect(isFocused: Boolean, effect: String): Modifier = when (effect) {    "glow"    -> this.glowFocusEffect(isFocused)    "scale"   -> this.scaleFocusEffect(isFocused)    "pulse"   -> this.pulseFocusEffect(isFocused)    "breathe" -> this.breatheFocusEffect(isFocused)
else      -> this}