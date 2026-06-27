package com.sanin.tv.settings
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.sanin.tv.R
import com.sanin.tv.connections.crashlytics.CrashlyticsInterface
import com.sanin.tv.databinding.FragmentExtensionsBinding
import com.sanin.tv.others.LanguageMapper.Companion.getLanguageName
import com.sanin.tv.parsers.AnimeSources
import com.sanin.tv.settings.extensionprefs.AnimeSourcePreferencesFragment
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.snackString
import com.sanin.tv.util.Logger
import com.sanin.tv.util.customAlertDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.extension.anime.AnimeExtensionManager
import eu.kanade.tachiyomi.extension.anime.model.AnimeExtension
import kotlinx.coroutines.launch
import rx.android.schedulers.AndroidSchedulers
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.Locale
class InstalledAnimeExtensionsFragment : Fragment(), SearchQueryHandler {
    private var _binding: FragmentExtensionsBinding? = null    
private val binding get() = _binding!!    
private lateinit var extensionsRecyclerView: RecyclerView    
private val skipIcons: Boolean = PrefManager.getVal(PrefName.SkipExtensionIcons)    
private val animeExtensionManager: AnimeExtensionManager = Injekt.get()    
private val extensionsAdapter = AnimeExtensionsAdapter(        { pkg ->            
val name = pkg.name
val changeUIVisibility: (Boolean) -> Unit = { show ->                
val activity = requireActivity() as ExtensionsActivity                activity.findViewById<ViewPager2>(R.id.viewPager).isVisible = show                activity.findViewById<TabLayout>(R.id.tabLayout).isVisible = show                activity.findViewById<TextInputLayout>(R.id.searchView).isVisible = show                activity.findViewById<ImageView>(R.id.languageselect).isVisible = show                activity.findViewById<TextView>(R.id.extensions).text =
if (show) getString(R.string.extensions) else name                activity.findViewById<FrameLayout>(R.id.fragmentExtensionsContainer).isGone = show            }

var itemSelected = false
val allSettings = pkg.sources.filterIsInstance<ConfigurableAnimeSource>()
if (allSettings.isNotEmpty()) {
    var selectedSetting = allSettings[0]
if (allSettings.size > 1) {
    val names = allSettings.map { getLanguageName(it.lang) }                        .toTypedArray()                    
var selectedIndex = 0                    requireContext().customAlertDialog().apply {                        setTitle("Select a Source")                        singleChoiceItems(names, selectedIndex) { which ->                            itemSelected = true                            selectedIndex = which                            selectedSetting = allSettings[selectedIndex]                            
val fragment =                                AnimeSourcePreferencesFragment().getInstance(selectedSetting.id) {                                    changeUIVisibility(true)                                }                            parentFragmentManager.beginTransaction()                                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down)                                .replace(R.id.fragmentExtensionsContainer, fragment)                                .addToBackStack(null)                                .commit()                        }                        onDismiss {
if (!itemSelected) {                                changeUIVisibility(true)                            }                        }                        show()                    }
} else {                    // If there's only one setting, proceed with the fragment transaction
val fragment =                        AnimeSourcePreferencesFragment().getInstance(selectedSetting.id) {                            changeUIVisibility(true)                        }                    parentFragmentManager.beginTransaction()                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down)                        .replace(R.id.fragmentExtensionsContainer, fragment)                        .addToBackStack(null)                        .commit()                }                // Hide ViewPager2 and TabLayout                changeUIVisibility(false)
} else {                Toast.makeText(requireContext(), "Source is not configurable", Toast.LENGTH_SHORT)                    .show()            }        },                
val fromPosition = viewHolder.absoluteAdapterPosition
val toPosition = target.absoluteAdapterPosition
val newList = extensionsAdapter.currentList.toMutableList().apply {                    add(toPosition, removeAt(fromPosition))                }                extensionsAdapter.submitList(newList)
return true            }

override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {                super.onSelectedChanged(viewHolder, actionState)
if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {                    viewHolder?.itemView?.elevation = 8f                    viewHolder?.itemView?.translationZ = 8f                }            }

