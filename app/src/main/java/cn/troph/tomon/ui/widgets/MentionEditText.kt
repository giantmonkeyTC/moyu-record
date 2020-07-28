package cn.troph.tomon.ui.widgets

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.emoji.widget.EmojiEditText

class MentionEditText(context: Context) : EmojiEditText(context) {
    val editTextList: MutableList<Editable> = mutableListOf()
    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    override fun addTextChangedListener(watcher: TextWatcher?) {
        super.addTextChangedListener(watcher)
    }
}