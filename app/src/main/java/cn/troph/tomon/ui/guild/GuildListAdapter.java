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
import cn.troph.tomon.core.structures.Guild;
import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.core.structures.TextChannel;
import cn.troph.tomon.ui.activities.TomonMainActivity;

public class GuildListAdapter extends RecyclerView.Adapter<GuildListAdapter.ViewHolder> {

    private List<Guild> mGuildList;
    private String mCurrentGuildId;
    private OnItemClickListener mOnItemClickListener;

    public GuildListAdapter(List<Guild> guilds) {
        mGuildList = guilds;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
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

    public String getCurrentGuildId() {
        return mCurrentGuildId;
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
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(position, guild);
                }
            });
        }
        holder.tvGuildName.setText(guild.getName());
        GuildAvatarUtils.setGuildAvatar(holder.ivAvatar, holder.tvNoIconTextHolder, guild);
        guild.updateMention();
        guild.updateUnread();

        boolean unread = guild.getUnread();
        if (unread) {
            holder.setNormalUnreadDot();
            for (GuildChannel channel : guild.getChannels()) {
                if (channel instanceof TextChannel && hasUnreadAtMeMessage((TextChannel) channel)) {
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
                    getSharedPreferences(TomonMainActivity.SP_NAME_CHANNEL_LIST_CONFIG, Context.MODE_PRIVATE);
            sp.edit().putString(TomonMainActivity.SP_KEY_GUILD_ID, mCurrentGuildId).apply();

        } else {
            if (guild.getId().equals(mCurrentGuildId)) {
                holder.ivSelectedFlag.setVisibility(View.VISIBLE);
            } else {
                holder.ivSelectedFlag.setVisibility(View.INVISIBLE);
            }
        }


    }

    private boolean hasUnreadAtMeMessage(TextChannel channel) {
        int mention = channel.getMention();
        return mention > 0;
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

    public interface OnItemClickListener {
        void onItemClick(int position, Guild guild);
    }
}
