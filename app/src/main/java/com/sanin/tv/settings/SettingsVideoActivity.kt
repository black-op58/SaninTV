package com.sanin.tv.settings

import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.R
import com.sanin.tv.databinding.ActivitySettingsBinding
import com.sanin.tv.navBarHeight
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.statusBarHeight
import com.sanin.tv.util.customAlertDialog

class SettingsVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            settingsContainer.updatePadding(top = statusBarHeight, bottom = navBarHeight)

            val qualityOptions = arrayOf(
                getString(R.string.quality_auto),
                getString(R.string.quality_fast),
                getString(R.string.quality_high),
                getString(R.string.quality_custom)
            )
            val videoOutputOptions = arrayOf(
                getString(R.string.video_output_gpu),
                getString(R.string.video_output_opengl),
                getString(R.string.video_output_vulkan)
            )
            val gpuContextOptions = arrayOf(
                getString(R.string.gpu_context_auto),
                getString(R.string.gpu_context_opengl),
                getString(R.string.gpu_context_vulkan)
            )
            val upscalingOptions = arrayOf(
                getString(R.string.upscaling_bilinear),
                getString(R.string.upscaling_bicubic),
                getString(R.string.upscaling_lanczos),
                getString(R.string.upscaling_nnedi3)
            )

            val settingsList = listOf(
                // ── Quality ───────────────────────────────────────────────────
                Settings(
                    type = 1,
                    name = getString(R.string.quality_profile),
                    desc = qualityOptions.getOrElse(PrefManager.getVal<Int>(PrefName.QualityProfile)) { qualityOptions[0] },
                    icon = R.drawable.ic_round_auto_awesome_24,
                    onClick = { b ->
                        val cur = PrefManager.getVal<Int>(PrefName.QualityProfile)
                        customAlertDialog().apply {
                            setTitle(R.string.quality_profile)
                            singleChoiceItems(qualityOptions, cur) { idx ->
                                PrefManager.setVal(PrefName.QualityProfile, idx)
                                b.settingsDesc.text = qualityOptions.getOrElse(idx) { qualityOptions[0] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.auto_hide_timeout),
                    desc = getString(R.string.auto_hide_timeout_desc),
                    icon = R.drawable.ic_round_fast_forward_24,
                    onClick = { b ->
                        val cur = PrefManager.getVal<Int>(PrefName.AutoHideTimeout)
                        val options = (2..10).map { "${it}s" }.toTypedArray()
                        customAlertDialog().apply {
                            setTitle(R.string.auto_hide_timeout)
                            singleChoiceItems(options, cur - 2) { idx ->
                                val value = idx + 2
                                PrefManager.setVal(PrefName.AutoHideTimeout, value)
                                b.settingsDesc.text = "${value}s"
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.buffer_size),
                    desc = "${PrefManager.getVal<Int>(PrefName.BufferSize)} MB",
                    icon = R.drawable.ic_round_area_chart_24,
                    onClick = { b ->
                        val cur = PrefManager.getVal<Int>(PrefName.BufferSize)
                        val options = listOf(16, 24, 32, 48, 64, 96, 128).map { "${it} MB" }.toTypedArray()
                        val sizes  = listOf(16, 24, 32, 48, 64, 96, 128)
                        customAlertDialog().apply {
                            setTitle(R.string.buffer_size)
                            singleChoiceItems(options, sizes.indexOf(cur).coerceAtLeast(0)) { idx ->
                                PrefManager.setVal(PrefName.BufferSize, sizes[idx])
                                b.settingsDesc.text = "${sizes[idx]} MB"
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                // ── Decoding ──────────────────────────────────────────────────
                Settings(
                    type = 2,
                    name = getString(R.string.hardware_decoding),
                    desc = getString(R.string.hardware_decoding_desc),
                    icon = R.drawable.ic_round_dns_24,
                    isChecked = PrefManager.getVal(PrefName.HardwareDecoding),
                    switch = { isChecked, _ ->
                        PrefManager.setVal(PrefName.HardwareDecoding, isChecked)
                    }
                ),
                // ── Rendering ─────────────────────────────────────────────────
                Settings(
                    type = 1,
                    name = getString(R.string.video_output),
                    desc = videoOutputOptions.getOrElse(PrefManager.getVal<Int>(PrefName.VideoOutput)) { videoOutputOptions[0] },
                    icon = R.drawable.ic_round_movie_filter_24,
                    onClick = { b ->
                        val cur = PrefManager.getVal<Int>(PrefName.VideoOutput)
                        customAlertDialog().apply {
                            setTitle(R.string.video_output)
                            singleChoiceItems(videoOutputOptions, cur) { idx ->
                                PrefManager.setVal(PrefName.VideoOutput, idx)
                                b.settingsDesc.text = videoOutputOptions.getOrElse(idx) { videoOutputOptions[0] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.gpu_context),
                    desc = gpuContextOptions.getOrElse(PrefManager.getVal<Int>(PrefName.GpuContext)) { gpuContextOptions[0] },
                    icon = R.drawable.ic_round_dns_24,
                    onClick = { b ->
                        val cur = PrefManager.getVal<Int>(PrefName.GpuContext)
                        customAlertDialog().apply {
                            setTitle(R.string.gpu_context)
                            singleChoiceItems(gpuContextOptions, cur) { idx ->
                                PrefManager.setVal(PrefName.GpuContext, idx)
                                b.settingsDesc.text = gpuContextOptions.getOrElse(idx) { gpuContextOptions[0] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                // ── Image quality ─────────────────────────────────────────────
                Settings(
                    type = 2,
                    name = getString(R.string.debanding),
                    desc = getString(R.string.debanding_desc),
                    icon = R.drawable.ic_round_auto_awesome_24,
                    isChecked = PrefManager.getVal(PrefName.Debanding),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.Debanding, isChecked) }
                ),
                Settings(
                    type = 2,
                    name = getString(R.string.interpolation),
                    desc = getString(R.string.interpolation_desc),
                    icon = R.drawable.ic_round_animation_24,
                    isChecked = PrefManager.getVal(PrefName.Interpolation),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.Interpolation, isChecked) }
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.upscaling_algorithm),
                    desc = upscalingOptions.getOrElse(PrefManager.getVal<Int>(PrefName.UpscalingAlgorithm)) { upscalingOptions[0] },
                    icon = R.drawable.ic_round_photo_size_select_actual_24,
                    onClick = { b ->
                        val cur = PrefManager.getVal<Int>(PrefName.UpscalingAlgorithm)
                        customAlertDialog().apply {
                            setTitle(R.string.upscaling_algorithm)
                            singleChoiceItems(upscalingOptions, cur) { idx ->
                                PrefManager.setVal(PrefName.UpscalingAlgorithm, idx)
                                b.settingsDesc.text = upscalingOptions.getOrElse(idx) { upscalingOptions[0] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                // ── Raw config ────────────────────────────────────────────────
                Settings(
                    type = 1,
                    name = getString(R.string.raw_configuration),
                    desc = getString(R.string.raw_configuration_desc),
                    icon = R.drawable.ic_round_dns_24,
                    onClick = { _ ->
                        val container = LinearLayout(this@SettingsVideoActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(48, 16, 48, 16)
                        }
                        val editText = EditText(this@SettingsVideoActivity).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            minLines = 6
                            maxLines = 16
                            hint = "key=value\nkey2=value2"
                            setText(PrefManager.getVal<String>(PrefName.RawConfiguration))
                        }
                        container.addView(editText)
                        customAlertDialog().apply {
                            setTitle(R.string.raw_configuration)
                            setCustomView(container)
                            setPosButton(R.string.ok) {
                                PrefManager.setVal(PrefName.RawConfiguration, editText.text.toString())
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                )
            )

            settingsRecyclerView.adapter = SettingsAdapter(settingsList, this@SettingsVideoActivity)
            settingsRecyclerView.layoutManager = LinearLayoutManager(this@SettingsVideoActivity)
            settingsRecyclerView.isFocusable = true
            settingsRecyclerView.isFocusableInTouchMode = false
            settingsRecyclerView.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    finish(); true
                } else false
            }
            settingsBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
    }
}
