package com.sanin.tv.parsers
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import com.sanin.tv.R
import com.sanin.tv.databinding.ItemExtensionTestBinding
import com.sanin.tv.getThemeColor
import com.bumptech.glide.Glide
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class ExtensionTestItem(    
private var extensionType: String,    
private var testType: String,    
private var extension: BaseParser,    
private var searchString: String) : BindableItem<ItemExtensionTestBinding>() {
    private lateinit var binding: ItemExtensionTestBinding    
private lateinit var context: Context    
private var job: Job? = null    
private var isRunning = false    
private var pingResult: Triple<Int, Int?, String>? = null    
private var searchResultData: TestResult = TestResult()    
private var episodeResultData: TestResult = TestResult()    
private var serverResultData: TestResult = TestResult()    
override fun bind(viewBinding: ItemExtensionTestBinding, position: Int) {        binding = viewBinding        context = binding.root.context                Glide.with(context).clear(binding.extensionIconImageView)        binding.extensionIconImageView.setImageDrawable(null)
if (extension.icon != null) {            binding.extensionIconImageView.setImageDrawable(extension.icon)
} else if (extension.iconUrl != null) {            Glide.with(context)                .load(extension.iconUrl)                .into(binding.extensionIconImageView)        }        binding.extensionNameTextView.text = extension.name        binding.extensionLoading.isVisible = isRunning        hideAllResults()        pingResult()        searchResult()        episodeResult()        serverResult()    }

override fun getLayout(): Int {
return R.layout.item_extension_test    }

override fun initializeViewBinding(view: View): ItemExtensionTestBinding {
return ItemExtensionTestBinding.bind(view)    }

private fun hideAllResults() {
if (::binding.isInitialized.not()) return        binding.searchResultText.isVisible = false        binding.episodeResultText.isVisible = false        binding.serverResultText.isVisible = false    }

fun cancelJob() {        job?.cancel()        job = null        binding.extensionLoading.isVisible = false    }

fun startTest() {        pingResult = null        searchResultData = TestResult()        
val searchResult = extension.search(searchString)        searchResultData.time = (System.currentTimeMillis() - searchStart).toInt()        searchResultData.size = searchResult.size        withContext(Dispatchers.Main) {            searchResult()        }
if (searchResultData.size == 0 || testType == "basic") {            done()
return        }

val chapterResultTime = System.currentTimeMillis()        
val chapterResult = extension.loadBook(searchResult.first().link, null)        episodeResultData.time = (System.currentTimeMillis() - chapterResultTime).toInt()        episodeResultData.size = chapterResult.links.size        withContext(Dispatchers.Main) {            episodeResult()            serverResult()        }        done()    }

private suspend 
fun done() {
if (::binding.isInitialized.not()) return        withContext(Dispatchers.Main) {            binding.extensionLoading.isVisible = false            isRunning = false        }    }

private fun pingResult() {
if (::binding.isInitialized.not()) return
if (pingResult == null) {            binding.pingResultText.isVisible = false
return
} else {            binding.pingResultText.isVisible = true        }        binding.pingResultText.setTextColor(            context.getThemeColor(com.google.android.material.R.attr.colorPrimary)        )        
val (code, time, message) = pingResult!!
if (code == 200) {            binding.pingResultText.text = context.getString(R.string.ping_success, time.toString())            binding.pingResultText.setCompoundDrawablesWithIntrinsicBounds(                R.drawable.ic_circle_check, 0, 0, 0            )
return        }        binding.pingResultText.text =            context.getString(R.string.ping_error, code.toString(), message)        binding.pingResultText.setCompoundDrawablesWithIntrinsicBounds(            R.drawable.ic_circle_cancel, 0, 0, 0        )        binding.pingResultText.setTextColor(            context.getThemeColor(com.google.android.material.R.attr.colorError)        )    }

@SuppressLint("SetTextI18n")    
private fun searchResult() {
if (::binding.isInitialized.not()) return
if (searchResultData.time == 0) {            binding.searchResultText.isVisible = false
return        }        binding.searchResultText.setTextColor(            context.getThemeColor(com.google.android.material.R.attr.colorPrimary)        )        binding.searchResultText.isVisible = true
if (searchResultData.size == 0) {
    val text = context.getString(                R.string.title_search_test,                context.getString(R.string.no_results_found)            )            binding.searchResultText.text = text            binding.searchResultText.setCompoundDrawablesWithIntrinsicBounds(                R.drawable.ic_circle_cancel, 0, 0, 0            )            binding.searchResultText.setTextColor(
}}})
