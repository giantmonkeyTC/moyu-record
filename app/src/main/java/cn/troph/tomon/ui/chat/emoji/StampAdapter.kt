import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.structures.StampPack
import cn.troph.tomon.ui.chat.emoji.CustomGuildStamp
import cn.troph.tomon.ui.chat.emoji.EmojiAdapter
import cn.troph.tomon.ui.chat.messages.STAMP_URL
import cn.troph.tomon.ui.chat.messages.STAMP_URL_GIF
import com.bumptech.glide.Glide
import com.cruxlab.sectionedrecyclerview.lib.BaseSectionAdapter
import com.cruxlab.sectionedrecyclerview.lib.SectionAdapter
import kotlinx.android.synthetic.main.emoji_image.view.*
import kotlinx.android.synthetic.main.emoji_item.view.*
import kotlinx.android.synthetic.main.item_message_stamp.view.*
import kotlinx.android.synthetic.main.stamp_image.view.*
import kotlinx.android.synthetic.main.stamp_item.view.*

class StampAdapter(
    private val stampPack: StampPack,
    private val stampClickListener: OnStampClickListener

) :
    SectionAdapter<StampAdapter.StampItemViewHolder, StampAdapter.StampHeaderViewHolder>(
        true,
        true
    ) {

    class StampItemViewHolder(itemView: View) : BaseSectionAdapter.ItemViewHolder(itemView)
    class StampHeaderViewHolder(itemView: View) : BaseSectionAdapter.HeaderViewHolder(itemView)

    override fun onCreateItemViewHolder(parent: ViewGroup?, type: Short): StampItemViewHolder {
        return StampAdapter.StampItemViewHolder(
            LayoutInflater.from(parent?.context!!).inflate(R.layout.stamp_image, parent, false)
        )
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): StampHeaderViewHolder {
        return StampAdapter.StampHeaderViewHolder(
            LayoutInflater.from(parent?.context!!).inflate(R.layout.stamp_item, parent, false)
        )
    }

    override fun onBindHeaderViewHolder(holder: StampHeaderViewHolder) {
        holder?.itemView?.guild_name_stamp?.text = "${stampPack.name.toUpperCase()}"
        holder?.itemView?.guild_name_stamp?.typeface = Typeface.DEFAULT_BOLD
    }

    override fun getItemCount(): Int {
        return stampPack.stamps.size
    }

    override fun onBindItemViewHolder(holder: StampItemViewHolder?, position: Int) {
        holder?.let {
            it.itemView?.let {
                it.imageview_stamp?.visibility = View.VISIBLE
                val item = stampPack.stamps[position]
                Glide.with(holder.itemView)
                    .load(
                        if (item.animated) STAMP_URL_GIF.format(item.hash) else STAMP_URL.format(
                            item.hash
                        )
                    )
                    .override(item.width, item.height)
                    .into(it.imageview_stamp)
                it.imageview_stamp.setOnClickListener {
                    stampClickListener.onStampSelected(mutableListOf(item.id))
                }
            }
        }
    }
}
class BottomStampAdapter()

interface OnBottomGuildSelectedListener {
    fun onGuildSelected(position: Int)
}


interface OnStampClickListener {
    fun onStampSelected(stamps: MutableList<String>)
}