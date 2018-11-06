package com.akki.khitkchat.ui.activity

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import com.amulyakhare.textdrawable.TextDrawable
import com.akki.khitkchat.R
import com.akki.khitkchat.di.ComponentsManager
import com.akki.khitkchat.extension.getFilePath
import com.akki.khitkchat.extension.getFirstLetter
import com.akki.khitkchat.ui.adapter.ConversationsAdapter
import com.akki.khitkchat.ui.presenter.ConversationsPresenter
import com.akki.khitkchat.ui.view.ConversationsView
import com.akki.khitkchat.ui.view.NotificationView
import com.akki.khitkchat.ui.viewmodel.ConversationViewModel
import com.akki.khitkchat.ui.widget.ActionView
import com.akki.khitkchat.ui.widget.SettingsPopup
import com.akki.khitkchat.ui.widget.ShortcutManager
import javax.inject.Inject

class ConversationsActivity : SkeletonActivity(), ConversationsView {

    @Inject
    lateinit var presenter: ConversationsPresenter
    @Inject
    lateinit var shortcutsManager: ShortcutManager

    private lateinit var conversationsList: RecyclerView
    private lateinit var noConversations: View
    private lateinit var addButton: FloatingActionButton
    private lateinit var actions: ActionView
    private lateinit var userAvatar: ImageView

    private lateinit var settingsPopup: SettingsPopup
    private lateinit var storagePermissionDialog: AlertDialog

