package com.sanin.tv.ui.components

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.profile.ProfileActivity
import com.sanin.tv.profile.notification.NotificationActivity
import com.sanin.tv.settings.SettingsActivity
import com.sanin.tv.settings.SettingsExtensionsActivity

@Composable
fun RightSideRail(
    visible: Boolean,
    avatarUrl: String?,
    username: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(220.dp)
                        .background(
                            color = Color(0xFF0D0D1A),
                            shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                        .padding(top = 64.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        AvatarButton(avatarUrl = avatarUrl, onClick = {})
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = username ?: "Guest",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    Spacer(modifier = Modifier.height(12.dp))

                    SideRailItem(
                        iconRes = R.drawable.ic_round_notifications_24,
                        label = "Notifications"
                    ) {
                        onDismiss()
                        context.startActivity(
                            Intent(context, NotificationActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }

                    SideRailItem(
                        iconRes = R.drawable.ic_round_person_24,
                        label = "Profile"
                    ) {
                        onDismiss()
                        val userId = Anilist.userid ?: -1
                        context.startActivity(
                            Intent(context, ProfileActivity::class.java)
                                .putExtra("userId", userId)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }

                    SideRailItem(
                        iconRes = R.drawable.ic_round_extension_24,
                        label = "Extensions"
                    ) {
                        onDismiss()
                        context.startActivity(
                            Intent(context, SettingsExtensionsActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }

                    SideRailItem(
                        iconRes = R.drawable.ic_round_manage_accounts_24,
                        label = "Settings"
                    ) {
                        onDismiss()
                        context.startActivity(
                            Intent(context, SettingsActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SideRailItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = Color(0xFF87CEEB),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
