package com.akki.khitkchat.ui.viewmodel.converter

import com.amulyakhare.textdrawable.TextDrawable
import com.akki.khitkchat.ui.viewmodel.ContactViewModel
import com.akki.khitkchat.data.entity.Conversation
import com.akki.khitkchat.extension.getFirstLetter

class ContactConverter {

    fun transform(conversation: Conversation): ContactViewModel {
        return ContactViewModel(
                conversation.deviceAddress,
                "${conversation.displayName} (${conversation.deviceName})",
                TextDrawable.builder()
                        .buildRound(conversation.displayName.getFirstLetter(), conversation.color)
        )
    }

    fun transform(conversationCollection: Collection<Conversation>): List<ContactViewModel> {
        return conversationCollection.map {
            transform(it)
        }
    }
}