package tachiyomi.domain.entries.anime.model

data class Anime(
    val id: Long = 0L,
    val source: Long = -1L,
    val url: String = "",
    val title: String = "",
    val thumbnailUrl: String? = null,
    val status: Long = 0L,
    val description: String? = null,
    val genre: List<String>? = null,
    val author: String? = null,
    val artist: String? = null,
    val initialized: Boolean = false,
    val viewerFlags: Long = 0L,
    val episodeFlags: Long = 0L,
    val coverLastModified: Long = 0L,
    val updateStrategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE,
)

enum class UpdateStrategy { ALWAYS_UPDATE, ONLY_FETCH_ONCE }
