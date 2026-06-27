package com.sanin.tv.settings
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.Anilist.activityMergeTimeMap
import com.sanin.tv.connections.anilist.Anilist.rowOrderMap
import com.sanin.tv.connections.anilist.Anilist.scoreFormats
import com.sanin.tv.connections.anilist.Anilist.staffNameLang
import com.sanin.tv.connections.anilist.Anilist.titleLang
import com.sanin.tv.connections.anilist.AnilistMutations
import com.sanin.tv.connections.anilist.api.ScoreFormat
import com.sanin.tv.connections.anilist.api.UserStaffNameLanguage
import com.sanin.tv.connections.anilist.api.UserTitleLanguage
import com.sanin.tv.databinding.ActivitySettingsAnilistBinding
import com.sanin.tv.initActivity
import com.sanin.tv.navBarHeight
import com.sanin.tv.restartApp
import com.sanin.tv.statusBarHeight
import com.sanin.tv.themes.ThemeManager
import com.sanin.tv.toast
import com.sanin.tv.util.customAlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
class AnilistSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsAnilistBinding    
private lateinit var anilistMutations: AnilistMutations    
override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)        ThemeManager(this).applyTheme()        initActivity(this)        
val context = this        binding = ActivitySettingsAnilistBinding.inflate(layoutInflater)        setContentView(binding.root)        anilistMutations = AnilistMutations()        binding.apply {            settingsAnilistLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {                topMargin = statusBarHeight                bottomMargin = navBarHeight            }            binding.anilistSettingsBack.setOnClickListener {                onBackPressedDispatcher.onBackPressed()            }

val currentTitleLang = Anilist.titleLanguage
val titleFormat = UserTitleLanguage.entries.firstOrNull { it.name == currentTitleLang }                ?: UserTitleLanguage.ENGLISH            settingsAnilistTitleLanguage.setText(titleLang[titleFormat.ordinal])            settingsAnilistTitleLanguage.setAdapter(                ArrayAdapter(context, R.layout.item_dropdown, titleLang)            )            settingsAnilistTitleLanguage.setOnItemClickListener { _, _, i, _ ->                
val selectedLanguage = when (i) {                    0 -> "ENGLISH"                    1 -> "ROMAJI"                    2 -> "NATIVE"
else -> "ENGLISH"                }                lifecycleScope.launch {                    anilistMutations.updateSettings(titleLanguage = selectedLanguage)                    Anilist.titleLanguage = selectedLanguage                    restartApp()                }                settingsAnilistTitleLanguage.clearFocus()            }

val currentStaffNameLang = Anilist.staffNameLanguage
val staffNameFormat =                UserStaffNameLanguage.entries.firstOrNull { it.name == currentStaffNameLang }                    ?: UserStaffNameLanguage.ROMAJI_WESTERN            settingsAnilistStaffLanguage.setText(staffNameLang[staffNameFormat.ordinal])            settingsAnilistStaffLanguage.setAdapter(                ArrayAdapter(context, R.layout.item_dropdown, staffNameLang)            )            settingsAnilistStaffLanguage.setOnItemClickListener { _, _, i, _ ->                
val selectedLanguage = when (i) {                    0 -> "ROMAJI_WESTERN"                    1 -> "ROMAJI"                    2 -> "NATIVE"
else -> "ROMAJI_WESTERN"                }                lifecycleScope.launch {                    anilistMutations.updateSettings(staffNameLanguage = selectedLanguage)                    Anilist.staffNameLanguage = selectedLanguage                    restartApp()                }                settingsAnilistStaffLanguage.clearFocus()            }

val currentMergeTimeDisplay =                activityMergeTimeMap.entries.firstOrNull { it.value == Anilist.activityMergeTime }?.key                    ?: "${Anilist.activityMergeTime} mins"            settingsAnilistActivityMergeTime.setText(currentMergeTimeDisplay)            settingsAnilistActivityMergeTime.setAdapter(                ArrayAdapter(context, R.layout.item_dropdown, activityMergeTimeMap.keys.toList())            )            settingsAnilistActivityMergeTime.setOnItemClickListener { _, _, i, _ ->                
val selectedDisplayTime = activityMergeTimeMap.keys.toList()[i]                
val selectedApiTime = activityMergeTimeMap[selectedDisplayTime] ?: 0                lifecycleScope.launch {                    anilistMutations.updateSettings(activityMergeTime = selectedApiTime)                    Anilist.activityMergeTime = selectedApiTime                    restartApp()                }                settingsAnilistActivityMergeTime.clearFocus()            }

val currentScoreFormat = Anilist.scoreFormat
val scoreFormat = ScoreFormat.entries.firstOrNull { it.name == currentScoreFormat }                ?: ScoreFormat.POINT_100            settingsAnilistScoreFormat.setText(scoreFormats[scoreFormat.ordinal])            settingsAnilistScoreFormat.setAdapter(                ArrayAdapter(context, R.layout.item_dropdown, scoreFormats)            )            settingsAnilistScoreFormat.setOnItemClickListener { _, _, i, _ ->                
val selectedFormat = when (i) {                    0 -> "POINT_100"                    1 -> "POINT_10_DECIMAL"                    2 -> "POINT_10"                    3 -> "POINT_5"                    4 -> "POINT_3"
else -> "POINT_100"                }                lifecycleScope.launch {                    anilistMutations.updateSettings(scoreFormat = selectedFormat)                    Anilist.scoreFormat = selectedFormat                    restartApp()                }                settingsAnilistScoreFormat.clearFocus()            }

val currentRowOrder =                rowOrderMap.entries.firstOrNull { it.value == Anilist.rowOrder }?.key ?: "Score"            settingsAnilistRowOrder.setText(currentRowOrder)            settingsAnilistRowOrder.setAdapter(                ArrayAdapter(context, R.layout.item_dropdown, rowOrderMap.keys.toList())            )            settingsAnilistRowOrder.setOnItemClickListener { _, _, i, _ ->                
val selectedDisplayOrder = rowOrderMap.keys.toList()[i]                
val selectedApiOrder = rowOrderMap[selectedDisplayOrder] ?: "score"                lifecycleScope.launch {                    anilistMutations.updateSettings(rowOrder = selectedApiOrder)                    Anilist.rowOrder = selectedApiOrder                    restartApp()                }

val displayAdultContent = Anilist.adult
val airingNotifications = Anilist.airingNotifications            binding.settingsRecyclerView1.adapter = SettingsAdapter(                arrayListOf(                    Settings(                        type = 2,                        name = getString(R.string.airing_notifications),                        desc = getString(R.string.airing_notifications_desc),                        icon = R.drawable.ic_round_notifications_active_24,                        isChecked = airingNotifications,                        switch = { isChecked, _ ->                            lifecycleScope.launch {                                anilistMutations.updateSettings(airingNotifications = isChecked)                                Anilist.airingNotifications = isChecked                                restartApp()                            }                        }                    ),                    Settings(                        type = 2,                        name = getString(R.string.display_adult_content),                        desc = getString(R.string.display_adult_content_desc),                        icon = R.drawable.ic_round_nsfw_24,                        isChecked = displayAdultContent,                        switch = { isChecked, _ ->                            lifecycleScope.launch {                                anilistMutations.updateSettings(displayAdultContent = isChecked)                                Anilist.adult = isChecked                                restartApp()                            }                        }                    ),                )            )            binding.settingsRecyclerView1.layoutManager =                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)        }
}}}
