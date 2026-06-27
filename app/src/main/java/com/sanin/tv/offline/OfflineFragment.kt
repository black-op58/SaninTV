package com.sanin.tv.offline
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.sanin.tv.R
import com.sanin.tv.databinding.FragmentOfflineBinding
import com.sanin.tv.isOnline
import com.sanin.tv.navBarHeight
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.startMainActivity
import com.sanin.tv.statusBarHeight
class OfflineFragment : Fragment() {
    private var offline = false    
override fun onCreateView(        inflater: LayoutInflater,        container: ViewGroup?,        savedInstanceState: Bundle?    ): View {
    val binding = FragmentOfflineBinding.inflate(inflater, container, false)        binding.refreshContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {            topMargin = statusBarHeight            bottomMargin = navBarHeight        }        offline = PrefManager.getVal(PrefName.OfflineMode)        binding.noInternet.text =
if (offline) "Offline Mode" else getString(R.string.no_internet)        binding.refreshButton.text = if (offline) "Go Online" else getString(R.string.refresh)        binding.refreshButton.setOnClickListener {
if (offline && isOnline(requireContext())) {                PrefManager.setVal(PrefName.OfflineMode, false)                startMainActivity(requireActivity())
} else {
if (isOnline(requireContext())) {                    startMainActivity(requireActivity())                }            }        }
return binding.root    }

override fun onResume() {        super.onResume()        offline = PrefManager.getVal(PrefName.OfflineMode)    }}