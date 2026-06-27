package com.sanin.tv.settings

// Buttons
if (mode == "nothing") {
    binding.previewButton1.visibility = View.GONE            binding.previewButton2.visibility = View.GONE
} else {
    binding.previewButton1.visibility = View.VISIBLE            binding.previewButton2.visibility = View.VISIBLE            binding.previewButton1.text = when (mode) {
    "mal" -> "VIEW ON MYANIMELIST"
else -> "VIEW ON ANILIST"
}            binding.previewButton2.text = when (mode) {
    "sanintv" -> "SANINTV PROFILE"
else -> "VIEW PROFILE"
}
}
}

override fun onDestroy() {
    tokenRefreshJob?.cancel()        _binding = null        super.onDestroy()
