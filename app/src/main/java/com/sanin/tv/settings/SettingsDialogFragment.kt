package com.sanin.tv.settings

import kotlin.concurrent.schedule
class SettingsDialogFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetSettingsBinding? = null    
private val binding get() = _binding!!    
private lateinit var pageType: PageType    
override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)        pageType = arguments?.getSerializableCompat("pageType") as? PageType ?: PageType.HOME    }

override fun onCreateView(        inflater: LayoutInflater,        container: ViewGroup?,        savedInstanceState: Bundle?    ): View {        _binding = BottomSheetSettingsBinding.inflate(inflater, container, false)
return binding.root    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {        super.onViewCreated(view, savedInstanceState)        
val window = dialog?.window        window?.statusBarColor = Color.CYAN        window?.navigationBarColor =            requireContext().getThemeColor(com.google.android.material.R.attr.colorSurface)        
val isRescueModeEarly: Boolean = PrefManager.getVal(PrefName.RescueMode)        
val notificationIcon = if (!isRescueModeEarly && Anilist.unreadNotificationCount > 0) {            R.drawable.ic_round_notifications_active_24
} else {            R.drawable.ic_round_notifications_none_24        }        binding.settingsNotification.setImageResource(notificationIcon)
if (isRescueModeEarly) binding.settingsNotification.visibility = View.GONE
if (Anilist.token != null) {            binding.settingsLogin.setText(R.string.logout)            binding.settingsLogin.setOnClickListener {                requireContext().customAlertDialog().apply {                    setTitle(R.string.logout)                    setMessage(R.string.logout_confirm)                    setPosButton(R.string.yes) {                        Anilist.removeSavedToken()                        startMainActivity(requireActivity())                    }                    setNegButton(R.string.no)                    show()                }            }

val isRescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)            binding.settingsUsername.text = if (isRescueMode) MAL.username ?: "MAL User" else Anilist.username            binding.settingsUserAvatar.loadImage(if (isRescueMode) MAL.avatar else Anilist.avatar)
} else {            binding.settingsUsername.visibility = View.GONE            binding.settingsLogin.setText(R.string.login)            binding.settingsLogin.setOnClickListener {                dismiss()                Anilist.loginIntent(requireActivity())            }        }

val isRescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)        binding.settingsNotificationCount.isVisible = !isRescueMode && Anilist.unreadNotificationCount > 0        binding.settingsNotificationCount.text = Anilist.unreadNotificationCount.toString()
if (isRescueMode) {            binding.settingsActivity.visibility = View.GONE        }        binding.settingsUserAvatar.setOnClickListener {
if (isRescueMode) {
    val malUsername = MAL.username
if (!malUsername.isNullOrBlank()) {                    openLinkInCustomTab("https://myanimelist.net/profile/$malUsername")
} else {                    snackString(getString(R.string.rescue_mode_active))                }                return@setOnClickListener            }            ContextCompat.startActivity(                requireContext(), Intent(requireContext(), ProfileActivity::class.java)                    .putExtra("userId", Anilist.userid), null            )        }        binding.settingsIncognito.isChecked = PrefManager.getVal(PrefName.Incognito)        binding.settingsIncognito.setOnCheckedChangeListener { _, isChecked ->            // Added check to ensure fragment is still active before updating
if (isAdded) {                PrefManager.setVal(PrefName.Incognito, isChecked)                incognitoNotification(requireContext())            }        }        binding.settingsRescueMode.isChecked = PrefManager.getVal(PrefName.RescueMode)        binding.settingsRescueMode.setOnCheckedChangeListener { _, isChecked ->            PrefManager.setVal(PrefName.RescueMode, isChecked)            activity?.let { act ->                dismiss()                
val intent = Intent(act, MainActivity::class.java)                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)                act.startActivity(intent)                act.overridePendingTransition(0, 0)                act.finish()                act.overridePendingTransition(0, 0)            }        }        binding.settingsExtensionSettings.setSafeOnClickListener {            startActivity(Intent(activity, ExtensionsActivity::class.java))            dismiss()        }        binding.settingsSettings.setSafeOnClickListener {            startActivity(Intent(activity, SettingsActivity::class.java))            dismiss()        }        binding.settingsActivity.setSafeOnClickListener {
if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) {                snackString(getString(R.string.rescue_mode_active))                return@setSafeOnClickListener
}}}}