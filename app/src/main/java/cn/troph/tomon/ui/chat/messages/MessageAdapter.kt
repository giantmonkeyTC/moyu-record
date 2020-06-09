package cn.troph.tomon.ui.chat.messages

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Environment
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.EnvironmentCompat
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.MessageAttachment
import cn.troph.tomon.core.utils.Assets
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.orhanobut.logger.Logger
import com.stfalcon.imageviewer.StfalconImageViewer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.bottom_sheet_message.view.*
import kotlinx.android.synthetic.main.dialog_photo_view.view.*
import kotlinx.android.synthetic.main.item_chat_file.view.*
import kotlinx.android.synthetic.main.item_chat_image.view.*
import kotlinx.android.synthetic.main.widget_message_item.view.*
import kotlinx.android.synthetic.main.widget_message_reaction.view.*
import java.time.LocalDateTime
import java.time.ZoneId

class MessageAdapter(private val messageList: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        when (viewType) {
            0 -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.widget_message_item, parent, false)
                )
            }
            1 -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat_file, parent, false)
                )
            }
            else -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat_image, parent, false)
                )
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        if (messageList[position].attachments.size == 0) {//normal msg
            return 0
        }
        var type = 0
        for (item in messageList[position].attachments.values) {
            if (isImage(item.fileName)) {
                type = 2
            } else {
                type = 1
            }
            break
        }
        return type
    }

    private fun isImage(name: String): Boolean {
        return name.endsWith("jpg", true) || name.endsWith("bmp", true) || name.endsWith(
            "gif",
            true
        ) || name.endsWith("png", true) || name.endsWith("jpeg", true)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        when (getItemViewType(position)) {
            0 -> {
                val msg = messageList[position]
                holder.view.setOnLongClickListener {
                    callBottomSheet(it, msg)
                    true
                }
                if (position > 1) {
                    bind(holder.itemView, msg, messageList[position - 1])
                } else {
                    bind(holder.itemView, msg)
                }
            }
            1 -> {
                if (position - 1 >= 0 && messageList[position - 1].authorId != messageList[position].authorId) {
                    holder.itemView.message_avatar_file.visibility = View.VISIBLE
                    holder.itemView.message_avatar_file.user = messageList[position].author
                }else{
                    holder.itemView.message_avatar_file.visibility = View.GONE
                }

                for (item in messageList[position].attachments.values) {
                    holder.itemView.textView.text = item.fileName
                    holder.itemView.setOnClickListener {
                        val msg = messageList[holder.adapterPosition]
                        for (file in msg.attachments.values) {
                            Logger.d(holder.itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath)
                            PRDownloader.download(
                                file.url,
                                holder.itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath,
                                file.fileName
                            )
                                .build().start(object : OnDownloadListener {
                                    override fun onDownloadComplete() {
                                        Logger.d("Down complete")
                                    }

                                    override fun onError(error: Error?) {
                                        Logger.d(error.toString())
                                    }
                                })
                            break
                        }
                    }
                    break
                }
            }
            2 -> {
                if (position - 1 >= 0 && messageList[position - 1].authorId != messageList[position].authorId) {
                    holder.itemView.message_avatar_image.visibility = View.VISIBLE
                    holder.itemView.message_avatar_image.user = messageList[position].author
                }else{
                    holder.itemView.message_avatar_image.visibility = View.GONE
                }

                for (item in messageList[position].attachments.values) {
                    Glide.with(holder.itemView).load(item.url).into(holder.itemView.chat_iv)
                    holder.itemView.setOnClickListener {
                        val msg = messageList[holder.adapterPosition]
                        for (image in msg.attachments.values) {
                            StfalconImageViewer.Builder<MessageAttachment>(
                                holder.itemView.context,
                                mutableListOf(image)
                            ) { view, images ->
                                Glide.with(view).load(images.url).into(view)
                            }.show()
                            break
                        }
                    }
                    break
                }
            }
        }

    }

    private fun bind(itemView: View, message: Message, prevMessage: Message? = null) {
        itemView.message_avatar.user = message.author
        itemView.widget_message_timestamp_text.text = timestampConverter(message.timestamp)
        itemView.widget_message_author_name_text.text = message.author?.name ?: ""
        itemView.widget_reactions.removeAllViews()

        if (message.content != null && (Assets.regexEmoji.containsMatchIn(message.content!!) || Assets.regexAtUser.containsMatchIn(
                message.content!!
            ))
        ) {
            itemView.widget_message_text.text = richText(message, itemView)
        } else
            itemView.widget_message_text.text = message.content
        if (message.attachments.size != 0)
            for (attachment in message.attachments) {
                Glide.with(itemView.context).load(attachment.url)
                    .into(itemView.widget_message_attachment)
                itemView.widget_message_attachment.setOnClickListener {
                    callPhotoView(itemView as ViewGroup, itemView, attachment.url)
                }
            }
        else Glide.with(itemView.context).clear(itemView.widget_message_attachment)
        if (message.reactions.size != 0)
            reactionsBinder(message = message, itemView = itemView)
        else {
            itemView.widget_reactions.visibility = View.GONE
            itemView.widget_reactions.removeAllViews()
        }
        if (if (prevMessage == null) true
            else
                message.authorId != prevMessage.authorId ||
                        message.type != prevMessage.type ||
                        message.timestamp.isAfter(
                            prevMessage.timestamp.plusMinutes(5)
                        )
        ) {
            itemView.message_avatar.visibility = View.VISIBLE
            itemView.widget_message_timestamp_text.visibility = View.VISIBLE
            itemView.widget_message_author_name_text.visibility = View.VISIBLE
        } else {
            itemView.message_avatar.visibility = View.GONE
            itemView.widget_message_timestamp_text.visibility = View.GONE
            itemView.widget_message_author_name_text.visibility = View.GONE
        }
    }

    private fun reactionsBinder(message: Message, itemView: View) {
        itemView.widget_reactions.removeAllViews()
        for (reaction in message.reactions) {
            message.reactions.observable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                if (message.reactions.size == 0)
                    itemView.widget_reactions.visibility = View.GONE
            }
            itemView.widget_reactions.visibility = View.VISIBLE
            val layoutInflater = LayoutInflater.from(itemView.context)
            val reaction_view =
                layoutInflater.inflate(R.layout.widget_message_reaction, null)
            val reaction_image =
                reaction_view.findViewById<ImageView>(R.id.widget_reaction_image)
            val reaction_emoji =
                reaction_view.findViewById<TextView>(R.id.widget_reaction_emoji)
            val reaction_count =
                reaction_view.findViewById<TextView>(R.id.widget_reaction_count)

            if (reaction.me) {
                reaction_view.widget_reaction_unit.setBackgroundResource(R.drawable.shape_message_reaction_me)
            }
            reaction_view.widget_reaction_unit.setOnClickListener {
                if (reaction.me)
                    reaction.delete().observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                        }, { err ->
                            println(
                                err.message
                            )
                        })
                else
                    reaction.addReaction().observeOn(AndroidSchedulers.mainThread()).subscribe {
                    }

            }

            if (reaction.id.matches(Regex("""^[0-9]+""")))
                Glide.with(itemView.context).asDrawable()
                    .load(Assets.emojiURL(reaction.id))
                    .into(
                        object : CustomTarget<Drawable>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                reaction_image.setImageDrawable(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
            else
                reaction_emoji.text = reaction.name
            reaction_count.text = "${reaction.count}"
            reaction.observable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                reaction_count.text = "${reaction.count}"
                if (reaction.me) {
                    reaction_view.widget_reaction_unit.setBackgroundResource(R.drawable.shape_message_reaction_me)
                } else
                    reaction_view.widget_reaction_unit.setBackgroundResource(R.drawable.shape_message_reaction)
            }
            itemView.widget_reactions.addView(reaction_view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    private fun callPhotoBottomSheet(parent: ViewGroup) {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_image, null)
        val dialog = BottomSheetDialog(parent.context)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun callPhotoView(parent: ViewGroup, viewItem: View, url: String) {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.dialog_photo_view, null)
        val dialog = AlertDialog.Builder(parent.context).create()
        Glide.with(viewItem.context).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                view.dialog_message_photo_view.setImageBitmap(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
        view.dialog_message_photo_view.setOnLongClickListener {
            callPhotoBottomSheet(parent)
            true
        }
        view.dialog_message_photo_view.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setView(view)
        dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

    }

    private fun richText(message: Message, itemView: View): SpannableString {

        val contentSpan = Assets.contentParser(message.content!!)
        val span = SpannableString(contentSpan.parseContent)
        contentSpan.contentEmoji.forEach {
            Glide.with(itemView.context).asDrawable()
                .load(Assets.emojiURL(it.id))
                .into(
                    object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            resource.setBounds(
                                0,
                                0,
                                (itemView.widget_message_text.textSize * 2).toInt(),
                                (itemView.widget_message_text.textSize * 2).toInt()
                            )
                            span.setSpan(
                                ImageSpan(resource),
                                it.start,
                                (it.end + 1),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            )

                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
        }
        contentSpan.contentAtUser.forEach {
            span.setSpan(
                BackgroundColorSpan(Color.parseColor("#5996b8")),
                it.start,
                (it.end) + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        return span
    }

    private fun timestampConverter(timestamp: LocalDateTime): String {
        val zoneLocal = ZoneId.systemDefault()
        val timeLocal = timestamp.atZone(zoneLocal).toLocalTime()
        val dateLocal = timestamp.atZone(zoneLocal).toLocalDate()
        val adjective =
            if (dateLocal.month == LocalDateTime.now().month && dateLocal.year == LocalDateTime.now().year)
                when (LocalDateTime.now().dayOfMonth - dateLocal.dayOfMonth) {
                    0 -> "今天"
                    1 -> "昨天"
                    2 -> "前天"
                    3, 4, 5, 6 -> "${LocalDateTime.now().dayOfMonth - dateLocal.dayOfMonth}天前"
                    else -> "${dateLocal.year}/${dateLocal.monthValue}/${dateLocal.dayOfMonth}"
                }
            else "${dateLocal.year}/${dateLocal.monthValue}/${dateLocal.dayOfMonth}"
        val divide: String = if (timeLocal.hour - 12 > 0) "下午" else "上午"
        val timeHour: String =
            if (timeLocal.hour - 12 > 0) "${timeLocal.hour - 12}" else "${timeLocal.hour}"
        val timeMinute: String =
            if (timeLocal.minute < 10) "0${timeLocal.minute}" else "${timeLocal.minute}"
        return "$adjective $divide$timeHour:$timeMinute"
    }

    private fun callBottomSheet(viewItem: View, message: Message) {
        val layoutInflater = LayoutInflater.from(viewItem.context)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_message, null)
        val dialog = BottomSheetDialog(viewItem.context)
        dialog.setContentView(view)

        view.delete_button.visibility = if (message.authorId == Client.global.me.id)
            View.VISIBLE
        else
            View.GONE

        view.edit_button.visibility = if (message.authorId == Client.global.me.id)
            View.VISIBLE
        else
            View.GONE

        view.cancel_button.setOnClickListener {
            dialog.dismiss()
        }
        view.delete_button.setOnClickListener {
            message.delete().observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    dialog.dismiss()
                }
        }
        view.edit_button.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = true, message = message)
            dialog.dismiss()
        }
        view.copy_message_button.setOnClickListener {
            val clipboard =
                view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("copy", message.content)
            clipboard.setPrimaryClip(clip)
            dialog.dismiss()
        }
        viewItem.setBackgroundColor(Color.parseColor("#3A3A3A"))
        dialog.show()
        dialog.setOnDismissListener { viewItem.setBackgroundColor(Color.parseColor("#242424")) }

    }

    class MessageViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}