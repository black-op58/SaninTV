package com.sanin.tv.media.anime

val params = empty.layoutParams as LinearLayout.LayoutParams        params.weight = 1 - div        empty.layoutParams = params
} else {        cont.visibility = View.GONE    }}

@OptIn(UnstableApi::class)
class EpisodeAdapter(    
private var type: Int,    
private val media: Media,    
private val fragment: AnimeWatchFragment,    
var arr: List<Episode> = arrayListOf(),    
var offlineMode: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val context = fragment.requireContext()    
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
return (when (viewType) {            0 -> EpisodeListViewHolder(                ItemEpisodeListBinding.inflate(                    LayoutInflater.from(parent.context),                    parent,                    false                )            )            1 -> EpisodeGridViewHolder(                ItemEpisodeGridBinding.inflate(                    LayoutInflater.from(parent.context),                    parent,                    false                )            )            2 -> EpisodeCompactViewHolder(                ItemEpisodeCompactBinding.inflate(                    LayoutInflater.from(parent.context),                    parent,                    false                )            )
else -> throw IllegalArgumentException()        })    }

override fun getItemViewType(position: Int): Int {
return type    }

override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val ep = arr[position]        
val title = if (!ep.title.isNullOrEmpty() && ep.title != "null") {            ep.title?.let { MediaNameAdapter.removeEpisodeNumber(it) }
} else {            ep.number        } ?: ""
when (holder) {            is EpisodeListViewHolder -> {
    val binding = holder.binding                setAnimation(fragment.requireContext(), holder.binding.root)                
val thumb = ep.thumb?.let {
if (it.url.isNotEmpty()) {
if (it.url.startsWith("content://") || it.url.startsWith("file://")) {                            it.url
} else {                            GlideUrl(it.url) { it.headers }                        }
} else null                }                Glide.with(binding.itemMediaImage).load(thumb ?: media.cover).override(400, 0)                    .into(binding.itemMediaImage)                binding.itemEpisodeNumber.text = ep.number                binding.itemEpisodeTitle.text = if (ep.number == title) "Episode $title" else title
if (ep.filler) {                }                handleProgress(                    binding.itemMediaProgressCont,                    binding.itemMediaProgress,                    binding.itemMediaProgressEmpty,                    media.id,                    ep.number                )            }            is EpisodeGridViewHolder -> {
    val binding = holder.binding                setAnimation(fragment.requireContext(), holder.binding.root)                
val thumb = ep.thumb?.let {
if (it.url.isNotEmpty()) {
if (it.url.startsWith("content://") || it.url.startsWith("file://")) {                            it.url
} else {                            GlideUrl(it.url) { it.headers }                        }
} else null                }                Glide.with(binding.itemMediaImage).load(thumb ?: media.cover).override(400, 0)                    .into(binding.itemMediaImage)                binding.itemEpisodeNumber.text = ep.number                binding.itemEpisodeTitle.text = title
if (ep.rating != null) {                    binding.itemEpisodeRating.visibility = View.VISIBLE                    binding.itemEpisodeRating.text = "★ ${ep.rating}"
} else {                    binding.itemEpisodeRating.visibility = View.GONE                }
if (ep.date != null) {                    binding.itemEpisodeDate.visibility = View.VISIBLE                    binding.itemEpisodeDate.text = ep.date
} else {                    binding.itemEpisodeDate.visibility = View.GONE                }
if (ep.filler) {                    binding.itemEpisodeFiller.visibility = View.VISIBLE                    binding.itemEpisodeFillerView.visibility = View.VISIBLE
} else {                    binding.itemEpisodeFiller.visibility = View.GONE                    binding.itemEpisodeFillerView.visibility = View.GONE                }
if (media.userProgress != null) {
if ((ep.number.toFloatOrNull() ?: 9999f) <= media.userProgress!!.toFloat()) {                        binding.itemEpisodeViewedCover.visibility = View.VISIBLE                        binding.itemEpisodeViewed.visibility = View.VISIBLE
} else {                        binding.itemEpisodeViewedCover.visibility = View.GONE                        binding.itemEpisodeViewed.visibility = View.GONE                        binding.itemEpisodeCont.setOnLongClickListener {                            updateProgress(media, ep.number)                            true                        }                    }
} else {                    binding.itemEpisodeViewedCover.visibility = View.GONE                    binding.itemEpisodeViewed.visibility = View.GONE                }                handleProgress(                    binding.itemMediaProgressCont,                    binding.itemMediaProgress,                    binding.itemMediaProgressEmpty,                    media.id,                    ep.number                )            }            is EpisodeCompactViewHolder -> {
    val binding = holder.binding                setAnimation(fragment.requireContext(), holder.binding.root)                binding.itemEpisodeNumber.text = ep.number        
private fun isRotationCoroutineRunningFor(episodeNumber: String): Boolean {
return episodeNumber in activeCoroutines        }    }

fun updateType(t: Int) {        type = t    }}