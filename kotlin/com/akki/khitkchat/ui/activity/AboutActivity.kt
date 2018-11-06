package com.akki.khitkchat.ui.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.akki.khitkchat.R
import android.speech.tts.TextToSpeech
import java.util.Locale

class AboutActivity : AppCompatActivity() {

    var t1: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        t1 = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                t1!!.setLanguage(Locale.UK)
            }
        })
        t1!!.speak("Developed By  Team of Akshit Akshita Hitesh Mayank and Monika" , TextToSpeech.QUEUE_FLUSH, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_about)


    }

    override fun onStart() {
        super.onStart()
        t1!!.speak("Developed By  Team of Akshit Akshita Hitesh Mayank and Monika" , TextToSpeech.QUEUE_FLUSH, null);
    }
}
