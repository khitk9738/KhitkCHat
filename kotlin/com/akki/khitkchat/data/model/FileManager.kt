package com.akki.khitkchat.data.model

import android.net.Uri
import java.io.File

interface FileManager {
    suspend fun extractApkFile(): Uri?
}
