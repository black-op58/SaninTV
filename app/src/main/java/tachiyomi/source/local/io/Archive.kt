package tachiyomi.source.local.io

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

object UniFileCompat {

    /**
     * Returns a [DocumentFile] for the given [File], usable with the content resolver.
     */
    fun fromFile(file: File, context: Context): DocumentFile? {
    return DocumentFile.fromFile(file)
    }

    /**
     * Returns a [DocumentFile] for the given [Uri].
     */
    fun fromUri(uri: Uri, context: Context): DocumentFile? {
    return when {
            uri.scheme == "file" -> DocumentFile.fromFile(File(uri.path!!))
            else -> DocumentFile.fromSingleUri(context, uri)
        }
    }

    /**
     * Returns a [DocumentFile] that represents a tree rooted at [uri].
     */
    fun fromTreeUri(uri: Uri, context: Context): DocumentFile? {
    return DocumentFile.fromTreeUri(context, uri)
    }

    /**
     * Returns the child [DocumentFile] with the given [name] under [parent], or null.
     */
    fun findFile(parent: DocumentFile, name: String): DocumentFile? {
    return parent.findFile(name)
    }
}
