package com.sanin.tv.media.user
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.sanin.tv.R
import com.sanin.tv.Refresh
import com.sanin.tv.databinding.ActivityListBinding
import com.sanin.tv.getThemeColor
import com.sanin.tv.hideSystemBarsExtendView
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.statusBarHeight
import com.sanin.tv.themes.ThemeManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding    
private val scope = lifecycleScope    
private var selectedTabIdx = 0    
override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)            
override fun onTabUnselected(tab: TabLayout.Tab?) {}

override fun onTabReselected(tab: TabLayout.Tab?) {}        })        
val model: ListViewModel by viewModels()        model.getLists().observe(this) {
    val defaultKeys = listOf(                "Reading",                "Watching",                "Completed",                "Paused",                "Dropped",                "Planning",                "Favourites",                "Rewatching",                "Rereading",                "All"            )            
val userKeys: Array<String> = resources.getStringArray(R.array.keys)
if (it != null) {                binding.listProgressBar.visibility = View.GONE                binding.listViewPager.adapter = ListViewPagerAdapter(it.size, false, this)                
val keys = it.keys.toList()                    .map { key -> userKeys.getOrNull(defaultKeys.indexOf(key)) ?: key }

val values = it.values.toList()                
val savedTab = this.selectedTabIdx                TabLayoutMediator(binding.listTabLayout, binding.listViewPager) { tab, position ->                    tab.text = "${keys[position]} (${values[position].size})"                }.attach()                binding.listViewPager.setCurrentItem(savedTab, false)            }        }

val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(true) }        live.observe(this) {
if (it) {                scope.launch {                    withContext(Dispatchers.IO) {                        model.loadLists(            popup.show()        }        binding.filter.setOnClickListener {
    val genres =                PrefManager.getVal<Set<String>>(PrefName.GenresList).toMutableSet().sorted()            
val popup = PopupMenu(this, it)            popup.menu.add("All")            genres.forEach { genre ->                popup.menu.add(genre)            }            popup.setOnMenuItemClickListener { menuItem ->                
val selectedGenre = menuItem.title.toString()                model.filterLists(selectedGenre)                true            }            popup.show()        }        binding.random.setOnClickListener {            //get the current tab
val currentTab =                binding.listTabLayout.getTabAt(binding.listTabLayout.selectedTabPosition)            
val currentFragment =                supportFragmentManager.findFragmentByTag("f" + currentTab?.position.toString()) as? ListFragment            currentFragment?.randomOptionClick()        }        binding.search.setOnClickListener {            toggleSearchView(binding.searchView.isVisible)
if (!binding.searchView.isVisible) {                model.unfilterLists()            }        }        binding.searchViewText.addTextChangedListener {            model.searchLists(binding.searchViewText.text.toString())        }    }

private fun toggleSearchView(isVisible: Boolean) {
if (isVisible) {            binding.searchView.visibility = View.GONE            binding.searchViewText.text.clear()
} else {            binding.searchView.visibility = View.VISIBLE            binding.searchViewText.requestFocus()            
val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager            imm.showSoftInput(binding.searchViewText, InputMethodManager.SHOW_IMPLICIT)        }    }}
}}
