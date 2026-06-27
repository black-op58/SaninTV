package com.sanin.tv.aniyomi.anime.custom

import android.app.Application
import com.sanin.tv.settings.BasePreferences
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get

class AppModule(
private val application: Application) : InjektModule {

override fun InjektScope.registerInjectables() {
addSingletonFactory {
BasePreferences(application, get())
}
}
}
