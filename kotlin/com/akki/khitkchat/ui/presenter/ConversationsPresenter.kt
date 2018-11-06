package com.akki.khitkchat.ui.presenter

import android.bluetooth.BluetoothDevice
import com.akki.khitkchat.data.entity.ChatMessage
import com.akki.khitkchat.data.entity.Conversation
import com.akki.khitkchat.data.model.*
import com.akki.khitkchat.ui.view.ConversationsView
import com.akki.khitkchat.ui.viewmodel.ConversationViewModel
import com.akki.khitkchat.ui.viewmodel.converter.ConversationConverter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class ConversationsPresenter(private val view: ConversationsView, private val connection: BluetoothConnector,
                             private val conversationStorage: ConversationsStorage, private val settings: SettingsManager,
                             private val converter: ConversationConverter) {

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {

            connection.setOnConnectListener(connectionListener)
            connection.setOnMessageListener(messageListener)

            loadConversations()

            val device = connection.getCurrentConversation()

            if (device != null && connection.isPending()) {
                view.notifyAboutConnectedDevice(converter.transform(device))
            } else {
                view.hideActions()
            }
        }

        override fun onError() = with(connection) {
            setOnPrepareListener(null)
            setOnConnectListener(null)
            setOnMessageListener(null)
        }
    }

    private val connectionListener = object : OnConnectionListener {

        override fun onConnected(device: BluetoothDevice) {

        }

        override fun onConnectionWithdrawn() {
            view.hideActions()
        }

        override fun onConnectionDestroyed() {
            view.showServiceDestroyed()
        }

        override fun onConnectionAccepted() {
            view.refreshList(connection.getCurrentConversation()?.deviceAddress)
        }

        override fun onConnectionRejected() {

        }

        override fun onConnectedIn(conversation: Conversation) {
            view.notifyAboutConnectedDevice(converter.transform(conversation))
        }

        override fun onConnectedOut(conversation: Conversation) {
            view.redirectToChat(converter.transform(conversation))
        }

        override fun onConnecting() {

        }

        override fun onConnectionLost() {
            view.refreshList(connection.getCurrentConversation()?.deviceAddress)
            view.hideActions()
        }

        override fun onConnectionFailed() {
            view.refreshList(connection.getCurrentConversation()?.deviceAddress)
            view.hideActions()
        }

        override fun onDisconnected() {
            view.refreshList(connection.getCurrentConversation()?.deviceAddress)
            view.hideActions()
        }
    }

    private val messageListener = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {
            loadConversations()
        }

        override fun onMessageSent(message: ChatMessage) {
            loadConversations()
        }

        override fun onMessageDelivered(id: String) {

        }

        override fun onMessageNotDelivered(id: String) {

        }

        override fun onMessageSeen(id: String) {

        }
    }

    fun loadConversations() = launch(UI) {

        val conversations = async(CommonPool) { conversationStorage.getConversations() }.await()

        if (conversations.isEmpty()) {
            view.showNoConversations()
        } else {
            val connectedDevice = if (connection.isConnected())
                connection.getCurrentConversation()?.deviceAddress else null
            view.showConversations(converter.transform(conversations), connectedDevice)
        }
    }

    fun prepareConnection() {
        connection.setOnPrepareListener(prepareListener)
        connection.prepare()
        view.dismissConversationNotification()
    }

    fun loadUserProfile() {
        view.showUserProfile(settings.getUserName(), settings.getUserColor())
    }

    fun releaseConnection() {
        connection.release()
    }

    fun startChat(conversation: ConversationViewModel) {
        connection.acceptConnection()
        view.hideActions()
        view.redirectToChat(conversation)
    }

    fun rejectConnection() {
        view.hideActions()
        connection.rejectConnection()
    }

    fun removeConversation(address: String) {
        connection.sendDisconnectRequest()
        launch {
            conversationStorage.removeConversationByAddress(address)
        }
        view.removeFromShortcuts(address)
        loadConversations()
    }

    fun disconnect() {
        connection.sendDisconnectRequest()
        loadConversations()
    }
}
