package com.sanin.tv.settings

_binding = null        super.onDestroyView()    }

companion object {
    fun newInstance(subscriptions: Map<Int, SubscriptionHelper.Companion.SubscribeMedia>): SubscriptionsBottomDialog {
    val dialog = SubscriptionsBottomDialog()            dialog.subscriptions = subscriptions
return dialog        }
