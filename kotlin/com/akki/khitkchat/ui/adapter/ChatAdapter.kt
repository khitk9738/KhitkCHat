package com.akki.khitkchat.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.akki.khitkchat.R
import com.akki.khitkchat.data.entity.MessageType
import com.akki.khitkchat.ui.viewmodel.ChatMessageViewModel
import com.squareup.picasso.Picasso
import java.util.*
import com.scottyab.aescrypt.AESCrypt
import java.security.GeneralSecurityException
import android.util.Log
import com.akki.khitkchat.ui.activity.ChatActivity

class ChatAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val OWN_TEXT_MESSAGE = 0
    private val OWN_IMAGE_MESSAGE = 1
    private val FOREIGN_TEXT_MESSAGE = 2
    private val FOREIGN_IMAGE_MESSAGE = 3

    val picassoTag = Object()

    var messages = LinkedList<ChatMessageViewModel>()

    var imageClickListener: ((view: ImageView, message: ChatMessageViewModel) -> Unit)? = null

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        val message = messages[position]

        if (viewHolder is ImageMessageViewHolder) {

            val holder: ImageMessageViewHolder? = viewHolder

            if (!message.isImageAvailable) {

                holder?.image?.visibility = View.GONE
                holder?.missingLabel?.visibility = View.VISIBLE
                holder?.missingLabel?.setText(message.imageProblemText)

            } else {

                holder?.image?.visibility = View.VISIBLE
                holder?.missingLabel?.visibility = View.GONE

                val size = message.imageSize
                holder?.image?.layoutParams = FrameLayout.LayoutParams(size.width, size.height)
                holder?.image?.setOnClickListener {
                    imageClickListener?.invoke(holder.image, message)
                }

                Picasso.with(context)
                        .load(message.imageUri)
                        .config(Bitmap.Config.RGB_565)
                        .error(R.drawable.send)
                        .placeholder(R.drawable.send)
                        .tag(picassoTag)
                        .resize(size.width, size.height)
                        .into(holder?.image)
            }

            holder?.date?.text = message.date
        } else if (viewHolder is TextMessageViewHolder) {
            val holder: TextMessageViewHolder? = viewHolder

            var msg:String?=message.text
            var password:String ="baguvix" //deviceAddress.toString()
            var decryptedMsg:String?=""
            try {
                decryptedMsg= AESCrypt.decrypt(password, msg);
            }catch (e:GeneralSecurityException  ){
                Log.getStackTraceString(e)
            }

            holder?.text?.text = decryptedMsg
            holder?.date?.text = message.date
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (messages[position].own) {
            when (message.type) {
                MessageType.IMAGE -> OWN_IMAGE_MESSAGE
                else -> OWN_TEXT_MESSAGE
            }
        } else {
            when (message.type) {
                MessageType.IMAGE -> FOREIGN_IMAGE_MESSAGE
                else -> FOREIGN_TEXT_MESSAGE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val layoutId = when (viewType) {
            OWN_TEXT_MESSAGE -> R.layout.item_message_text_own
            OWN_IMAGE_MESSAGE -> R.layout.item_message_image_own
            FOREIGN_TEXT_MESSAGE -> R.layout.item_message_text_foreign
            FOREIGN_IMAGE_MESSAGE -> R.layout.item_message_image_foreign
            else -> 0
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return when (viewType) {
            OWN_IMAGE_MESSAGE, FOREIGN_IMAGE_MESSAGE -> ImageMessageViewHolder(view)
            else -> TextMessageViewHolder(view)
        }
    }

    class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val text: TextView = itemView.findViewById(R.id.tv_text)
    }

    class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val image: ImageView = itemView.findViewById(R.id.iv_image)
        val missingLabel: TextView = itemView.findViewById(R.id.tv_missing_file)
    }
}
