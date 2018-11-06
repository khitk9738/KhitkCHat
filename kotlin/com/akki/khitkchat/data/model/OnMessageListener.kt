package com.akki.khitkchat.data.model

import com.akki.khitkchat.data.entity.ChatMessage

interface OnMessageListener {
    fun onMessageReceived(message: ChatMessage)
    fun onMessageSent(message: ChatMessage)
    fun onMessageDelivered(id: String)
    fun onMessageNotDelivered(id: String)
    fun onMessageSeen(id: String)
}