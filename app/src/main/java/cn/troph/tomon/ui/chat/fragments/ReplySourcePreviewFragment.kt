package cn.troph.tomon.ui.chat.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.utils.Converter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.Target
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlinx.android.synthetic.main.fragment_reply_source.*
import kotlinx.android.synthetic.main.item_message_reply.view.*

class ReplySourcePreviewFragment(val message: Message) : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reply_source, container, false)
    }

    private fun guildMemberOf(msg: Message): GuildMember? {
        var member: GuildMember? = null
        if (Client.global.channels[msg.channelId] is TextChannel) {
            member = (Client.global.channels[msg.channelId] as TextChannel).members[msg.authorId
                ?: ""]
        }
        return member
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val sourceMessage = message.replySource
        sourceMessage?.let {
            reply_source_avatar.user = sourceMessage.author
            reply_source_author.text = guildMemberOf(sourceMessage)?.displayName ?: sourceMessage.author?.name ?: ""
            Converter.toMarkdownTextView(
                Markwon.builder(requireContext()) // automatically create Glide instance
                    .usePlugin(ImagesPlugin.create())
                    .usePlugin(HtmlPlugin.create())
                    .usePlugin(GlideImagesPlugin.create(requireContext())) // use supplied Glide instance
                    .usePlugin(GlideImagesPlugin.create(Glide.with(requireContext()))) // if you need more control
                    .usePlugin(GlideImagesPlugin.create(object : GlideImagesPlugin.GlideStore {
                        override fun cancel(target: Target<*>) {
                            Glide.with(requireContext()).clear(target)
                        }

                        override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {
                            return Glide.with(requireContext()).load(drawable.destination)
                        }
                    })).build(),
                sourceMessage.content ?: "",
                reply_source_content
            )
            reply_source_content.movementMethod = ScrollingMovementMethod()
        }
        reply_source_cancel.setOnClickListener {
            dismiss()
        }

    }
}