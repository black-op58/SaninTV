package com.sanin.tv.media
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.sanin.tv.util.DpadHelper
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sanin.tv.GesturesListener
import com.sanin.tv.R
import com.sanin.tv.Refresh
import com.sanin.tv.ZoomOutPageTransformer
import com.sanin.tv.blurImage
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.mal.MAL
import com.sanin.tv.copyToClipboard        setContentView(binding.root)        screenWidth = resources.displayMetrics.widthPixels.toFloat()        navBar = binding.mediaBottomBar        // Ui init        binding.mediaBottomBarContainer?.let {            it.setPadding(0, 0, 0, navBarHeight)        }        AndroidBug5497Workaround.assistActivity(this) { keyboardVisible ->            // Optionally hide nav bar when keyboard is visible            navBar.visibility = if (keyboardVisible) View.GONE else View.VISIBLE        }        binding.mediaBanner.updateLayoutParams { height += statusBarHeight }        binding.mediaBannerNoKen.updateLayoutParams { height += statusBarHeight }        binding.mediaClose.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight }        binding.incognito.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin += statusBarHeight }        binding.mediaCollapsing.minimumHeight = statusBarHeight        binding.mediaTitle.isSelected = true        mMaxScrollSize = binding.mediaAppBar.totalScrollRange        binding.mediaAppBar.addOnOffsetChangedListener(this)        binding.mediaClose.setOnClickListener {            onBackPressedDispatcher.onBackPressed()        }

