package cn.troph.tomon.ui.chat.messages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import androidx.emoji.widget.EmojiTextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageType
import cn.troph.tomon.core.structures.HeaderMessage
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.MessageAttachment
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.utils.*
import cn.troph.tomon.ui.chat.fragments.GuildUserInfoFragment
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.source.UrlSource
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.Logger
import com.stfalcon.imageviewer.StfalconImageViewer
import io.noties.markwon.Markwon
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet_message.view.*
import kotlinx.android.synthetic.main.header_loading_view.view.*
import kotlinx.android.synthetic.main.item_chat_file.view.*
import kotlinx.android.synthetic.main.item_chat_image.view.*
import kotlinx.android.synthetic.main.item_invite_link.view.*
import kotlinx.android.synthetic.main.item_message_stamp.view.*
import kotlinx.android.synthetic.main.item_message_video.view.*
import kotlinx.android.synthetic.main.item_reaction_view.view.*
import kotlinx.android.synthetic.main.item_system_welcome_msg.view.*
import kotlinx.android.synthetic.main.widget_message_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

const val INVITE_LINK = "https://beta.tomon.co/invite/"
const val STAMP_URL = "https://cdn.tomon.co/stamps/%s.png?x-oss-process=image/resize,p_80"
const val STAMP_URL_GIF = "https://cdn.tomon.co/stamps/%s.gif"

