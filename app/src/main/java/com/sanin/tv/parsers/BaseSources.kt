package com.sanin.tv.parsers

import com.sanin.tv.others.Lazier
import eu.kanade.tachiyomi.animesource.model.ShowResponse

abstract class BaseSources {    
abstract val list: List<Lazier<BaseParser>>    
val names: List<String> get() = list.map { it.name }

fun flushText() {        list.forEach {
if (it.get.isInitialized())                it.get.value?.showUserText = ""        }    }

open operator 
fun get(i: Int): BaseParser? {
return list[i].get.value    }

fun saveResponse(i: Int, mediaId: Int, response: ShowResponse) {        get(i)?.saveShowResponse(mediaId, response, true)    }}