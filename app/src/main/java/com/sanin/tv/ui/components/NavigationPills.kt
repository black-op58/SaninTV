package com.sanin.tv.ui.components

import android.content.Intent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanin.tv.R
import com.sanin.tv.feature_navigation.NavigationPillsViewModel
import com.sanin.tv.profile.notification.NotificationActivity

// ─── Main Tab Order & Glyphs (guaranteed order: Home → Anime → Search → Library) ───
private val MAIN_TAB_ORDER = listOf("home", "anime", "search", "library")
private val MAIN_TAB_GLYPHS = mapOf(
    "home"    to "\u25EC",
    "anime"   to "\u25C7",
    "search"  to "\u2315",
    "library" to "\u229E"
)
private val MAIN_TAB_LABELS = mapOf(
    "home"    to "Home",
    "anime"   to "Anime",
    "search"  to "Search",
    "library" to "Library"
)

// ─── Context Action Order & Glyphs ───
private val CONTEXT_TAB_ORDER = listOf("back", "details", "watch", "comments")
private val CONTEXT_GLYPHS = mapOf(
    "back"     to "\u2039",
    "details"  to "\u25CE",
    "watch"    to "\u25B7",
    "comments" to "\u25CC"
)
private val CONTEXT_LABELS = mapOf(
    "back"     to "Back",
    "details"  to "Details",
    "watch"    to "Watch",
    "comments" to "Comments"
)

@Composable
fun NavigationPills(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: NavigationPillsViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsState()
    val isExpanded by viewModel.isExpanded.collectAsState()
    val notificationCount by viewModel.notificationCount.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val context = LocalContext.current

    val isMain = currentRoute == "home" || currentRoute == "anime" ||
            currentRoute == "search" || currentRoute == "library"

    val pillHeight by animateDpAsState(
        targetValue = if (isExpanded) 64.dp else 48.dp,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessVeryLow),
        label = "pillHeight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(pillHeight)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabOrder = if (isMain) MAIN_TAB_ORDER else CONTEXT_TAB_ORDER
            val glyphs   = if (isMain) MAIN_TAB_GLYPHS else CONTEXT_GLYPHS
            val labels   = if (isMain) MAIN_TAB_LABELS else CONTEXT_LABELS

            tabOrder.forEach { tab ->
                NavigationPill(
                    glyph      = glyphs[tab] ?: "",
                    label      = labels[tab] ?: "",
                    isActive   = currentRoute == tab,
                    isExpanded = isExpanded,
                    focusEffect = settings.focusEffect,
                    onClick    = { if (tab == "back") onBack() else onNavigate(tab) }
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // ── Notification bell with badge count ────────────────────────
            var notifFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .focusable()
                    .onFocusChanged { notifFocused = it.isFocused }
                    .navigationPillFocusEffect(notifFocused, settings.focusEffect)
                    .border(
                        width = if (notifFocused) 2.dp else 0.dp,
                        color = if (notifFocused) Color(0xFF87CEEB).copy(alpha = 0.8f)
                                else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable {
                        context.startActivity(
                            Intent(context, NotificationActivity::class.java)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_notifications_24),
                    contentDescription = "Notifications",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(21.dp)
                )
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.TopEnd)
                            .background(Color(0xFFE53935), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                            color = Color.White,
                            fontSize = 7.sp,
                            lineHeight = 7.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // ── Avatar button: tapping opens the right side rail ──────────
            AvatarButton(
                avatarUrl = avatarUrl,
                onClick   = { viewModel.showSideRail() }
            )
        }
    }
}

@Composable
fun NavigationPill(
    glyph: String,
    label: String,
    isActive: Boolean,
    isExpanded: Boolean,
    focusEffect: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val pillWidth by animateDpAsState(
        targetValue = if (isExpanded) 88.dp else 48.dp,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessVeryLow),
        label = "pillWidth"
    )

    Box(
        modifier = Modifier
            .width(pillWidth)
            .fillMaxHeight()
            .background(
                color = if (isActive) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(50)
            )
            .border(
                width = if (isActive) 1.dp else 0.dp,
                color = if (isActive) Color(0xFF87CEEB).copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(50)
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .navigationPillFocusEffect(isFocused, focusEffect)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isExpanded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = glyph,
                    color = if (isActive) Color(0xFF87CEEB) else Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                Text(
                    text = label,
                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = glyph,
                color = if (isActive) Color(0xFF87CEEB) else Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp
            )
        }
    }
}
