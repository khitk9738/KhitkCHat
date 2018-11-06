package com.akki.khitkchat.data.model

import android.support.annotation.ColorInt

interface SettingsManager {
    fun saveUserName(name: String)
    fun saveUserColor(@ColorInt color: Int)
    fun getUserName(): String
    @ColorInt
    fun getUserColor(): Int
}