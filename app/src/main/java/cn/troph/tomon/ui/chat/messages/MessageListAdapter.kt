package cn.troph.tomon.ui.chat.messages

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
import cn.troph.tomon.ui.widgets.UserAvatar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.bottom_sheet_message.view.*
import kotlinx.android.synthetic.main.dialog_photo_view.view.*
import java.time.LocalDateTime
import java.time.ZoneId

class MessageListAdapter : RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View, val parent: ViewGroup) : RecyclerView.ViewHolder(itemView) {

        private var userAvatar: UserAvatar = itemView.findViewById(R.id.message_avatar)
        private var text: TextView = itemView.findViewById(R.id.widget_message_text)
        private var timestampText: TextView =
            itemView.findViewById(R.id.widget_message_timestamp_text)
        private var authorNameText: TextView =
            itemView.findViewById(R.id.widget_message_author_name_text)
        private var attachmentImage: ImageView =
            itemView.findViewById(R.id.widget_message_attachment)
        var disposable: Disposable? = null

        private fun callPhotoBottomSheet(parent: ViewGroup){
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_image,null)
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
            view.dialog_message_photo_view.setOnLongClickListener{
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

        fun bind(message: Message, prevMessage: Message? = null) {
            userAvatar.user = message.author
            timestampText.text = timestampConverter(message.timestamp)
            authorNameText.text = message.author?.name ?: ""
            text.text = message.content ?: ""
            if (message.attachments.size != 0)
                for (attachment in message.attachments) {
                    Glide.with(attachmentImage.context).load(attachment.url)
                        .into(attachmentImage)
                    attachmentImage.setOnClickListener {
                        callPhotoView(parent, itemView, attachment.url)
                    }
                }
            else Glide.with(attachmentImage.context).clear(attachmentImage)

            if (if (prevMessage == null) true
                else
                    message.authorId != prevMessage.authorId ||
                            message.type != prevMessage.type ||
                            message.timestamp.isAfter(
                                prevMessage.timestamp.plusMinutes(5)
                            )
            ) {
                userAvatar.visibility = View.VISIBLE
                timestampText.visibility = View.VISIBLE
                authorNameText.visibility = View.VISIBLE
            } else {
                userAvatar.visibility = View.GONE
                timestampText.visibility = View.GONE
                authorNameText.visibility = View.GONE
            }

            disposable?.dispose()
            disposable =
                message.observable.observeOn(AndroidSchedulers.mainThread())
                    .doOnError { error -> println(error) }.subscribe {
                        text.text = message.content ?: ""
                        userAvatar.user = message.author
                        timestampText.text = timestampConverter(message.timestamp)
                        authorNameText.text = message.author?.name ?: ""
                        if (message.attachments.size != 0)
                            for (attachment in message.attachments) {
                                Glide.with(attachmentImage.context).load(attachment.url)
                                    .into(attachmentImage)
                            }
                        else Glide.with(attachmentImage.context).clear(attachmentImage)

                    }
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
                        else -> ""
                    }
                else "${dateLocal.year}/${dateLocal.monthValue}/${dateLocal.dayOfMonth}"
            val divide: String = if (timeLocal.hour - 12 > 0) "下午" else "上午"
            val timeHour: String =
                if (timeLocal.hour - 12 > 0) "${timeLocal.hour - 12}" else "${timeLocal.hour}"
            val timeMinute: String =
                if (timeLocal.minute < 10) "0${timeLocal.minute}" else "${timeLocal.minute}"
            return "$adjective $divide$timeHour:$timeMinute"
        }
    }

    var channelId: String? = null
        set(value) {
            field = value
            val messages = value?.let { (Client.global.channels[value] as TextChannel).messages }
            disposable?.dispose()
            disposable =
                messages?.observable?.observeOn(AndroidSchedulers.mainThread())
                    ?.doOnError { error -> println(error) }
                    ?.subscribe {
                        list = if (channelId == null) emptyList() else
                            messages.list.toList()
                    }
        }
    var disposable: Disposable? = null
    var list: List<String> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        channelId = AppState.global.channelSelection.value.channelId
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                channelId = it.channelId
                list = if (channelId == null) emptyList() else
                    (Client.global.channels[channelId!!] as TextChannel).messages.list.toList()
            }
    }

    override fun getItemId(position: Int): Long {
        val id = list[position]
        return id.toLong()
    }

    private fun callBottomSheet(parent: ViewGroup, viewItem: View, message: Message) {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_message, null)
        val dialog = BottomSheetDialog(parent.context)
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
                .doOnError { error -> println(error) }
                .subscribe {
                    dialog.dismiss()
                }
        }
        view.edit_button.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = true, message = message)
            dialog.dismiss()
        }
        viewItem.setBackgroundColor(Color.parseColor("#3A3A3A"))
        dialog.show()
        dialog.setOnDismissListener { viewItem.setBackgroundColor(Color.parseColor("#242424")) }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val inflatedView =
            layoutInflater.inflate(R.layout.widget_message_item, parent, false)
        return ViewHolder(inflatedView, parent)
    }

    override fun getItemCount(): Int =
        list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val id = list[position]
        val channel = Client.global.channels[channelId ?: ""] as TextChannel
        val message = channel.messages[(id.split(Regex("^0*")))[1]]
        holder.itemView.setOnLongClickListener {
            callBottomSheet(holder.parent, it, message!!)
            true
        }
        if (position > 1) {
            val prevMessageId = list[position - 1]
            val prevMessage = channel.messages[(prevMessageId.split(Regex("^0*")))[1]]
            if (message != null && prevMessage != null) {
                holder.bind(message, prevMessage)
            }
        } else
            if (message != null) {
                holder.bind(message)
            }
    }
}