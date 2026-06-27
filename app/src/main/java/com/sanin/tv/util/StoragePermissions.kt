package com.sanin.tv.util
class StoragePermissions(
private val activity: androidx.activity.result.ActivityResultCaller) {

private lateinit var launcher: androidx.activity.result.ActivityResultLauncher<Void?>

private var complete: ((Boolean) -> Unit)? = null
init {
}
}
