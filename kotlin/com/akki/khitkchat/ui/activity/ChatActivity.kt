package com.akki.khitkchat.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.akki.khitkchat.R
import com.akki.khitkchat.di.ComponentsManager
import com.akki.khitkchat.extension.toReadableFileSize
import com.akki.khitkchat.ui.adapter.ChatAdapter
import com.akki.khitkchat.ui.presenter.ChatPresenter
import com.akki.khitkchat.ui.util.SimpleTextWatcher
import com.akki.khitkchat.ui.view.ChatView
import com.akki.khitkchat.ui.view.NotificationView
import com.akki.khitkchat.ui.viewmodel.ChatMessageViewModel
import com.akki.khitkchat.ui.widget.ActionView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import android.net.Uri
import android.os.Environment
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.scottyab.aescrypt.AESCrypt
import java.util.regex.Pattern
import java.security.GeneralSecurityException
import android.util.Log
import android.speech.tts.TextToSpeech
import java.util.Locale


class ChatActivity : SkeletonActivity(), ChatView {
    //var filePaths= arrayListOf<String>()
    var t1: TextToSpeech? = null
    var filePaths:String ?=null
    lateinit var externalFile:File
    lateinit var external:Uri
    lateinit var filToShare:File
    @Inject

    lateinit var presenter: ChatPresenter
    private val layoutManager = LinearLayoutManager(this)
    private lateinit var actions: ActionView
    private lateinit var chatList: RecyclerView
    private lateinit var filepick: ImageButton
    private lateinit var messageField: EditText
    private lateinit var sendButtonsSwitcher: ViewSwitcher
    private lateinit var transferringImagePreview: ImageView
    private lateinit var transferringImageSize: TextView
    private lateinit var transferringImageHeader: TextView
    private lateinit var transferringImageProgressLabel: TextView
    private lateinit var transferringImageProgressBar: ProgressBar

    private lateinit var presharingContainer: CardView
    private lateinit var presharingImage: ImageView

    private lateinit var textSendingHolder: ViewGroup
    private lateinit var imageSendingHolder: ViewGroup

    private lateinit var chatAdapter: ChatAdapter

    private var deviceAddress: String? = null

    private val showAnimation =
            lazy { AnimationUtils.loadAnimation(this, R.anim.anime_fade_slide_in) }
    private val hideAnimation =
            lazy { AnimationUtils.loadAnimation(this, R.anim.anime_fade_slide_out) }

