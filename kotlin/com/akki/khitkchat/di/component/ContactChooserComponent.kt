package com.akki.khitkchat.di.component

import com.akki.khitkchat.di.PerActivity
import com.akki.khitkchat.di.module.*
import com.akki.khitkchat.ui.activity.*
import dagger.Component

@PerActivity
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(ContactChooserModule::class))
interface ContactChooserComponent {
    fun inject(activity: ContactChooserActivity)
}
