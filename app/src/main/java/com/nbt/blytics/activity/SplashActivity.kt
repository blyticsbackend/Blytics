package com.nbt.blytics.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.nbt.blytics.R
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.THEME
import com.nbt.blytics.utils.switchToThemeMode

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION /*or View.SYSTEM_UI_FLAG_FULLSCREEN*/
        }
        setSystemMode()
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            if (pref.getBooleanValue(SharePreferences.PASSWORD_LOCK_IS_ACTIVE).not()) {
                val intent = if (pref.getStringValue(SharePreferences.USER_ID, "").isBlank()) {
                    Intent(this, UserActivity::class.java)
                } else {
                    Intent(this, MainActivity::class.java)
                }
                startActivity(intent)
                finish()
            } else {
                pref.setStringValue(pref.USER_ID, "")
                startActivity(Intent(this@SplashActivity, UserActivity::class.java))
                finish()
            }
        }, 1000)
    }

    private fun setSystemMode() {
        if (pref.getBooleanValue(SharePreferences.IS_MODE_SELECTED)) {
            val mode = pref.getIntValue(SharePreferences.SELECTED_MODE)
            if (MainActivity.isModeChange.not()) {
                when (mode) {
                    0 -> {
                        MainActivity.isModeChange = true
                        switchToThemeMode(THEME.MODE_DEFAULT)
                        Log.d("Check_Theme==", "  0")
                    }

                    1 -> {
                        MainActivity.isModeChange = true
                        switchToThemeMode(THEME.MODE_NIGHT)
                        Log.d("Check_Theme==", "  1")
                    }

                    else -> {
                        MainActivity.isModeChange = true
                    }
                }
            }
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Log.d("Check_Theme==", "  DefaultNightMode")
        }
    }
}