package com.sanin.tv.media
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.App.Companion.context
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.AnilistSearch.SearchType
import com.sanin.tv.connections.anilist.AnilistSearch.SearchType.Companion.toAnilistString
import com.sanin.tv.connections.anilist.SearchResults
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
class SupportingSearchAdapter(
private val activity: SearchActivity, 
private val type: SearchType) :    HeaderInterface() {    
@SuppressLint("ClickableViewAccessibility")    
override fun onBindViewHolder(holder: SearchHeaderViewHolder, position: Int) {        binding = holder.binding        searchHistoryAdapter = SearchHistoryAdapter(type) {            binding.searchBarText.setText(it)            binding.searchBarText.setSelection(it.length)        }        binding.searchHistoryList.layoutManager = LinearLayoutManager(binding.root.context)        binding.searchHistoryList.adapter = searchHistoryAdapter
val imm: InputMethodManager =            activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager        }        binding.clearHistory.setOnClickListener {            it.startAnimation(fadeOutAnimation())            it.visibility = View.GONE            searchHistoryAdapter.clearHistory()        }        updateClearHistoryVisibility()        
fun searchTitle() {
    val searchText = binding.searchBarText.text.toString().takeIf { it.isNotEmpty() }

val result: SearchResults<*> = when (type) {                SearchType.CHARACTER -> activity.characterResult                SearchType.STUDIO -> activity.studioResult                SearchType.STAFF -> activity.staffResult                SearchType.USER -> activity.userResult
else -> throw IllegalArgumentException("Invalid search type")            }            result.search = searchText            activity.search()        }        textWatcher = 
object : TextWatcher {
    override fun afterTextChanged(s: Editable) {}

override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
if (s.toString().isBlank()) {                    activity.emptyMediaAdapter()                    CoroutineScope(Dispatchers.IO).launch {                        delay(200)                        activity.runOnUiThread {                            setHistoryVisibility(true)                        }                    }
} else {                    setHistoryVisibility(false)                    searchTitle()                }            }        }        binding.searchBarText.addTextChangedListener(textWatcher)        binding.searchBarText.setOnEditorActionListener { _, actionId, _ ->            return@setOnEditorActionListener when (actionId) {                EditorInfo.IME_ACTION_SEARCH -> {                    searchTitle()                    binding.searchBarText.clearFocus()                    imm.hideSoftInputFromWindow(binding.searchBarText.windowToken, 0)                    true
}
else -> false            }        }        binding.searchBar.setEndIconOnClickListener { searchTitle() }        search = Runnable { searchTitle() }        requestFocus = Runnable { binding.searchBarText.requestFocus() }    }
