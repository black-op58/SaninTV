package com.sanin.tv.home

binding.characterSearch.setOnClickListener {
    startActivity(requireContext(), SearchType.CHARACTER)            dismiss()
}        binding.staffSearch.setOnClickListener {
    startActivity(requireContext(), SearchType.STAFF)            dismiss()
}        binding.studioSearch.setOnClickListener {
    startActivity(requireContext(), SearchType.STUDIO)            dismiss()
}        binding.userSearch.setOnClickListener {
    startActivity(requireContext(), SearchType.USER)            dismiss()
}
}

private fun startActivity(context: Context, type: SearchType) {
    ContextCompat.startActivity(            context,            Intent(context, SearchActivity::class.java).putExtra("type", type.toAnilistString()),            null        )
}

override fun onDestroyView() {
    super.onDestroyView()        _binding = null
}

companion object {
    fun newInstance() = SearchBottomSheet()
