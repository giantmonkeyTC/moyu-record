package cn.troph.tomon.ui.chat.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import kotlinx.android.synthetic.main.item_bot_command.view.*

class BotCommandAdapter(private val commandList: MutableList<String>) :
    RecyclerView.Adapter<CommandViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        return CommandViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_bot_command, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return commandList.size
    }

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        holder.itemView.bot_command_tv.text = commandList[position]
    }

}

class CommandViewHolder(v: View) : RecyclerView.ViewHolder(v)