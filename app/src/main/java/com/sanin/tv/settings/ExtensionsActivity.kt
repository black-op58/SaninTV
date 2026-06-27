package com.sanin.tv.settings

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.sanin.tv.R
import com.sanin.tv.databinding.ActivityExtensionsBinding
import com.sanin.tv.initActivity
import com.sanin.tv.media.MediaType
import com.sanin.tv.navBarHeight
import com.sanin.tv.others.AndroidBug5497Workaround
import com.sanin.tv.others.LanguageMapper
import com.sanin.tv.parsers.ParserTestActivity
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.statusBarHeight
import com.sanin.tv.themes.ThemeManager
import com.sanin.tv.util.customAlertDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Locale

class ExtensionsActivity : AppCompatActivity() {
    lateinit var binding: ActivityExtensionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager(this).applyTheme()
        binding = ActivityExtensionsBinding.inflate(layoutInflater)

        binding.addRepoFab.setOnClickListener {
            val type = intent.getSerializableExtra("type") as? MediaType
                ?: MediaType.ANIME
            val repos = when (type) {
                MediaType.NOVEL -> {
                    PrefManager.getVal(PrefName.NovelExtensionRepos)
                }
                else -> emptyList<String>()
            }
            AddRepositoryBottomSheet.newInstance(
                type,
                repos.toList(),
                AddRepositoryBottomSheet::addRepo,
                AddRepositoryBottomSheet::removeRepo

            ).show(supportFragmentManager, "add_repo")
        }
    }
}

interface SearchQueryHandler {
    fun updateContentBasedOnQuery(query: String?)
    fun notifyDataChanged()
}
