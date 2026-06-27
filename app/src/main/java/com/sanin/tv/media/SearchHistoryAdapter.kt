package com.sanin.tv.media

init {        searchHistoryLiveData =            PrefManager.getLiveVal(historyType, mutableListOf<SearchHistory>()).asLiveClass()        searchHistoryLiveData?.observeForever { data ->            searchHistory = data.toMutableList()            submitList(searchHistory?.sorted())        }    }

fun remove(item: String) {        searchHistory?.let { list ->            list.removeAll { it.search == item }        }        PrefManager.setVal(historyType, searchHistory)        submitList(searchHistory?.sorted())    }

fun add(item: String) {
    val maxSize = 25
if (searchHistory?.any { it.search == item } == true || item.isBlank()) return
if (PrefManager.getVal(PrefName.Incognito)) return        searchHistory?.add(SearchHistory(item, System.currentTimeMillis()))
if ((searchHistory?.size ?: 0) > maxSize) {            searchHistory?.removeAt(                searchHistory?.sorted()?.lastIndex ?: 0            )        }        submitList(searchHistory?.sorted())        PrefManager.setVal(historyType, searchHistory)    }

fun clearHistory() {        searchHistory?.clear()        PrefManager.setVal(historyType, searchHistory)        submitList(searchHistory?.sorted())    }

override fun onCreateViewHolder(        parent: ViewGroup,        viewType: Int    ): SearchHistoryAdapter.SearchHistoryViewHolder {
    val view = LayoutInflater.from(parent.context)            .inflate(R.layout.item_search_history, parent, false)
return SearchHistoryViewHolder(view)    }

override fun onBindViewHolder(        holder: SearchHistoryAdapter.SearchHistoryViewHolder,        position: Int    ) {
    val item = getItem(position)        holder.binding.searchHistoryTextView.text = item        holder.binding.closeTextView.setOnClickListener {
    val currentPosition = holder.bindingAdapterPosition
if (currentPosition >= itemCount || currentPosition < 0) return@setOnClickListener            remove(getItem(currentPosition))        }        holder.binding.searchHistoryTextView.setOnClickListener {
    val currentPosition = holder.bindingAdapterPosition
if (currentPosition >= itemCount || currentPosition < 0) return@setOnClickListener            searchClicked(getItem(currentPosition))        }    }    inner 
class SearchHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = ItemSearchHistoryBinding.bind(view)    }

companion object {
    val DIFF_CALLBACK_INSTALLED = 
object : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(                oldItem: String,                newItem: String            ): Boolean {
return oldItem == newItem            }

override fun areContentsTheSame(                oldItem: String,                newItem: String            ): Boolean {
return oldItem == newItem            }        }    }
