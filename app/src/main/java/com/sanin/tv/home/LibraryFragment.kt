package com.sanin.tv.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.sanin.tv.connections.anilist.AnilistHomeViewModel
import com.sanin.tv.databinding.FragmentLibraryBinding
import com.sanin.tv.media.Media
import com.sanin.tv.media.MediaAdaptor
import com.sanin.tv.media.user.ListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val model: AnilistHomeViewModel by activityViewModels()

    private val statusLabels = listOf(
        "All", "Watching", "Completed", "Paused", "Dropped", "Planning"
    )
    private val statusKeys = listOf(
        null, "CURRENT", "COMPLETED", "PAUSED", "DROPPED", "PLANNING"
    )

    private var selectedStatus: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStatusChips()
        setupGrid()
        setupOpenListButton()
        observeData()
    }

    private fun setupStatusChips() {
        statusLabels.forEachIndexed { index, label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setOnClickListener {
                    binding.statusChipGroup.clearCheck()
                    isChecked = true
                    selectedStatus = statusKeys[index]
                    refreshDisplay()
                }
            }
            binding.statusChipGroup.addView(chip)
        }
    }

    private fun setupGrid() {
        binding.libraryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
    }

    private fun setupOpenListButton() {
        binding.openFullListButton.setOnClickListener {
            ContextCompat.startActivity(
                requireContext(),
                Intent(requireContext(), ListActivity::class.java),
                null
            )
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe all user anime lists
            val allMedia = mutableListOf<Media>()

            model.getAnimeContinue().observe(viewLifecycleOwner) { continuing ->
                allMedia.removeAll { it.userProgress != null || it.userStatus == "CURRENT" }
                allMedia.addAll(continuing ?: emptyList())
                refreshDisplay()
            }

            model.getAnimeFav().observe(viewLifecycleOwner) { favs ->
                // Merge favourites without duplicating
                val existingIds = allMedia.map { it.id }.toSet()
                favs?.filter { it.id !in existingIds }?.let { allMedia.addAll(it) }
                refreshDisplay()
            }

            model.getAnimePlanned().observe(viewLifecycleOwner) { planned ->
                val existingIds = allMedia.map { it.id }.toSet()
                planned?.filter { it.id !in existingIds }?.let { allMedia.addAll(it) }
                refreshDisplay()
            }
        }
    }

    private fun refreshDisplay() {
        val allMedia = collectAllMedia()
        val filtered = if (selectedStatus == null) {
            allMedia
        } else {
            allMedia.filter { media ->
                media.userStatus == selectedStatus ||
                        (selectedStatus == "CURRENT" && media.userProgress != null)
            }
        }

        binding.libraryRecyclerView.swapAdapter(
            MediaAdaptor(0, filtered.toMutableList(), requireActivity()),
            false
        )

        binding.libraryEmptyText.isVisible = filtered.isEmpty()
        binding.libraryCountText.text = "${filtered.size} titles"
    }

    private fun collectAllMedia(): List<Media> {
        val seen = mutableSetOf<Int>()
        val result = mutableListOf<Media>()

        fun addUnique(list: List<Media>?) {
            list?.forEach { if (seen.add(it.id)) result.add(it) }
        }

        addUnique(model.getAnimeContinue().value)
        addUnique(model.getAnimeFav().value)
        addUnique(model.getAnimePlanned().value)
        addUnique(model.getMissingSequels().value)

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
