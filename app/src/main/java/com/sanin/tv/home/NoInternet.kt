package com.sanin.tv.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sanin.tv.databinding.ActivityNoInternetBinding

class NoInternet : AppCompatActivity() {
    // TODO: Full implementation was not present in the source ZIP — reconstructed from stub
    private lateinit var binding: ActivityNoInternetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoInternetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var doubleBackToExitPressedOnce = false
        onBackPressedDispatcher.addCallback(this,
            object : androidx.activity.OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
    if (doubleBackToExitPressedOnce) {
                        finishAffinity()
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    Handler(Looper.getMainLooper()).postDelayed(
                        { doubleBackToExitPressedOnce = false },
                        2000
                    )
                }
            }
        )
    }
}
