package cn.troph.tomon.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import cn.troph.tomon.R
import cn.troph.tomon.core.structures.Guild
import com.bumptech.glide.Glide
import com.github.florent37.shapeofview.shapes.RoundRectView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable

class GuildAvatar : FrameLayout {

    private lateinit var clipView: RoundRectView
    private lateinit var imageView: ImageView
    private lateinit var nameView: ConstraintLayout
    private lateinit var textView: TextView

    var disposable: Disposable? = null

    var guild: Guild? = null
        set(value) {
            field = value
            update()
            disposable?.dispose()
            disposable = value?.observable?.observeOn(AndroidSchedulers.mainThread())?.subscribe {
                update()
            }
        }

    var selecting: Boolean = false
        set(value) {
            field = value
            val side = width.coerceAtMost(height)
            var radius = if (selecting) side * 0.3f else side * 0.5f
            clipView.bottomLeftRadius = radius
            clipView.bottomRightRadius = radius
            clipView.topLeftRadius = radius
            clipView.topRightRadius = radius
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

    private fun update() {
        val url = guild?.iconURL
        imageView.visibility = if (url != null) View.VISIBLE else View.GONE
        nameView.visibility = if (url == null) View.VISIBLE else View.GONE
        (context as AppCompatActivity)?.let {
            if (!it.isFinishing && !it.isDestroyed) {
                Glide.with(it).load(url).into(imageView)
            }

        }
        textView.text = guild?.name
    }

}