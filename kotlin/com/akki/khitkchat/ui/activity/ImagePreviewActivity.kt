package com.akki.khitkchat.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView
import com.akki.khitkchat.R
import com.akki.khitkchat.data.entity.ChatMessage
import com.akki.khitkchat.di.ComponentsManager
import com.akki.khitkchat.ui.presenter.ImagePreviewPresenter
import com.akki.khitkchat.ui.view.ImagePreviewView
import com.akki.khitkchat.ui.viewmodel.ChatMessageViewModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import javax.inject.Inject
import android.speech.tts.TextToSpeech
import java.util.Locale

class ImagePreviewActivity : SkeletonActivity(), ImagePreviewView {

    var t1: TextToSpeech? = null
    private lateinit var imageView: PhotoView

    @Inject
    lateinit var presenter: ImagePreviewPresenter

    private var own = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        t1 = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                t1!!.setLanguage(Locale.UK)
            }
        })
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_preview, ActivityType.CHILD_ACTIVITY)

        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1)
        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        own = intent.getBooleanExtra(EXTRA_OWN, false)
        ComponentsManager.injectImagePreview(this, messageId, File(imagePath))

        toolbar?.setTitleTextAppearance(this, R.style.ActionBar_TitleTextStyle)
        toolbar?.setSubtitleTextAppearance(this, R.style.ActionBar_SubTitleTextStyle)

        imageView = findViewById<PhotoView>(R.id.pv_preview).apply {
            minimumScale = .75f
            maximumScale = 2f
        }

        presenter.loadImage()
    }

    override fun displayImage(fileUrl: String) {

        val callback = object : Callback {

            override fun onSuccess() {
                ActivityCompat.startPostponedEnterTransition(this@ImagePreviewActivity)
            }

            override fun onError() {
                ActivityCompat.startPostponedEnterTransition(this@ImagePreviewActivity)
            }
        }

        ActivityCompat.postponeEnterTransition(this)
        Picasso.with(this)
                .load(fileUrl)
                .config(Bitmap.Config.RGB_565)
                .noFade()
                .into(imageView, callback)
    }

    override fun showFileInfo(name: String, readableSize: String) {
        title = name
        t1!!.speak("File"+name , TextToSpeech.QUEUE_FLUSH, null);
        toolbar?.subtitle = readableSize
    }

    override fun close() {
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!own) {
            menuInflater.inflate(R.menu.menu_image_preview, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_remove -> {
                confirmFileRemoval()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmFileRemoval() {

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.images__removal_confirmation))
                .setPositiveButton(getString(R.string.general__yes), { _, _ ->
                    presenter.removeFile()
                })
                .setNegativeButton(getString(R.string.general__no), null)
                .show()
        t1!!.speak("Do you really want to delete this file" , TextToSpeech.QUEUE_FLUSH, null);
    }

    companion object {

        const val EXTRA_MESSAGE_ID = "extra.message_id"
        const val EXTRA_IMAGE_PATH = "extra.image_path"
        const val EXTRA_OWN = "extra.own"

        fun start(activity: Activity, transitionView: ImageView, message: ChatMessage) {
            start(activity, transitionView, message.uid, message.filePath ?: "unknown", message.own)
        }

        fun start(activity: Activity, transitionView: ImageView, message: ChatMessageViewModel) {
            start(activity, transitionView, message.uid, message.imagePath ?: "unknown", message.own)
        }

        fun start(activity: Activity, transitionView: ImageView, messageId: Long, imagePath: String, ownMessage: Boolean) {

            val intent = Intent(activity, ImagePreviewActivity::class.java)
                    .putExtra(EXTRA_MESSAGE_ID, messageId)
                    .putExtra(EXTRA_IMAGE_PATH, imagePath)
                    .putExtra(EXTRA_OWN, ownMessage)

            val options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity, transitionView, activity.getString(R.string.id_transition_image))
            activity.startActivity(intent, options.toBundle())
        }
    }
}
