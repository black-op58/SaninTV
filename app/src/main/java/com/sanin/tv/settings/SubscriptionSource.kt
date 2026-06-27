package com.sanin.tv.settings
class SubscriptionSourceAdapter(context: android.content.Context) :
    androidx.recyclerview.widget.RecyclerView.Adapter<SubscriptionSourceAdapter.ViewHolder>() {
    class ViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
    if (which == SimpleDialog.OnDialogResultListener.BUTTON_POSITIVE) {
    if (dialogTag == "colorPicker") {
    val color = extras.getInt(SimpleColorDialog.COLOR)                PrefManager.setVal(PrefName.CustomThemeInt, color)                Logger.log("Custom Theme: $color")            }
}
return true    }
fun reload() {        PrefManager.setCustomVal("reload", true)        Handler(Looper.getMainLooper()).postDelayed({            reloadActivity()            finishAndRemoveTask()        }, 100)    }}
binding.extensionIconImageView.layoutParams = layoutParams        parserIcon?.let {            binding.extensionIconImageView.setImageDrawable(it)        } ?: run {            binding.extensionIconImageView.setImageResource(R.drawable.control_background_40dp)        }
binding.extensionPinImageView.visibility = View.GONE        binding.extensionVersionTextView.visibility = View.GONE        binding.deleteTextView.visibility = View.VISIBLE        binding.deleteTextView.setOnClickListener {            showRemoveAllSubscriptionsDialog(it.context)        }
binding.updateTextView.visibility = View.GONE        binding.settingsImageView.visibility = View.GONE    }
private fun updateSubscriptionCount() {        binding.subscriptionCount.text = subscriptions.size.toString()        binding.subscriptionCount.visibility =          
if (subscriptions.isEmpty()) View.GONE else View.VISIBLE    }
private fun showRemoveAllSubscriptionsDialog(context: Context) {        context.customAlertDialog().apply {            setTitle(R.string.remove_all_subscriptions)            setMessage(R.string.remove_all_subscriptions_desc, parserName)            setPosButton(R.string.ok) { removeAllSubscriptions() }
setNegButton(R.string.cancel)            show()        }
}
private fun removeAllSubscriptions() {        subscriptions.forEach { subscription ->            SubscriptionHelper.deleteSubscription(subscription.id, false)        }
if (isExpanded) {
    val startPosition = adapter.getAdapterPosition(this) + 1            repeat(subscriptions.size) {                adapter.removeGroupAtAdapterPosition(startPosition)            }
}
subscriptions.clear()        onGroupRemoved(this)    }
private fun removeSubscription(id: Any?) {        subscriptions.removeAll { it.id == id }
updateSubscriptionCount()      
if (subscriptions.isEmpty()) {            onGroupRemoved(this)
} else {            adapter.notifyItemChanged(adapter.getAdapterPosition(this))        }
}
private fun toggleSubscriptions() {
    val startPosition = adapter.getAdapterPosition(this) + 1
if (isExpanded) {            subscriptions.forEachIndexed { index, subscribeMedia ->                adapter.add(                    startPosition + index,                    SubscriptionItem(subscribeMedia.id, subscribeMedia, adapter) { removedId ->                        removeSubscription(removedId)                    })            }
} else {            repeat(subscriptions.size) {                adapter.removeGroupAtAdapterPosition(startPosition)            }
}
}
override fun getLayout(): Int = R.layout.item_extension  
override fun initializeViewBinding(view: View): ItemExtensionBinding =        ItemExtensionBinding.bind(view)
