package com.akki.khitkchat.di.module

import com.akki.khitkchat.data.model.ConversationsStorage
import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.ui.activity.ContactChooserActivity
import com.akki.khitkchat.ui.presenter.ContactChooserPresenter
import com.akki.khitkchat.ui.viewmodel.converter.ContactConverter
import dagger.Module
import dagger.Provides

@Module
class ContactChooserModule(private val activity: ContactChooserActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(storage: ConversationsStorage, converter: ContactConverter): ContactChooserPresenter = ContactChooserPresenter(activity, storage, converter)
}
