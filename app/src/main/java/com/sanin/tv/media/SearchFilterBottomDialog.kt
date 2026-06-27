package com.sanin.tv.media
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.sanin.tv.BottomSheetDialogFragment
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.databinding.BottomSheetSearchFilterBinding
import com.sanin.tv.databinding.ItemChipBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
class SearchFilterBottomDialog : BottomSheetDialogFragment() {
    private var _binding: BottomSheetSearchFilterBinding? = null    
private val binding get() = _binding!!    
private lateinit var activity: SearchActivity    
override fun onCreateView(        inflater: LayoutInflater,        container: ViewGroup?,        savedInstanceState: Bundle?    ): View {        _binding = BottomSheetSearchFilterBinding.inflate(inflater, container, false)        RecyclerView.Adapter<FilterChipAdapter.SearchChipViewHolder>() {        inner 
class SearchChipViewHolder(
val binding: ItemChipBinding) :            RecyclerView.ViewHolder(binding.root)        
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchChipViewHolder {
    val binding =                ItemChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
return SearchChipViewHolder(binding)        }

override fun onBindViewHolder(holder: SearchChipViewHolder, position: Int) {
    val title = list[position]            holder.setIsRecyclable(false)            holder.binding.root.apply {                text = title                isCheckable = true                perform.invoke(this)            }        }

override fun getItemCount(): Int = list.size    }

override fun onDestroy() {        _binding = null        super.onDestroy()    }

companion object {
    fun newInstance() = SearchFilterBottomDialog()    }}
}
