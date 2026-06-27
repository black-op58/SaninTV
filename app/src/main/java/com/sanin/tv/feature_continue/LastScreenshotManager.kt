package com.sanin.tv.feature_continue

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

/**
 * Saves and loads the last-watched frame screenshot per media ID.
 *
 * Screenshots are stored as JPEG files in the app's internal files directory
 * under "screenshots/<mediaId>.jpg".
 *
 * Enable/disable via [PrefName.ContinueWatchingShowScreenshot].
 *
 * Integration point in the player:
 *   // When playback pauses / stops, capture the current frame:
 *   player.createMessage { _, _ ->
 *       val bitmap = playerView.videoSurfaceView?.drawToBitmap() ?: return@createMessage
 *       LastScreenshotManager.save(context, mediaId, bitmap)
 *   }
 */
object LastScreenshotManager {

    private fun screenshotDir(context: Context): File =
        File(context.filesDir, "screenshots").also { it.mkdirs() }

    private fun screenshotFile(context: Context, mediaId: Int): File =
        File(screenshotDir(context), "$mediaId.jpg")

    /** Save [bitmap] as the last-watched screenshot for [mediaId]. */
    fun save(context: Context, mediaId: Int, bitmap: Bitmap) {
        try {
            val file = screenshotFile(context, mediaId)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
        } catch (_: Exception) {
            // Silently ignore I/O errors — screenshot is non-critical
        }
    }

    /**
     * Load the last screenshot for [mediaId], or null if none exists.
     * Loading is done synchronously and should be called from a background thread.
     */
    fun load(context: Context, mediaId: Int): Bitmap? {
        val file = screenshotFile(context, mediaId)
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Exception) {
            null
        }
    }

    /** Check whether a screenshot exists for [mediaId] without loading it. */
    fun exists(context: Context, mediaId: Int): Boolean =
        screenshotFile(context, mediaId).exists()

    /** Delete the screenshot for [mediaId]. */
    fun clear(context: Context, mediaId: Int) {
        screenshotFile(context, mediaId).delete()
    }

    /** Delete all saved screenshots. */
    fun clearAll(context: Context) {
        screenshotDir(context).listFiles()?.forEach { it.delete() }
    }
}
