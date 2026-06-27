package com.sanin.tv.connections.tmdb

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.sanin.tv.App
import com.sanin.tv.Mapper
import com.sanin.tv.client
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.util.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File

/**
 * Fetches anime logo art from the SaninTV IdMapper server, which proxies
 * TMDB image data server-side — no API key is required from the user.
 *
 * Cache hierarchy (fastest → slowest):
 *  1. In-memory HashMap           — instant, lost on process death
 *  2. Disk JSON file              — survives restarts; stored in either the
 *                                   user-chosen SAF folder or internal app dir
 *  3. Network (IdMapper /api/logo) — only for entries not yet in cache
 */
object TmdbApi {

    private const val LOGO_API        = "https://bf5b5bdc-1ff2-45f3-a2e1-f5c295318ea9-00-2s9qdttnwhf66.picard.replit.dev/api/logo"
    private const val LOGO_TIMEOUT_MS = 5_000L  // fall back to text title if server is sleeping
    private const val CACHE_DIR       = "tmdb"
    private const val CACHE_FILE      = "logos.json"

    // Layer 1 — in-memory: anilistId → URL (or "" = confirmed no logo)
    private val cache = HashMap<Int, String>()
    private var diskLoaded = false

    // ── Storage helpers ───────────────────────────────────────────────────────

    private fun getInternalFile(): File {
        val ctx = App.context ?: return File(CACHE_FILE)
        return File(ctx.getDir(CACHE_DIR, Context.MODE_PRIVATE), CACHE_FILE)
    }

    private fun getSafCacheFile(): DocumentFile? {
        val ctx = App.context ?: return null
        val uriStr = PrefManager.getVal<String>(PrefName.CacheStorageUri)
        if (uriStr.isBlank()) return null
        val tree = DocumentFile.fromTreeUri(ctx, Uri.parse(uriStr)) ?: return null
        return tree.findFile(CACHE_FILE)
    }

    private fun getSafCacheTree(): DocumentFile? {
        val ctx = App.context ?: return null
        val uriStr = PrefManager.getVal<String>(PrefName.CacheStorageUri)
        if (uriStr.isBlank()) return null
        return DocumentFile.fromTreeUri(ctx, Uri.parse(uriStr))
    }

    // ── Disk I/O ─────────────────────────────────────────────────────────────

    private fun readDiskRaw(): String? {
        val ctx = App.context ?: return null
        val safFile = getSafCacheFile()
        return if (safFile != null) {
            ctx.contentResolver.openInputStream(safFile.uri)?.use { it.readText() }
        } else {
            val f = getInternalFile()
            if (f.exists()) f.readText() else null
        }
    }

    private fun writeDiskRaw(json: String) {
        val ctx = App.context ?: return
        val tree = getSafCacheTree()
        if (tree != null) {
            val safFile = tree.findFile(CACHE_FILE)
                ?: tree.createFile("application/json", CACHE_FILE)
                ?: return
            ctx.contentResolver.openOutputStream(safFile.uri, "w")?.use { it.write(json.toByteArray()) }
        } else {
            val f = getInternalFile()
            f.parentFile?.mkdirs()
            f.writeText(json)
        }
    }

    @Synchronized
    private fun loadFromDisk() {
        if (diskLoaded) return
        diskLoaded = true
        try {
            val raw = readDiskRaw() ?: return
            val map = Mapper.json.decodeFromString<Map<String, String>>(raw)
            map.forEach { (k, v) -> cache[k.toInt()] = v }
            Logger.log("TmdbApi: loaded ${map.size} cache entries")
        } catch (e: Exception) {
            Logger.log("TmdbApi: disk load failed — ${e.message}")
        }
    }

    @Synchronized
    private fun saveToDisk() {
        try {
            writeDiskRaw(Mapper.json.encodeToString(cache.mapKeys { it.key.toString() }))
        } catch (e: Exception) {
            Logger.log("TmdbApi: disk save failed — ${e.message}")
        }
    }

    // ── Storage migration ─────────────────────────────────────────────────────

