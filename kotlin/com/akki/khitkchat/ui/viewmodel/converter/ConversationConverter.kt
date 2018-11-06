package com.akki.khitkchat.ui.viewmodel.converter

import android.content.Context
import com.amulyakhare.textdrawable.TextDrawable
import com.akki.khitkchat.R
import com.akki.khitkchat.data.entity.Conversation
import com.akki.khitkchat.data.entity.MessageType
import com.akki.khitkchat.extension.getFirstLetter
import com.akki.khitkchat.extension.getRelativeTime
import com.akki.khitkchat.ui.viewmodel.ConversationViewModel

class ConversationConverter(private val context: Context) {

    fun transform(conversation: Conversation): ConversationViewModel {

        val lastMessage = when {
            conversation.messageType == MessageType.IMAGE -> context.getString(R.string.chat__image_message, "\uD83D\uDCCE")
            !conversation.lastMessage.isNullOrEmpty() -> conversation.lastMessage
            else -> null
        }

        val lastActivity = if (!conversation.lastMessage.isNullOrEmpty() || conversation.messageType == MessageType.IMAGE) {
            conversation.lastActivity?.getRelativeTime(context)
        } else {
            null
        }

        return ConversationViewModel(
                conversation.deviceAddress,
                conversation.deviceName,
                conversation.displayName,
                "${conversation.displayName} (${conversation.deviceName})",
                conversation.color,
                lastMessage,
                conversation.lastActivity,
                lastActivity,
                conversation.notSeen

        )
    }

    fun transform(conversationCollection: Collection<Conversation>): List<ConversationViewModel> {
        return conversationCollection.map {
            transform(it)
        }
    }
}
