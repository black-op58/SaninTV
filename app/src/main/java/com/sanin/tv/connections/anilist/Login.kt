package com.sanin.tv.connections.anilist
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sanin.tv.logError
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.startMainActivity
import com.sanin.tv.themes.ThemeManager
class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)        ThemeManager(this).applyTheme()        
val data: Uri? = intent?.data
try {            Anilist.token =                Regex("""(?<=access_token=).+(?=&token_type)""").find(data.toString())!!.value            PrefManager.setVal(PrefName.AnilistToken, Anilist.token ?: "")        } catch (e: Exception) {            logError(e)        }        startMainActivity(this)    }}