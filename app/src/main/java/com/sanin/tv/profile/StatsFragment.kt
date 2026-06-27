package com.sanin.tv.profile
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.api.Query
import com.sanin.tv.databinding.FragmentStatisticsBinding
import com.sanin.tv.profile.ChartBuilder.Companion.ChartPacket
import com.sanin.tv.profile.ChartBuilder.Companion.ChartType
import com.sanin.tv.profile.ChartBuilder.Companion.MediaType
import com.sanin.tv.profile.ChartBuilder.Companion.StatType
import com.sanin.tv.setBaseline
import com.sanin.tv.statusBarHeight
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.xwray.groupie.GroupieAdapter
import eu.kanade.tachiyomi.util.system.getSerializableCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
class StatsFragment :    Fragment() {
    private lateinit var binding: FragmentStatisticsBinding    
private var adapter: GroupieAdapter = GroupieAdapter()    
private var stats: MutableList<Query.StatisticsUser?> = mutableListOf()    
private var type: MediaType = MediaType.ANIME    
private var statType: StatType = StatType.COUNT    
private lateinit var user: Query.UserProfile    
private lateinit var activity: ProfileActivity    
private var loadedFirstTime = false    
override fun onCreateView(        inflater: LayoutInflater,        container: ViewGroup?,        savedInstanceState: Bundle?    ): View {        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
return binding.root    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {        super.onViewCreated(view, savedInstanceState)        activity = requireActivity() as ProfileActivity        user = arguments?.getSerializableCompat<Query.UserProfile>("user") as Query.UserProfile        binding.statisticList.setBaseline(activity.binding.profileNavBarContainer!!)        binding.statisticList.adapter = adapter        binding.statisticList.recycledViewPool.setMaxRecycledViews(0, 0)        binding.statisticList.isNestedScrollingEnabled = true        binding.statisticList.layoutManager =            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)        binding.statisticProgressBar.visibility = View.VISIBLE        binding.compare.visibility = if (user.id == Anilist.userid) View.GONE else View.VISIBLE        binding.filterContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {            topMargin = statusBarHeight        }        binding.sourceType.setAdapter(            ArrayAdapter(                requireContext(),                R.layout.item_dropdown,                MediaType.entries.map { it.name.uppercase(Locale.ROOT).replace("_", " ") }            )        )        binding.sourceFilter.setAdapter(            ArrayAdapter(                requireContext(),                R.layout.item_dropdown,                StatType.entries.map { it.name.uppercase(Locale.ROOT).replace("_", " ") }            )        )        binding.compare.setOnCheckedChangeListener { _, isChecked ->
if (isChecked) {                activity.lifecycleScope.launch {
if (Anilist.userid != null) {                        withContext(Dispatchers.Main) {                            binding.statisticProgressBar.visibility = View.VISIBLE                            binding.statisticList.visibility = View.GONE                        }

val userStats =                            Anilist.query.getUserStatistics(Anilist.userid!!)?.data?.user
if (userStats != null) {                            stats.add(userStats)                            withContext(Dispatchers.Main) {                                loadStats(type == MediaType.ANIME)                                binding.statisticProgressBar.visibility = View.GONE                                binding.statisticList.visibility = View.VISIBLE                            }                        }                    }                }
} else {                stats.removeAll(                    stats.filter { it?.id == Anilist.userid }.toSet()                )                loadStats(type == MediaType.ANIME)            }        }        binding.filterContainer.visibility = View.GONE    }

override fun onPause() {        super.onPause()        binding.statisticList.visibility = View.GONE    }

override fun onResume() {        super.onResume()
if (this::binding.isInitialized) {            binding.statisticList.visibility = View.VISIBLE            binding.statisticList.setBaseline(activity.binding.profileNavBarContainer!!)            binding.root.requestLayout()
if (!loadedFirstTime) {                activity.lifecycleScope.launch {                    stats.clear()                    stats.add(Anilist.query.getUserStatistics(user.id)?.data?.user)                    withContext(Dispatchers.Main) {                        binding.filterContainer.visibility = View.VISIBLE                        binding.sourceType.setOnItemClickListener { _, _, i, _ ->                            type = MediaType.entries.toTypedArray()[i]                            loadStats(type == MediaType.ANIME)                        }                        binding.sourceFilter.setOnItemClickListener { _, _, i, _ ->                            statType = StatType.entries.toTypedArray()[i]                            loadStats(type == MediaType.ANIME)                        }                        loadStats(type == MediaType.ANIME)                        binding.statisticProgressBar.visibility = View.GONE                    }                }                loadedFirstTime = true
} else {                loadStats(type == MediaType.ANIME)            }        }    }

private fun standardizeChartPackets(        packets: MutableList<ChartPacket>    ): MutableList<ChartPacket> {
if (packets.size <= 1) return packets
val allNames = linkedSetOf<String>()        packets.forEach { p -> p.names.forEach { allNames.add(it.toString()) } }

val referenceNames = allNames.toList()        
val standardized = packets.map { packet ->            
val valuesMap = packet.names.map { it.toString() }.zip(packet.statData).toMap()            
val standardizedValues = referenceNames.map { name -> valuesMap[name] ?: 0 }

@Suppress("UNCHECKED_CAST")            
val staffChart = ChartBuilder.buildChart(                activity,                ChartType.TwoDimensional,                AAChartType.Line,                statType,                type,                chartPackets,                xAxisName = "Staff",                polar = false,                passedCategories = chartPackets[0].names as List<String>,                scrollPos = 0.0f            )            adapter.add(ChartItem("Staff", staffChart, activity))        }    }

private fun convertScore(score: Int, type: String?): Int {
return when (type) {            "POINT_100" -> score            "POINT_10_DECIMAL" -> score            "POINT_10" -> score * 10            "POINT_5" -> score * 20            "POINT_3" -> score * 33
else -> score        }    }

companion object {
    fun newInstance(user: Query.UserProfile): StatsFragment {
    val args = Bundle().apply {                putSerializable("user", user)            }
return StatsFragment().apply {                arguments = args            }        }    }}