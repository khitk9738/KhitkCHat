package com.akki.khitkchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.akki.khitkchat.R
import com.akki.khitkchat.data.entity.ChatMessage
import com.akki.khitkchat.di.ComponentsManager
import com.akki.khitkchat.ui.adapter.ImagesAdapter
import com.akki.khitkchat.ui.presenter.ReceivedImagesPresenter
import com.akki.khitkchat.ui.view.ReceivedImagesView
import javax.inject.Inject

class ReceivedImagesActivity : SkeletonActivity(), ReceivedImagesView {

    @Inject
    lateinit var presenter: ReceivedImagesPresenter

    private var address: String? = null

    private lateinit var imagesGrid: RecyclerView
    private lateinit var noImagesLabel: TextView

    private var imagesAdapter = ImagesAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_received_images, ActivityType.CHILD_ACTIVITY)

        address = intent.getStringExtra(EXTRA_ADDRESS)

        ComponentsManager.injectReceivedImages(this, address)

        noImagesLabel = findViewById(R.id.tv_no_images)
        imagesGrid = findViewById<RecyclerView>(R.id.rv_images).apply {
            layoutManager = GridLayoutManager(this@ReceivedImagesActivity, calculateNoOfColumns())
            adapter = imagesAdapter
        }

        imagesAdapter.clickListener = { view, message ->
            ImagePreviewActivity.start(this, view, message)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.loadImages()
    }

    override fun displayImages(imageMessages: List<ChatMessage>) {
        imagesAdapter.images = ArrayList(imageMessages)
        imagesAdapter.notifyDataSetChanged()
    }

    override fun showNoImages() {
        imagesGrid.visibility = View.GONE
        noImagesLabel.visibility = View.VISIBLE
    }

    private fun calculateNoOfColumns(): Int {
        val displayMetrics = resources.displayMetrics
        val no = displayMetrics.widthPixels / resources.getDimensionPixelSize(R.dimen.thumbnail_width)
        return if (no == 0) 1 else no
    }

    companion object {

        private const val EXTRA_ADDRESS = "extra.address"

        fun start(context: Context, address: String?) {
            val intent = Intent(context, ReceivedImagesActivity::class.java)
                    .putExtra(EXTRA_ADDRESS, address)
            context.startActivity(intent)
        }
    }
}
