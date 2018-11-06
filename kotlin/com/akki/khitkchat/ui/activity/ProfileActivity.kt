package com.akki.khitkchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorInt
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.akki.khitkchat.R
import com.akki.khitkchat.di.ComponentsManager
import com.akki.khitkchat.extension.getFirstLetter
import com.akki.khitkchat.ui.presenter.ProfilePresenter
import com.akki.khitkchat.ui.util.SimpleTextWatcher
import com.akki.khitkchat.ui.view.ProfileView
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener
import javax.inject.Inject
import android.speech.tts.TextToSpeech
import java.util.Locale

class ProfileActivity : SkeletonActivity(), ProfileView {

    var t1: TextToSpeech? = null
    @Inject
    lateinit var presenter: ProfilePresenter

    private lateinit var nameField: EditText
    private lateinit var nameLabel: TextView
    private lateinit var avatar: ImageView
    private lateinit var colorPicker: View

    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile, ActivityType.CHILD_ACTIVITY)

        t1 = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                t1!!.setLanguage(Locale.UK)
            }
        })


        ComponentsManager.injectProfile(this)

        editMode = intent.getBooleanExtra(EXTRA_EDIT_MODE, false)
        supportActionBar?.setDisplayHomeAsUpEnabled(editMode)
        supportActionBar?.setDisplayShowHomeEnabled(editMode)

        if (editMode) {
            title = getString(R.string.profile__profile)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }

        colorPicker = findViewById(R.id.v_color)
        nameField = findViewById(R.id.et_name)
        nameLabel = findViewById(R.id.tv_name)
        avatar = findViewById(R.id.iv_avatar)

        colorPicker.setOnClickListener {
            presenter.prepareColorPicker()
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            presenter.saveUser()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.loadSavedUser()
        nameField.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()
        nameField.removeTextChangedListener(textWatcher)
        hideKeyboard()
    }

    override fun showUserData(name: String, color: Int) {
        nameLabel.text = if (name.isEmpty()) getString(R.string.profile__your_name) else name
        nameLabel.setTextColor(resources.getColor(
                if (name.isEmpty()) R.color.text_light
                else{
                    t1!!.speak("Welcome "+name, TextToSpeech.QUEUE_FLUSH, null);
                    R.color.text_dark
                }))
        val drawable = TextDrawable.builder().buildRound(name.getFirstLetter(), color)
        avatar.setImageDrawable(drawable)
        colorPicker.setBackgroundColor(color)

    }

    override fun showColorPicker(@ColorInt color: Int) {
        ChromaDialog.Builder()
                .initialColor(color)
                .colorMode(ColorMode.RGB)
                .onColorSelected(colorSelectListener)
                .create()
                .show(supportFragmentManager, "ChromaDialog")
    }

    override fun redirectToConversations() {

        if (!editMode) {

            val intent = Intent(this, ConversationsActivity::class.java)
            if (getIntent().action == Intent.ACTION_SEND) {
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra(Intent.EXTRA_TEXT))
            }
            startActivity(intent)
        }
        finish()
    }

    override fun prefillUsername(name: String) {
        nameField.setText(name)
        t1!!.speak("Welcome "+name, TextToSpeech.QUEUE_FLUSH, null);
    }

    override fun showNotValidNameError() {
        nameField.error = getString(R.string.profile__validation_error)
        t1!!.speak(getString(R.string.profile__validation_error), TextToSpeech.QUEUE_FLUSH, null);
    }

    private val textWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(text: String) {
            nameField.error = null
            presenter.onNameChanged(text)
        }
    }

    private val colorSelectListener = object : ColorSelectListener {
        override fun onColorSelected(color: Int) {
            presenter.onColorPicked(color)
        }
    }

    companion object {

        private const val EXTRA_EDIT_MODE = "extra.edit_mode"

        fun start(context: Context, editMode: Boolean) {
            val intent = Intent(context, ProfileActivity::class.java)
                    .putExtra(EXTRA_EDIT_MODE, editMode)
            context.startActivity(intent)
        }
    }
}
