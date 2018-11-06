package com.akki.khitkchat.ui.presenter

import com.akki.khitkchat.data.model.MessagesStorage
import com.akki.khitkchat.ui.view.ReceivedImagesView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

class ReceivedImagesPresenter(private val address: String?, private val view: ReceivedImagesView, private val model: MessagesStorage,
                              private val uiContext: CoroutineContext = UI, private val bgContext: CoroutineContext = CommonPool) {

    fun loadImages() = launch(uiContext) {
        val messages = async(bgContext) { model.getFileMessagesByDevice(address) }.await()
        if (messages.isNotEmpty()) {
            view.displayImages(messages)
        } else {
            view.showNoImages()
        }
    }
}
