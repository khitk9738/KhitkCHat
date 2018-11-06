package com.akki.khitkchat.di.module

import com.akki.khitkchat.data.model.SettingsManager
import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.ui.activity.ProfileActivity
import com.akki.khitkchat.ui.presenter.ProfilePresenter
import dagger.Module
import dagger.Provides

@Module
class ProfileModule(private val activity: ProfileActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(settings: SettingsManager): ProfilePresenter = ProfilePresenter(activity, settings)
}