class MessageAdapter(
    private val messageList: MutableList<Message>,
    private val reactionSelectorListener: ReactionSelectorListener,
    private val avatarLongClickListener: OnAvatarLongClickListener
) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    var onItemClickListner: OnItemClickListener? = null
    private var markdown: Markwon? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (markdown == null) {
            markdown = Markwon.create(parent.context)
        }

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
            5 -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_system_welcome_msg, parent, false)
                )
            }
            6 -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_message_stamp, parent, false)
                )
            }
            7 -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_message_video, parent, false)
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
        } else if (messageList[position].type == MessageType.GUILD_MEMBER_JOIN) {
            return 5
        } else if (messageList[position].type == MessageType.DEFAULT && messageList[position].attachments.size > 0) {
            var type = 0
            for (item in messageList[position].attachments.values) {
                if (isImage(item.type)) {
                    type = 2
                } else if (isVideo(item.type)) {
                    type = 7
                } else {
                    type = 1
                }
                break
            }
            return type

        } else if (messageList[position].stamps.size > 0 && messageList[position].type == MessageType.DEFAULT) {
            return 6
        } else if (messageList[position].type == MessageType.DEFAULT) {
            messageList[position].content?.let {
                if (it.contains(INVITE_LINK))
                    return 4
            }
            return 0
        }
        return 0
    }

    private fun isImage(name: String): Boolean {
        return name.endsWith("jpg", true) || name.endsWith("bmp", true) || name.endsWith(
            "gif",
            true
        ) || name.endsWith("png", true) || name.endsWith("jpeg", true)
    }

    private fun isVideo(name: String): Boolean {
        return name.endsWith("mp4", true) || name.endsWith("avi", true) || name.endsWith(
            "3gp",
            true
        )

    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            onItemClickListner?.let {
                it.onItemClick(holder.adapterPosition)
            }
        }
        when (getItemViewType(position)) {
            0 -> {
                val msg = messageList[position]
                holder.itemView.setOnLongClickListener {
                    callBottomSheet(holder, 0)
                    true
                }
                holder.itemView.message_avatar.setOnClickListener {
                    messageList[holder.adapterPosition].authorId?.let {
                        if (it != Client.global.me.id) {
                            val context = holder.itemView.context as AppCompatActivity
                            val guildUserInfoFragment = GuildUserInfoFragment(it)
                            guildUserInfoFragment.show(
                                context.supportFragmentManager,
                                guildUserInfoFragment.tag
                            )
                        }

                    }
                }
                holder.itemView.message_avatar.setOnLongClickListener {
                    messageList[holder.adapterPosition].authorId?.let {
                        if (it != Client.global.me.id) {
                            messageList[holder.adapterPosition].author?.let { author ->
                                avatarLongClickListener.onAvatarLongClick(identifier = author.identifier)
                            }
                        }
                    }
                    true
                }
                if (position > 1) {
                    bind(holder.itemView, msg, messageList[position - 1], holder)
                } else {
                    bind(holder.itemView, msg, holder = holder)
                }
                showReaction(holder, msg)
            }
            1 -> { //附件
                if (position == 0 || messageList[position - 1].authorId != messageList[position].authorId ||
                    messageList[position].timestamp.isAfter(
                        messageList[position - 1].timestamp.plusMinutes(5)
                    )

                ) {
                    holder.itemView.message_avatar_file.visibility = View.VISIBLE
                    holder.itemView.message_avatar_file.setOnClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                val context = holder.itemView.context as AppCompatActivity
                                GuildUserInfoFragment(it).show(context.supportFragmentManager, null)
                            }
                        }
                    }
                    holder.itemView.widget_message_timestamp_text_file.visibility = View.VISIBLE
                    holder.itemView.widget_message_author_name_text_file.visibility = View.VISIBLE
                    holder.itemView.message_avatar_file.setOnLongClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                messageList[holder.adapterPosition].author?.let { author ->
                                    avatarLongClickListener.onAvatarLongClick(identifier = author.identifier)
                                }
                            }
                        }
                        true
                    }
                    holder.itemView.message_avatar_file.user = messageList[position].author
                    holder.itemView.widget_message_author_name_text_file.text =
                        "${messageList[position].author?.name}${if (messageList[position].author?.type == 32) " \uD83E\uDD16" else ""}"
                    val message = messageList[position]
                    if (Client.global.channels[message.channelId] is TextChannel) {
                        val member =
                            (Client.global.channels[message.channelId] as TextChannel).members[message.authorId
                                ?: ""]
                        if (member != null) {
                            holder.itemView.widget_message_author_name_text_file.setTextColor(
                                (if (member.roles.color == null)
                                    0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt())
                            )
                        }
                    } else {
                        holder.itemView.widget_message_author_name_text_file.setTextColor(
                            holder.itemView.context.getColor(
                                R.color.white
                            )
                        )
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
                            Toast.makeText(
                                holder.itemView.context,
                                "文件保存至:${holder.itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath}",
                                Toast.LENGTH_SHORT
                            ).show()
                            PRDownloader.download(
                                file.url,
                                holder.itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath,
                                file.fileName
                            )
                                .build().start(object : OnDownloadListener {
                                    override fun onDownloadComplete() {
                                        Toast.makeText(
                                            holder.itemView.context,
                                            "下载完成",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onError(error: Error?) {
                                        Toast.makeText(
                                            holder.itemView.context,
                                            "下载失败",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            break
                        }
                    }
                    break
                }
            }
            2 -> { //图片
                if (position == 0 || messageList[position - 1].authorId != messageList[position].authorId || messageList[position].timestamp.isAfter(
                        messageList[position - 1].timestamp.plusMinutes(5)
                    )
                ) {
                    holder.itemView.message_avatar_image.setOnLongClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                messageList[holder.adapterPosition].author?.let { author ->
                                    avatarLongClickListener.onAvatarLongClick(identifier = author.identifier)
                                }
                            }
                        }
                        true
                    }
                    holder.itemView.message_avatar_image.visibility = View.VISIBLE
                    holder.itemView.message_avatar_image.setOnClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                val context = holder.itemView.context as AppCompatActivity
                                GuildUserInfoFragment(it).show(context.supportFragmentManager, null)
                            }
                        }
                    }
                    holder.itemView.widget_message_author_name_text_image.visibility = View.VISIBLE
                    holder.itemView.widget_message_timestamp_text_image.visibility = View.VISIBLE
                    holder.itemView.message_avatar_image.user = messageList[position].author
                    holder.itemView.widget_message_author_name_text_image.text =
                        "${messageList[position].author?.name}${if (messageList[position].author?.type == 32) " \uD83E\uDD16" else ""}"
                    val message = messageList[position]
                    if (Client.global.channels[message.channelId] is TextChannel) {
                        val member =
                            (Client.global.channels[message.channelId] as TextChannel).members[message.authorId
                                ?: ""]
                        if (member != null) {
                            holder.itemView.widget_message_author_name_text_image.setTextColor(
                                (if (member.roles.color == null)
                                    0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt())
                            )
                        }
                    } else {
                        holder.itemView.widget_message_author_name_text_image.setTextColor(
                            holder.itemView.context.getColor(
                                R.color.white
                            )
                        )
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
//                    holder.itemView.chat_iv.updateLayoutParams {
//                        item.height?.let {
//                            val calHeight =
//                                DensityUtil.px2dip(holder.itemView.context, it.toFloat())
//                            height = if (calHeight > 300) {
//                                DensityUtil.dip2px(holder.itemView.context, 300f)
//                            } else {
//                                it
//                            }
//                        }
//                    }
                    Glide.with(holder.itemView)
                        .load(
                            if (item.url.isEmpty()) item.fileName else "${item.url}${if (item.url.endsWith(
                                    ".gif",
                                    true
                                )
                            ) "" else "?x-oss-process=image/resize,p_50"}"
                        )
                        .placeholder(R.drawable.loadinglogo)
                        .override(
                            if (item.width != null) item.width!! else DensityUtil.dip2px(
                                holder.itemView.context,
                                200f
                            ),
                            if (item.height != null) item.height!! else DensityUtil.dip2px(
                                holder.itemView.context,
                                200f
                            )
                        )
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
                    holder.itemView.loading_text_header.text =
                        if (msg.isGuild) {
                            if (msg.channelText.length <= 10) "欢迎来到#${msg.channelText} 频道!" else "欢迎来到#${msg.channelText.substring(
                                0,
                                10
                            )}··· 频道!"
                        } else {
                            if (msg.channelText.length <= 10) "与@${msg.channelText} 私聊的开始" else "与@${msg.channelText.substring(
                                0,
                                10
                            )}··· 私聊的开始"
                        }
                    holder.itemView.loading_text_header.setCompoundDrawablesWithIntrinsicBounds(
                        if (msg.isGuild) holder.itemView.context.getDrawable(
                            R.drawable.ic_group
                        ) else holder.itemView.context.getDrawable(R.drawable.ic_chat_begin_dm),
                        null,
                        null,
                        null
                    )
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
                    holder.itemView.message_avatar_invite.setOnLongClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                messageList[holder.adapterPosition].author?.let { author ->
                                    avatarLongClickListener.onAvatarLongClick(identifier = author.identifier)
                                }
                            }
                        }
                        true
                    }
                    holder.itemView.message_avatar_invite.visibility = View.VISIBLE
                    holder.itemView.message_avatar_invite.setOnClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                val context = holder.itemView.context as AppCompatActivity
                                GuildUserInfoFragment(it).show(context.supportFragmentManager, null)
                            }
                        }
                    }
                    holder.itemView.widget_message_author_name_text_invite.visibility = View.VISIBLE
                    holder.itemView.widget_message_timestamp_text_invite.visibility = View.VISIBLE
                    holder.itemView.message_avatar_invite.user = messageList[position].author
                    holder.itemView.widget_message_author_name_text_invite.text =
                        "${messageList[position].author?.name}${if (messageList[position].author?.type == 32) " \uD83E\uDD16" else ""}"
                    val message = messageList[position]
                    if (Client.global.channels[message.channelId] is TextChannel) {
                        val member =
                            (Client.global.channels[message.channelId] as TextChannel).members[message.authorId
                                ?: ""]
                        if (member != null) {
                            holder.itemView.widget_message_author_name_text_invite.setTextColor(
                                (if (member.roles.color == null)
                                    0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt())
                            )
                        }
                    } else {
                        holder.itemView.widget_message_author_name_text_invite.setTextColor(
                            holder.itemView.context.getColor(
                                R.color.white
                            )
                        )
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
                                if (it.joined)
                                    holder.itemView.join_image.setImageResource(R.drawable.shadow_panel)
                                holder.itemView.invite_guild_name.text =
                                    if (it.guild.name.length <= 10) it.guild.name else "${it.guild.name.substring(
                                        0,
                                        10
                                    )}···"
                                holder.itemView.joined_cover.visibility =
                                    if (it.joined) View.VISIBLE else View.GONE
                                if (it.guild.icon != null) {
                                    holder.itemView.default_avatar_text.visibility = View.GONE
                                    Glide.with(holder.itemView).load(Assets.iconURL(it.guild.icon))
                                        .into(holder.itemView.user_avatar_invite)
                                } else {
                                    holder.itemView.user_avatar_invite.setImageResource(R.drawable.shape_guild_placeholder)
                                    holder.itemView.default_avatar_text.visibility = View.VISIBLE
                                    holder.itemView.default_avatar_text.text =
                                        it.guild.name.substring(0, 1)
                                }
                            }, {
                                holder.itemView.default_avatar_text.visibility = View.GONE
                                holder.itemView.join_image.setImageResource(R.drawable.invalidation)
                                holder.itemView.user_avatar_invite.setImageResource(
                                    R.drawable.ic_exclamation_circle_duotone
                                )
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
            5 -> {
                val welcomeArray =
                    holder.itemView.context.resources.getStringArray(R.array.welcome_array)
                val index = Random.nextInt(0, welcomeArray.size)
                val welMsg = welcomeArray[index]
                val name = "**" + messageList[position].author?.name + "**"
                markdown?.setMarkdown(holder.itemView.system_txt_welcome, welMsg.format(name))
            }
            6 -> {
                if (position == 0 || messageList[position - 1].authorId != messageList[position].authorId || messageList[position].timestamp.isAfter(
                        messageList[position - 1].timestamp.plusMinutes(5)
                    )
                ) {
                    holder.itemView.message_avatar_stamp.visibility = View.VISIBLE
                    holder.itemView.message_avatar_stamp.setOnClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                val context = holder.itemView.context as AppCompatActivity
                                GuildUserInfoFragment(it).show(context.supportFragmentManager, null)
                            }
                        }
                    }
                    holder.itemView.message_avatar_stamp.setOnLongClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                messageList[holder.adapterPosition].author?.let { author ->
                                    avatarLongClickListener.onAvatarLongClick(identifier = author.identifier)
                                }
                            }
                        }
                        true
                    }
                    holder.itemView.widget_message_author_name_text_stamp.visibility = View.VISIBLE
                    holder.itemView.widget_message_timestamp_text_stamp.visibility = View.VISIBLE
                    holder.itemView.message_avatar_stamp.user = messageList[position].author
                    holder.itemView.widget_message_author_name_text_stamp.text =
                        "${messageList[position].author?.name}${if (messageList[position].author?.type == 32) " \uD83E\uDD16" else ""}"
                    val message = messageList[position]
                    if (Client.global.channels[message.channelId] is TextChannel) {
                        val member =
                            (Client.global.channels[message.channelId] as TextChannel).members[message.authorId
                                ?: ""]
                        if (member != null) {
                            holder.itemView.widget_message_author_name_text_stamp.setTextColor(
                                (if (member.roles.color == null)
                                    0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt())
                            )
                        }
                    } else {
                        holder.itemView.widget_message_author_name_text_stamp.setTextColor(
                            holder.itemView.context.getColor(
                                R.color.white
                            )
                        )
                    }

                    if (messageList[position].type == MessageType.SYSTEM) {
                        holder.itemView.widget_message_author_name_text_image.text = "T🐱"
                    }

                    holder.itemView.widget_message_timestamp_text_stamp.text =
                        timestampConverter(messageList[position].timestamp)
                } else {
                    holder.itemView.message_avatar_stamp.visibility = View.GONE
                    holder.itemView.widget_message_author_name_text_stamp.visibility = View.GONE
                    holder.itemView.widget_message_timestamp_text_stamp.visibility = View.GONE
                }

                holder.itemView.chat_iv_stamp.setOnLongClickListener {
                    callBottomSheet(holder, 2)
                    true
                }
                for (item in messageList[position].stamps) {
//                    holder.itemView.chat_iv_stamp.updateLayoutParams {
//                        item.height?.let {
//                            val calHeight =
//                                DensityUtil.px2dip(holder.itemView.context, it.toFloat())
//                            height = if (calHeight > 300) {
//                                DensityUtil.dip2px(holder.itemView.context, 300f)
//                            } else {
//                                it
//                            }
//                        }
//                    }
                    Glide.with(holder.itemView)
                        .load(
                            if (item.animated) STAMP_URL_GIF.format(item.hash) else STAMP_URL.format(
                                item.hash
                            )
                        )
                        .placeholder(R.drawable.loadinglogo)
                        .override(item.width, item.height)
                        .into(holder.itemView.chat_iv_stamp)
                    break
                }
                if (messageList[position].isSending) {
                    val apl = AlphaAnimation(0.1f, 0.78f)
                    apl.duration = 1000
                    apl.repeatCount = -1
                    holder.itemView.chat_iv_stamp.startAnimation(apl)
                } else {
                    holder.itemView.chat_iv_stamp.clearAnimation()
                }
                showReaction(holder, messageList[position])
            }
            7 -> {
                if (position == 0 || messageList[position - 1].authorId != messageList[position].authorId || messageList[position].timestamp.isAfter(
                        messageList[position - 1].timestamp.plusMinutes(5)
                    )
                ) {
                    holder.itemView.message_avatar_video.setOnLongClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                messageList[holder.adapterPosition].author?.let { author ->
                                    avatarLongClickListener.onAvatarLongClick(identifier = author.identifier)
                                }
                            }
                        }
                        true
                    }
                    holder.itemView.message_avatar_video.visibility = View.VISIBLE
                    holder.itemView.message_avatar_video.setOnClickListener {
                        messageList[holder.adapterPosition].authorId?.let {
                            if (it != Client.global.me.id) {
                                val context = holder.itemView.context as AppCompatActivity
                                GuildUserInfoFragment(it).show(context.supportFragmentManager, null)
                            }
                        }
                    }
                    holder.itemView.widget_message_author_name_text_video.visibility = View.VISIBLE
                    holder.itemView.widget_message_timestamp_text_video.visibility = View.VISIBLE
                    holder.itemView.message_avatar_video.user = messageList[position].author
                    holder.itemView.widget_message_author_name_text_video.text =
                        "${messageList[position].author?.name}${if (messageList[position].author?.type == 32) " \uD83E\uDD16" else ""}"
                    val message = messageList[position]
                    if (Client.global.channels[message.channelId] is TextChannel) {
                        val member =
                            (Client.global.channels[message.channelId] as TextChannel).members[message.authorId
                                ?: ""]
                        if (member != null) {
                            holder.itemView.widget_message_author_name_text_video.setTextColor(
                                (if (member.roles.color == null)
                                    0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt())
                            )
                        }
                    } else {
                        holder.itemView.widget_message_author_name_text_video.setTextColor(
                            holder.itemView.context.getColor(
                                R.color.white
                            )
                        )
                    }

                    if (messageList[position].type == MessageType.SYSTEM) {
                        holder.itemView.widget_message_author_name_text_video.text = "T🐱"
                    }

                    holder.itemView.widget_message_timestamp_text_video.text =
                        timestampConverter(messageList[position].timestamp)
                } else {
                    holder.itemView.message_avatar_video.visibility = View.GONE
                    holder.itemView.widget_message_author_name_text_video.visibility = View.GONE
                    holder.itemView.widget_message_timestamp_text_video.visibility = View.GONE
                }

                holder.itemView.video_player.setOnLongClickListener {
                    callBottomSheet(holder, 2)
                    true
                }
                for (item in messageList[position].attachments.values) {
//                    holder.itemView.chat_iv.updateLayoutParams {
//                        item.height?.let {
//                            val calHeight =
//                                DensityUtil.px2dip(holder.itemView.context, it.toFloat())
//                            height = if (calHeight > 300) {
//                                DensityUtil.dip2px(holder.itemView.context, 300f)
//                            } else {
//                                it
//                            }
//                        }
//                    }
                    val uri = Uri.parse(item.url)
                    val videoPlayer = holder.itemView.video_player
                    val videoPreview = holder.itemView.video_preview
                    videoPreview.updateLayoutParams {
                        item.width?.let {
                            this.width =
                                item.width!!
                            this.height =
                                item.height!!
                        }
                    }
                    holder.itemView.video_player.updateLayoutParams {
                        item.width?.let {
                            this.width = item.width!!
                            this.height = item.height!!
                        }
                    }
                    val vidPlayer =
                        AliPlayerFactory.createAliPlayer(holder.itemView.context).apply {
                            setOnPreparedListener {
                                this.start()
                            }
                            setOnCompletionListener {
                                holder.itemView.play_status.visibility = View.VISIBLE
                            }
                            setOnStateChangedListener {
                                when (it) {
                                    3 -> {
                                        holder.itemView.play_pause.visibility = View.VISIBLE1
                                    }
                                    4 -> {

                                    }
                                    6 -> {

                                    }
                                }
                            }
                        }

                    holder.itemView.play_status.setOnClickListener {
                        val urlSource = UrlSource()
                        urlSource.uri = Uri.parse(item.url).toString()
                        vidPlayer?.setDataSource(urlSource)
                        vidPlayer?.prepare()
                        holder.itemView.play_status.visibility = View.GONE
                        videoPreview.visibility = View.GONE
                    }
                    videoPreview.setBackgroundColor(holder.itemView.context.resources.getColor(R.color.primaryColor))
                    videoPlayer.surfaceTextureListener =
                        object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureSizeChanged(
                                surface: SurfaceTexture?,
                                width: Int,
                                height: Int
                            ) {

                            }

                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

                            }

                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                                return true
                            }

                            override fun onSurfaceTextureAvailable(
                                surface: SurfaceTexture?,
                                width: Int,
                                height: Int
                            ) {
                                holder.itemView.play_status.visibility = View.VISIBLE
                                vidPlayer?.setSurface(Surface(surface))
                            }
                        }
                    holder.itemView.play_pause.setOnClickListener {
                        vidPlayer?.pause()
                        holder.itemView.play_pause.visibility = View.GONE
                        holder.itemView.play_status.visibility = View.VISIBLE
                    }
                    holder.itemView.video_player.setOnClickListener {

                    }
                    break
                }
                if (messageList[position].isSending) {
                    val apl = AlphaAnimation(0.1f, 0.78f)
                    apl.duration = 1000
                    apl.repeatCount = -1
                    holder.itemView.video_player.startAnimation(apl)
                } else {
                    holder.itemView.video_player.clearAnimation()
                }
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
            if (index == vh.itemView.flow_reaction_ll.childCount - 1) {
                break
            }
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
                image.visibility = View.VISIBLE
                Glide.with(image).load(
                    if (value.emoji == null)
                        Assets.emojiURL(value.id, false) else
                        value.emoji?.url
                ).placeholder(R.drawable.ic_timelapse_white_24dp).into(image)
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
        if (Client.global.channels[message.channelId] is TextChannel) {
            val member =
                (Client.global.channels[message.channelId] as TextChannel).members[message.authorId
                    ?: ""]
            if (message.type == MessageType.SYSTEM) {
                itemView.widget_message_author_name_text.text = "T🐱"
            } else {
                itemView.widget_message_author_name_text.text =
                    "${message.author?.name} ${if (message.author?.type == 32) " \uD83E\uDD16" else ""}"

                if (member != null) {
                    itemView.widget_message_author_name_text.setTextColor(
                        (if (member.roles.color == null)
                            0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt())
                    )
                }
            }
        } else {
            itemView.widget_message_author_name_text.text = message.author?.name
            itemView.widget_message_author_name_text.setTextColor(itemView.context.getColor(R.color.white))
        }



        if (message.content != null && (Assets.regexEmoji.containsMatchIn(message.content!!) || Assets.regexAtUser.containsMatchIn(
                message.content!!
            ))
        ) {
            richText(message, itemView)
        } else {
            markdown?.setMarkdown(itemView.widget_message_text, message.content ?: "")
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
            it.start
            Glide.with(itemView.context).asDrawable()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .load(Assets.emojiURL(it.id))
                .placeholder(R.drawable.image_circle_grey)
                .override(20)
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
//            span.setSpan(
//                object : ClickableSpan() {
//                    override fun onClick(widget: View) {
//                       Logger.d("click success")
//                    }
//                }, it.start,
//                (it.end) + 1,
//                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
//            )
            itemView.widget_message_text.text = span
        }
    }

    private fun timestampConverter(timestamp: LocalDateTime): String {
        val timeLocal = timestamp.toLocalTime()
        val dateLocal = timestamp.toLocalDate()
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
                    notifyDataSetChanged()
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

interface OnItemClickListener {
    fun onItemClick(position: Int)
}

interface OnAvatarLongClickListener {
    fun onAvatarLongClick(identifier: String)
}