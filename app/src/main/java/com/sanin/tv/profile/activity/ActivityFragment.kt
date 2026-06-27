package com.sanin.tv.profile.activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.api.Activity
import com.sanin.tv.databinding.FragmentFeedBinding
import com.sanin.tv.media.MediaDetailsActivity
import com.sanin.tv.navBarHeight
import com.sanin.tv.profile.ProfileActivity
import com.sanin.tv.util.ActivityMarkdownCreator
import com.xwray.groupie.GroupieAdapter
import eu.kanade.tachiyomi.util.system.getSerializableCompat
import kotlinx.coroutines.launch
class ActivityFragment : Fragment() {
    private lateinit var type: ActivityType    
private var userId: Int? = null    
private var activityId: Int? = null    
private lateinit var binding: FragmentFeedBinding    
private var adapter: GroupieAdapter = GroupieAdapter()    
private var page: Int = 1    
private var allActivities: MutableList<Activity> = mutableListOf()    
private var currentFilter: ActivityFilterType = ActivityFilterType.ALL    
private var hasMoreActivities: Boolean = true    
override fun onCreateView(        inflater: LayoutInflater,        container: ViewGroup?,        savedInstanceState: Bundle?    ): View {        binding = FragmentFeedBinding.inflate(inflater, container, false)
return binding.root    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {        super.onViewCreated(view, savedInstanceState)        arguments?.let {            type = it.getSerializableCompat<ActivityType>("type") as ActivityType            userId = it.getInt("userId")            activityId = it.getInt("activityId")        }        binding.titleBar.visibility =
if (type == ActivityType.OTHER_USER) View.VISIBLE else View.GONE        binding.titleText.text =
if (userId == Anilist.userid) getString(R.string.create_new_activity) else getString(R.string.write_a_message)                // Set up filter icon visibility        binding.filterButton.visibility = if (type == ActivityType.OTHER_USER) View.VISIBLE else View.GONE        binding.filterButton.setOnClickListener {            showFilterBottomSheet()        }                binding.titleImage.setOnClickListener { handleTitleImageClick() }        binding.listRecyclerView.adapter = adapter        binding.listRecyclerView.layoutManager = LinearLayoutManager(context)        binding.listProgressBar.isVisible = true        binding.feedRefresh.updateLayoutParams<ViewGroup.MarginLayoutParams> {            bottomMargin = navBarHeight        }        binding.emptyTextView.text = getString(R.string.nothing_here)        lifecycleScope.launch {            getList()
if (adapter.itemCount == 0) {                binding.emptyTextView.isVisible = true            }            binding.listProgressBar.isVisible = false        }        binding.feedSwipeRefresh.setOnRefreshListener {            lifecycleScope.launch {                adapter.clear()                allActivities.clear()                page = 1                hasMoreActivities = true                getList()                binding.feedSwipeRefresh.isRefreshing = false            }        }        binding.listRecyclerView.addOnScrollListener(
object :            RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {                super.onScrolled(recyclerView, dx, dy)
if (shouldLoadMore()) {                    lifecycleScope.launch {                        binding.feedRefresh.isVisible = true                        getList()                        binding.feedRefresh.isVisible = false                    }                }            }        })    }

private fun showFilterBottomSheet() {        ActivityFilterBottomSheet.newInstance(currentFilter) { filterType ->            currentFilter = filterType            lifecycleScope.launch {                binding.listProgressBar.isVisible = true                adapter.clear()                allActivities.clear()                page = 1                hasMoreActivities = true                getList()                binding.listProgressBar.isVisible = false            }        }.show(childFragmentManager, "ActivityFilterBottomSheet")    }

private fun shouldLoadMore(): Boolean {
    val layoutManager =            (binding.listRecyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()        
val adapter = binding.listRecyclerView.adapter
return hasMoreActivities &&                !binding.listRecyclerView.canScrollVertically(1) &&                !binding.feedRefresh.isVisible && adapter?.itemCount != 0 &&                layoutManager == (adapter!!.itemCount - 1)    }

private fun onActivityClick(id: Int, type: String) {
    val intent = when (type) {            "USER" -> Intent(requireContext(), ProfileActivity::class.java).putExtra("userId", id)            "MEDIA" -> Intent(                requireContext(),                MediaDetailsActivity::class.java            ).putExtra("mediaId", id)
else -> return        }        ContextCompat.startActivity(requireContext(), intent, null)    }

override fun onResume() {        super.onResume()
if (this::binding.isInitialized) {            binding.root.requestLayout()        }    }

companion object {        
enum class ActivityType { GLOBAL, USER, OTHER_USER, ONE }

fun newInstance(            type: ActivityType,            userId: Int? = null,            activityId: Int? = null        ): ActivityFragment {
return ActivityFragment().apply {                arguments = Bundle().apply {                    putSerializable("type", type)                    userId?.let { putInt("userId", it) }                    activityId?.let { putInt("activityId", it) }                }            }        }    }
