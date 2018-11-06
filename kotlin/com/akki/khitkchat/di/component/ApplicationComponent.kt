package com.akki.khitkchat.di.component

import com.akki.khitkchat.data.model.ConversationsStorage
import com.akki.khitkchat.data.model.FileManager
import com.akki.khitkchat.data.model.MessagesStorage
import com.akki.khitkchat.data.model.SettingsManager
import com.akki.khitkchat.di.module.ApplicationModule
import com.akki.khitkchat.ui.presenter.*
import com.akki.khitkchat.ui.viewmodel.converter.ChatMessageConverter
import com.akki.khitkchat.ui.viewmodel.converter.ContactConverter
import com.akki.khitkchat.ui.viewmodel.converter.ConversationConverter
import com.akki.khitkchat.ui.widget.ShortcutManager
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun conversationsStorage(): ConversationsStorage
    fun messagesStorage(): MessagesStorage
    fun settingsManager(): SettingsManager

    fun shortcutManager(): ShortcutManager
    fun fileManager(): FileManager

    fun contactConverter(): ContactConverter
    fun conversationConverter(): ConversationConverter
    fun chatMessageConverter(): ChatMessageConverter
}
