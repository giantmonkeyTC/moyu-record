package cn.troph.tomon.ui.chat.fragments

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client.Companion.global
import cn.troph.tomon.core.MessageType
import cn.troph.tomon.core.events.*
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.utils.Assets.contentParser
import cn.troph.tomon.core.utils.Assets.regexAtUser
import cn.troph.tomon.core.utils.Assets.regexEmoji
import cn.troph.tomon.ui.activities.ChatActivity
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import cn.troph.tomon.ui.utils.LocalDateUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.widget_dmchannel_item.view.*

class DmChannelSelectorAdapter(private val dmChannelList: MutableList<DmChannel>) :
    RecyclerView.Adapter<DmChannelSelectorAdapter.ViewHolder>() {

    private val mLastedMessageCache: ArrayMap<String, Message> = arrayMapOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DmChannelSelectorAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.widget_dmchannel_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dmChannelList.size
    }

    private fun bind(holder: ViewHolder, dmChannel: DmChannel) {
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            val bundle = Bundle()
            bundle.putString("guildId", "@me")
            bundle.putString("channelId", dmChannel.id)
            intent.putExtras(bundle)
            startActivity(
                holder.itemView.context,
                intent,
                ActivityOptions.makeCustomAnimation(
                    holder.itemView.context,
                    R.anim.slide_in_right_custom,
                    R.anim.no_animation
                ).toBundle()
            )
            AppState.global.channelSelection.value =
                ChannelSelection(guildId = "@me", channelId = dmChannel.id)
        }
        setTextChannelLatestMsgDespAndTime(holder, dmChannel)
        holder.itemView.dmchannel_user_avatar.user = dmChannel.recipient
        holder.itemView.text_name.text = dmChannel.recipient?.name
        holder.itemView.dm_user_unread_tv.visibility =
            if (dmChannel.unread) View.VISIBLE else View.GONE
        dmChannel.messageNotifications
    }

    private fun registerObserverForChannel(position: Int, holder: RecyclerView.ViewHolder) {
        val channel = dmChannelList.get(position)
        channel.observable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            notifyItemChanged(
                position,
                Any()
            )
        }
        global.eventBus.observeEventsOnUi().subscribe(
            Consumer { event ->
                if (event is MessageCreateEvent) {
                    val (message) = event
                    if (message.channelId == channel.id) {
                        holder.itemView.dm_user_unread_tv.visibility =
                            if (channel.unread) View.VISIBLE else View.GONE
                        mLastedMessageCache.put(channel.id, message)
                        notifyItemChanged(position, Any())
                    }
                } else if (event is MessageAtMeEvent) {
                    val (message) = event
                    if (message.channelId == channel.id) {
                        mLastedMessageCache.put(channel.id, message)
                        notifyItemChanged(position, Any())
                    }
                } else if (event is MessageReadEvent) {
                    val (message) = event
                    if (message.channelId == channel.id) {
                        holder.itemView.dm_user_unread_tv.visibility =
                            if (channel.unread) View.VISIBLE else View.GONE
                        mLastedMessageCache.put(channel.id, message)
                        notifyItemChanged(position, Any())
                    }
                } else if (event is MessageDeleteEvent) {
                    val (message) = event
                    if (message.channelId == channel.id) {
                        holder.itemView.dm_user_unread_tv.visibility =
                            if (channel.unread) View.VISIBLE else View.GONE
                        mLastedMessageCache.remove(channel.id)
                        notifyItemChanged(position, Any())
                    }
                } else if (event is MessageUpdateEvent) {
                    val (message) = event
                    if (message.channelId == channel.id) {
                        mLastedMessageCache.put(channel.id, message)
                        notifyItemChanged(position, Any())
                    }
                }
            })
    }

    private fun setTextChannelLatestMsgDespAndTime(
        holder: DmChannelSelectorAdapter.ViewHolder,
        textChannel: DmChannel
    ) {
        val messages = textChannel.messages
        if (messages.latestMessage != null) {
            holder.itemView.text_user_last_msg.visibility = View.VISIBLE
            setMessageDesp(
                holder,
                messages.latestMessage!!,
                textChannel
            )
            val timestamp = messages.latestMessage!!.timestamp
            holder.itemView.last_message_time.text =
                LocalDateUtils.timestampConverterSimple(holder.itemView.context, timestamp)
        } else {
            val message = mLastedMessageCache[textChannel.id]
            if (message != null) {
                holder.itemView.text_user_last_msg.visibility = View.VISIBLE
                setMessageDesp(holder, message, textChannel)
                val timestamp = message.timestamp
                holder.itemView.last_message_time.text =
                    LocalDateUtils.timestampConverterSimple(holder.itemView.context, timestamp)
            } else {
                holder.itemView.text_user_last_msg.text = ""
                holder.itemView.last_message_time.text = ""
                messages.fetch(null, null, 1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Consumer { messages ->
                        if (messages.size == 0) {
                            holder.itemView.text_user_last_msg.visibility = View.GONE
                            return@Consumer
                        }
                        holder.itemView.text_user_last_msg.visibility = View.VISIBLE
                        val message = messages[0]
                        val msgChannelId = message.channelId
                        if (msgChannelId == textChannel.id) {
                            mLastedMessageCache[textChannel.id] = message
                            setMessageDesp(holder, message, textChannel)
                            val timestamp = message.timestamp
                            holder.itemView.last_message_time.text =
                                LocalDateUtils.timestampConverterSimple(
                                    holder.itemView.context,
                                    timestamp
                                )
                        }
                    }, Consumer {
                    })
            }
        }
    }

    private fun setMessageDesp(
        holder: DmChannelSelectorAdapter.ViewHolder,
        msg: Message,
        channel: DmChannel
    ) {
        val name: String = getAuthorNameInChannel(holder.itemView.context, msg.authorId, channel)
        if (msg.stamps.size > 0 && msg.type === MessageType.DEFAULT) {
            holder.itemView.text_user_last_msg.text =
                holder.itemView.context.getString(R.string.msg_desp_stamp)
        } else if (!TextUtils.isEmpty(msg.content) &&
            (regexAtUser.containsMatchIn(msg.content!!) || regexEmoji.containsMatchIn(msg.content!!))
        ) {
            val (contentAtUsers, contentEmojis) = contentParser(msg.content!!)
            var tmpMsg = msg.content
            for ((_, _, raw, _, name1) in contentEmojis) {
                tmpMsg = tmpMsg!!.replaceFirst(raw.toRegex(), "[$name1]")
            }
            for ((_, _, _, id) in contentAtUsers) {
                tmpMsg = tmpMsg!!.replaceFirst(
                    "<@" + id + ">".toRegex(), "@" + getAuthorNameInChannel(
                        holder.itemView.context,
                        id, channel
                    )
                )
            }
            holder.itemView.text_user_last_msg.text = String.format("%s: %s", name, tmpMsg)
        } else if (TextUtils.isEmpty(msg.content) && msg.attachments.size > 0) {
            for (attachment in msg.attachments) {
                var content = ""
                content = if (isImage(attachment.fileName)) {
                    holder.itemView.context.getString(R.string.attachment_image)
                } else if (isVideo(attachment.fileName)) {
                    holder.itemView.context.getString(R.string.attachment_video)
                } else {
                    holder.itemView.context.getString(R.string.attachment_file)
                }
                holder.itemView.text_user_last_msg.text = String.format(
                    "%s: %s",
                    name, content
                )
                break
            }
        } else {
            if (TextUtils.isEmpty(msg.content)) {
                holder.itemView.text_user_last_msg.visibility = View.GONE
            } else
                holder.itemView.text_user_last_msg.text = String.format(
                    "%s: %s",
                    name, msg.content
                )
        }

    }

    private fun getAuthorNameInChannel(
        context: Context,
        authorId: String?,
        channel: DmChannel
    ): String {
        var displayName: String? = null
        val recipient = if (authorId == global.me.id) global.me else channel.recipient
        if (recipient != null) {
            displayName = recipient.name
        }
        if (TextUtils.isEmpty(displayName)) {
            displayName = context.resources.getString(R.string.unknown_name)
        }
        if (displayName!!.length > 14) {
            displayName = displayName.substring(0, 14) + "..."
        }
        return displayName
    }

    private fun isImage(name: String): Boolean {
        return name.endsWith("jpg") || name.endsWith("bmp") || name.endsWith(
            "gif"
        ) || name.endsWith("png") || name.endsWith("jpeg")
    }

    private fun isVideo(name: String): Boolean {
        return name.endsWith("mp4") || name.endsWith("avi") || name.endsWith(
            "3gp"
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        registerObserverForChannel(position, holder)
        bind(holder, dmChannelList[position])
    }

}