package com.sanin.tv.media.anime
import com.sanin.tv.media.MediaDetailsViewModel
import com.sanin.tv.media.SourceAdapter
import com.sanin.tv.media.SourceSearchDialogFragment
import com.sanin.tv.parsers.ShowResponse
import kotlinx.coroutines.CoroutineScope
class AnimeSourceAdapter(    sources: List<ShowResponse>,    
val model: MediaDetailsViewModel,    
val i: Int,    
val id: Int,    fragment: SourceSearchDialogFragment,    scope: CoroutineScope) : SourceAdapter(sources, fragment, scope) {
    override suspend 
fun onItemClick(source: ShowResponse) {        model.overrideEpisodes(i, source, id)    }}