val bannerAnimations: Boolean = PrefManager.getVal(PrefName.BannerAnimations)
if (bannerAnimations) {
    val adi = AccelerateDecelerateInterpolator()            
val generator = RandomTransitionGenerator(                (10000 + 15000 * ((PrefManager.getVal(PrefName.AnimationSpeed) as Float))).toLong(),                adi            )            binding.mediaBanner.setTransitionGenerator(generator)        }            }        })        banner.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent)
true }
if (PrefManager.getVal(PrefName.Incognito)) {
    val mediaTitle = "    ${media.userPreferredName}"            binding.mediaTitle.text = mediaTitle            binding.incognito.visibility = View.VISIBLE
} else {            binding.mediaTitle.text = media.userPreferredName        }        binding.mediaTitle.setOnLongClickListener {            copyToClipboard(media.userPreferredName)            true        }        binding.mediaTitleCollapse.text = media.userPreferredName        binding.mediaTitleCollapse.setOnLongClickListener {            copyToClipboard(media.userPreferredName)            true        }        binding.mediaStatus.text = media.status ?: ""        
val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)        
fun fav(media: Media):  PopImageButton? {            //Fav Button
return if (Anilist.userid != null && !rescueMode) {
if (media.isFav) binding.mediaFav.setImageDrawable(                    AppCompatResources.getDrawable(                        this,                        R.drawable.ic_round_favorite_24                    )                )                PopImageButton(                    scope,                    binding.mediaFav,                    R.drawable.ic_round_favorite_24,                    R.drawable.ic_round_favorite_border_24,                    R.color.bg_opp,                    R.color.violet_400,                    media.isFav                binding.mediaAddToList.text = userStatus
} else {                binding.mediaAddToList.setText(R.string.add_list)            }            total()            binding.mediaAddToList.setOnClickListener {
if (rescueMode) {
if (MAL.token != null) {
if (supportFragmentManager.findFragmentByTag("dialog") == null)                            MediaListDialogFragment().show(supportFragmentManager, "dialog")
} else snackString("Please login to MAL")
} else if (Anilist.userid != null) {
if (supportFragmentManager.findFragmentByTag("dialog") == null)                        MediaListDialogFragment().show(supportFragmentManager, "dialog")
} else snackString(getString(R.string.please_login_anilist))            }            binding.mediaAddToList.setOnLongClickListener {                PrefManager.setCustomVal(                    "${media.id}_progressDialog",                    true,                )                snackString(getString(R.string.auto_update_reset))                true            }        }        progress()        model.getMedia().observe(this) {
if (it != null) {
    val oldId = media.id                media = it                // mapping button for LOCAL media
if (media.format?.startsWith("LOCAL") == true) {                    binding.mediaMapping?.visibility = View.VISIBLE                    binding.mediaMapping?.setOnClickListener {
    val isAnime = media.anime != null
val isNovel = media.format == "LOCAL_NOVEL"                        
val folderName = media.folderName ?: media.name ?: media.nameRomaji
val dialog = LocalMappingSearchDialog.newInstance(                            folderName = folderName,                            isAnime = isAnime,                            isNovel = isNovel                        ) { _ ->                            // Re-trigger it
val updatedMedia = media.copy(id = 0)                            model.loading = false                            model.loadMedia(updatedMedia)                        }                        dialog.show(supportFragmentManager, "localMapping")                    }                }                scope.launch {
    val favIcon = fav(it)                    syncMediaFavStateIfNeeded(favIcon)
if (media.isFav != favIcon?.clicked) favIcon?.clicked()                }                binding.mediaNotify.setOnClickListener {
    val i = Intent(Intent.ACTION_SEND)                    i.type = "text/plain"                    i.putExtra(Intent.EXTRA_TEXT, media.shareLink)                    startActivity(Intent.createChooser(i, media.userPreferredName))                }                binding.mediaNotify.setOnLongClickListener {                    openLinkInBrowser(media.shareLink)                    true                }                binding.mediaCover.setOnClickListener {                    openLinkInBrowser(media.shareLink)                }
if (oldId == 0 && media.id != 0) {
if (media.format?.startsWith("LOCAL") == true) {                        binding.mediaCoverImage.loadImage(media.cover)                        blurImage(if (bannerAnimations) binding.mediaBanner else binding.mediaBannerNoKen, media.banner ?: media.cover)                    }                }    //Collapsing UI Stuff    
private var isCollapsed = false    
private val percent = 45    
private var mMaxScrollSize = 0    
private var screenWidth: Float = 0f    
override fun onOffsetChanged(appBar: AppBarLayout, i: Int) {
if (mMaxScrollSize == 0) mMaxScrollSize = appBar.totalScrollRange
val percentage = abs(i) * 100 / mMaxScrollSize        binding.mediaCover.visibility =
if (binding.mediaCover.scaleX == 0f) View.GONE else View.VISIBLE
val duration = (200 * (PrefManager.getVal(PrefName.AnimationSpeed) as Float)).toLong()
if (percentage >= percent && !isCollapsed) {            isCollapsed = true            ObjectAnimator.ofFloat(binding.mediaTitle, "translationX", 0f).setDuration(duration)                .start()            ObjectAnimator.ofFloat(binding.mediaAccessContainer, "translationX", screenWidth)                .setDuration(duration).start()            ObjectAnimator.ofFloat(binding.mediaCover, "translationX", screenWidth)                .setDuration(duration).start()            ObjectAnimator.ofFloat(binding.mediaCollapseContainer, "translationX", screenWidth)                .setDuration(duration).start()            binding.mediaBanner.pause()        }
if (percentage <= percent && isCollapsed) {            isCollapsed = false            ObjectAnimator.ofFloat(binding.mediaTitle, "translationX", -screenWidth)                .setDuration(duration).start()            ObjectAnimator.ofFloat(binding.mediaAccessContainer, "translationX", 0f)                .setDuration(duration).start()            ObjectAnimator.ofFloat(binding.mediaCover, "translationX", 0f).setDuration(duration)                .start()            ObjectAnimator.ofFloat(binding.mediaCollapseContainer, "translationX", 0f)                .setDuration(duration).start()
if (PrefManager.getVal(PrefName.BannerAnimations)) binding.mediaBanner.resume()        }
if (percentage == 1 && model.scrolledToTop.value != false) model.scrolledToTop.postValue(            false        )
if (percentage == 0 && model.scrolledToTop.value != true) model.scrolledToTop.postValue(true)    }

class PopImageButton(        
private val scope: CoroutineScope,        
private val image: ImageView,        
private val d1: Int,        
private val d2: Int,        
private val c1: Int,        
private val c2: Int,        
var clicked: Boolean,        needsInitialClick: Boolean = false,        callback: suspend (Boolean) -> (Unit)    ) {
    private var disabled = false        
private val context = image.context        
private var pressable = true        init {            enabled(true)
if (needsInitialClick) {                scope.launch {                    clicked()                }            }            image.setOnClickListener {
if (pressable && !disabled) {                    pressable = false                    clicked = !clicked                    scope.launch {                        launch(Dispatchers.IO) {                            callback.invoke(clicked)                        }                        clicked()                        pressable = true                    }                }            }        }        suspend 
fun clicked() {            ObjectAnimator.ofFloat(image, "scaleX", 1f, 0f).setDuration(69).start()            ObjectAnimator.ofFloat(image, "scaleY", 1f, 0f).setDuration(100).start()            delay(100)
if (clicked) {                ObjectAnimator.ofArgb(                    image,                    "ColorFilter",                    ContextCompat.getColor(context, c1),                    ContextCompat.getColor(context, c2)                ).setDuration(120).start()                image.setImageDrawable(AppCompatResources.getDrawable(context, d1))
} else image.setImageDrawable(AppCompatResources.getDrawable(context, d2))            ObjectAnimator.ofFloat(image, "scaleX", 0f, 1.5f).setDuration(120).start()            ObjectAnimator.ofFloat(image, "scaleY", 0f, 1.5f).setDuration(100).start()            delay(120)            ObjectAnimator.ofFloat(image, "scaleX", 1.5f, 1f).setDuration(100).start()            ObjectAnimator.ofFloat(image, "scaleY", 1.5f, 1f).setDuration(100).start()            delay(200)
if (clicked) {                ObjectAnimator.ofArgb(                    image,                    "ColorFilter",                    ContextCompat.getColor(context, c2),                    ContextCompat.getColor(context, c1)                ).setDuration(200).start()            }        }

fun enabled(enabled: Boolean) {            disabled = !enabled            image.alpha = if (disabled) 0.33f else 1f        }    }

override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    val viewPager = binding.mediaViewPager
val tabCount = navBar.tabCount
if (DpadHelper.handleTabDpad(event.keyCode, event, viewPager.currentItem, tabCount) { idx ->                navBar.selectTabAt(idx)                viewPager.setCurrentItem(idx, true)            }) return true
return super.dispatchKeyEvent(event)    }

companion object {
    var mediaSingleton: Media? = null    }
