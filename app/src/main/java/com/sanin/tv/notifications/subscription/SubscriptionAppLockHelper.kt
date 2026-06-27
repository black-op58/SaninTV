package com.sanin.tv.notifications.subscription
import com.sanin.tv.others.calc.CalcActivity
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
object SubscriptionAppLockHelper {
    fun isAppLocked(): Boolean {
return PrefManager.getVal<String>(PrefName.AppPassword).isNotEmpty() && !CalcActivity.hasPermission    }}