package com.akki.khitkchat.ui.presenter

import com.akki.khitkchat.data.model.MessagesStorage
import com.akki.khitkchat.extension.toReadableFileSize
import com.akki.khitkchat.ui.view.ImagePreviewView
import kotlinx.coroutines.experimental.launch
import java.io.File

class ImagePreviewPresenter(private val messageId: Long, private val image: File, private val view: ImagePreviewView, private val storage: MessagesStorage) {

    fun loadImage() {
        view.showFileInfo(image.name, image.length().toReadableFileSize())
        view.displayImage("file://${image.absolutePath}")
    }

    fun removeFile() {
        image.delete()
        launch {
            storage.removeFileInfo(messageId)
        }
        view.close()
    }
}