    fun migrateStorageSaf(newTreeUri: Uri) {
        val ctx = App.context ?: return
        try {
            val json = readDiskRaw()
            val newTree = DocumentFile.fromTreeUri(ctx, newTreeUri)
            val newFile = newTree?.findFile(CACHE_FILE)
                ?: newTree?.createFile("application/json", CACHE_FILE)
            if (newFile != null && json != null) {
                ctx.contentResolver.openOutputStream(newFile.uri, "w")
                    ?.use { it.write(json.toByteArray()) }
            }
            getSafCacheFile()?.delete() ?: getInternalFile().delete()
            diskLoaded = false
        } catch (e: Exception) {
            Logger.log("TmdbApi: SAF migration failed — ${e.message}")
        }
    }

    fun resetStorageToInternal() {
        val ctx = App.context ?: return
        try {
            val json = readDiskRaw()
            getSafCacheFile()?.delete()
            if (json != null) {
                val f = getInternalFile()
                f.parentFile?.mkdirs()
                f.writeText(json)
            }
            diskLoaded = false
        } catch (e: Exception) {
            Logger.log("TmdbApi: reset to internal failed — ${e.message}")
        }
    }

    // ── Public helpers ────────────────────────────────────────────────────────

    fun cacheFileSizeBytes(): Long =
        getSafCacheFile()?.length() ?: getInternalFile().length()

    fun cacheFilePath(): String? =
        getSafCacheFile()?.uri?.lastPathSegment ?: getInternalFile().absolutePath

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the best English logo URL for [anilistId], or null if the
     * server has no logo, is unreachable, or doesn't respond within
     * [LOGO_TIMEOUT_MS]. On null the caller falls back to the plain AniList
     * text title — no error is shown to the user.
     *
     * No user API key is needed — the key is held server-side.
     */
    suspend fun getLogoUrl(anilistId: Int, isAnime: Boolean): String? {
        if (!isAnime) return null

        if (!diskLoaded) withContext(Dispatchers.IO) { loadFromDisk() }
        cache[anilistId]?.let { return it.ifEmpty { null } }

        return withContext(Dispatchers.IO) {
            try {
                // 5-second hard timeout — if the logo server is cold-starting
                // or unreachable, fall back to text title immediately rather
                // than making the user wait.
                val result = withTimeout(LOGO_TIMEOUT_MS) {
                    val response = client.get("$LOGO_API?anilist_id=$anilistId")
                    Mapper.json.decodeFromString<LogoApiResponse>(response.text)
                }

                val logo = result.logos
                    .filter { it.iso639 == "en" && it.aspectRatio > 1.2 }
                    .maxByOrNull { it.voteAverage }
                    ?: result.logos.maxByOrNull { it.voteAverage }

                val logoUrl = logo?.filePath ?: ""

                cache[anilistId] = logoUrl
                saveToDisk()

                Logger.log("TmdbApi: fetched anilist=$anilistId → $logoUrl")
                logoUrl.ifEmpty { null }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Timeout, network error, or JSON parse failure — show text title
                Logger.log("TmdbApi: fallback anilist=$anilistId → ${e.message}")
                null
            }
        }
    }

    /**
     * Clears the in-memory cache and deletes the disk cache file.
     */
    fun clearCache() {
        cache.clear()
        diskLoaded = false
        try {
            getSafCacheFile()?.delete()
            getInternalFile().delete()
            Logger.log("TmdbApi: cache cleared")
        } catch (e: Exception) {
            Logger.log("TmdbApi: clearCache error — ${e.message}")
        }
    }
}

// ─── Response models ───────────────────────────────────────────────────────────

@Serializable
data class LogoApiResponse(
    val logos:     List<LogoImage> = emptyList(),
    val backdrops: List<LogoImage> = emptyList(),
    val posters:   List<LogoImage> = emptyList()
)

@Serializable
data class LogoImage(
    @SerialName("file_path")    val filePath:    String  = "",
    @SerialName("iso_639_1")    val iso639:      String? = null,
    @SerialName("vote_average") val voteAverage: Double  = 0.0,
    @SerialName("vote_count")   val voteCount:   Int     = 0,
    @SerialName("aspect_ratio") val aspectRatio: Double  = 0.0,
    @SerialName("width")        val width:       Int     = 0,
    @SerialName("height")       val height:      Int     = 0
)
