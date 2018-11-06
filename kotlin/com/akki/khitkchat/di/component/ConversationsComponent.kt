package com.akki.khitkchat.di.component

import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.di.module.ConversationsModule
import com.akki.khitkchat.ui.activity.ConversationsActivity
import dagger.Component

@PerActivity
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(ConversationsModule::class))
interface ConversationsComponent {
    fun inject(activity: ConversationsActivity)
}
