package com.sanin.tv.providers

import kotlinx.serialization.Serializable

@Serializable
data class ProviderConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val type: ProviderType,
    val enabled: Boolean = true,
    val priority: Int = 0
)

@Serializable
enum class ProviderType {
    CONSUMET_GOGOANIME,
    CONSUMET_ZORO,
    ALL_ANIME,
    ANIME_PAHE,
    CUSTOM
}
