package cn.troph.tomon.ui.guild;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.structures.Guild;

public class GuildListAdapter extends RecyclerView.Adapter<GuildListAdapter.ViewHolder> {

    private List<Guild> mGuildList;

    public GuildListAdapter(List<Guild> guilds) {
        mGuildList = guilds;
    }

    public void setDataAndNotifyChanged(List<Guild> newGuilds) {
        mGuildList = newGuilds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GuildListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View guildListItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guild_list, parent, false);
        return new ViewHolder(guildListItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GuildListAdapter.ViewHolder holder, int position) {
        Guild guild = mGuildList.get(position);
        holder.tvGuildName.setText(guild.getName());
        Glide.with(holder.ivAvatar).load(guild.getIconURL())
                .transform(new RoundedCorners(25))
                .into(holder.ivAvatar);

    }

    @Override
    public int getItemCount() {
        return mGuildList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivSelectedFlag;
        ImageView ivAvatar;
        TextView tvGuildName;
        RelativeLayout ivUnreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSelectedFlag = itemView.findViewById(R.id.iv_selected_flag);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvGuildName = itemView.findViewById(R.id.tv_guild_name);
            ivUnreadDot = itemView.findViewById(R.id.rl_unread_dot);
        }

    }
}
