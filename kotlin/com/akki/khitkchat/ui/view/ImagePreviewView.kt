package com.akki.khitkchat.ui.view

interface ImagePreviewView {
    fun showFileInfo(name: String, readableSize: String)
    fun displayImage(fileUrl: String)
    fun close()
}
