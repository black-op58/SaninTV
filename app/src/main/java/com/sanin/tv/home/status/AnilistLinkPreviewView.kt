package com.sanin.tv.home.status
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.sanin.tv.databinding.ItemAnilistLinkPreviewBinding
import com.sanin.tv.loadImage
import com.sanin.tv.media.Media
import com.sanin.tv.media.MediaDetailsActivity
class AnilistLinkPreviewView 
@JvmOverloads constructor(    context: Context,    attrs: AttributeSet? = null,    defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var previewBinding: ItemAnilistLinkPreviewBinding? = null    init {        showEmpty()    }

private fun showEmpty() {        removeAllViews()        previewBinding = null    }

fun setMediaData(media: Media) {        showPreview(media)    }

private fun showPreview(media: Media) {        removeAllViews()        previewBinding = ItemAnilistLinkPreviewBinding.inflate(            LayoutInflater.from(context),            this,            }
if (episodesOrChapters != null) {                previewEpisodes.text = episodesOrChapters                previewEpisodes.isVisible = true
} else {                previewEpisodes.isVisible = false            }            previewCard.setOnClickListener {
    val intent = Intent(context, MediaDetailsActivity::class.java).apply {                    putExtra("mediaId", media.id)
if (context !is android.app.Activity) {                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)                    }                }                context.startActivity(intent)            }        }    }

private fun buildSeasonYearString(media: Media): String {
    val season = media.anime?.season?.lowercase()?.replaceFirstChar { it.uppercase() }

val year = media.anime?.seasonYear ?: media.startDate?.year
return listOfNotNull(season, year).joinToString(" "))
