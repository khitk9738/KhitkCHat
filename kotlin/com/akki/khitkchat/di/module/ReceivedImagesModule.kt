package com.akki.khitkchat.di.module

import com.akki.khitkchat.data.model.MessagesStorage
import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.ui.activity.ReceivedImagesActivity
import com.akki.khitkchat.ui.presenter.ReceivedImagesPresenter
import dagger.Module
import dagger.Provides

@Module
class ReceivedImagesModule(private val address: String?, private val activity: ReceivedImagesActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(messages: MessagesStorage): ReceivedImagesPresenter = ReceivedImagesPresenter(address, activity, messages)
}
