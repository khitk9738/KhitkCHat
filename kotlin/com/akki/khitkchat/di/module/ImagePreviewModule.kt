package com.akki.khitkchat.di.module

import com.akki.khitkchat.data.model.MessagesStorage
import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.ui.activity.ImagePreviewActivity
import com.akki.khitkchat.ui.presenter.ImagePreviewPresenter
import dagger.Module
import dagger.Provides
import java.io.File

@Module
class ImagePreviewModule(private val messageId: Long, private val image: File, private val activity: ImagePreviewActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(messages: MessagesStorage): ImagePreviewPresenter = ImagePreviewPresenter(messageId, image, activity, messages)
}