override fun clearView(                recyclerView: RecyclerView,                viewHolder: RecyclerView.ViewHolder            ) {                super.clearView(recyclerView, viewHolder)                extensionsAdapter.updatePref()                viewHolder.itemView.elevation = 0f                viewHolder.itemView.translationZ = 0f            }        }        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(extensionsRecyclerView)        lifecycleScope.launch {            animeExtensionManager.installedExtensionsFlow.collect { extensions ->                extensionsAdapter.updateData(sortToAnimeSourcesList(extensions))            }        }
return binding.root    }

private fun sortToAnimeSourcesList(inpt: List<AnimeExtension.Installed>): List<AnimeExtension.Installed> {
    val sourcesMap = inpt.associateBy { it.name }

val orderedSources = AnimeSources.pinnedAnimeSources.mapNotNull { name ->            sourcesMap[name]        }
return orderedSources + inpt.filter { !AnimeSources.pinnedAnimeSources.contains(it.name) }    }

override fun onDestroyView() {        super.onDestroyView()
_binding = null    }

override fun updateContentBasedOnQuery(query: String?) {        extensionsAdapter.filter(            query ?: "",            sortToAnimeSourcesList(animeExtensionManager.installedExtensionsFlow.value)        )    }

override fun notifyDataChanged() { // Do nothing    }

private class AnimeExtensionsAdapter(        
private val onSettingsClicked: (AnimeExtension.Installed) -> Unit,        
private val onUninstallClicked: (AnimeExtension.Installed) -> Unit,        
private val onUpdateClicked: (AnimeExtension.Installed) -> Unit,        
val skipIcons: Boolean    ) : ListAdapter<AnimeExtension.Installed, AnimeExtensionsAdapter.ViewHolder>(        DIFF_CALLBACK_INSTALLED    ) {
    fun updateData(newExtensions: List<AnimeExtension.Installed>) {            submitList(newExtensions)        }

fun updatePref() {
    val map = currentList.map { it.name }            PrefManager.setVal(PrefName.AnimeSourcesOrder, map)            AnimeSources.pinnedAnimeSources = map            AnimeSources.performReorderAnimeSources()        }

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)                .inflate(R.layout.item_extension, parent, false)
return ViewHolder(view)        }

override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val extension = getItem(position)            
val nsfw = if (extension.isNsfw) "(18+)" else ""            
val lang = getLanguageName(extension.lang)            holder.extensionNameTextView.text = extension.name
val versionText = "$lang ${extension.versionName} $nsfw"            holder.extensionVersionTextView.text = versionText
if (!skipIcons) {                holder.extensionIconImageView.setImageDrawable(extension.icon)            }
if (extension.hasUpdate) {                holder.updateView.isVisible = true
} else {                holder.updateView.isVisible = false            }            holder.deleteView.setOnClickListener {                onUninstallClicked(extension)            }            holder.updateView.setOnClickListener {                onUpdateClicked(extension)            }            holder.settingsImageView.setOnClickListener {                onSettingsClicked(extension)            }        }

fun filter(query: String, currentList: List<AnimeExtension.Installed>) {
    val filteredList = ArrayList<AnimeExtension.Installed>()
for (extension in currentList) {
if (extension.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))) {                    filteredList.add(extension)                }            }
if (filteredList != currentList)                submitList(filteredList)        }        inner 
class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val extensionNameTextView: TextView = view.findViewById(R.id.extensionNameTextView)            
val extensionVersionTextView: TextView =                view.findViewById(R.id.extensionVersionTextView)            
val settingsImageView: ImageView = view.findViewById(R.id.settingsImageView)            
val extensionIconImageView: ImageView = view.findViewById(R.id.extensionIconImageView)            
val deleteView: ImageView = view.findViewById(R.id.deleteTextView)            
val updateView: ImageView = view.findViewById(R.id.updateTextView)        }

companion object {
    val DIFF_CALLBACK_INSTALLED =                
object : DiffUtil.ItemCallback<AnimeExtension.Installed>() {
    override fun areItemsTheSame(                        oldItem: AnimeExtension.Installed,                        newItem: AnimeExtension.Installed                    ): Boolean {
return oldItem.pkgName == newItem.pkgName                    }

override fun areContentsTheSame(                        oldItem: AnimeExtension.Installed,                        newItem: AnimeExtension.Installed                    ): Boolean {
return oldItem == newItem                    }                }        }    }}

class InstalledAnimeExtensionsFragment : androidx.fragment.app.Fragment() {

private fun setupDiffCallback() {

val callback = 
object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Any>() {

override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
}
})
