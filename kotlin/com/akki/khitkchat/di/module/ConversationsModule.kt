package com.akki.khitkchat.di.module

import com.akki.khitkchat.data.model.BluetoothConnector
import com.akki.khitkchat.data.model.BluetoothConnectorImpl
import com.akki.khitkchat.data.model.ConversationsStorage
import com.akki.khitkchat.data.model.SettingsManager
import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.ui.activity.ConversationsActivity
import com.akki.khitkchat.ui.presenter.ConversationsPresenter
import com.akki.khitkchat.ui.viewmodel.converter.ConversationConverter
import dagger.Module
import dagger.Provides

@Module
class ConversationsModule(private val activity: ConversationsActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(connector: BluetoothConnector, storage: ConversationsStorage,
                                  settings: SettingsManager, converter: ConversationConverter): ConversationsPresenter =
            ConversationsPresenter(activity, connector, storage, settings, converter)

    @Provides
    @PerActivity
    internal fun provideConnector(): BluetoothConnector = BluetoothConnectorImpl(activity)
}
