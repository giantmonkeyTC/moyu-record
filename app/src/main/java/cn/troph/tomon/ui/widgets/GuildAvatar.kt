package cn.troph.tomon.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import cn.troph.tomon.R
import com.bumptech.glide.Glide
import com.github.florent37.shapeofview.shapes.RoundRectView

class GuildAvatar : FrameLayout {

    lateinit var clipView: RoundRectView
    lateinit var imageView: ImageView
    lateinit var nameView: ConstraintLayout
    lateinit var textView: TextView

    var url: String? = null
        set(value) {
            field = value
            Glide.with(this).load(url).into(imageView)
            updateVisibility()
        }

    var name: String? = null
        set(value) {
            field = value
            textView.text = value
        }

    var selecting: Boolean = false
        set(value) {
            field = value
            var radius = if (selecting) 10f else 20f
            clipView.bottomLeftRadiusDp = radius
            clipView.bottomRightRadiusDp = radius
            clipView.topLeftRadiusDp = radius
            clipView.topRightRadiusDp = radius
            nameView.setBackgroundResource(if (selecting) R.color.guildSelectBackground else R.color.guildBackground)
        }

    constructor(context: Context) : super(context) {
        init(context, null);
    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0) {
        init(context, attrs);
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs);
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.widget_guild_avatar, null)
        clipView = view.findViewById(R.id.view_clip)
        imageView = view.findViewById(R.id.image_avatar)
        nameView = view.findViewById(R.id.view_name)
        textView = view.findViewById(R.id.text_name)
        addView(view)
    }

    private fun updateVisibility() {
        imageView.visibility = if (url != null) View.VISIBLE else View.GONE
        nameView.visibility = if (url == null) View.VISIBLE else View.GONE
    }
}