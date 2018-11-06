package com.akki.khitkchat.ui.widget

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.akki.khitkchat.R
import com.akki.khitkchat.extension.getFirstLetter
import com.akki.khitkchat.ui.util.EmptyAnimatorListener

class SettingsPopup(context: Context) : PopupWindow() {

    private val APPEARING_ANIMATION_DURATION: Long = 200

    private var windowManager: WindowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var displayMetrics: DisplayMetrics = DisplayMetrics()

    @ColorInt
    private var color = Color.GRAY
    private var userName = ""
    private var profileClickListener: (() -> (Unit))? = null
    private var settingsClickListener: (() -> (Unit))? = null
    private var imagesClickListener: (() -> (Unit))? = null

    private var rootView: View
    private var container: View
    private var avatar: ImageView
    private var userNameLabel: TextView

    private var isDismissing: Boolean = false

    fun populateData(userName: String, @ColorInt color: Int) {
        this.userName = userName
        this.color = color
    }

    fun setCallbacks(profileClickListener: () -> (Unit), imagesClickListener: () -> (Unit), settingsClickListener: () -> (Unit)) {
        this.profileClickListener = profileClickListener
        this.imagesClickListener = imagesClickListener
        this.settingsClickListener = settingsClickListener

    }

    init {

        this.windowManager.defaultDisplay.getMetrics(displayMetrics)

        @SuppressLint("InflateParams")
        rootView = inflater.inflate(R.layout.popup_settings, null)
        container = rootView.findViewById(R.id.fl_container)
        avatar = rootView.findViewById(R.id.iv_avatar)
        userNameLabel = rootView.findViewById(R.id.tv_username)

        rootView.findViewById<View>(R.id.ll_user_profile_container).setOnClickListener({
            dismiss()
            profileClickListener?.invoke()
        })

        rootView.findViewById<View>(R.id.ll_images_button).setOnClickListener({
            dismiss()
            imagesClickListener?.invoke()
        })

        rootView.findViewById<View>(R.id.ll_settings_button).setOnClickListener({
            dismiss()
            settingsClickListener?.invoke()
        })

        contentView = rootView
    }

    fun show(anchor: View) {

        prepare()

        populateUi()

        val xPosition: Int
        val yPosition: Int

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)

        val anchorRect = Rect(location[0], location[1],
                location[0] + anchor.width, location[1] + anchor.height)

        xPosition = anchorRect.right + rootView.measuredWidth
        yPosition = anchorRect.top

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            container.visibility = View.VISIBLE
        }

        showAtLocation(anchor, Gravity.NO_GRAVITY, xPosition, yPosition)

        container.post({

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && container.isAttachedToWindow) {
                val animator = ViewAnimationUtils.createCircularReveal(container,
                        container.width, 0, 0f, container.measuredWidth.toFloat())
                container.visibility = View.VISIBLE
                animator.duration = APPEARING_ANIMATION_DURATION
                animator.start()
            }
        })
    }

    override fun dismiss() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isDismissing) {
            val animator = ViewAnimationUtils.createCircularReveal(container,
                    container.width, 0, container.measuredWidth.toFloat(), 0f)
            container.visibility = View.VISIBLE
            animator.addListener(object : EmptyAnimatorListener() {

                override fun onAnimationStart(animation: Animator?) {
                    isDismissing = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    actualDismiss()
                }
            })
            animator.duration = APPEARING_ANIMATION_DURATION
            animator.start()
        } else {
            actualDismiss()
        }
    }

    private fun actualDismiss() {
        isDismissing = false
        super.dismiss()
    }

    private fun prepare() {
        setBackgroundDrawable(ColorDrawable())
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        isTouchable = true
        isFocusable = true
        isOutsideTouchable = true
    }

    private fun populateUi() {
        val drawable = TextDrawable.builder().buildRound(userName.getFirstLetter(), color)
        avatar.setImageDrawable(drawable)
        userNameLabel.text = userName
    }
}
