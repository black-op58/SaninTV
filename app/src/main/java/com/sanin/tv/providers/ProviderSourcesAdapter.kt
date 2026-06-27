package com.sanin.tv.providers

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sanin.tv.databinding.ItemProviderSourceBinding
import java.util.Collections

class ProviderSourcesAdapter(
    private val providers: MutableList<ProviderConfig>,
    private val onChanged: (List<ProviderConfig>) -> Unit,
    private val onEditUrl: (Int, ProviderConfig) -> Unit,
    private val onTestProvider: (ProviderConfig) -> Unit
) : RecyclerView.Adapter<ProviderSourcesAdapter.ViewHolder>() {

    private var touchHelper: ItemTouchHelper? = null

    fun attachTouchHelper(helper: ItemTouchHelper) {
        touchHelper = helper
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProviderSourceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = providers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(providers[position])
    }

    inner class ViewHolder(private val b: ItemProviderSourceBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(config: ProviderConfig) {
            b.providerName.text = config.name
            b.providerUrl.text  = config.baseUrl
            b.providerType.text = config.type.name.replace('_', ' ')

            b.providerEnabled.setOnCheckedChangeListener(null)
            b.providerEnabled.isChecked = config.enabled
            b.providerEnabled.setOnCheckedChangeListener { _, checked ->
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_ID.toInt()) return@setOnCheckedChangeListener
                providers[pos] = providers[pos].copy(enabled = checked)
                onChanged(providers.toList())
            }

            b.providerEditUrl.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_ID.toInt())
                    onEditUrl(pos, providers[pos])
            }

            b.providerTest.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_ID.toInt())
                    onTestProvider(providers[pos])
            }

            b.providerDragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN)
                    touchHelper?.startDrag(this)
                false
            }
        }
    }

    // ── Drag-to-reorder ───────────────────────────────────────────────────────

    fun move(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) Collections.swap(providers, i, i + 1)
        } else {
            for (i in from downTo to + 1) Collections.swap(providers, i, i - 1)
        }
        notifyItemMoved(from, to)
        reindex()
        onChanged(providers.toList())
    }

    // ── Public mutations ──────────────────────────────────────────────────────

    fun updateUrl(position: Int, newUrl: String) {
        providers[position] = providers[position].copy(baseUrl = newUrl)
        notifyItemChanged(position)
        onChanged(providers.toList())
    }

    fun add(config: ProviderConfig) {
        providers.add(config)
        notifyItemInserted(providers.size - 1)
        onChanged(providers.toList())
    }

    fun replaceAll(newList: List<ProviderConfig>) {
        providers.clear()
        providers.addAll(newList)
        notifyDataSetChanged()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun reindex() {
        providers.forEachIndexed { i, p -> providers[i] = p.copy(priority = i) }
    }
}
