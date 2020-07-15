package cn.troph.tomon.ui.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Color.HSVToColor
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.ColorUtils
import cn.troph.tomon.R
import cn.troph.tomon.core.structures.User
import cn.troph.tomon.core.utils.Snowflake
import cn.troph.tomon.ui.chat.fragments.GuildUserInfoFragment
import com.bumptech.glide.Glide
import com.github.florent37.shapeofview.shapes.CircleView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable

class UserAvatar : FrameLayout {

    enum class AvatarRare {
        N,
        R,
        SR,
        SSR
    }

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
        val seed = Snowflake.deconstruct(if (user == null) "0" else user!!.id).timestamp
        val rare = rareById(seed)
        val color = colorById(seed, rare)
        imageView.setBackgroundColor(color)
        val placeholder = when (rare) {
            AvatarRare.SSR -> R.drawable.avatar_ssr
            AvatarRare.SR -> R.drawable.avatar_sr
            AvatarRare.R -> R.drawable.avatar_r
            else -> R.drawable.avatar_n
        }
        Glide.with(this).load(url).placeholder(placeholder).into(imageView)

    }

    private fun rareById(seed: Long): AvatarRare {
        val p = (seed % 10001237) % 20
        if (p == 7L) {
            return AvatarRare.SSR
        } else if (p == 4L || p == 13L || p == 19L)
            return AvatarRare.N
        else if (p == 1L || p == 5L || p == 10L || p == 15L)
            return AvatarRare.SR
        else
            return AvatarRare.R
    }

    private fun colorById(seed: Long, rare: AvatarRare): Int {
        val mod = (((seed % 100000000) / 100000000.0) * 360)
        var l = 0f
        var s = 0f
        when (rare) {
            AvatarRare.R, AvatarRare.N, AvatarRare.SR -> {
                s = 0.26f
                l = 0.56f
            }
            AvatarRare.SSR -> {
                s = 0.25f
                l = 0.05f
            }
        }
        val floatArray = FloatArray(3)
        floatArray[0] = mod.toFloat()
        floatArray[1] = s
        floatArray[2] = l
        return ColorUtils.HSLToColor(floatArray)

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