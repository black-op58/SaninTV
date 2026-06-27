package com.sanin.tv.settings

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sanin.tv.databinding.ActivitySettingsNotificationsBinding

class SettingsNotificationActivity : AppCompatActivity() {
    // TODO: Implementation was not present in the source ZIP
    private lateinit var binding: ActivitySettingsNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    private fun setup() {
        // stub — full implementation missing from source ZIP
    }
}
