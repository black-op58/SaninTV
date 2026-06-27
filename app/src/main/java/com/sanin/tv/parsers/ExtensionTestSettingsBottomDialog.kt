package com.sanin.tv.parsers
class ExtensionTestSettingsBottomDialog : com.google.android.material.bottomsheet.BottomSheetDialogFragment() {

private fun setup() {

val viewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[ExtensionTestViewModel::class.java]
viewModel.let {

else -> emptyList()        }        adapter.clear()        extDataList.forEach { data ->            
val isSelected = extensionsToTest.contains(data.name)            adapter.add(ExtensionSelectItem(data.name, data.icon, data.iconUrl, isSelected, ::selectedCallback))        }    }

private fun selectedCallback(name: String, isSelected: Boolean) {
if (isSelected) {            extensionsToTest.add(name)
} else {            extensionsToTest.remove(name)        }    }

companion object {
    fun newInstance(): ExtensionTestSettingsBottomDialog {
return ExtensionTestSettingsBottomDialog()        }

var extensionType = "anime"        
var testType = "basic"        
var searchQuery = ""        
var extensionsToTest: MutableList<String> = mutableListOf()    }}