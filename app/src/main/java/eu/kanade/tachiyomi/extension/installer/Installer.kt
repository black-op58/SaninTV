package eu.kanade.tachiyomi.extension.installer

abstract class Installer {
    // TODO: Implementation was not present in the source ZIP
    abstract fun install(filename: String)
    open fun onDestroy() {}
}
