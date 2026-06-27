package com.sanin.tv.settings

import com.sanin.tv.databinding.ItemSettingsBinding
import com.sanin.tv.databinding.ItemSettingsSwitchBinding
import com.sanin.tv.settings.saving.PrefName

data class Settings(
    val type: Int,
    val name: String,
    val desc: String,
    val icon: Int,
    val onClick: ((ItemSettingsBinding) -> Unit)? = null,
    val onLongClick: (() -> Unit)? = null,
    val switch: ((isChecked: Boolean, view: ItemSettingsSwitchBinding) -> Unit)? = null,
    val attach: ((ItemSettingsBinding) -> Unit)? = null,
    val attachToSwitch: ((ItemSettingsSwitchBinding) -> Unit)? = null,
    val isVisible: Boolean = true,
    val isActivity: Boolean = false,
    var isChecked: Boolean = false,
    val focusEffect: String = "glow",
    val pref: PrefName? = null,
)
