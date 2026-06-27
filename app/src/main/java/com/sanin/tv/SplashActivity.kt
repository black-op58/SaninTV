package com.sanin.tv

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Clear the windowBackground after our layout is drawn to free memory
        window.setBackgroundDrawable(null)

        val root = findViewById<View>(R.id.splashRoot)

        // Wait 2 seconds then fade out over 500ms, then launch MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            root.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    overridePendingTransition(0, 0)
                }
                .start()
        }, 2000)
    }
}
