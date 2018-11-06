package com.akki.khitkchat.ui.presenter

import com.akki.khitkchat.data.model.ConversationsStorage
import com.akki.khitkchat.ui.view.ContactChooserView
import com.akki.khitkchat.ui.viewmodel.converter.ContactConverter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

class ContactChooserPresenter(private val view: ContactChooserView, private val model: ConversationsStorage, private val converter: ContactConverter,
                              private val uiContext: CoroutineContext = UI, private val bgContext: CoroutineContext = CommonPool) {

    fun loadContacts() = launch(uiContext) {

        val contacts = async(bgContext) { model.getConversations() }.await()

        if (contacts.isNotEmpty()) {
            val viewModels = converter.transform(contacts)
            view.showContacts(viewModels)
        } else {
            view.showNoContacts()
        }
    }
}
