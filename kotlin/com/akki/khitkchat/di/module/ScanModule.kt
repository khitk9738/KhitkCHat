package com.akki.khitkchat.di.module

import com.akki.khitkchat.data.model.*
import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.ui.activity.ImagePreviewActivity
import com.akki.khitkchat.ui.activity.ScanActivity
import com.akki.khitkchat.ui.presenter.ImagePreviewPresenter
import com.akki.khitkchat.ui.presenter.ScanPresenter
import dagger.Module
import dagger.Provides
import java.io.File

@Module
class ScanModule(private val activity: ScanActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(scanner: BluetoothScanner, connector: BluetoothConnector, fileManager: FileManager): ScanPresenter =
            ScanPresenter(activity, scanner, connector, fileManager)

    @Provides
    @PerActivity
    internal fun provideConnector(): BluetoothConnector = BluetoothConnectorImpl(activity)

    @Provides
    @PerActivity
    internal fun provideScanner(): BluetoothScanner = BluetoothScannerImpl(activity)
}
