package cn.troph.tomon.ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import cn.troph.tomon.R
import com.bumptech.glide.Glide
import com.github.florent37.shapeofview.shapes.RoundRectView

class UserAvatar : FrameLayout {

    private lateinit var clipView: RoundRectView
    private lateinit var defaultView: ImageView
    private lateinit var imageView: ImageView

    var url: String? = null
        set(value) {
            field = value
            Glide.with(this).load(url).into(imageView)
            updateVisibility()
        }

    var backgrondColor: Int = Color.WHITE
        set(value) {
            field = value
            defaultView.setBackgroundColor(value)
        }

    var userId: String = "0"
        set(value) {
            field = value
            val number = userId.toLongOrNull() ?: 0L
            val hue = (number % 100000000).toFloat() / 100000000.0f * 360f
            val color = Color.HSVToColor(floatArrayOf(hue, 23.1f, 47.5f))
            backgrondColor = color
        }

    var cornerRadius: Float = 0f
        set(value) {
            field = value
            clipView.bottomLeftRadius = value
            clipView.bottomRightRadius = value
            clipView.topLeftRadius = value
            clipView.topRightRadius = value
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
        val view = inflater.inflate(R.layout.widget_user_avatar, null)
        clipView = view.findViewById(R.id.view_clip)
        defaultView = view.findViewById(R.id.image_default)
        imageView = view.findViewById(R.id.image_avatar)
        addView(view)
        if (attrs != null) {
            val attributes =
                context.obtainStyledAttributes(attrs, R.styleable.UserAvatar)
            val cr = attributes.getDimensionPixelSize(
                R.styleable.UserAvatar_corner_radius,
                cornerRadius.toInt()
            )
            cornerRadius = cr.toFloat()
            attributes.recycle()
        }
    }

    private fun updateVisibility() {
        imageView.visibility = if (url != null) View.VISIBLE else View.GONE
        defaultView.visibility = if (url == null) View.VISIBLE else View.GONE
    }

}