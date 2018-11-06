package com.akki.khitkchat.data.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.akki.khitkchat.data.entity.ChatMessage
import com.akki.khitkchat.data.entity.Conversation

@Database(entities = arrayOf(ChatMessage::class, Conversation::class), version = 2)
@TypeConverters(Converter::class)
abstract class ChatDatabase: RoomDatabase() {

    abstract fun conversationsDao(): ConversationsDao

    abstract fun messagesDao(): MessagesDao
}
