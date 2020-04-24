package cn.troph.tomon.ui.chat.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.Event
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.ui.widgets.Avatar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class GuildSelectorAdapter: RecyclerView.Adapter<GuildSelectorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private var text: TextView = itemView.findViewById(R.id.text_name)
        private var avatar: Avatar = itemView.findViewById(R.id.image_avatar)

        fun bind(guild: Guild) {
            text.text = guild.name
            avatar.url = guild.iconURL
        }
    }

    private var observable: Observable<Event<Guild>> = Observable.create(Client.global.guilds)

    init {
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe { event ->
            println("guild selector event: $event")
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView = layoutInflater.inflate(R.layout.widget_guild_selector_item, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int = Client.global.guilds.list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = Client.global.guilds.list
        val guild = if (position >= 0 && position < list.size) list[position] else null
        if (guild != null) {
            holder.bind(guild)
        }
    }


}