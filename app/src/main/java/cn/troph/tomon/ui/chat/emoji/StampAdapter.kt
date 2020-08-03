import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.structures.Stamp
import cn.troph.tomon.core.structures.StampPack
import cn.troph.tomon.ui.chat.emoji.*
import cn.troph.tomon.ui.chat.emoji.OnBottomGuildSelectedListener
import cn.troph.tomon.ui.chat.messages.STAMP_URL
import cn.troph.tomon.ui.chat.messages.STAMP_URL_GIF
import com.bumptech.glide.Glide
import com.cruxlab.sectionedrecyclerview.lib.BaseSectionAdapter
import com.cruxlab.sectionedrecyclerview.lib.SectionAdapter
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.emoji_image.view.*
import kotlinx.android.synthetic.main.emoji_item.view.*
import kotlinx.android.synthetic.main.item_bottom_emoji_icon.view.*
import kotlinx.android.synthetic.main.item_message_stamp.view.*
import kotlinx.android.synthetic.main.stamp_image.view.*
import kotlinx.android.synthetic.main.stamp_item.view.*
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException

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
                    stampClickListener.onStampSelected(mutableListOf(item))
                }
            }
        }
    }
}

class BottomStampAdapter(
    private val urlList: MutableList<StampIcon>,
    private val onBottomPackSelectedListener: OnBottomPackSelectedListener
):
    RecyclerView.Adapter<BottomStampAdapter.BottomStampVH>(){
    class BottomStampVH(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomStampVH {
        return BottomStampAdapter.BottomStampVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bottom_emoji_icon, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return urlList.size
    }

    override fun onBindViewHolder(holder: BottomStampVH, position: Int) {
        if (urlList[position].url != null) {
            holder.itemView.bottom_emoji_iv.visibility = View.VISIBLE
            holder.itemView.ctv.visibility = View.GONE
            holder.itemView.civ.visibility = View.GONE
            Glide.with(holder.itemView).load(urlList[position].url)
                .into(holder.itemView.bottom_emoji_iv)

        } else {
            if (urlList[position].drawable == null) {
                holder.itemView.bottom_emoji_iv.visibility = View.GONE
                holder.itemView.ctv.visibility = View.VISIBLE
                holder.itemView.civ.visibility = View.GONE
                urlList[position].text?.let {
                    try {
                        holder.itemView.ctv.text = it[0].toUpperCase().toString()
                    } catch (e: NumberFormatException) {
                        holder.itemView.ctv.text = it[0].toUpperCase().toString()
                    } catch (e: IllegalArgumentException) {
                        holder.itemView.ctv.text = it[0].toUpperCase().toString()
                    }

                }
            } else {
                holder.itemView.bottom_emoji_iv.visibility = View.GONE
                holder.itemView.ctv.visibility = View.GONE
                holder.itemView.civ.visibility = View.VISIBLE
                urlList[position].drawable?.let {
                    holder.itemView.civ.setImageDrawable(it)
                }
            }
        }

        holder.itemView.setOnClickListener {
            urlList.forEach {
                it.isHighLight = false
            }
            urlList[holder.adapterPosition].isHighLight = true
            onBottomPackSelectedListener.onPackSelected(holder.adapterPosition)
            notifyDataSetChanged()
        }
        holder.itemView.isActivated = urlList[position].isHighLight
    }
}

interface OnBottomPackSelectedListener {
    fun onPackSelected(position: Int)
}

data class StampIcon(
    @SerializedName("url") val url: String? = null,
    @SerializedName("text") val text: String?,
    @SerializedName("drawable") val drawable: Drawable? = null,
    @SerializedName("is_highlight") var isHighLight: Boolean = false
)

interface OnStampClickListener {
    fun onStampSelected(stamps: MutableList<Stamp>)
}