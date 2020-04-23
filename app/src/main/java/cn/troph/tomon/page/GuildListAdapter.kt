package cn.troph.tomon.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.Event
import cn.troph.tomon.core.structures.Guild
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable


class GuildListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var textView: TextView = itemView.findViewById(R.id.guild_item_text)
    private var imageView: ImageView = itemView.findViewById(R.id.guild_item_image)

    fun bind(guild: Guild) {
        textView.text = guild.name
        Glide.with(itemView).load(guild.iconURL).into(imageView)
    }
}

class GuildListAdapter : RecyclerView.Adapter<GuildListViewHolder>() {

    private var observable: Observable<Event<Guild>> = Observable.create(Client.global.guilds)

    init {
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe { event ->
            println("update guild lsit!")
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuildListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView = layoutInflater.inflate(R.layout.guild_item, parent, false)
        return GuildListViewHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return Client.global.guilds.size
    }

    override fun onBindViewHolder(holder: GuildListViewHolder, position: Int) {
        val list = Client.global.guilds.list
        val guild = if (position >= 0 && position < list.size) list[position] else null
        if (guild != null) {
            holder.bind(guild)
        }
    }


}