    private val conversationsAdapter = ConversationsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_conversations, ActivityType.CUSTOM_TOOLBAR_ACTIVITY)
        ComponentsManager.injectConversations(this)

        actions = findViewById(R.id.av_actions)
        userAvatar = findViewById(R.id.iv_avatar)

        conversationsList = findViewById<RecyclerView>(R.id.rv_conversations).apply {
            layoutManager = LinearLayoutManager(this@ConversationsActivity)
            adapter = conversationsAdapter
        }

        noConversations = findViewById(R.id.ll_empty_holder)
        addButton = findViewById(R.id.fab_new_conversation)

        settingsPopup = SettingsPopup(this)
        settingsPopup.setCallbacks(
                profileClickListener = { ProfileActivity.start(context = this, editMode = true) },
                imagesClickListener = { ReceivedImagesActivity.start(context = this, address = null) },
                settingsClickListener = { val intent=Intent(this,AboutActivity::class.java)
                    startActivity(intent)
                    true }
        )

        conversationsAdapter.clickListener = { ChatActivity.start(this, it.address) }
        conversationsAdapter.longClickListener = { conversation, isCurrent ->
            showContextMenu(conversation, isCurrent)
        }

        addButton.setOnClickListener {
            ScanActivity.startForResult(this, REQUEST_SCAN)
        }

        findViewById<Button>(R.id.btn_scan).setOnClickListener {
            ScanActivity.startForResult(this, REQUEST_SCAN)
        }

        findViewById<View>(R.id.ll_options).setOnClickListener {
            settingsPopup.show(it)
        }

        if (intent.action == Intent.ACTION_SEND) {

            var textToShare: String? = null
            var fileToShare: String? = null

            if ("text/plain" == intent.type) {
                textToShare = intent.getStringExtra(Intent.EXTRA_TEXT).trim()
            } else if (intent.type.startsWith("image/")) {
                val imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                fileToShare = imageUri.getFilePath(this)
            }else{
                val imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                fileToShare = imageUri.getFilePath(this)
            }


            ContactChooserActivity.start(this, textToShare, fileToShare)
        }

        storagePermissionDialog = AlertDialog.Builder(this)
                .setView(R.layout.dialog_storage_permission)
                .setPositiveButton(R.string.general__ok) { _, _ ->
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
                }
                .setNegativeButton(R.string.general__exit, { _, _ -> finish() })
                .setCancelable(false)
                .create()

        shortcutsManager.addSearchShortcut()
    }

    private fun showContextMenu(conversation: ConversationViewModel, isCurrent: Boolean) {

        val labels = ArrayList<String>()
        labels.add(getString(R.string.conversations__remove))
        if (isCurrent) {
            labels.add(getString(R.string.general__disconnect))
        }
        if (shortcutsManager.isRequestPinShortcutSupported()) {
            labels.add(getString(R.string.conversations__pin_to_home_screen))
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.conversations__options))
                .setItems(labels.toTypedArray(), { _, which ->
                    when (which) {
                        0 -> {
                            confirmRemoval(conversation.address)
                        }
                        1 -> {
                            if (isCurrent) {
                                presenter.disconnect()
                            } else {
                                requestPinShortcut(conversation)
                            }
                        }
                        2 -> {
                            requestPinShortcut(conversation)
                        }
                    }
                })
        builder.create().show()
    }

    private fun requestPinShortcut(conversation: ConversationViewModel) {
        shortcutsManager.requestPinConversationShortcut(
                conversation.address, conversation.displayName, conversation.color)
    }

    private fun confirmRemoval(address: String) {

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.conversations__removal_confirmation))
                .setPositiveButton(getString(R.string.general__yes), { _, _ -> presenter.removeConversation(address) })
                .setNegativeButton(getString(R.string.general__no), null)
                .show()
    }

    override fun onStart() {
        super.onStart()

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && !storagePermissionDialog.isShowing) {
            storagePermissionDialog.show()
        }

        presenter.prepareConnection()
        presenter.loadUserProfile()
    }

    override fun dismissConversationNotification() {
        (getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(NotificationView.NOTIFICATION_TAG_CONNECTION, NotificationView.NOTIFICATION_ID_CONNECTION)
    }

    override fun onStop() {
        super.onStop()
        presenter.releaseConnection()
    }

    override fun hideActions() {
        actions.visibility = View.GONE
    }

    override fun showNoConversations() {
        conversationsList.visibility = View.GONE
        addButton.visibility = View.GONE
        noConversations.visibility = View.VISIBLE
    }

    override fun showConversations(conversations: List<ConversationViewModel>, connected: String?) {
        conversationsList.visibility = View.VISIBLE
        addButton.visibility = View.VISIBLE
        noConversations.visibility = View.GONE

        conversationsAdapter.setData(ArrayList(conversations), connected)
        conversationsAdapter.notifyDataSetChanged()
    }

    override fun showServiceDestroyed() {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.general__service_lost))
                .setPositiveButton(getString(R.string.general__restart), { _, _ ->
                    presenter.prepareConnection()
                    presenter.loadUserProfile()
                })
                .setCancelable(false)
                .show()
    }

    override fun refreshList(connected: String?) {
        conversationsAdapter.setCurrentConversation(connected)
        conversationsAdapter.notifyDataSetChanged()
    }

    override fun notifyAboutConnectedDevice(conversation: ConversationViewModel) {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.conversations__connection_request, conversation.displayName, conversation.deviceName),
                ActionView.Action(getString(R.string.general__start_chat)) { presenter.startChat(conversation) },
                ActionView.Action(getString(R.string.general__disconnect)) { presenter.rejectConnection() }
        )
    }

    override fun showRejectedNotification(conversation: ConversationViewModel) {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.conversations__connection_rejected,
                        conversation.displayName, conversation.deviceName))
                .setPositiveButton(getString(R.string.general__ok), null)
                .setCancelable(false)
                .show()
    }

    override fun redirectToChat(conversation: ConversationViewModel) {
        ChatActivity.start(this, conversation.address)
    }

    override fun showUserProfile(name: String, color: Int) {
        val drawable = TextDrawable.builder().buildRound(name.getFirstLetter(), color)
        userAvatar.setImageDrawable(drawable)
        settingsPopup.populateData(name, color)
    }

    override fun removeFromShortcuts(address: String) {
        shortcutsManager.removeConversationShortcut(address)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SCAN && resultCode == Activity.RESULT_OK) {
            val device = data
                    ?.getParcelableExtra<BluetoothDevice>(ScanActivity.EXTRA_BLUETOOTH_DEVICE)

            if (device != null) {
                ChatActivity.start(this, device.address)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED && !storagePermissionDialog.isShowing) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !shouldShowRequestPermissionRationale(permissions[0])) {

                AlertDialog.Builder(this)
                        .setMessage(Html.fromHtml(getString(R.string.conversations__storage_permission)))
                        .setPositiveButton(getString(R.string.conversations__permissions_settings)) { _, _ ->

                            val intent = Intent()
                                    .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .addCategory(Intent.CATEGORY_DEFAULT)
                                    .setData(Uri.parse("package:$packageName"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                            startActivity(intent)
                        }
                        .setCancelable(false)
                        .show()
            } else {
                storagePermissionDialog.show()
            }
        }
    }

    companion object {

        private const val REQUEST_STORAGE_PERMISSION = 101
        private const val REQUEST_SCAN = 102

        fun start(context: Context) =
                context.startActivity(Intent(context, ConversationsActivity::class.java))
    }
}
