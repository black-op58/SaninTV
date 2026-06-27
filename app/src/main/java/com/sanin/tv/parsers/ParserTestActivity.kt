package com.sanin.tv.parsers
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.R
import com.sanin.tv.databinding.ActivityParserTestBinding
import com.sanin.tv.initActivity
import com.sanin.tv.navBarHeight
import com.sanin.tv.statusBarHeight
import com.sanin.tv.themes.ThemeManager
import com.sanin.tv.toast
import com.xwray.groupie.GroupieAdapter
class ParserTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParserTestBinding
val adapter = GroupieAdapter()    
val extensionsToTest: MutableList<ExtensionTestItem> = mutableListOf()    
override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)        ThemeManager(this).applyTheme()        initActivity(this)        binding = ActivityParserTestBinding.inflate(layoutInflater)        binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {            topMargin = statusBarHeight        }        binding.extensionResultsRecyclerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {            bottomMargin = navBarHeight        }        setContentView(binding.root)        binding.extensionResultsRecyclerView.adapter = adapter        binding.extensionResultsRecyclerView.layoutManager = LinearLayoutManager(            this,            LinearLayoutManager.VERTICAL,            false        )
}}
