package com.akki.khitkchat.ui.widget

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.RequiresApi
import com.amulyakhare.textdrawable.TextDrawable
import com.akki.khitkchat.R
import com.akki.khitkchat.ui.activity.ChatActivity
import com.akki.khitkchat.ui.activity.ConversationsActivity
import com.akki.khitkchat.ui.activity.ScanActivity
import com.akki.khitkchat.extension.getBitmap
import com.akki.khitkchat.extension.getFirstLetter
import java.util.*

class ShortcutManagerImpl(private val context: Context) : ShortcutManager {

    private val ID_SEARCH = "id.search"

    private var shortcutManager: android.content.pm.ShortcutManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager = context
                    .getSystemService(android.content.pm.ShortcutManager::class.java)
        }
    }

    override fun addSearchShortcut() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            if (shortcutManager != null) {

                val isSearchAdded = shortcutManager!!.dynamicShortcuts
                        .filter { it.id == ID_SEARCH }.any()

                if (isSearchAdded) {
                    return
                }
            }

            val shortcut = ShortcutInfo.Builder(context, ID_SEARCH)
                    .setShortLabel(context.getString(R.string.scan__scan))
                    .setLongLabel(context.getString(R.string.scan__scan))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_search_black_24dp))
                    .setIntents(arrayOf(
                            Intent(Intent.ACTION_MAIN, Uri.EMPTY, context, ConversationsActivity::class.java)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            Intent(Intent.ACTION_SEARCH, Uri.EMPTY, context, ScanActivity::class.java)

                    ))
                    .build()

            shortcutManager?.addDynamicShortcuts(Arrays.asList(shortcut))
        }
    }

    override fun addConversationShortcut(address: String, name: String, @ColorInt color: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            removeLatestIfNeeded(address)

            val shortcut = createConversationShortcut(address, name, color)
            shortcutManager?.addDynamicShortcuts(Arrays.asList(shortcut))
        }
    }

    override fun removeConversationShortcut(address: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager?.removeDynamicShortcuts(Arrays.asList(address))
        }
    }

    override fun requestPinConversationShortcut(address: String, name: String, color: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcut = createConversationShortcut(address, name, color)
            shortcutManager?.requestPinShortcut(shortcut, null)
        }
    }

    override fun isRequestPinShortcutSupported(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (shortcutManager != null) shortcutManager!!.isRequestPinShortcutSupported else false
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createConversationShortcut(address: String, name: String, @ColorInt color: Int): ShortcutInfo {

        val drawable = TextDrawable.builder().buildRound(name.getFirstLetter(), color)

        return ShortcutInfo.Builder(context, address)
                .setShortLabel(name)
                .setLongLabel(name)
                .setIcon(Icon.createWithBitmap(drawable.getBitmap()))
                .setIntents(arrayOf(
                        Intent(Intent.ACTION_MAIN, Uri.EMPTY, context, ConversationsActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                        Intent(Intent.ACTION_VIEW, Uri.EMPTY, context, ChatActivity::class.java)
                                .putExtra(ChatActivity.EXTRA_ADDRESS, address)
                ))
                .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun removeLatestIfNeeded(newShortcutId: String) {

        shortcutManager?.removeDynamicShortcuts(Arrays.asList(newShortcutId))

        val conversations = shortcutManager?.dynamicShortcuts
                ?.filter { it.id != ID_SEARCH }
                ?.sortedByDescending { it.lastChangedTimestamp }

        if (conversations != null && conversations.size == 2) {
            shortcutManager?.removeDynamicShortcuts(Arrays.asList(conversations[1].id))
        }
    }
}
