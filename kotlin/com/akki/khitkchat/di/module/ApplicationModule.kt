package com.akki.khitkchat.di.module

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.akki.khitkchat.data.model.*
import com.akki.khitkchat.ui.viewmodel.converter.ChatMessageConverter
import com.akki.khitkchat.ui.viewmodel.converter.ContactConverter
import com.akki.khitkchat.ui.viewmodel.converter.ConversationConverter
import com.akki.khitkchat.ui.widget.ShortcutManager
import com.akki.khitkchat.ui.widget.ShortcutManagerImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class ApplicationModule(private val context: Context) {

    @Provides
    @Singleton
    internal fun provideMessagesStorage(): MessagesStorage = MessagesStorageImpl(context)

    @Provides
    @Singleton
    internal fun provideConversationsStorage(): ConversationsStorage = ConversationsStorageImpl(context)

    @Provides
    @Singleton
    internal fun provideSettingsManager(): SettingsManager = SettingsManagerImpl(context)

    @Provides
    @Singleton
    internal fun provideShortcutManager(): ShortcutManager = ShortcutManagerImpl(context)

    @Provides
    @Singleton
    internal fun provideFileManager(): FileManager = FileManagerImpl(context)

    @Provides
    @Singleton
    internal fun provideContactConverter(): ContactConverter = ContactConverter()

    @Provides
    @Singleton
    internal fun provideConversationConverter(): ConversationConverter = ConversationConverter(context)

    @Provides
    @Singleton
    internal fun provideChatMessageConverter(): ChatMessageConverter {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return ChatMessageConverter(context, displayMetrics)
    }
}
