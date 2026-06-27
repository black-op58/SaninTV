package com.sanin.tv.parsers

import com.sanin.tv.others.Lazier
import com.sanin.tv.providers.BuiltInAnimeParser
import com.sanin.tv.util.Logger
import eu.kanade.tachiyomi.extension.anime.AnimeExtensionManager
import eu.kanade.tachiyomi.extension.anime.model.AnimeExtension
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object AnimeSources : WatchSources() {

    var pinnedAnimeSources: List<String> = listOf()

    private val builtInEntry = Lazier({ BuiltInAnimeParser() }, "Built-In")
    private val localEntry = Lazier({ LocalAnimeParser() }, "Local")

    @Volatile
    private var extensionEntries: List<Lazier<BaseParser>> = listOf()

    @Volatile
    private var currentExtensions: List<AnimeExtension.Installed> = listOf()

    override val list: List<Lazier<BaseParser>>
        get() = listOf(builtInEntry, localEntry) + extensionEntries

    init {
        startObservingExtensions()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startObservingExtensions() {
        GlobalScope.launch {
            try {
                val mgr = Injekt.get<AnimeExtensionManager>()
                mgr.installedExtensionsFlow.collect { extensions ->
                    val nonNsfw = extensions.filter { !it.isNsfw }
                    currentExtensions = nonNsfw
                    extensionEntries = buildEntries(nonNsfw, pinnedAnimeSources)
                    Logger.log("AnimeSources: updated list — ${list.size} sources")
                }
            } catch (e: Exception) {
                Logger.log("AnimeSources: extension observer failed — ${e.message}")
            }
        }
    }

    fun performReorderAnimeSources() {
        extensionEntries = buildEntries(currentExtensions, pinnedAnimeSources)
    }

    private fun buildEntries(
        extensions: List<AnimeExtension.Installed>,
        pinned: List<String>
    ): List<Lazier<BaseParser>> {
        val ordered = if (pinned.isEmpty()) {
            extensions
        } else {
            val byName = extensions.associateBy { it.name }
            val orderedList = pinned.mapNotNull { byName[it] }
            orderedList + extensions.filter { it.name !in pinned }
        }
        return ordered.map { ext ->
            Lazier<BaseParser>({ DynamicAnimeParser(ext) }, ext.name)
        }
    }
}
