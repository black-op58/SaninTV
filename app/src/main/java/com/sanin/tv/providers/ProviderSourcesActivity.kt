package com.sanin.tv.providers

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sanin.tv.R
import com.sanin.tv.databinding.ActivityProviderSourcesBinding
import com.sanin.tv.databinding.DialogAddProviderBinding
import com.sanin.tv.initActivity
import com.sanin.tv.navBarHeight
import com.sanin.tv.snackString
import com.sanin.tv.statusBarHeight
import com.sanin.tv.util.customAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Lets the user manage built-in stream providers.
 *
 * Features:
 *  - Enable / disable providers per toggle
 *  - Drag to reorder (lower position = higher priority)
 *  - Edit the base URL (e.g. point Consumet to a self-hosted instance)
 *  - Test a single provider or all providers against a sample anime
 *  - Add a fully custom provider with a URL template
 *  - Reset all providers to their defaults
 *
 * The [StreamFetcher] object reads [ProviderRepository] on every fetch call,
 * so changes here take effect immediately on the next playback attempt.
 */
class ProviderSourcesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderSourcesBinding
    private lateinit var adapter: ProviderSourcesAdapter

    // Sample anime used for provider tests (well-known title, ep 1)
    private val testTitle   = "Frieren: Beyond Journey's End"
    private val testMalId   = 52991
    private val testEpisode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivity(this)
        binding = ActivityProviderSourcesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.providerLayout.updatePadding(top = statusBarHeight, bottom = navBarHeight)

        setupRecycler()
        setupButtons()
    }

    // ── RecyclerView setup ────────────────────────────────────────────────────

    private fun setupRecycler() {
        val providers = ProviderRepository.load().toMutableList()

        adapter = ProviderSourcesAdapter(
            providers      = providers,
            onChanged      = { ProviderRepository.save(it) },
            onEditUrl      = { pos, config -> showEditUrlDialog(pos, config) },
            onTestProvider = { config -> testSingleProvider(config) }
        )

        binding.providerRecycler.layoutManager = LinearLayoutManager(this)
        binding.providerRecycler.adapter = adapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                rv: RecyclerView,
                from: RecyclerView.ViewHolder,
                to: RecyclerView.ViewHolder
            ): Boolean {
                adapter.move(from.bindingAdapterPosition, to.bindingAdapterPosition)
                return true
            }
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
        })
        touchHelper.attachToRecyclerView(binding.providerRecycler)
        adapter.attachTouchHelper(touchHelper)
    }

    // ── Button wiring ─────────────────────────────────────────────────────────

    private fun setupButtons() {
        binding.providerBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.providerAddCustom.setOnClickListener { showAddCustomDialog() }

        binding.providerReset.setOnClickListener {
            customAlertDialog().apply {
                setTitle(getString(R.string.provider_reset_title))
                setMessage(getString(R.string.provider_reset_confirm))
                setPosButton(getString(R.string.provider_reset)) {
                    ProviderRepository.reset()
                    adapter.replaceAll(ProviderRepository.load())
                    snackString(getString(R.string.provider_reset_done))
                }
                setNegButton(getString(R.string.cancel))
                show()
            }
        }

        binding.providerTestAll.setOnClickListener { testAllProviders() }
    }

    // ── Edit base URL dialog ──────────────────────────────────────────────────

    private fun showEditUrlDialog(position: Int, config: ProviderConfig) {
        val input = EditText(this).apply {
            setText(config.baseUrl)
            hint = "https://api.consumet.org"
            setPadding(48, 24, 48, 24)
        }
        customAlertDialog().apply {
            setTitle(getString(R.string.provider_edit_url_title, config.name))
            setCustomView(input)
            setPosButton(getString(R.string.save)) {
                val newUrl = input.text.toString().trim().trimEnd('/')
                if (newUrl.startsWith("http")) {
                    adapter.updateUrl(position, newUrl)
                } else {
                    snackString(getString(R.string.provider_invalid_url))
                }
            }
            setNegButton(getString(R.string.cancel))
            show()
        }
    }

    // ── Add custom provider dialog ────────────────────────────────────────────

    private fun showAddCustomDialog() {
        val dialogBinding = DialogAddProviderBinding.inflate(LayoutInflater.from(this))
        customAlertDialog().apply {
            setTitle(getString(R.string.provider_add_custom_title))
            setCustomView(dialogBinding.root)
            setPosButton(getString(R.string.add)) {
                val name = dialogBinding.providerDialogName.text?.toString()?.trim() ?: ""
                val url  = dialogBinding.providerDialogUrl.text?.toString()?.trim()?.trimEnd('/') ?: ""
                when {
                    name.isEmpty()          -> snackString(getString(R.string.provider_name_empty))
                    !url.startsWith("http") -> snackString(getString(R.string.provider_invalid_url))
                    else -> {
                        adapter.add(
                            ProviderConfig(
                                id       = "custom_${System.currentTimeMillis()}",
                                name     = name,
                                baseUrl  = url,
                                type     = ProviderType.CUSTOM,
                                enabled  = true,
                                priority = adapter.itemCount
                            )
                        )
                        snackString(getString(R.string.provider_added, name))
                    }
                }
            }
            setNegButton(getString(R.string.cancel))
            show()
        }
    }

    // ── Single-provider test ──────────────────────────────────────────────────

    private fun testSingleProvider(config: ProviderConfig) {
        snackString(getString(R.string.provider_testing, config.name))
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    StreamFetcher.fetchFromProvider(config, testTitle, testMalId, testEpisode, false)
                }.getOrNull()
            }
            if (result != null) {
                snackString(getString(R.string.provider_test_ok, config.name, result.quality))
            } else {
                snackString(getString(R.string.provider_test_fail, config.name))
            }
        }
    }

    // ── Test all enabled providers ────────────────────────────────────────────

    private fun testAllProviders() {
        snackString(getString(R.string.provider_testing_all))
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    StreamFetcher.fetchStreamUrl(testTitle, testMalId, testEpisode, false)
                }.getOrNull()
            }
            if (result != null) {
                val preview = result.url.take(60) + if (result.url.length > 60) "…" else ""
                snackString(getString(R.string.provider_test_all_ok, result.providerName, preview))
            } else {
                snackString(getString(R.string.provider_test_all_fail))
            }
        }
    }
}
