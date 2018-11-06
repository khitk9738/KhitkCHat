package com.akki.khitkchat.ui.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.akki.khitkchat.R
import com.akki.khitkchat.di.ComponentsManager
import com.akki.khitkchat.ui.adapter.DevicesAdapter
import com.akki.khitkchat.ui.presenter.ScanPresenter
import com.akki.khitkchat.ui.view.ScanView
import com.akki.khitkchat.ui.widget.ExpiringProgressBar
import javax.inject.Inject
import android.speech.tts.TextToSpeech
import java.util.Locale

class ScanActivity : SkeletonActivity(), ScanView {

    var t1: TextToSpeech? = null
    private lateinit var container: View
    private lateinit var turnOnHolder: View
    private lateinit var listHolder: View
    private lateinit var progress: View

    private lateinit var infoLabel: TextView
    private lateinit var discoveryLabel: TextView
    private lateinit var progressBar: ExpiringProgressBar
    private lateinit var makeDiscoverableButton: Button
    private lateinit var scanForDevicesButton: Button

    private lateinit var pairedDevicesList: RecyclerView

    private val devicesAdapter: DevicesAdapter = DevicesAdapter(this)

    @Inject
    lateinit var presenter: ScanPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scan, ActivityType.CHILD_ACTIVITY)
        ComponentsManager.injectScan(this)

        t1 = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                t1!!.setLanguage(Locale.UK)
            }
        })

        container = findViewById(R.id.fl_container)
        turnOnHolder = findViewById(R.id.ll_turn_on)
        listHolder = findViewById(R.id.cl_list)
        progress = findViewById(R.id.fl_progress)

        infoLabel = findViewById(R.id.tv_info)
        discoveryLabel = findViewById(R.id.tv_discovery_label)
        progressBar = findViewById(R.id.epb_progress)

        makeDiscoverableButton = findViewById(R.id.btn_make_discoverable)
        scanForDevicesButton = findViewById(R.id.btn_scan)

        pairedDevicesList = findViewById<RecyclerView>(R.id.rv_paired_devices).apply {
            layoutManager = LinearLayoutManager(this@ScanActivity)
            adapter = devicesAdapter
        }

        devicesAdapter.listener = {
            presenter.onDevicePicked(it.address)
            progress.visibility = View.VISIBLE
        }

        presenter.checkBluetoothAvailability()

        findViewById<Button>(R.id.btn_turn_on).setOnClickListener {
            presenter.turnOnBluetooth()
        }

        makeDiscoverableButton.setOnClickListener {
            presenter.makeDiscoverable()
        }

        scanForDevicesButton.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                presenter.scanForDevices()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    explainAskingLocationPermission()
                } else {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
                }
            }
        }

        findViewById<ImageView>(R.id.iv_share).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                presenter.shareApk()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    explainAskingStoragePermission()
                } else {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
                }
            }
        }
    }

    override fun shareApk(uri: Uri) {

        val sharingIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            `package` = "com.android.bluetooth"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        try {
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.scan__share_intent)))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.scan__unable_to_share_apk), Toast.LENGTH_LONG).show()
            t1!!.speak("Unable to start Bluetooth sharing", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    override fun openChat(device: BluetoothDevice) {
        val intent: Intent = Intent().putExtra(EXTRA_BLUETOOTH_DEVICE, device)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun showPairedDevices(pairedDevices: List<BluetoothDevice>) {

        turnOnHolder.visibility = View.GONE
        listHolder.visibility = View.VISIBLE

        if (pairedDevices.isNotEmpty()) {
            devicesAdapter.pairedList = ArrayList(pairedDevices)
            devicesAdapter.notifyDataSetChanged()
        }
    }

    override fun showBluetoothScanner() {
        container.visibility = View.VISIBLE
        presenter.checkBluetoothEnabling()
    }

    override fun showBluetoothEnablingRequest() {
        turnOnHolder.visibility = View.VISIBLE
        listHolder.visibility = View.GONE
    }

    override fun requestBluetoothEnabling() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
    }

    override fun showBluetoothIsNotAvailableMessage() {
        AlertDialog.Builder(this)
                .setMessage(R.string.scan__no_access_to_bluetooth)
                .setPositiveButton(R.string.general__ok, { _, _ -> finish() })
                .show()

        t1!!.speak("Cannot get access to Bluetooth on your device", TextToSpeech.QUEUE_FLUSH, null);
    }

    override fun showBluetoothEnablingFailed() {
        AlertDialog.Builder(this)
                .setMessage(R.string.scan__bluetooth_disabled)
                .setPositiveButton(R.string.general__ok, null)
                .show()
        t1!!.speak("This app requires Bluetooth enabled to work properly", TextToSpeech.QUEUE_FLUSH, null);
    }

    override fun requestMakingDiscoverable() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60)
        startActivityForResult(discoverableIntent, REQUEST_MAKE_DISCOVERABLE)
    }

    override fun showDiscoverableProcess() {
        makeDiscoverableButton.text = getString(R.string.scan__discoverable)
        makeDiscoverableButton.isEnabled = false
    }

    override fun showDiscoverableFinished() {
        makeDiscoverableButton.text = getString(R.string.scan__make_discoverable)
        makeDiscoverableButton.isEnabled = true
        t1!!.speak("Make Discoverable", TextToSpeech.QUEUE_FLUSH, null);
    }

    override fun showScanningStarted(seconds: Int) {
        progressBar.runExpiring(seconds)
        progressBar.visibility = View.VISIBLE
        discoveryLabel.visibility = View.VISIBLE
        scanForDevicesButton.text = getString(R.string.scan__stop_scanning)
    }

    override fun showScanningStopped() {
        progressBar.cancel()
        progressBar.visibility = View.GONE
        discoveryLabel.visibility = View.GONE
        scanForDevicesButton.text = getString(R.string.scan__scan_for_devices)
    }

    override fun showBluetoothDiscoverableFailure() {
        AlertDialog.Builder(this)
                .setMessage(R.string.scan__unable_to_make_discoverable)
                .setPositiveButton(R.string.general__ok, null)
                .show()
        t1!!.speak("Unable to make your device discoverable", TextToSpeech.QUEUE_FLUSH, null);
    }

    override fun showServiceUnavailable() {
        progress.visibility = View.GONE

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(R.string.scan__unable_to_connect_service)
                .setPositiveButton(R.string.general__ok, null)
                .show()
        t1!!.speak("Unable to engage connection, please restart the app", TextToSpeech.QUEUE_FLUSH, null);
    }

    override fun showUnableToConnect() {
        progress.visibility = View.GONE

        if (!isStarted()) return

        AlertDialog.Builder(this)
                .setMessage(R.string.scan__unable_to_connect)
                .setPositiveButton(R.string.general__ok, null)
                .show()
        t1!!.speak("Unable to connect to this device", TextToSpeech.QUEUE_FLUSH, null);
    }

    override fun addFoundDevice(device: BluetoothDevice) {
        devicesAdapter.addNewFoundDevice(device)
        devicesAdapter.notifyDataSetChanged()
        pairedDevicesList.smoothScrollToPosition(pairedDevicesList.adapter.itemCount)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.onPairedDevicesReady()
            } else {
                presenter.onBluetoothEnablingFailed()
            }
        } else if (requestCode == REQUEST_MAKE_DISCOVERABLE) {
            if (resultCode > 0) {
                presenter.onMadeDiscoverable()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.scanForDevices()
            } else {
                explainAskingLocationPermission()
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.shareApk()
            } else {
                explainAskingStoragePermission()
            }
        }
    }

    private fun explainAskingLocationPermission() {
        AlertDialog.Builder(this)
                .setMessage(R.string.scan__permission_explanation_location)
                .setPositiveButton(R.string.general__ok, { _, _ ->
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
                })
                .show()
        t1!!.speak("To be able to find other devices this app requires location permission", TextToSpeech.QUEUE_FLUSH, null);
    }

    private fun explainAskingStoragePermission() {
        AlertDialog.Builder(this)
                .setMessage(R.string.scan__permission_explanation_storage)
                .setPositiveButton(R.string.general__ok, { _, _ ->
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
                })
                .show()
        t1!!.speak("To be able to extract a file this app requires storage permission", TextToSpeech.QUEUE_FLUSH, null);
    }

    override fun showExtractionApkFailureMessage() {
        AlertDialog.Builder(this)
                .setMessage(R.string.scan__unable_to_fetch_apk)
                .setPositiveButton(R.string.general__ok, null)
                .show()
        t1!!.speak("Unable to fetch an apk file", TextToSpeech.QUEUE_FLUSH, null);
    }

    override fun onStop() {
        super.onStop()
        presenter.cancelScanning()
    }

    companion object {

        const val EXTRA_BLUETOOTH_DEVICE = "extra.bluetooth_device"

        private const val REQUEST_ENABLE_BLUETOOTH = 101
        private const val REQUEST_MAKE_DISCOVERABLE = 102
        private const val REQUEST_LOCATION_PERMISSION = 103
        private const val REQUEST_STORAGE_PERMISSION = 104

        fun start(context: Context) =
                context.startActivity(Intent(context, ScanActivity::class.java))

        fun startForResult(context: Activity, requestCode: Int) =
                context.startActivityForResult(
                        Intent(context, ScanActivity::class.java), requestCode)
    }
}
