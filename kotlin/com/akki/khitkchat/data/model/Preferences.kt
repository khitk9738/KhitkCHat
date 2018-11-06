package com.akki.khitkchat.data.model

import com.akki.khitkchat.ui.util.NotificationSettings

interface Preferences {
    fun isSoundEnabled(): Boolean
    fun isVibrationEnabled(): Boolean
    fun getSettings(): NotificationSettings
}
