package com.akki.khitkchat.di.component

import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.di.module.ChatModule
import com.akki.khitkchat.di.module.ConversationsModule
import com.akki.khitkchat.di.module.ProfileModule
import com.akki.khitkchat.di.module.ReceivedImagesModule
import com.akki.khitkchat.ui.activity.ChatActivity
import com.akki.khitkchat.ui.activity.ConversationsActivity
import com.akki.khitkchat.ui.activity.ProfileActivity
import com.akki.khitkchat.ui.activity.ReceivedImagesActivity
import dagger.Component

@PerActivity
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(ReceivedImagesModule::class))
interface ReceivedImagesComponent {
    fun inject(activity: ReceivedImagesActivity)
}
