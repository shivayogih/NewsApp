package com.springr.newsapplication.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.pm.PackageInfoCompat
import com.springr.newsapplication.R
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }else {
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        setContentView(R.layout.activity_splash)
        versionInfo
        activityScope.launch {
            delay(2000)
            val intent = Intent(this@SplashActivity, NewsActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
    private val versionInfo: Unit
        get() {
            val context = applicationContext
            val manager = context.packageManager
            try {
                val info = manager.getPackageInfo(context.packageName, 0)
             val   versionName = info.versionName
                val   versionCode = PackageInfoCompat.getLongVersionCode(info).toInt()
                tvVersion!!.text = "Version: $versionName"
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                val   versionName = "!!!"
            }
        }
    override fun onPause() {
        activityScope.cancel()
        super.onPause()
    }
}