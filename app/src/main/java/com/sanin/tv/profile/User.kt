package com.sanin.tv.profile
import com.sanin.tv.connections.anilist.api.Activity
import kotlinx.serialization.Serializable
@Serializable
data class User(    
val id: Int,    
val name: String,    
val pfp: String?,    
val banner: String?,    // for media
val status: String? = null,    
val score: Float? = null,    
val progress: Int? = null,    
val totalEpisodes: Int? = null,    
val nextAiringEpisode: Int? = null,    
val activity: List<Activity> = mutableListOf(),    // for follow status
var isFollowing: Boolean? = null,    
var isFollower: Boolean? = null,) : java.io.Serializable {    
companion object {
    private const val serialVersionUID: Long = 1    }}
package com.sanin.tv.profile
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sanin.tv.databinding.ActivitySingleStatBinding
import com.sanin.tv.getThemeColor
import com.sanin.tv.initActivity
import com.sanin.tv.themes.ThemeManager
import com.sanin.tv.toast
import com.github.aachartmodel.aainfographics.aachartcreator.AAOptions
class SingleStatActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySingleStatBinding    
override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)        ThemeManager(this).applyTheme()        initActivity(this)        binding = ActivitySingleStatBinding.inflate(layoutInflater)        setContentView(binding.root)        
val chartOptions = chartOptions
if (chartOptions != null) {            chartOptions.chart?.backgroundColor = getThemeColor(android.R.attr.windowBackground)            binding.chartView.aa_drawChartWithChartOptions(chartOptions)            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
} else {            toast("No chart data")            finish()        }    }

companion object {
    var chartOptions: AAOptions? = null  // I cba to pass this through an intent    }}
package com.sanin.tv.profile
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.databinding.ItemFollowerBinding
import com.sanin.tv.databinding.ItemFollowerGridBinding
import com.sanin.tv.loadImage
import com.sanin.tv.openLinkInCustomTab
import com.sanin.tv.setAnimation
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.snackString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class UsersAdapter(
private val user: MutableList<User>, 
private val grid: Boolean = false) :    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {
    private val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)    inner 
class UsersViewHolder(
val binding: ViewBinding) :        RecyclerView.ViewHolder(binding.root) {        init {            itemView.setOnClickListener {
    val pos = bindingAdapterPosition
if (pos < 0 || pos >= user.size) return@setOnClickListener
val u = user[pos]
if (rescueMode) {
    val malUrl = u.banner ?: "https://myanimelist.net/profile/${u.name}"                    openLinkInCustomTab(malUrl)
} else {                    ContextCompat.startActivity(                        binding.root.context, Intent(binding.root.context, ProfileActivity::class.java)                            .putExtra("userId", u.id), null                    )                }            }        }    }

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
return UsersViewHolder(
if (grid) ItemFollowerGridBinding.inflate(                LayoutInflater.from(parent.context),                parent,                false            ) else                ItemFollowerBinding.inflate(                    LayoutInflater.from(parent.context),                    parent,                    false                )        )    }

override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {        setAnimation(holder.binding.root.context, holder.binding.root)        
val user = user.getOrNull(position) ?: return
if (grid) {
    val b = holder.binding as ItemFollowerGridBinding            b.profileUserAvatar.loadImage(user.pfp)            b.profileUserName.text = user.name            b.profileCompactScoreBG.isVisible = false            b.profileInfo.isVisible = false            b.profileCompactProgressContainer.isVisible = false
} else {
    val b = holder.binding as ItemFollowerBinding            b.profileUserAvatar.loadImage(user.pfp)
if (rescueMode) {                b.profileBannerImage.loadImage(user.pfp)
} else {                b.profileBannerImage.loadImage(user.banner ?: user.pfp)            }            b.profileUserName.text = user.name
if (rescueMode || user.id == Anilist.userid || user.isFollowing == null) {                b.followStatusChip.isVisible = false
} else {                b.followStatusChip.isVisible = true
fun followText(): String {
return b.root.context.getString(
when {                            user.isFollowing == true && user.isFollower == true -> R.string.mutual                            user.isFollowing == true -> R.string.unfollow                            user.isFollower == true -> R.string.follows_you
else -> R.string.follow                        }                    )                }                b.followStatusChip.text = followText()                b.followStatusChip.setOnClickListener {                    b.root.findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {
    val res = Anilist.mutation.toggleFollow(user.id)
if (res?.data?.toggleFollow != null) {                            withContext(Dispatchers.Main) {                                snackString(R.string.success)                                user.isFollowing = res.data.toggleFollow.isFollowing                                b.followStatusChip.text = followText()                            }                        }                    }                }            }        }    }

override fun getItemCount(): Int = user.size}
}}
