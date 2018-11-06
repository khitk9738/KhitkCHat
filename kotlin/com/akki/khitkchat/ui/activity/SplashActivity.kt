package com.akki.khitkchat.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.akki.khitkchat.R
import com.akki.khitkchat.data.model.SettingsManagerImpl
import android.view.Window;
import android.media.MediaPlayer
import android.R.raw
import android.view.WindowManager;
import android.widget.VideoView
import android.speech.tts.TextToSpeech
import java.util.Locale




class SplashActivity : AppCompatActivity() {

    var t1: TextToSpeech? = null
    var videoView: VideoView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        t1 = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                t1!!.setLanguage(Locale.UK)
            }
        })


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try {
            videoView = VideoView(this)
            setContentView(videoView)
            val path = Uri.parse("android.resource://" + packageName + "/" + +R.raw.vid1)
            videoView!!.setVideoURI(path)

            videoView!!.setOnCompletionListener(MediaPlayer.OnCompletionListener { jump() })
            videoView!!.start()
        } catch (e: Exception) {
            jump()
        }

        /*Handler().postDelayed({

            val settings = SettingsManagerImpl(this)
            val newIntent = Intent(this,
                    if (settings.getUserName().isEmpty())
                        ProfileActivity::class.java else ConversationsActivity::class.java)

            if (intent.action == Intent.ACTION_SEND) {
                newIntent.action = Intent.ACTION_SEND
                newIntent.type = intent.type
                newIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
                newIntent.putExtra(Intent.EXTRA_STREAM, intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
            }

            startActivity(newIntent)

            finish()
        }, 500)*/
    }

    private fun jump() {

        if (isFinishing)
            return
        val settings = SettingsManagerImpl(this)
        t1!!.speak("Kheet K Chat", TextToSpeech.QUEUE_FLUSH, null);
        val newIntent = Intent(this,
                if (settings.getUserName().isEmpty())
                    ProfileActivity::class.java else ConversationsActivity::class.java)

        if (intent.action == Intent.ACTION_SEND) {
            newIntent.action = Intent.ACTION_SEND
            newIntent.type = intent.type
            newIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
            newIntent.putExtra(Intent.EXTRA_STREAM, intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        }

        startActivity(newIntent)
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        val settings = SettingsManagerImpl(this)
        val newIntent = Intent(this,
                if (settings.getUserName().isEmpty())
                    ProfileActivity::class.java else ConversationsActivity::class.java)

        if (intent.action == Intent.ACTION_SEND) {
            newIntent.action = Intent.ACTION_SEND
            newIntent.type = intent.type
            newIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
            newIntent.putExtra(Intent.EXTRA_STREAM, intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        }

        startActivity(newIntent)
    }
}
