package com.sanin.tv.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.databinding.ActivityFaqBinding

class FAQActivity : AppCompatActivity() {
    // TODO: Full implementation was not present in the source ZIP
    private lateinit var binding: ActivityFaqBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.devsTitle2.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
