package com.akki.khitkchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.akki.khitkchat.R
import com.akki.khitkchat.di.ComponentsManager
import com.akki.khitkchat.ui.adapter.ContactsAdapter
import com.akki.khitkchat.ui.viewmodel.ContactViewModel
import com.akki.khitkchat.ui.presenter.ContactChooserPresenter
import com.akki.khitkchat.ui.view.ContactChooserView
import javax.inject.Inject

class ContactChooserActivity : SkeletonActivity(), ContactChooserView {

    @Inject
    lateinit var presenter: ContactChooserPresenter

    private lateinit var contactsList: RecyclerView
    private lateinit var noContactsLabel: TextView

    private val contactsAdapter = ContactsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_chooser, ActivityType.CHILD_ACTIVITY)
        ComponentsManager.injectContactChooser(this)

        noContactsLabel = findViewById(R.id.tv_no_contacts)

        contactsList = findViewById<RecyclerView>(R.id.rv_contacts).apply {
            layoutManager = LinearLayoutManager(this@ContactChooserActivity)
            adapter = contactsAdapter
        }

        val message = intent.getStringExtra(EXTRA_MESSAGE)
        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)

        contactsAdapter.clickListener = {
            ChatActivity.start(this, it.address, message, filePath)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.loadContacts()
    }

    override fun showContacts(contacts: List<ContactViewModel>) {
        contactsAdapter.conversations = ArrayList(contacts)
        contactsAdapter.notifyDataSetChanged()
    }

    override fun showNoContacts() {
        noContactsLabel.visibility = View.VISIBLE
        contactsList.visibility = View.GONE
    }

    companion object {

        private const val EXTRA_MESSAGE = "extra.message"
        private const val EXTRA_FILE_PATH = "extra.file_path"

        fun start(context: Context, message: String?, filePath: String?) {
            val intent = Intent(context, ContactChooserActivity::class.java)
                    .putExtra(EXTRA_MESSAGE, message)
                    .putExtra(EXTRA_FILE_PATH, filePath)

            context.startActivity(intent)
        }
    }
}
