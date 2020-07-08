package cn.troph.tomon.ui.chat.messages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Environment
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import androidx.emoji.widget.EmojiTextView
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageType
import cn.troph.tomon.core.structures.HeaderMessage
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.MessageAttachment
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.utils.Assets
import cn.troph.tomon.core.utils.DensityUtil
import cn.troph.tomon.core.utils.FileUtils
import cn.troph.tomon.core.utils.Url
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.Logger
import com.stfalcon.imageviewer.StfalconImageViewer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet_message.view.*
import kotlinx.android.synthetic.main.header_loading_view.view.*
import kotlinx.android.synthetic.main.item_chat_file.view.*
import kotlinx.android.synthetic.main.item_chat_image.view.*
import kotlinx.android.synthetic.main.item_invite_link.view.*
import kotlinx.android.synthetic.main.item_reaction_view.view.*
import kotlinx.android.synthetic.main.widget_message_item.view.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

const val INVITE_LINK = "https://beta.tomon.co/invite/"

class MessageAdapter(
    private val messageList: MutableList<Message>,
    private val reactionSelectorListener: ReactionSelectorListener
) :
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
            2 -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat_image, parent, false)
                )
            }
            4 -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_invite_link, parent, false)
                )
            }
            else -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.header_loading_view, parent, false)
                )
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        if (messageList[position] is HeaderMessage) {
            return 3
        }
        messageList[position].content?.let {
            if (it.contains(INVITE_LINK) && messageList[position].attachments.size == 0)
                return 4
            else
                return@let
        }
        if (messageList[position].attachments.size == 0) {//normal msg
            return 0
        }
        var type = 0
        for (item in messageList[position].attachments.values) {
            if (isImage(item.type)) {
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
                holder.itemView.setOnLongClickListener {
                    callBottomSheet(holder, 0)
                    true
                }
                if (position > 1) {
                    bind(holder.itemView, msg, messageList[position - 1], holder)
                } else {
                    bind(holder.itemView, msg, holder = holder)
                }
                showReaction(holder, msg)
            }
            1 -> {
                if (position == 0 || messageList[position - 1].authorId != messageList[position].authorId ||
                    messageList[position].timestamp.isAfter(
                        messageList[position - 1].timestamp.plusMinutes(5)
                    )

                ) {
                    holder.itemView.message_avatar_file.visibility = View.VISIBLE
                    holder.itemView.widget_message_timestamp_text_file.visibility = View.VISIBLE
                    holder.itemView.widget_message_author_name_text_file.visibility = View.VISIBLE

                    holder.itemView.message_avatar_file.user = messageList[position].author
                    holder.itemView.widget_message_author_name_text_file.text =
                        messageList[position].author?.name
                    val message = messageList[position]
                    val member = (Client.global.channels[message.channelId] as TextChannel).members[message.authorId?:""]
                    if (member != null) {
                        holder.itemView.widget_message_author_name_text_file.setTextColor( (if (member.roles.color == null)
                            0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt()))
                    }
                    if (messageList[position].type == MessageType.SYSTEM) {
                        holder.itemView.widget_message_author_name_text_file.text = "T🐱"
                    }
                    holder.itemView.widget_message_timestamp_text_file.text =
                        timestampConverter(messageList[position].timestamp)
                } else {
                    holder.itemView.message_avatar_file.visibility = View.GONE
                    holder.itemView.widget_message_timestamp_text_file.visibility = View.GONE
                    holder.itemView.widget_message_author_name_text_file.visibility = View.GONE
                }
                holder.itemView.setOnLongClickListener {
                    callBottomSheet(holder, 1)
                    true
                }
                showReaction(holder, messageList[position])
                for (item in messageList[position].attachments.values) {
                    if (messageList[position].isSending) {
                        val apl = AlphaAnimation(0.1f, 0.78f)
                        apl.duration = 1000
                        apl.repeatCount = -1
                        holder.itemView.textView.text = item.fileName
                        holder.itemView.message_file_size.text =
                            FileUtils.sizeConverter(item.size.toString())
                        holder.itemView.textView.startAnimation(apl)
                    } else {
                        holder.itemView.textView.text = item.fileName
                        holder.itemView.message_file_size.text =
                            FileUtils.sizeConverter(item.size.toString())
                        holder.itemView.textView.clearAnimation()
                    }

                    holder.itemView.btn_file_save.setOnClickListener {
                        val msg = messageList[holder.adapterPosition]
                        for (file in msg.attachments.values) {
                            GeneralSnackbar.make(
                                GeneralSnackbar.findSuitableParent(holder.itemView)!!,
                                "文件保存至:${holder.itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            PRDownloader.download(
                                file.url,
                                holder.itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath,
                                file.fileName
                            )
                                .build().start(object : OnDownloadListener {
                                    override fun onDownloadComplete() {
                                        GeneralSnackbar.make(
                                            GeneralSnackbar.findSuitableParent(holder.itemView)!!,
                                            "下载完成",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onError(error: Error?) {
                                        GeneralSnackbar.make(
                                            GeneralSnackbar.findSuitableParent(holder.itemView)!!,
                                            "下载失败",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            break
                        }
                    }
                    break
                }
            }
            2 -> {
                if (position == 0 || messageList[position - 1].authorId != messageList[position].authorId || messageList[position].timestamp.isAfter(
                        messageList[position - 1].timestamp.plusMinutes(5)
                    )
                ) {
                    holder.itemView.message_avatar_image.visibility = View.VISIBLE
                    holder.itemView.widget_message_author_name_text_image.visibility = View.VISIBLE
                    holder.itemView.widget_message_timestamp_text_image.visibility = View.VISIBLE
                    holder.itemView.message_avatar_image.user = messageList[position].author

                    holder.itemView.widget_message_author_name_text_image.text =
                        messageList[position].author?.name
                    val message = messageList[position]
                    val member = (Client.global.channels[message.channelId] as TextChannel).members[message.authorId?:""]
                    if (member != null) {
                        holder.itemView.widget_message_author_name_text_image.setTextColor( (if (member.roles.color == null)
                            0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt()))
                    }
                    if (messageList[position].type == MessageType.SYSTEM) {
                        holder.itemView.widget_message_author_name_text_image.text = "T🐱"
                    }

                    holder.itemView.widget_message_timestamp_text_image.text =
                        timestampConverter(messageList[position].timestamp)
                } else {
                    holder.itemView.message_avatar_image.visibility = View.GONE
                    holder.itemView.widget_message_author_name_text_image.visibility = View.GONE
                    holder.itemView.widget_message_timestamp_text_image.visibility = View.GONE
                }

                holder.itemView.chat_iv.setOnLongClickListener {
                    callBottomSheet(holder, 2)
                    true
                }
                for (item in messageList[position].attachments.values) {
                    holder.itemView.chat_iv.updateLayoutParams {
                        item.height?.let {
                            height =
                                DensityUtil.px2dip(holder.itemView.context, it.toFloat())
                        }
                    }
                    Glide.with(holder.itemView)
                        .load(if (item.url.isEmpty()) item.fileName else "${item.url}?x-oss-process=image/resize,p_50")
                        .placeholder(R.drawable.loadinglogo)
                        .dontAnimate()
                        .into(holder.itemView.chat_iv)
                    holder.itemView.chat_iv.setOnClickListener {
                        val msg = messageList[holder.adapterPosition]
                        for (image in msg.attachments.values) {
                            StfalconImageViewer.Builder<MessageAttachment>(
                                holder.itemView.context,
                                mutableListOf(image)
                            ) { view, images ->
                                Glide.with(view).load(images.url)
                                    .placeholder(R.drawable.loadinglogo).into(view)
                            }.show()
                            break
                        }
                    }
                    break
                }
                if (messageList[position].isSending) {
                    val apl = AlphaAnimation(0.1f, 0.78f)
                    apl.duration = 1000
                    apl.repeatCount = -1
                    holder.itemView.chat_iv.startAnimation(apl)
                } else {
                    holder.itemView.chat_iv.clearAnimation()
                }
                showReaction(holder, messageList[position])
            }
            3 -> {
                val msg = messageList[position] as HeaderMessage
                if (msg.isEnd) {
                    holder.itemView.loading_text_header.visibility = View.VISIBLE
                    holder.itemView.animation_view.visibility = View.GONE
                } else {
                    holder.itemView.loading_text_header.visibility = View.GONE
                    holder.itemView.animation_view.visibility = View.VISIBLE
                }
            }
            4 -> {
                if (position == 0 || messageList[position - 1].authorId != messageList[position].authorId || messageList[position].timestamp.isAfter(
                        messageList[position - 1].timestamp.plusMinutes(5)
                    )
                ) {
                    holder.itemView.message_avatar_invite.visibility = View.VISIBLE
                    holder.itemView.widget_message_author_name_text_invite.visibility = View.VISIBLE
                    holder.itemView.widget_message_timestamp_text_invite.visibility = View.VISIBLE
                    holder.itemView.message_avatar_invite.user = messageList[position].author
                    holder.itemView.widget_message_author_name_text_invite.text =
                        messageList[position].author?.name
                    val message = messageList[position]
                    val member = (Client.global.channels[message.channelId] as TextChannel).members[message.authorId?:""]
                    if (member != null) {
                        holder.itemView.widget_message_author_name_text_invite.setTextColor( (if (member.roles.color == null)
                            0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt()))
                    }
                    if (messageList[position].type == MessageType.SYSTEM) {
                        holder.itemView.widget_message_author_name_text_invite.text = "T🐱"
                    }
                    holder.itemView.widget_message_timestamp_text_invite.text =
                        timestampConverter(messageList[position].timestamp)
                } else {
                    holder.itemView.message_avatar_invite.visibility = View.GONE
                    holder.itemView.widget_message_author_name_text_invite.visibility = View.GONE
                    holder.itemView.widget_message_timestamp_text_invite.visibility = View.GONE
                }
                holder.itemView.setOnLongClickListener {
                    callBottomSheet(holder, 4)
                    true
                }

                holder.itemView.setOnClickListener {
                    Client.global.guilds.join(Url.parseInviteCode(messageList[holder.adapterPosition].content!!))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(
                            {
                                notifyItemChanged(holder.adapterPosition)
                            }, {
                                GeneralSnackbar.make(
                                    GeneralSnackbar.findSuitableParent(holder.itemView)!!,
                                    "加入失败",
                                    Snackbar.LENGTH_SHORT
                                )
                            })
                }

                messageList[position].content?.let {
                    holder.itemView.link_tv.text = it
                    Client.global.guilds.fetchInvite(Url.parseInviteCode(it))
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                Glide.with(holder.itemView).load(it.inviter.avatar_url)
                                    .placeholder(R.drawable.user_avatar_placeholder)
                                    .into(holder.itemView.user_avatar_invite)
                                holder.itemView.invite_guild_name.text = it.guild.name
                                holder.itemView.joined_cover.visibility =
                                    if (it.joined) View.VISIBLE else View.GONE
                            }, {
                                holder.itemView.invite_guild_name.text = "无效邀请"
                                holder.itemView.joined_cover.visibility = View.GONE
                            })
                }
                holder.itemView.message_avatar_invite.user = messageList[position].author
                holder.itemView.widget_message_author_name_text_invite.text =
                    messageList[position].author?.name
                holder.view.widget_message_timestamp_text_invite.text =
                    timestampConverter(messageList[position].timestamp)
                showReaction(holder, messageList[position])
            }
        }
    }

    private fun showReaction(vh: MessageViewHolder, msg: Message) {
        vh.itemView.flow_reaction_ll.visibility = View.GONE
        for (i in 0 until vh.itemView.flow_reaction_ll.childCount - 1) {
            vh.itemView.flow_reaction_ll[i].visibility = View.GONE
        }
        for ((index, value) in msg.reactions.withIndex()) {
            vh.itemView.flow_reaction_ll.visibility = View.VISIBLE
            vh.itemView.flow_reaction_ll[index].visibility = View.VISIBLE
            val ll = vh.itemView.flow_reaction_ll[index] as LinearLayout
            val text = ll.getChildAt(1) as EmojiTextView
            val image = ll.getChildAt(0) as ImageView
            if (value.me) {
                ll.background =
                    vh.itemView.resources.getDrawable(R.drawable.round_corner_reaction_me, null)
            } else {
                ll.background =
                    vh.itemView.resources.getDrawable(R.drawable.round_corner_reaction, null)
            }

            if (value.isChar) {
                image.visibility = View.GONE
                text.text = "${value.name} ${value.count}"
            } else {
                Glide.with(image).load(value.emoji?.url).into(image)
                text.text = value.count.toString()
            }
            ll.setOnClickListener {
                val msg1 = messageList[vh.adapterPosition]
                val reactionIndex = vh.itemView.flow_reaction_ll.indexOfChild(it)
                val reaction = msg1.reactions.withIndex().elementAt(reactionIndex)
                if (reaction.value.me) {
                    reaction.value.delete().observeOn(AndroidSchedulers.mainThread()).subscribe(
                        Consumer {

                        })
                } else {
                    reaction.value.addReaction().observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            Consumer {

                            })
                }
                return@setOnClickListener
            }
        }
        vh.itemView.flow_reaction_ll[vh.itemView.flow_reaction_ll.childCount - 1].setOnClickListener {
            reactionSelectorListener.OnReactionAddClicked(messageList[vh.adapterPosition])
        }
    }

    private fun bind(
        itemView: View,
        message: Message,
        prevMessage: Message? = null,
        holder: MessageViewHolder
    ) {
        itemView.message_avatar.user = message.author
        itemView.widget_message_timestamp_text.text = timestampConverter(message.timestamp)
        val member = (Client.global.channels[message.channelId] as TextChannel).members[message.authorId?:""]
        if (message.type == MessageType.SYSTEM) {
            itemView.widget_message_author_name_text.text = "T🐱"
        } else {
            itemView.widget_message_author_name_text.text = message.author?.name
            if (member != null) {
                itemView.widget_message_author_name_text.setTextColor( (if (member.roles.color == null)
                    0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt()))
            }
        }


        if (message.content != null && (Assets.regexEmoji.containsMatchIn(message.content!!) || Assets.regexAtUser.containsMatchIn(
                message.content!!
            ))
        ) {
            richText(message, itemView)
        } else
            itemView.widget_message_text.text = message.content
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
        if (message.isSending) {
            val apl = AlphaAnimation(0.1f, 0.78f)
            apl.duration = 1000
            apl.repeatCount = -1
            itemView.widget_message_text.startAnimation(apl)
        } else {
            itemView.widget_message_text.clearAnimation()
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

    private fun isMoreThanFiveMins(fromDate: LocalDateTime, toDate: LocalDateTime): Boolean {
        val min =
            (toDate.toInstant(ZoneOffset.UTC).toEpochMilli() - fromDate.toInstant(ZoneOffset.UTC)
                .toEpochMilli()) / (1000 * 60)
        return min > 5
    }

    private fun richText(
        message: Message,
        itemView: View
    ) {

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
                            val width =
                                (resource.intrinsicWidth.toFloat() / resource.intrinsicHeight.toFloat()) * DensityUtil.dip2px(
                                    itemView.context,
                                    15f
                                ).toFloat()
                            resource.setBounds(
                                0,
                                0,
                                width.toInt(),
                                DensityUtil.dip2px(itemView.context, 15f)
                            )
                            span.setSpan(
                                ImageSpan(resource),
                                it.start,
                                (it.end + 1),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            )
                            itemView.widget_message_text.text = span
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
        }
        contentSpan.contentAtUser.forEach {
            span.setSpan(
                ForegroundColorSpan(Color.parseColor("#5996b8")),
                it.start,
                (it.end) + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            itemView.widget_message_text.text = span
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

    private fun callBottomSheet(viewHolder: MessageViewHolder, viewType: Int) {
        val layoutInflater = LayoutInflater.from(viewHolder.itemView.context)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_message, null)
        val dialog = BottomSheetDialog(viewHolder.itemView.context)
        dialog.setContentView(view)

        view.quote_button.visibility = View.GONE
        //view.quote_button.visibility = if (viewType == 0) View.VISIBLE else View.GONE

        view.share_button.visibility = View.GONE

//        view.share_button.visibility =
//            if (viewType == 0 || viewType == 1 || viewType == 2) View.VISIBLE else View.GONE
        view.reaction_message_button.visibility =
            if (viewType == 0 || viewType == 2 || viewType == 1 || viewType == 4) View.VISIBLE else View.GONE
        view.copy_message_button.visibility = if (viewType == 0) View.VISIBLE else View.GONE

        view.delete_button.visibility =
            if (messageList[viewHolder.adapterPosition].authorId == Client.global.me.id)
                View.VISIBLE
            else
                View.GONE

        view.edit_button.visibility =
            if (messageList[viewHolder.adapterPosition].authorId == Client.global.me.id && viewType == 0)
                View.VISIBLE
            else
                View.GONE

        view.reaction_message_button.setOnClickListener {
            dialog.dismiss()
            reactionSelectorListener.OnReactionAddClicked(messageList[viewHolder.adapterPosition])
        }

        view.cancel_button.setOnClickListener {
            dialog.dismiss()
        }

        view.delete_button.setOnClickListener {
            messageList[viewHolder.adapterPosition].delete().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).doOnError {
                    Logger.d(it.message)
                }
                .subscribe(Consumer {
                    dialog.dismiss()
                })
        }

        view.edit_button.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = true, message = messageList[viewHolder.adapterPosition])
            dialog.dismiss()
        }



        view.copy_message_button.setOnClickListener {
            val clipboard =
                view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData =
                ClipData.newPlainText("copy", messageList[viewHolder.adapterPosition].content)
            clipboard.setPrimaryClip(clip)
            dialog.dismiss()
        }

        viewHolder.itemView.setBackgroundColor(viewHolder.itemView.context.getColor(R.color.channelSelectBackground))

        dialog.show()

        dialog.setOnDismissListener {
            viewHolder.itemView.setBackgroundColor(
                viewHolder.itemView.context.getColor(
                    R.color.background2
                )
            )
        }
    }

    class MessageViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}

interface ReactionSelectorListener {
    fun OnReactionAddClicked(msg: Message)
}