package com.sanin.tv.home
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.databinding.DialogQrLoginBinding
import com.sanin.tv.databinding.DialogUserAgentBinding
import com.sanin.tv.databinding.FragmentLoginBinding
import com.sanin.tv.okHttpClient
import com.sanin.tv.openLinkInBrowser
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.settings.saving.internal.PreferenceKeystore
import com.sanin.tv.settings.saving.internal.PreferencePackager
import com.sanin.tv.startMainActivity
import com.sanin.tv.toast
import com.sanin.tv.util.Logger
import com.sanin.tv.util.QrUtils
import com.sanin.tv.util.customAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rescueMode = PrefManager.getVal<Boolean>(PrefName.RescueMode)

        if (rescueMode) {
            // MAL mode — keep the original browser-based flow, no QR
            binding.loginButton.text = getString(R.string.login)
            (binding.loginButton as com.google.android.material.button.MaterialButton)
                .setIconResource(R.drawable.ic_myanimelist)
            binding.loginButton.setOnClickListener {
                com.sanin.tv.connections.mal.MAL.loginIntent(requireActivity())
            }
            binding.loginQrButton.visibility = View.GONE
        } else {
            // AniList mode — browser button + QR button
            binding.loginButton.setOnClickListener { Anilist.loginIntent(requireActivity()) }
            binding.loginQrButton.visibility = View.VISIBLE
            binding.loginQrButton.setOnClickListener { showAnilistQrDialog() }
        }
        binding.loginTelegram.setOnClickListener { openLinkInBrowser(getString(R.string.telegram)) }

        val openDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    try {
                        val jsonString =
                            requireActivity().contentResolver.openInputStream(uri)?.readBytes()
                                ?: throw Exception("Error reading file")
                        val name =
                            DocumentFile.fromSingleUri(requireActivity(), uri)?.name ?: "settings"
                        when {
                            name.endsWith(".sani") -> passwordAlertDialog { password ->
                                if (password != null) {
                                    val salt = jsonString.copyOfRange(0, 16)
                                    val encrypted = jsonString.copyOfRange(16, jsonString.size)
                                    val decryptedJson = try {
                                        PreferenceKeystore.decryptWithPassword(
                                            password, encrypted, salt
                                        )
                                    } catch (e: Exception) {
                                        toast("Incorrect password")
                                        return@passwordAlertDialog
                                    }
                                    if (PreferencePackager.unpack(decryptedJson)) restartApp()
                                } else {
                                    toast("Password cannot be empty")
                                }
                            }
                            name.endsWith(".ani") -> {
                                val json = jsonString.toString(Charsets.UTF_8)
                                if (PreferencePackager.unpack(json)) restartApp()
                            }
                            else -> toast("Invalid file type")
                        }
                    } catch (e: Exception) {
                        Logger.log(e)
                        toast("Error importing settings")
                    }
                }
            }

        binding.importSettingsButton.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("*/*"))
        }
    }

    // ── AniList QR login ──────────────────────────────────────────────────────

    /**
     * Shows a QR code the user scans on their phone to authorise the TV.
     *
     * Automatic flow (requires SaninTV relay server):
     *  1. TV creates a short-lived session on the relay server.
     *  2. QR code encodes the relay URL for that session.
     *  3. User scans on their phone → browser opens relay page → user taps
     *     "Connect with AniList" → AniList OAuth → relay page captures the
     *     token automatically and sends it to the relay server.
     *  4. TV polls the relay server every 2 s.  When the token arrives the TV
     *     logs in with no further user interaction.
     *
     * Fallback (manual paste) is preserved for offline / self-hosted use via
     * the "Paste from Clipboard" button.
     *
     * ⚠️  Register the relay host as a redirect URI in your AniList OAuth app
     *     settings (https://anilist.co/settings/developer) so AniList accepts
     *     the callback URL:  <QR_RELAY_HOST>/api/qr-auth/<sessionId>
     */
    private fun showAnilistQrDialog() {
        val dialogBinding = DialogQrLoginBinding.inflate(layoutInflater)
        var pollJob: Job? = null
        var dismissing = false
        var dialogRef: AlertDialog? = null

        fun isValidToken(s: String) =
            s.length >= 40 && s.all { it.isLetterOrDigit() || it == '_' || it == '-' }

        fun submitToken(token: String) {
            if (dismissing) return
            dismissing = true
            pollJob?.cancel()
            dialogRef?.dismiss()
            Anilist.token = token
            PrefManager.setVal(PrefName.AnilistToken, token)
            toast(getString(R.string.qr_login_save_token))
            startMainActivity(requireActivity())
        }

        fun startCountdown(token: String) {
            pollJob?.cancel()
            pollJob = lifecycleScope.launch {
                for (i in 3 downTo 1) {
                    withContext(Dispatchers.Main) {
                        dialogBinding.qrCountdownText.visibility = View.VISIBLE
                        dialogBinding.qrCountdownText.text =
                            getString(R.string.qr_login_auto_submit, i)
                    }
                    delay(1000L)
                }
                withContext(Dispatchers.Main) { submitToken(token) }
            }
        }

        // ── Manual paste fallback ─────────────────────────────────────────────
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString()?.trim() ?: ""
                if (isValidToken(text)) startCountdown(text)
                else {
                    pollJob?.cancel()
                    dialogBinding.qrCountdownText.visibility = View.GONE
                }
            }
        }
        dialogBinding.qrTokenInput.addTextChangedListener(watcher)

        // ── Build the dialog with a loading QR placeholder ───────────────────
        dialogBinding.qrInstructions.text = getString(R.string.qr_login_anilist_instructions)
        dialogBinding.qrTokenInputLayout.hint = getString(R.string.qr_login_paste_token)

        requireActivity().customAlertDialog()
            .setTitle(R.string.qr_login_title_anilist)
            .setCustomView(dialogBinding.root)
            .setNeutralButton(R.string.qr_login_paste_clipboard)
            .setPosButton(R.string.ok) {
                pollJob?.cancel()
                val token = dialogBinding.qrTokenInput.text?.toString()?.trim() ?: ""
                if (token.isBlank()) {
                    toast(getString(R.string.qr_login_invalid_token))
                    return@setPosButton
                }
                submitToken(token)
            }
            .setNegButton(R.string.cancel) { pollJob?.cancel() }
            .onDismiss { pollJob?.cancel() }
            .attach { d -> dialogRef = d }
            .setOnShowListener {
                // Clipboard paste button — keep dialog open
                dialogRef?.getButton(AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
                    val cb = requireContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val text = cb.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: ""
                    if (text.isNotBlank()) dialogBinding.qrTokenInput.setText(text)
                    else toast(getString(R.string.qr_login_clipboard_empty))
                }

                // ── Try relay server for auto-login ──────────────────────────
                pollJob = lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val sessionId = createQrSession()
                        if (sessionId != null) {
                            val relayUrl = "${QR_RELAY_HOST}/api/qr-auth/$sessionId"
                            withContext(Dispatchers.Main) {
                                dialogBinding.qrCodeImage.setImageBitmap(
                                    QrUtils.generateQrBitmap(relayUrl, 512)
                                )
                                dialogBinding.qrInstructions.text =
                                    getString(R.string.qr_login_anilist_instructions)
                            }
                            // Poll until token arrives, dialog is dismissed, or session expires
                            pollForToken(sessionId) { token ->
                                withContext(Dispatchers.Main) { submitToken(token) }
                            }
                        } else {
                            // Relay unavailable — fall back to the direct OAuth URL
                            val fallbackUrl = "https://anilist.co/api/v2/oauth/authorize" +
                                    "?client_id=${Anilist.clientID}&response_type=token"
                            withContext(Dispatchers.Main) {
                                dialogBinding.qrCodeImage.setImageBitmap(
                                    QrUtils.generateQrBitmap(fallbackUrl, 512)
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Logger.log("QR relay error: ${e.message}")
                        // Fall back to direct OAuth QR
                        val fallbackUrl = "https://anilist.co/api/v2/oauth/authorize" +
                                "?client_id=${Anilist.clientID}&response_type=token"
                        withContext(Dispatchers.Main) {
                            dialogBinding.qrCodeImage.setImageBitmap(
                                QrUtils.generateQrBitmap(fallbackUrl, 512)
                            )
                        }
                    }
                }
            }
            .show()
    }

    /** POST /api/qr-session → returns sessionId, or null if the relay is unreachable. */
    private fun createQrSession(): String? = try {
        val body = "{}".toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$QR_RELAY_HOST/api/qr-session")
            .post(body)
            .build()
        okHttpClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val json = JSONObject(resp.body?.string() ?: return null)
            json.optString("sessionId").takeIf { it.isNotBlank() }
        }
    } catch (e: Exception) {
        Logger.log("createQrSession error: ${e.message}")
        null
    }

    /**
     * Polls GET /api/qr-session/{id}/poll every [POLL_INTERVAL_MS] until a token
     * arrives, the coroutine is cancelled, or the session expires (HTTP 404/410).
     */
    private suspend fun pollForToken(
        sessionId: String,
        onToken: suspend (String) -> Unit,
    ) {
        val pollUrl = "$QR_RELAY_HOST/api/qr-session/$sessionId/poll"
        while (true) {
            delay(POLL_INTERVAL_MS)
            try {
                val request = Request.Builder().url(pollUrl).get().build()
                val responseText = okHttpClient.newCall(request).execute().use { resp ->
                    when (resp.code) {
                        404, 410 -> return   // session gone / expired
                        200      -> resp.body?.string()
                        else     -> null
                    }
                } ?: continue

                val json = JSONObject(responseText)
                val token = json.optString("token").takeIf { it.isNotBlank() }
                if (token != null) {
                    onToken(token)
                    return
                }
                // json.optBoolean("waiting") == true → keep polling
            } catch (e: Exception) {
                Logger.log("pollForToken error: ${e.message}")
                // Network hiccup — retry after next interval
            }
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private fun passwordAlertDialog(callback: (CharArray?) -> Unit) {
        val password = CharArray(16).apply { fill('0') }
        val dialogView = DialogUserAgentBinding.inflate(layoutInflater).apply {
            userAgentTextBox.hint = "Password"
            subtitle.visibility = View.VISIBLE
            subtitle.text = getString(R.string.enter_password_to_decrypt_file)
        }
        requireActivity().customAlertDialog().apply {
            setTitle("Enter Password")
            setCustomView(dialogView.root)
            setPosButton(R.string.ok) {
                val editText = dialogView.userAgentTextBox
                if (editText.text?.isNotBlank() == true) {
                    editText.text?.toString()?.trim()?.toCharArray(password)
                    callback(password)
                } else {
                    toast("Password cannot be empty")
                }
            }
            setNegButton(R.string.cancel) { password.fill('0'); callback(null) }
        }.show()
    }

    private fun restartApp() {
        val intent = Intent(requireActivity(), requireActivity().javaClass)
        requireActivity().finish()
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Base URL of the SaninTV relay server (no trailing slash).
         *
         * Configurable via local.properties:  QR_RELAY_HOST=https://your-relay-server.example.com
         * Falls back to the default relay if not set.
         * Must be registered as a redirect URI in the AniList OAuth app settings:
         *   https://anilist.co/settings/developer  →  <QR_RELAY_HOST>/api/qr-auth/<sessionId>
         */
        val QR_RELAY_HOST: String
            get() = com.sanin.tv.BuildConfig.QR_RELAY_HOST

        /** How often (ms) to poll the relay server for the authorisation token. */
        private const val POLL_INTERVAL_MS = 2_000L
    }
}
