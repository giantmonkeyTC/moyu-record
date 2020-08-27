package cn.troph.tomon.ui.guild;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.structures.Channel;
import cn.troph.tomon.core.structures.Guild;
import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.core.structures.TextChannel;
import cn.troph.tomon.ui.activities.ChannelListActivity;

public class GuildListAdapter extends RecyclerView.Adapter<GuildListAdapter.ViewHolder> {

    private List<Guild> mGuildList;
    private String mCurrentGuildId;
    private static final int GUILD_AVATAR_CORNER_RADIUS = 25;

    public GuildListAdapter(List<Guild> guilds) {
        mGuildList = guilds;
    }

    public void setDataAndNotifyChanged(List<Guild> newGuilds) {
        if (mGuildList != null) {
            mGuildList.clear();
        }
        mGuildList = newGuilds;
        notifyDataSetChanged();
    }

    public void setCurrentGuildId(String guildId) {
        mCurrentGuildId = guildId;
    }

    public List<Guild> getGuildList() {
        return mGuildList;
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
        String iconUrl = guild.getIconURL();
        if (TextUtils.isEmpty(iconUrl)) {
            Glide.with(holder.ivAvatar).load(R.drawable.guild_avatar_placeholder)
                    .transform(new CenterCrop(), new RoundedCorners(GUILD_AVATAR_CORNER_RADIUS))
                    .into(holder.ivAvatar);
            holder.tvNoIconTextHolder.setText(guild.getName().substring(0,1));
            holder.tvNoIconTextHolder.setVisibility(View.VISIBLE);

        } else {
            Glide.with(holder.ivAvatar).load(guild.getIconURL())
                    .transform(new CenterCrop(), new RoundedCorners(GUILD_AVATAR_CORNER_RADIUS))
                    .into(holder.ivAvatar);
            holder.tvNoIconTextHolder.setVisibility(View.GONE);
        }

        guild.updateMention();
        guild.updateUnread();

        boolean unread = guild.getUnread();
        if (unread) {
            holder.setNormalUnreadDot();
            for (GuildChannel channel : guild.getChannels()) {
                if (channel instanceof TextChannel && ((TextChannel) channel).getUnreadMention() > 0) {
                    holder.setAlertUnreadDot();
                    break;
                }
            }
            holder.setUnreadDotVisibility(View.VISIBLE);
        } else {
            holder.setUnreadDotVisibility(View.INVISIBLE);
        }

        if (TextUtils.isEmpty(mCurrentGuildId) && position == 0) {
            holder.ivSelectedFlag.setVisibility(View.VISIBLE);
            mCurrentGuildId = guild.getId();
            SharedPreferences sp = holder.ivSelectedFlag.getContext().
                    getSharedPreferences(ChannelListActivity.SP_NAME_CHANNEL_LIST_CONFIG, Context.MODE_PRIVATE);
            sp.edit().putString(ChannelListActivity.SP_KEY_GUILD_ID, mCurrentGuildId).apply();

        } else {
            if (guild.getId().equals(mCurrentGuildId)) {
                holder.ivSelectedFlag.setVisibility(View.VISIBLE);
            } else {
                holder.ivSelectedFlag.setVisibility(View.INVISIBLE);
            }
        }


    }

    @Override
    public int getItemCount() {
        return mGuildList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivSelectedFlag;
        ImageView ivAvatar;
        TextView tvGuildName;
        ImageView ivUnreadDotStroke;
        ImageView ivUnreadDotContent;
        TextView tvNoIconTextHolder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSelectedFlag = itemView.findViewById(R.id.iv_selected_flag);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvGuildName = itemView.findViewById(R.id.tv_guild_name);
            ivUnreadDotStroke = itemView.findViewById(R.id.iv_unread_dot_stroke);
            ivUnreadDotContent = itemView.findViewById(R.id.iv_unread_dot_content);
            tvNoIconTextHolder = itemView.findViewById(R.id.tv_no_icon_text);
        }

        public void setUnreadDotVisibility(int visibility) {
            ivUnreadDotContent.setVisibility(visibility);
            ivUnreadDotStroke.setVisibility(visibility);
        }

        public void setAlertUnreadDot() {
            ivUnreadDotContent.setBackgroundResource(R.drawable.shape_unread_dot_content_alert);
        }

        public void setNormalUnreadDot() {
            ivUnreadDotContent.setBackgroundResource(R.drawable.shape_unread_dot_content);
        }

    }
}
