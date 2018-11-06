package com.akki.khitkchat.di.module

import com.akki.khitkchat.data.model.*
import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.ui.activity.ChatActivity
import com.akki.khitkchat.ui.activity.ConversationsActivity
import com.akki.khitkchat.ui.presenter.ChatPresenter
import com.akki.khitkchat.ui.presenter.ConversationsPresenter
import com.akki.khitkchat.ui.viewmodel.converter.ChatMessageConverter
import dagger.Module
import dagger.Provides

@Module
class ChatModule(private val address: String, private val activity: ChatActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(messages: MessagesStorage, conversations: ConversationsStorage,
                                  scanner: BluetoothScanner, connector: BluetoothConnector, converter: ChatMessageConverter): ChatPresenter =
            ChatPresenter(address, activity, conversations, messages, scanner, connector, converter)

    @Provides
    @PerActivity
    internal fun provideConnector(): BluetoothConnector = BluetoothConnectorImpl(activity)

    @Provides
    @PerActivity
    internal fun provideScanner(): BluetoothScanner = BluetoothScannerImpl(activity)
}
