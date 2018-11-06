package com.akki.khitkchat.ui.view

import android.support.annotation.ColorInt
import com.akki.khitkchat.data.entity.Conversation
import com.akki.khitkchat.ui.viewmodel.ConversationViewModel

interface ConversationsView {

    fun redirectToChat(conversation: ConversationViewModel)
    fun notifyAboutConnectedDevice(conversation: ConversationViewModel)
    fun showServiceDestroyed()
    fun showNoConversations()
    fun showRejectedNotification(conversation: ConversationViewModel)
    fun hideActions()
    fun refreshList(connected: String?)
    fun showConversations(conversations: List<ConversationViewModel>, connected: String?)
    fun showUserProfile(name: String, @ColorInt color: Int)
    fun dismissConversationNotification()
    fun removeFromShortcuts(address: String)
}
