package com.sanin.tv.others

import com.sanin.tv.media.Media
import com.sanin.tv.util.Logger
import org.jsoup.Jsoup

object MalScraper {
suspend fun loadMedia(media: Media) {
try {

val res = Jsoup.connect("https://myanimelist.net/anime/${media.idMAL}").get()

val a = res.select("span[itemprop=name]").firstOrNull()?.text()
if (a != null) {
media.nameMAL = a                    media.typeMAL =
if (res.select("div.spaceit_pad > a")                                .isNotEmpty()                        ) res.select("div.spaceit_pad > a")[0].text() else null                }            }        } catch (e: Exception) {            // if (e is TimeoutCancellationException) snackString(currContext()?.getString(R.string.error_loading_mal_data))        }    }
}}
