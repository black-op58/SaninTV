package com.sanin.tv.media
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.sanin.tv.App.Companion.context
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.AnilistSearch.SearchType
import com.sanin.tv.databinding.ItemChipBinding
import com.sanin.tv.openLinkInBrowser
import com.sanin.tv.others.imagesearch.ImageSearchActivity
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.google.android.material.checkbox.MaterialCheckBox.STATE_CHECKED
import com.google.android.material.checkbox.MaterialCheckBox.STATE_INDETERMINATE
import com.google.android.material.checkbox.MaterialCheckBox.STATE_UNCHECKED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
class SearchAdapter(
private val activity: SearchActivity, 
private val type: SearchType) :    HeaderInterface() {
    private fun updateFilterTextViewDrawable() {
if (s.toString().isBlank()) {                    activity.emptyMediaAdapter()                    CoroutineScope(Dispatchers.IO).launch {                        delay(200)                        activity.runOnUiThread {                            setHistoryVisibility(true)                        }                    }
} else {                    setHistoryVisibility(false)                    searchTitle()                }            }        }        binding.searchBarText.addTextChangedListener(textWatcher)        binding.searchBarText.setOnEditorActionListener { _, actionId, _ ->            return@setOnEditorActionListener when (actionId) {                EditorInfo.IME_ACTION_SEARCH -> {                    searchTitle()                    binding.searchBarText.clearFocus()                    imm.hideSoftInputFromWindow(binding.searchBarText.windowToken, 0)                    true
}
else -> false            }        }        binding.searchBar.setEndIconOnClickListener { searchTitle() }        binding.searchResultGrid.setOnClickListener {            it.alpha = 1f            binding.searchResultList.alpha = 0.33f            activity.style = 0            PrefManager.setVal(PrefName.SearchStyle, 0)            activity.recycler()        }        binding.searchResultList.setOnClickListener {            it.alpha = 1f            binding.searchResultGrid.alpha = 0.33f            activity.style = 1            PrefManager.setVal(PrefName.SearchStyle, 1)            activity.recycler()        }
if (Anilist.adult) {            binding.searchAdultCheck.visibility = View.VISIBLE            binding.searchAdultCheck.isChecked = adult            binding.searchAdultCheck.setOnCheckedChangeListener { _, b ->                adult = b                searchTitle()            }
} else binding.searchAdultCheck.visibility = View.GONE        binding.searchList.apply {
if (Anilist.userid != null) {                visibility = View.VISIBLE                checkedState = when (listOnly) {                    null -> STATE_UNCHECKED                    true -> STATE_CHECKED                    false -> STATE_INDETERMINATE                }                addOnCheckedStateChangedListener { _, state ->                    listOnly = when (state) {                        STATE_CHECKED -> true                        STATE_INDETERMINATE -> false                        STATE_UNCHECKED -> null
else -> null                    }                }                setOnTouchListener { _, event ->                    (event.actionMasked == MotionEvent.ACTION_DOWN).also {
if (it) checkedState = (checkedState + 1) % 3                        searchTitle()                    }                }
} else visibility = View.GONE        }

override fun getItemCount(): Int = chips.size
