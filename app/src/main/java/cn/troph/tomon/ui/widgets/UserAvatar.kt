package cn.troph.tomon.ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import cn.troph.tomon.R
import cn.troph.tomon.core.structures.User
import com.bumptech.glide.Glide
import com.github.florent37.shapeofview.shapes.CircleView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable

class UserAvatar : FrameLayout {

    private lateinit var clipView: CircleView
    private lateinit var imageView: ImageView

    private var disposable: Disposable? = null

    var user: User? = null
        set(value) {
            field = value
            update()
            disposable?.dispose()
            if (isAttachedToWindow) {
                listen()
            }
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
        imageView = view.findViewById(R.id.image_avatar)
        addView(view)
    }

    private fun update() {
        val url = user?.avatarURL
        Glide.with(this).load(url).placeholder(R.drawable.ic_avatar_default).into(imageView)
        val number = user?.id?.toLongOrNull() ?: 0L
        val hue = (number % 100000000).toFloat() / 100000000.0f * 360f
        val color = Color.HSVToColor(floatArrayOf(hue, 23.1f, 47.5f))
        imageView.setBackgroundColor(color)
    }

    private fun listen() {
        disposable = user?.observable?.observeOn(AndroidSchedulers.mainThread())?.subscribe {
            update()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (disposable == null) {
            listen()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposable?.dispose()
        disposable = null
    }

}