    private val textWatcher = object : SimpleTextWatcher() {

        private var previousText: String? = null

        override fun afterTextChanged(text: String) {

            if (previousText.isNullOrEmpty() && text.isNotEmpty()) {
                sendButtonsSwitcher.showNext()
            } else if (!previousText.isNullOrEmpty() && text.isEmpty()) {
                sendButtonsSwitcher.showPrevious()
            }
            previousText = text
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        t1 = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                t1!!.setLanguage(Locale.UK)
            }
        })

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chat, ActivityType.CHILD_ACTIVITY)

        deviceAddress = intent.getStringExtra(EXTRA_ADDRESS)

        ComponentsManager.injectChat(this, deviceAddress.toString())


        title = if (deviceAddress.isNullOrEmpty()) getString(R.string.app_name) else deviceAddress
        toolbar?.let {
            it.subtitle = getString(R.string.chat__not_connected)
            it.setTitleTextAppearance(this, R.style.ActionBar_TitleTextStyle)
            it.setSubtitleTextAppearance(this, R.style.ActionBar_SubTitleTextStyle)
        }

        filepick = findViewById(R.id.attach)

        textSendingHolder = findViewById(R.id.ll_text_sending_holder)
        imageSendingHolder = findViewById(R.id.ll_image_sending_holder)
        sendButtonsSwitcher = findViewById(R.id.vs_send_buttons)

        transferringImagePreview = findViewById(R.id.iv_transferring_image)
        transferringImageSize = findViewById(R.id.tv_file_size)
        transferringImageHeader = findViewById(R.id.tv_sending_image_label)
        transferringImageProgressLabel = findViewById(R.id.tv_file_sending_percentage)
        transferringImageProgressBar = findViewById(R.id.pb_transferring_progress)


        presharingContainer = findViewById(R.id.cv_presharing_image_holder)
        presharingImage = findViewById(R.id.iv_presharing_image)

        actions = findViewById(R.id.av_actions)
        messageField = findViewById<EditText>(R.id.et_message).apply {
            addTextChangedListener(textWatcher)
        }

       filepick.setOnClickListener {
            /*FilePickerBuilder.getInstance().setMaxCount(10)
                    .setSelectedFiles(filePaths)
                    .setActivityTheme(R.style.ActionBarStyle)
                    .enableVideoPicker(true)
                    .pickFile(this);*/
           MaterialFilePicker()
                   .withActivity(this)
                   .withRequestCode(1)
                   .withFilter(Pattern.compile(".*\\.*$")) // Filtering files and directories by file name using regexp
                   .withFilterDirectories(true) // Set directories filterable (false by default)
                   .withHiddenFiles(true) // Show hidden files and folders
                   .start()
        }


        findViewById<ImageButton>(R.id.ib_send).setOnClickListener {
            var msg:String=messageField.text.toString().trim()
            var password:String ="baguvix" //deviceAddress.toString()
            var encryptedMsg:String=""
            try {
                encryptedMsg= AESCrypt.encrypt(password, msg);
            }catch (e:GeneralSecurityException  ){
                Log.getStackTraceString(e)
            }
            presenter.sendMessage(encryptedMsg)
        }

        findViewById<ImageButton>(R.id.ib_image).setOnClickListener {
            EasyImage.openChooserWithGallery(this, "chooserTitle", 0)
        }


        findViewById<ImageButton>(R.id.ib_cancel).setOnClickListener {
            presenter.cancelFileTransfer()
        }

        findViewById<Button>(R.id.btn_retry).setOnClickListener {
            hideAnimation.value.let {
                it.fillAfter = true
                presharingContainer.startAnimation(it)
                presenter.proceedPresharing()
            }
        }

        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            hideAnimation.value.let {
                it.fillAfter = true
                presharingContainer.startAnimation(it)
                presenter.cancelPresharing()
            }
        }

        chatAdapter = ChatAdapter(this).apply {
            imageClickListener = { view, message ->
                ImagePreviewActivity.start(this@ChatActivity, view, message)
            }
        }

        chatList = findViewById<RecyclerView>(R.id.rv_chat).apply {

            val manager = this@ChatActivity.layoutManager
            manager.reverseLayout = true
            layoutManager = manager
            adapter = chatAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView?, scrollState: Int) {

                    val picasso = Picasso.with(this@ChatActivity)
                    if (scrollState == RecyclerView.SCROLL_STATE_IDLE || scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        picasso.resumeTag(chatAdapter.picassoTag)
                    } else {
                        picasso.pauseTag(chatAdapter.picassoTag)
                    }
                }
            })
        }

        if (Intent.ACTION_SEND == intent.action) {

            val textToShare = intent.getStringExtra(EXTRA_MESSAGE)
            val fileToShare = intent.getStringExtra(EXTRA_FILE_PATH)

            if (textToShare != null) {
                messageField.setText(textToShare)
            } else if (fileToShare != null) {
                Handler().postDelayed({
                    presenter.sendFile(File(fileToShare))
                }, 1000)
            }

            intent.action = Intent.ACTION_VIEW
        }
    }



    override fun onStart() {
        super.onStart()
        presenter.prepareConnection()
    }

    override fun onStop() {
        super.onStop()
        presenter.releaseConnection()
    }

    override fun dismissMessageNotification() {
        (getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(NotificationView.NOTIFICATION_TAG_MESSAGE, NotificationView.NOTIFICATION_ID_MESSAGE)
    }

    override fun showPartnerName(name: String, device: String) {
        title = "$name ($device)"
    }

    override fun showStatusConnected() {
        toolbar?.subtitle = getString(R.string.chat__connected)
    }

    override fun showStatusNotConnected() {
        toolbar?.subtitle = getString(R.string.chat__not_connected)
    }

    override fun showStatusPending() {
        toolbar?.subtitle = getString(R.string.chat__pending)
    }

    override fun showNotConnectedToSend() =
            Toast.makeText(this, getString(R.string.chat__not_connected_to_send), Toast.LENGTH_LONG).show()

    override fun afterMessageSent() {
        messageField.text = null
    }

    override fun showNotConnectedToThisDevice(currentDevice: String) {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__connected_to_another, currentDevice),
                ActionView.Action(getString(R.string.chat__connect)) { presenter.connectToDevice() },
                null
        )
    }

    override fun showNotConnectedToAnyDevice() {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__not_connected_to_this_device),
                ActionView.Action(getString(R.string.chat__connect)) { presenter.connectToDevice() },
                null
        )
    }

    override fun showWainingForOpponent() {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__waiting_for_device),
                ActionView.Action(getString(R.string.general__cancel)) { presenter.resetConnection() },
                null
        )
    }

    override fun showConnectionRequest(displayName: String, deviceName: String) {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__connection_request, displayName, deviceName),
                ActionView.Action(getString(R.string.general__start_chat)) { presenter.acceptConnection() },
                ActionView.Action(getString(R.string.chat__disconnect)) { presenter.rejectConnection() }
        )
    }

    override fun showServiceDestroyed() {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.general__service_lost))
                .setPositiveButton(getString(R.string.general__restart), { _, _ -> presenter.prepareConnection() })
                .setCancelable(false)
                .show()
    }

    override fun hideActions() {
        actions.visibility = View.GONE
    }

    override fun showMessagesHistory(messages: List<ChatMessageViewModel>) {
        chatAdapter.messages = LinkedList(messages)
        chatAdapter.notifyDataSetChanged()
    }

    override fun showReceivedMessage(message: ChatMessageViewModel) {
        chatAdapter.messages.addFirst(message)
        chatAdapter.notifyItemInserted(0)
        layoutManager.scrollToPosition(0)
    }

    override fun showSentMessage(message: ChatMessageViewModel) {
        chatAdapter.messages.addFirst(message)
        chatAdapter.notifyItemInserted(0)
        layoutManager.scrollToPosition(0)
    }

    override fun showRejectedConnection() {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__connection_rejected))
                .setPositiveButton(getString(R.string.general__ok), null)
                .setCancelable(false)
                .show()
    }

    override fun showBluetoothDisabled() {
        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__bluetooth_is_disabled),
                ActionView.Action(getString(R.string.chat__enable)) { presenter.enableBluetooth() },
                null
        )
    }

    override fun showLostConnection() {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__connection_lost))
                .setPositiveButton(getString(R.string.chat__reconnect), { _, _ -> presenter.reconnect() })
                .setNegativeButton(getString(R.string.general__cancel), null)
                .setCancelable(false)
                .show()
    }

    override fun showDisconnected() {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__partner_disconnected))
                .setPositiveButton(getString(R.string.chat__reconnect), { _, _ -> presenter.reconnect() })
                .setNegativeButton(getString(R.string.general__cancel), null)
                .setCancelable(false)
                .show()
    }

    override fun showFailedConnection() {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__unable_to_connect))
                .setPositiveButton(getString(R.string.general__try_again), { _, _ -> presenter.connectToDevice() })
                .setNegativeButton(getString(R.string.general__cancel), null)
                .setCancelable(false)
                .show()

    }

    override fun showNotValidMessage() {
        Toast.makeText(this, getString(R.string.chat__message_cannot_be_empty), Toast.LENGTH_SHORT).show()
    }

    override fun showDeviceIsNotAvailable() {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__device_is_not_available))
                .setPositiveButton(getString(R.string.chat__rescan), { _, _ -> ScanActivity.start(this) })
                .show()
    }

    override fun requestBluetoothEnabling() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
    }

    override fun showBluetoothEnablingFailed() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__bluetooth_required))
                .setPositiveButton(getString(R.string.general__ok), null)
                .show()
    }

    override fun showImageTooBig(maxSize: Long) {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__too_big_image, maxSize.toReadableFileSize()))
                .setPositiveButton(getString(R.string.general__ok), null)
                .show()
    }

    override fun showPresharingImage(path: String) {

        showAnimation.value.let {
            it.fillAfter = true
            presharingContainer.visibility = View.VISIBLE
            presharingContainer.startAnimation(it)
        }

        Picasso.with(this)
                .load("file://$path")
                .into(presharingImage)
    }

    override fun showImageTransferLayout(fileAddress: String?, fileSize: Long, transferType: ChatView.FileTransferType) {

        textSendingHolder.visibility = View.GONE
        imageSendingHolder.visibility = View.VISIBLE

        transferringImageHeader.text = getString(if (transferType == ChatView.FileTransferType.SENDING)
            R.string.chat__sending_image else R.string.chat__receiving_images)

        Picasso.with(this)
                .load("file://$fileAddress")
                .into(object : Target {

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        transferringImagePreview.setImageResource(R.drawable.ic_photo)
                    }

                    override fun onBitmapFailed(errorDrawable: Drawable?) {
                        transferringImagePreview.setImageResource(R.drawable.ic_photo)
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        transferringImagePreview.setImageBitmap(bitmap)
                    }
                })

        transferringImageSize.text = fileSize.toReadableFileSize()
        transferringImageProgressLabel.text = "0%"
        //FIXME should work with Long
        transferringImageProgressBar.progress = 0
        transferringImageProgressBar.max = fileSize.toInt()
    }

    @SuppressLint("SetTextI18n")
    override fun updateImageTransferProgress(transferredBytes: Long, totalBytes: Long) {

        val percents = transferredBytes.toFloat() / totalBytes * 100
        transferringImageProgressLabel.text = "${Math.round(percents)}%"
        //FIXME should work with Long
        transferringImageProgressBar.progress = transferredBytes.toInt()

    }

    override fun hideImageTransferLayout() {
        textSendingHolder.visibility = View.VISIBLE
        imageSendingHolder.visibility = View.GONE
    }

    override fun showImageTransferCanceled() {
        Toast.makeText(this, R.string.chat__partner_canceled_image_transfer, Toast.LENGTH_LONG).show()
    }

    override fun showImageTransferFailure() {
        Toast.makeText(this, R.string.chat__problem_during_file_transfer, Toast.LENGTH_LONG).show()
    }

    override fun showReceiverUnableToReceiveImages() {

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(R.string.chat__partner_unable_to_receive_images)
                .setPositiveButton(R.string.general__ok, null)
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.onBluetoothEnabled()
            } else {
                presenter.onBluetoothEnablingFailed()
            }
        }
        else if (requestCode == 1 && resultCode == RESULT_OK) {
        filePaths = data!!.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Toast.makeText(this, "Picked file: " + filePaths, Toast.LENGTH_LONG).show();
            externalFile=File(Environment.getExternalStorageDirectory(),filePaths)
            external= Uri.fromFile(externalFile)
            filToShare=File(external.toString())
            Handler().postDelayed({
                presenter.sendFile(File(filePaths))
            }, 1000)
            t1!!.speak("Sending File" , TextToSpeech.QUEUE_FLUSH, null);
        // Do anything with file
        }/*
        else if(requestCode == REQUEST_CODE_DOC) {
            var docPaths = mutableListOf<String>()
            if(resultCode== Activity.RESULT_OK && data!=null)
                   {
                       docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                   }
            Toast.makeText(this, "Picked file: " + docPaths[0], Toast.LENGTH_LONG).show();
           /* val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            shareIntent.type = "*"*/

            // For a file in shared storage.  For data in private storage, use a ContentProvider.
            externalFile=File(Environment.getExternalStorageDirectory(),docPaths[0])
            external= Uri.fromFile(externalFile)
            filToShare=File(external.toString())
            Handler().postDelayed({
                presenter.sendFile(File(docPaths[0]))
            }, 1000)*/
            /*shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(shareIntent,"How You want to share"))
            val intent =ShareCompat.IntentBuilder.from(this)
            intent!!.action = Intent.ACTION_SEND
            externalFile=File(Environment.getExternalStorageDirectory(),docPaths[0])
            external= Uri.fromFile(externalFile)
            filToShare=File(external.toString())
            intent.setType("*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(filToShare));
            startActivity(Intent.createChooser(intent, "Share using"));*/
            /* var pm: PackageManager= getPackageManager();
            var list:List<ResolveInfo>  = pm.queryIntentActivities(intent, 0);
            if (list.size > 0) {
                var packageName :String?=null
                var className :String?= null;
                var found:Boolean? = false;

                for ( var info :ResolveInfo!= list) {
                    packageName = info.activityInfo.packageName;
                    if (packageName.equals("com.android.bluetooth")) {
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }intent.setClassName(packageName, className)
            startActivity(intent)
            Handler().postDelayed({
                presenter.sendFile((filToShare))
            }, 1000)*//*
            intent.action = Intent.ACTION_VIEW*/
            /*intent.type = "
            startActivity(Intent.createChooser(intent, "Send Files"))*/

        else {
           EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {

                override fun onImagesPicked(imageFiles: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
                    if (imageFiles.isNotEmpty()) {
                        t1!!.speak("Sending Image" , TextToSpeech.QUEUE_FLUSH, null);
                        presenter.sendFile(imageFiles[0])

                    }
                }

                override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                    Toast.makeText(this@ChatActivity, "${e?.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_images -> {
                ReceivedImagesActivity.start(this, deviceAddress)
                true
            }
            R.id.action_disconnect -> {
                presenter.disconnect()
                true
            }
            R.id.about -> {
                val intent=Intent(this,AboutActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val REQUEST_ENABLE_BLUETOOTH = 101

        const val EXTRA_ADDRESS = "extra.address"
        private const val EXTRA_MESSAGE = "extra.message"
        private const val EXTRA_FILE_PATH = "extra.file_path"

        fun start(context: Context, address: String) {
            val intent: Intent = Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_ADDRESS, address)
            context.startActivity(intent)
        }

        fun start(context: Context, address: String, message: String?, filePath: String?) {
            val intent: Intent = Intent(context, ChatActivity::class.java)
                    .setAction(Intent.ACTION_SEND)
                    .putExtra(EXTRA_ADDRESS, address)
                    .putExtra(EXTRA_MESSAGE, message)
                    .putExtra(EXTRA_FILE_PATH, filePath)
            context.startActivity(intent)
        }
    }
}
