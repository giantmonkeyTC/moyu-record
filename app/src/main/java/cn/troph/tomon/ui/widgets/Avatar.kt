package cn.troph.tomon.ui.widgets

import android.content.Context
import android.util.AttributeSet
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class Avatar : CircleImageView {

    var url: String? = null
        set(value) {
            field = value
            Glide.with(this).load(url).into(this)
        }

    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0) {

    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {

    }
}