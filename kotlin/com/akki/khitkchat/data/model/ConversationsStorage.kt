package com.akki.khitkchat.data.model

import com.akki.khitkchat.data.entity.Conversation

interface ConversationsStorage {
    suspend fun getConversations(): List<Conversation>
    suspend fun getConversationByAddress(address: String): Conversation?
    suspend fun insertConversation(conversation: Conversation)
    suspend fun removeConversationByAddress(address: String)
}
