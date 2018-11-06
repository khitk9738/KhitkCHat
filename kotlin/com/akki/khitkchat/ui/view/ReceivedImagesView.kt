package com.akki.khitkchat.ui.view

import com.akki.khitkchat.data.entity.ChatMessage

interface ReceivedImagesView {
    fun displayImages(imageMessages: List<ChatMessage>)
    fun showNoImages()
}
