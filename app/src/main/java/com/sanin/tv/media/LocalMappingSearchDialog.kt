package com.sanin.tv.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.databinding.DialogLocalMappingSearchBinding
import com.sanin.tv.util.Logger
class LocalMappingSearchDialog : androidx.fragment.app.DialogFragment() {

private val adapter = LocalMappingResultAdapter(emptyList()) {}

private fun search(query: String) {
lifecycleScope.launch {
}}}
