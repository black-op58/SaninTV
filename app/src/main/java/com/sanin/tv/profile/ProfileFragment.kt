package com.sanin.tv.profile
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.api.Query
import com.sanin.tv.databinding.FragmentProfileBinding
import com.sanin.tv.loadImage
import com.sanin.tv.media.Media
import com.sanin.tv.media.MediaAdaptor
import com.sanin.tv.media.MediaDetailsActivity
import com.sanin.tv.navBarHeight
import com.sanin.tv.setSlideIn
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.statusBarHeight
import com.sanin.tv.util.DpadHelper.enableDpadNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null    
private val binding get() = _binding!!    
private var user: Query.UserProfile? = null    
private var userId: Int = -1    
private val model: ProfileViewModel by activityViewModels()    
companion object {
    fun newInstance(userId: Int, user: Query.UserProfile?): ProfileFragment {
return ProfileFragment().apply {                arguments = Bundle().apply {                    putInt("userId", userId)
if (user != null) putSerializable("user", user as Serializable)                }            }        }    }

override fun onCreateView(        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?    ): View {        _binding = FragmentProfileBinding.inflate(inflater, container, false)
return binding.root    }

override fun onDestroyView() {        super.onDestroyView()        _binding = null    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {        super.onViewCreated(view, savedInstanceState)        userId = arguments?.getInt("userId", -1) ?: -1        user = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)            arguments?.getSerializable("user", Query.UserProfile::class.java)
else            
@Suppress("DEPRECATION")            arguments?.getSerializable("user") as? Query.UserProfile        user?.let { populateStaticFields(it) }        // Load/refresh user data from network.        model.getUserProfile(userId).observe(viewLifecycleOwner) { profile ->
if (profile != null) {                populateStaticFields(profile)                populateAnimeMangaLists(profile)            }        }        lifecycleScope.launch {            withContext(Dispatchers.IO) { model.loadProfile(userId) }        }    }

private fun populateStaticFields(profile: Query.UserProfile) {
if (_binding == null) return        binding.profileUserAvatar.loadImage(profile.avatar?.large)        binding.profileUserName.text = profile.name ?: ""        
val bioHexColor = String.format(            "#%06X",            0xFFFFFF and (ContextCompat.getColor(requireContext(), R.color.bg_opp_color))        )        
val bio = profile.about ?: ""
if (bio.isNotBlank()) {            binding.profileUserBio.visibility = View.VISIBLE            binding.profileUserBio.loadData(                "<html><body style='color:$bioHexColor
font-family:sans-serif'>$bio</body></html>",                "text/html", "utf-8"            )            binding.profileUserBio.webViewClient = 
object : WebViewClient() {
    override fun shouldOverrideUrlLoading(                    view: WebView?,                    url: String?                ): Boolean {                    url?.let {
try {                            startActivity(                                android.content.Intent(                                    android.content.Intent.ACTION_VIEW,                                    android.net.Uri.parse(it)                                )                            )                        } catch (_: Exception) {}                    }
return true                }            }
} else {            binding.profileUserBio.visibility = View.GONE        }    }

private fun populateAnimeMangaLists(profile: Query.UserProfile) {
if (_binding == null) return
val favAnime: List<Media> = profile.favourites?.anime?.nodes            ?.mapNotNull { it }            ?.map {                Media(                    id     = it.id ?: return@map null,                    name   = it.title?.english ?: it.title?.romaji ?: "",                    nameRomaji = it.title?.romaji ?: "",                    cover  = it.coverImage?.large,                    banner = it.bannerImage,                    isAdult = it.isAdult ?: false                )            }            ?.filterNotNull() ?: emptyList()        
val favManga: List<Media> = profile.favourites?.manga?.nodes            ?.mapNotNull { it }            ?.map {                Media(                    id     = it.id ?: return@map null,                    name   = it.title?.english ?: it.title?.romaji ?: "",                    nameRomaji = it.title?.romaji ?: "",                    cover  = it.coverImage?.large,                    banner = it.bannerImage,                    isAdult = it.isAdult ?: false,                    anime  = false                )            }            ?.filterNotNull() ?: emptyList()
if (favAnime.isEmpty()) {            binding.profileFavAnimeContainer.visibility = View.GONE
} else {            binding.profileFavAnimeContainer.visibility = View.VISIBLE            binding.profileFavAnimeRecyclerView.apply {                adapter = MediaAdaptor(0, ArrayList(favAnime), requireActivity())                layoutManager = LinearLayoutManager(                    requireContext(), LinearLayoutManager.HORIZONTAL, false                )                enableDpadNavigation()            }        }
if (favManga.isEmpty()) {            binding.profileFavMangaContainer.visibility = View.GONE
} else {            binding.profileFavMangaContainer.visibility = View.VISIBLE            binding.profileFavMangaRecyclerView.apply {                adapter = MediaAdaptor(0, ArrayList(favManga), requireActivity())                layoutManager = LinearLayoutManager(                    requireContext(), LinearLayoutManager.HORIZONTAL, false                )                enableDpadNavigation()            }        }

val favCharacters = profile.favourites?.characters?.nodes?.filterNotNull() ?: emptyList()
if (favCharacters.isEmpty()) {            binding.profileFavCharacterContainer.visibility = View.GONE
} else {            binding.profileFavCharacterContainer.visibility = View.VISIBLE            binding.profileFavCharacterRecyclerView.apply {                adapter = CharacterAdapter(favCharacters, requireActivity())                layoutManager = LinearLayoutManager(                    requireContext(), LinearLayoutManager.HORIZONTAL, false                )                enableDpadNavigation()            }        }    }
