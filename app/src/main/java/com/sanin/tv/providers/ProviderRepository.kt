package com.sanin.tv.providers

import android.content.Context
import android.content.SharedPreferences
import com.sanin.tv.App
import com.sanin.tv.Mapper
import com.sanin.tv.util.Logger
import kotlinx.serialization.encodeToString

object ProviderRepository {

    private const val PREFS_NAME = "provider_sources"
    private const val KEY_PROVIDERS = "providers_v1"

    private val prefs: SharedPreferences?
        get() = App.context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val defaults: List<ProviderConfig> = listOf(
        ProviderConfig(
            id       = "consumet_gogo",
            name     = "Gogoanime",
            baseUrl  = "https://api.consumet.org",
            type     = ProviderType.CONSUMET_GOGOANIME,
            enabled  = true,
            priority = 0
        ),
        ProviderConfig(
            id       = "consumet_zoro",
            name     = "Zoro (Aniwatch)",
            baseUrl  = "https://api.consumet.org",
            type     = ProviderType.CONSUMET_ZORO,
            enabled  = true,
            priority = 1
        ),
        ProviderConfig(
            id       = "all_anime",
            name     = "AllAnime",
            baseUrl  = "https://api.allanime.day",
            type     = ProviderType.ALL_ANIME,
            enabled  = true,
            priority = 2
        ),
        ProviderConfig(
            id       = "anime_pahe",
            name     = "AnimePahe",
            baseUrl  = "https://animepahe.ru",
            type     = ProviderType.ANIME_PAHE,
            enabled  = false,
            priority = 3
        ),
    )

    fun load(): List<ProviderConfig> {
        val json = prefs?.getString(KEY_PROVIDERS, null) ?: return defaults
        return try {
            Mapper.json.decodeFromString(json)
        } catch (e: Exception) {
            Logger.log("ProviderRepository: load failed — ${e.message}")
            defaults
        }
    }

    fun save(providers: List<ProviderConfig>) {
        try {
            prefs?.edit()
                ?.putString(KEY_PROVIDERS, Mapper.json.encodeToString(providers))
                ?.apply()
        } catch (e: Exception) {
            Logger.log("ProviderRepository: save failed — ${e.message}")
        }
    }

    fun reset() = save(defaults)
}
