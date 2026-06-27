package com.sanin.tv.settings
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.R
import com.sanin.tv.copyToClipboard
import com.sanin.tv.databinding.ActivitySettingsExtensionsBinding
import com.sanin.tv.databinding.DialogUserAgentBinding
import com.sanin.tv.databinding.ItemRepositoryBinding
import com.sanin.tv.initActivity
import com.sanin.tv.media.MediaType
import com.sanin.tv.navBarHeight
import com.sanin.tv.parsers.ParserTestActivity
import com.sanin.tv.restartApp
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.statusBarHeight
import com.sanin.tv.themes.ThemeManager
import com.sanin.tv.util.customAlertDialog
import eu.kanade.domain.base.BasePreferences
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
class SettingsExtensionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsExtensionsBinding    
private val extensionInstaller = Injekt.get<BasePreferences>().extensionInstaller()    
override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)        ThemeManager(this).applyTheme()        initActivity(this
val context = this                        name = getString(R.string.extension_test),                        desc = getString(R.string.extension_test_desc),                        icon = R.drawable.ic_round_search_sources_24,                        isActivity = true,                        onClick = {                            ContextCompat.startActivity(                                context,                                Intent(context, ParserTestActivity::class.java),                                null                            )                        }                    ),                    Settings(                        type = 1,                        name = getString(R.string.user_agent),                        desc = getString(R.string.user_agent_desc),                        icon = R.drawable.ic_round_video_settings_24,                        onClick = {
    val dialogView = DialogUserAgentBinding.inflate(layoutInflater
val editText = dialogView.userAgentTextBox                            editText.setText(PrefManager.getVal<String>(PrefName.DefaultUserAgent))                            context.customAlertDialog().apply {                                setTitle(R.string.user_agent)                                setCustomView(dialogView.root)                                setPosButton(R.string.ok) {                                    PrefManager.setVal(                                        PrefName.DefaultUserAgent,                                        editText.text.toString()                                    )                                }                                setNeutralButton(R.string.reset) {                                    PrefManager.removeVal(PrefName.DefaultUserAgent)                                    editText.setText("")                                }                                setNegButton(R.string.cancel)                            }.show()                        }                    ),                    Settings(                        type = 2,                        name = getString(R.string.proxy),                        desc = getString(R.string.proxy_desc),                        icon = R.drawable.swap_horizontal_circle_24,                        isChecked = PrefManager.getVal(PrefName.EnableSocks5Proxy),                        switch = { isChecked, _ ->                            PrefManager.setVal(PrefName.EnableSocks5Proxy, isChecked)                            restartApp()                        }                    ),                    Settings(                        type = 1,                        name = getString(R.string.proxy_setup),                        desc = getString(R.string.proxy_setup_desc),                        icon = R.drawable.lan_24,                        onClick = {                            ProxyDialogFragment().show(supportFragmentManager, "dialog")                        }                    ),                    Settings(                        type = 2,                        name = getString(R.string.force_legacy_installer),                        desc = getString(R.string.force_legacy_installer_desc),                        icon = R.drawable.ic_round_new_releases_24,                        isChecked = extensionInstaller.get() == BasePreferences.ExtensionInstaller.LEGACY,                        switch = { isChecked, _ ->
if (isChecked) {                                extensionInstaller.set(BasePreferences.ExtensionInstaller.LEGACY
} else {                                extensionInstaller.set(BasePreferences.ExtensionInstaller.PACKAGEINSTALLER)                            }                        }                    ),                    Settings(                        type = 2,                        name = getString(R.string.skip_loading_extension_icons),                        desc = getString(R.string.skip_loading_extension_icons_desc),                        icon = R.drawable.ic_round_no_icon_24,                        isChecked = PrefManager.getVal(PrefName.SkipExtensionIcons),                        switch = { isChecked, _ ->                            PrefManager.setVal(PrefName.SkipExtensionIcons, isChecked)                        }                    ),                    Settings(                        type = 2,                        name = getString(R.string.NSFWExtention),                        desc = getString(R.string.NSFWExtention_desc),                        icon = R.drawable.ic_round_nsfw_24,                        isChecked = PrefManager.getVal(PrefName.NSFWExtension),                        switch = { isChecked, _ ->                            PrefManager.setVal(PrefName.NSFWExtension, isChecked)                        }                    )                )            )            settingsRecyclerView.apply {                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)                setHasFixedSize(true)            }        }    }
