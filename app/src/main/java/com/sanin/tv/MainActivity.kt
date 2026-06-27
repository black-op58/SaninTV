package com.sanin.tv

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.feature_navigation.NavigationPillsViewModel
import com.sanin.tv.home.AnimeFragment
import com.sanin.tv.home.HomeFragment
import com.sanin.tv.home.LibraryFragment
import com.sanin.tv.home.SearchFragment
import com.sanin.tv.ui.components.NavigationPills
import com.sanin.tv.ui.components.RightSideRail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val navViewModel: NavigationPillsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Clear the splash windowBackground after content is ready to free memory
        window.setBackgroundDrawable(null)

        // Hide the progress bar once the activity is ready
        findViewById<View>(R.id.mainProgressBar).visibility = View.GONE

        // Mount the Compose NavigationPills into the ComposeView slot at the top
        findViewById<ComposeView>(R.id.navPillsCompose).setContent {
            val currentRoute by navViewModel.currentRoute.collectAsState()
            NavigationPills(
                currentRoute = currentRoute,
                onNavigate   = { route -> navigateTo(route) },
                onBack       = { onBackPressedDispatcher.onBackPressed() }
            )
        }

        // Mount the right side rail overlay — covers the whole screen above everything
        findViewById<ComposeView>(R.id.sideRailCompose).setContent {
            val sideRailVisible by navViewModel.sideRailVisible.collectAsState()
            val avatarUrl by navViewModel.avatarUrl.collectAsState()
            val username by navViewModel.username.collectAsState()
            RightSideRail(
                visible    = sideRailVisible,
                avatarUrl  = avatarUrl,
                username   = username,
                onDismiss  = { navViewModel.hideSideRail() }
            )
        }

        // Load the default tab on first launch only
        if (savedInstanceState == null) {
            navigateTo("home")
        }
    }

    override fun onResume() {
        super.onResume()
        // Keep the notification badge and user avatar/name up to date
        navViewModel.updateNotificationCount(Anilist.unreadNotificationCount)
        navViewModel.updateUserInfo(Anilist.avatar, Anilist.username)
    }

    /**
     * Navigate to one of the four main tabs.
     * Updates the NavPills highlight and swaps the fragment in [R.id.fragmentContainer].
     */
    fun navigateTo(route: String) {
        navViewModel.setRoute(route)

        val fragment: Fragment = when (route) {
            "home"    -> HomeFragment()
            "anime"   -> AnimeFragment()
            "search"  -> SearchFragment()
            "library" -> LibraryFragment()
            else      -> HomeFragment()
        }

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, fragment, route)
        }
    }
}
