package com.akki.khitkchat

import android.app.Activity
import android.app.Application
import android.os.StrictMode
import com.crashlytics.android.Crashlytics
import com.akki.khitkchat.di.ComponentsManager
import com.akki.khitkchat.ui.activity.ChatActivity
import com.akki.khitkchat.ui.activity.ConversationsActivity
import com.akki.khitkchat.ui.util.StartStopActivityLifecycleCallbacks
import io.fabric.sdk.android.Fabric

class ChatApplication : Application() {

    var isConversationsOpened = false
    var currentChat: String? = null

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }

        ComponentsManager.initialize(this)

        registerActivityLifecycleCallbacks(object : StartStopActivityLifecycleCallbacks() {

            override fun onActivityStarted(activity: Activity?) {

                isConversationsOpened = activity is ConversationsActivity

                if (activity is ChatActivity) {
                    currentChat = activity.intent.getStringExtra(ChatActivity.EXTRA_ADDRESS)
                }
            }

            override fun onActivityStopped(activity: Activity?) {

                if (activity is ConversationsActivity) {
                    isConversationsOpened = false
                }

                if (activity is ChatActivity) {
                    currentChat = null
                }
            }
        })

        if (BuildConfig.DEBUG) {

            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }
    }
}
