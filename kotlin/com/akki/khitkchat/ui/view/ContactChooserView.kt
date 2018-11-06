package com.akki.khitkchat.ui.view

import com.akki.khitkchat.ui.viewmodel.ContactViewModel

interface ContactChooserView {
    fun showContacts(contacts: List<ContactViewModel>)
    fun showNoContacts()
}