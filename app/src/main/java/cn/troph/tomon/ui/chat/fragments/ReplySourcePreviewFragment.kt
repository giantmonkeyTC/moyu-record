package cn.troph.tomon.ui.chat.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import kotlinx.android.synthetic.main.fragment_reply_source.*

class ReplySourcePreviewFragment(val message: Message) : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.Theme_App_Dialog_FullScreen)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reply_source, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val sourceMessage = message.replySource
        sourceMessage?.let {
            reply_source_avatar.user = sourceMessage.author
            reply_source_author.text = sourceMessage.author?.name ?: ""
            reply_source_content.text = sourceMessage.content
        }
        reply_source_cancel.setOnClickListener {
            dismiss()
        }

    }
}