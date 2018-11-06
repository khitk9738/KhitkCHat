package com.akki.khitkchat.ui.view

import android.app.Notification
import com.akki.khitkchat.data.entity.TransferringFile
import com.akki.khitkchat.ui.util.NotificationSettings

interface NotificationView {

    companion object {
        const val NOTIFICATION_ID_MESSAGE = 7438925
        const val NOTIFICATION_ID_CONNECTION = 5438729
        const val NOTIFICATION_ID_FILE = 1415665
        const val NOTIFICATION_TAG_MESSAGE = "tag.message"
        const val NOTIFICATION_TAG_CONNECTION = "tag.connection"
        const val NOTIFICATION_TAG_FILE = "tag.file"
    }

    fun getForegroundNotification(message: String): Notification
    fun showNewMessageNotification(message: String, displayName: String?, deviceName: String, address: String, settings: NotificationSettings)
    fun showConnectionRequestNotification(deviceName: String, settings: NotificationSettings)
    fun showFileTransferNotification(displayName: String?, deviceName: String, address: String, file: TransferringFile, transferredBytes: Long, silently: Boolean, settings: NotificationSettings)
    fun updateFileTransferNotification(transferredBytes: Long, totalBytes: Long)
    fun dismissMessageNotification()
    fun dismissConnectionNotification()
    fun dismissFileTransferNotification()
}