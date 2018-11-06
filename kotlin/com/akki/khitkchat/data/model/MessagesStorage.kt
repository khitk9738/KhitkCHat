package com.akki.khitkchat.data.model

import com.akki.khitkchat.data.entity.ChatMessage

interface MessagesStorage {
    suspend fun getMessagesByDevice(address: String): List<ChatMessage>
    suspend fun getMessageById(uid: Long): ChatMessage?
    suspend fun getFileMessagesByDevice(address: String?): List<ChatMessage>
    suspend fun insertMessage(message: ChatMessage)
    suspend fun updateMessage(message: ChatMessage)
    suspend fun updateMessages(messages: List<ChatMessage>)
    suspend fun removeFileInfo(messageId: Long)
}
