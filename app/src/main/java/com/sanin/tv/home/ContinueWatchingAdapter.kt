package com.sanin.tv.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sanin.tv.R
import com.sanin.tv.media.MediaDetailsActivity
import com.sanin.tv.toPx

class ContinueWatchingAdapter :
    ListAdapter<WatchEntry, ContinueWatchingAdapter.VH>(DIFF) {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val cover: ImageView = itemView.findViewById(R.id.cwCover)
        val title: TextView = itemView.findViewById(R.id.cwTitle)
        val episode: TextView = itemView.findViewById(R.id.cwEpisode)
        val progress: ProgressBar = itemView.findViewById(R.id.cwProgressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_continue_watching, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
    val entry = getItem(position)

        holder.title.text = entry.mediaTitle
val epLabel = buildString {
            append("Ep ")
            append(entry.episodeNumber)
            if (!entry.episodeTitle.isNullOrBlank() &&
                !entry.episodeTitle.equals(entry.episodeNumber)
            ) {
                append(" · ")
                append(entry.episodeTitle)
            }
        }
        holder.episode.text = epLabel
val pct = (entry.progressFraction * 100f).toInt().coerceIn(0, 100)
        holder.progress.progress = pct

        Glide.with(holder.cover)
            .load(entry.mediaCover)
            .transform(RoundedCorners(8.toPx))
            .into(holder.cover)

        holder.itemView.setOnClickListener {
    val ctx = it.context
val intent = Intent(ctx, MediaDetailsActivity::class.java).apply {
                putExtra("mediaId", entry.mediaId)
                putExtra("anime", true)
            }
            ctx.startActivity(intent)
        }
    }

    companion object {
    private val DIFF = object : DiffUtil.ItemCallback<WatchEntry>() {
    override fun areItemsTheSame(a: WatchEntry, b: WatchEntry) =
                a.mediaId == b.mediaId

            override fun areContentsTheSame(a: WatchEntry, b: WatchEntry) =
                a == b
        }
    }
}
