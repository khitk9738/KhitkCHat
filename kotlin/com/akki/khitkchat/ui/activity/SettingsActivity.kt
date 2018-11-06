package com.akki.khitkchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.akki.khitkchat.R
import com.akki.khitkchat.ui.fragment.SettingsFragment

class SettingsActivity : SkeletonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings, ActivityType.CHILD_ACTIVITY)

        fragmentManager.beginTransaction()
                .add(R.id.fl_settings_container, SettingsFragment())
                .commitAllowingStateLoss()
    }

    companion object {

        fun start(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }
}
