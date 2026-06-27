package com.sanin.tv.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.KeyEvent
import com.google.android.material.chip.Chip
import com.sanin.tv.databinding.FragmentSearchBinding
import com.sanin.tv.feature_search.AdvancedSearchFilter
import com.sanin.tv.feature_search.AdvancedSearchFilterBottomSheet
import com.sanin.tv.feature_search.SearchViewModel
import com.sanin.tv.media.MediaAdaptor
import com.sanin.tv.util.DpadHelper.enableDpadNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by activityViewModels()

    private val animeGenres = listOf(
        "Action", "Adventure", "Comedy", "Drama", "Fantasy",
        "Horror", "Mecha", "Mystery", "Romance", "Sci-Fi",
        "Slice of Life", "Sports", "Supernatural", "Thriller"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchBar()
        setupGenreChips()
        setupFilterButton()
        setupResultsGrid()
        setupHistoryList()
        observeViewModel()

        // Restore query text if returning to tab
        val currentQuery = searchViewModel.query.value
        if (currentQuery.isNotBlank()) {
            binding.searchEditText.setText(currentQuery)
            binding.searchEditText.setSelection(currentQuery.length)
        }
    }

    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                searchViewModel.onQueryChanged(s?.toString() ?: "")
                updateHistoryVisibility()
            }
        })

        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                searchViewModel.onSearchSubmit(v.text.toString())
                true
            } else false
        }

        // D-pad down from search bar moves focus to results or history
        binding.searchEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                val target = if (binding.historySection.isVisible) binding.historyContainer
                             else binding.resultsRecyclerView
                target.requestFocus()
                true
            } else false
        }

        binding.searchClearButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            searchViewModel.onQueryChanged("")
        }
    }

    private fun setupGenreChips() {
        animeGenres.forEach { genre ->
            val chip = Chip(requireContext()).apply {
                text = genre
                isCheckable = true
                isFocusable = true
                isFocusableInTouchMode = false
                setOnClickListener {
                    val current = searchViewModel.filter.value
                    val newGenre = if (current.genre == genre) null else genre
                    searchViewModel.applyFilter(current.copy(genre = newGenre))
                    isChecked = newGenre != null
                }
                setOnKeyListener { v, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
                    ) {
                        v.performClick()
                        true
                    } else false
                }
            }
            binding.genreChipGroup.addView(chip)
        }
    }

    private fun setupFilterButton() {
        binding.filterButton.setOnClickListener {
            val sheet = AdvancedSearchFilterBottomSheet.newInstance(searchViewModel.filter.value)
            sheet.onFiltersApplied = { filter ->
                searchViewModel.applyFilter(filter)
                updateFilterBadge(filter)
            }
            sheet.show(childFragmentManager, "filter")
        }
    }

    private fun updateFilterBadge(filter: AdvancedSearchFilter) {
        val count = filter.activeFilterCount()
        binding.filterBadge.isVisible = count > 0
        binding.filterBadge.text = count.toString()
    }

    private fun setupResultsGrid() {
        binding.resultsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.resultsRecyclerView.enableDpadNavigation()
    }

    private fun setupHistoryList() {
        binding.historyClearButton.setOnClickListener {
            searchViewModel.clearHistory()
        }
    }

    private fun updateHistoryVisibility() {
        val showHistory = binding.searchEditText.text.isNullOrBlank()
        binding.historySection.isVisible = showHistory && searchViewModel.history.value.isNotEmpty()
        binding.resultsSection.isVisible = !showHistory
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.isLoading.collect { loading ->
                binding.searchProgressBar.isVisible = loading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.results.collect { media ->
                binding.resultsRecyclerView.swapAdapter(
                    MediaAdaptor(0, media.toMutableList(), requireActivity()),
                    false
                )
                binding.resultsEmptyText.isVisible = media.isEmpty() &&
                        !searchViewModel.isLoading.value &&
                        binding.searchEditText.text?.isNotBlank() == true
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.history.collect { history ->
                updateHistoryList(history)
                updateHistoryVisibility()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.filter.collect { filter ->
                updateFilterBadge(filter)
                syncGenreChips(filter.genre)
            }
        }
    }

    private fun updateHistoryList(history: List<String>) {
        binding.historyContainer.removeAllViews()
        history.forEach { entry ->
            val chip = Chip(requireContext()).apply {
                text = entry
                isCloseIconVisible = true
                setOnClickListener {
                    binding.searchEditText.setText(entry)
                    searchViewModel.onSearchSubmit(entry)
                }
                setOnCloseIconClickListener {
                    searchViewModel.removeFromHistory(entry)
                }
            }
            binding.historyContainer.addView(chip)
        }
    }

    private fun syncGenreChips(activeGenre: String?) {
        for (i in 0 until binding.genreChipGroup.childCount) {
            val chip = binding.genreChipGroup.getChildAt(i) as? Chip ?: continue
            chip.isChecked = chip.text == activeGenre
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
