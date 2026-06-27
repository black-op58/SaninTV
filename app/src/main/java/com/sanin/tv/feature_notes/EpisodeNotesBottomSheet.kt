package com.sanin.tv.feature_notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Feature 5: Episode Notes — Bottom sheet UI
 * Shows existing notes for an episode and allows adding/editing a note with a timestamp.
 */
class EpisodeNotesBottomSheet : BottomSheetDialogFragment() {

    companion object {
    private const val ARG_MEDIA_ID = "mediaId"
        private const val ARG_EPISODE = "episode"
        private const val ARG_TIMESTAMP = "timestamp"

        fun newInstance(mediaId: Int, episodeNumber: Float, currentPositionMs: Long): EpisodeNotesBottomSheet {
    return EpisodeNotesBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MEDIA_ID, mediaId)
                    putFloat(ARG_EPISODE, episodeNumber)
                    putLong(ARG_TIMESTAMP, currentPositionMs)
                }
            }
        }
    }

    private var mediaId: Int = 0
    private var episodeNumber: Float = 0f
    private var currentTimestampMs: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaId = arguments?.getInt(ARG_MEDIA_ID) ?: 0
        episodeNumber = arguments?.getFloat(ARG_EPISODE) ?: 0f
        currentTimestampMs = arguments?.getLong(ARG_TIMESTAMP) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
    val ctx = requireContext()
        val scroll = ScrollView(ctx)
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }
        scroll.addView(layout)

        val title = TextView(ctx).apply {
            text = "Episode Notes — Ep $episodeNumber"
            textSize = 18f
        }
        layout.addView(title)

        val timestampLabel = TextView(ctx).apply {
            text = "At: ${EpisodeNotesManager.formatTimestamp(currentTimestampMs)}"
            textSize = 13f
        }
        layout.addView(timestampLabel)

        val existing = EpisodeNotesManager.getNote(mediaId, episodeNumber)

        val editText = EditText(ctx).apply {
            hint = "Write your note here…"
            minLines = 3
            existing?.let { setText(it.text) }
        }
        layout.addView(editText)

        val saveBtn = Button(ctx).apply { text = "Save Note" }
        layout.addView(saveBtn)

        val deleteBtn = Button(ctx).apply {
            text = "Delete Note"
            isEnabled = existing != null
        }
        layout.addView(deleteBtn)

        val allNotesLabel = TextView(ctx).apply {
            text = "All notes for this media:"
            textSize = 14f
            setPadding(0, 24, 0, 4)
        }
        layout.addView(allNotesLabel)

        val allNotes = EpisodeNotesManager.getAllNotes(mediaId)
        if (allNotes.isEmpty()) {
            layout.addView(TextView(ctx).apply { text = "No notes yet." })
        } else {
            allNotes.forEach { note ->
                layout.addView(TextView(ctx).apply {
                    text = "Ep ${note.episodeNumber} @ ${EpisodeNotesManager.formatTimestamp(note.timestampMs)}: ${note.text}"
                    textSize = 12f
                    setPadding(0, 4, 0, 4)
                })
            }
        }

        saveBtn.setOnClickListener {
    val text = editText.text.toString().trim()
            if (text.isNotEmpty()) {
                EpisodeNotesManager.saveNote(mediaId, episodeNumber, currentTimestampMs, text)
                dismiss()
            }
        }

        deleteBtn.setOnClickListener {
            EpisodeNotesManager.deleteNote(mediaId, episodeNumber)
            dismiss()
        }

        return scroll
    }
}
