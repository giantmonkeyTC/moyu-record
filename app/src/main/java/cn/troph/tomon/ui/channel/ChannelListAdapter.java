package cn.troph.tomon.ui.channel;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.ChannelType;
import cn.troph.tomon.core.MessageType;
import cn.troph.tomon.core.collections.MessageCollection;
import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.core.structures.GuildMember;
import cn.troph.tomon.core.structures.Message;
import cn.troph.tomon.core.structures.MessageAttachment;
import cn.troph.tomon.core.structures.TextChannel;
import cn.troph.tomon.core.structures.VoiceChannel;
import cn.troph.tomon.core.structures.VoiceUpdate;
import cn.troph.tomon.ui.utils.LocalDateUtils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChannelListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChannelRV> mDataList = new ArrayList<>();
    private ArrayMap<String, Message> mLastedMessageCache = new ArrayMap<>();

    public ChannelListAdapter(ChannelGroupRV root) {
        if (root != null) {
            mDataList = root.flatten();
        }
    }

    public void setDataAndNotifyChanged(ChannelGroupRV root) {
        mDataList = root.flatten();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == ChannelType.CATEGORY.getValue()) {
            View groupView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_channel_category, parent, false);
            viewHolder = new ChannelGroupViewHolder(groupView);
        } else {
            View channelView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_channel_list, parent, false);
            viewHolder = new ChannelViewHolder(channelView);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChannelGroupViewHolder) {
            onBindChannelGroupViewHolder((ChannelGroupViewHolder) holder, position);
        } else {
            onBindChannelViewHolder((ChannelViewHolder) holder, position);
        }
    }

    private void onBindChannelViewHolder(ChannelViewHolder holder, int position) {
        ChannelRV channelRV = mDataList.get(position);
        GuildChannel channel = channelRV.getChannel();
        holder.tvChannelName.setText(channel.getName());
        ChannelType type = channel.getType();
        if (type == ChannelType.TEXT) {
            holder.tvChannelTime.setVisibility(View.VISIBLE);
            holder.ivChannelIcon.setImageResource(R.drawable.channel_text_icon);
            TextChannel textChannel = (TextChannel) channel;
            holder.setUnreadView(textChannel.getUnread(), textChannel.getMention() > 0);
            setTextChannelLatestMsgDespAndTime(holder, textChannel);
        } else if (type == ChannelType.VOICE) {
            holder.tvChannelTime.setVisibility(View.INVISIBLE);
            holder.ivChannelIcon.setImageResource(R.drawable.channel_voice_icon);
            holder.setUnreadView(false, false);
            setVoiceChannelStateDesp(holder, (VoiceChannel) channel);
        }
        int indent = channel.getIndent() + 2;
        int marginStart = indent * holder.itemView.getContext().getResources().getDimensionPixelSize(
                R.dimen.channel_list_margin_left);
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) holder.ivChannelIcon.getLayoutParams();
        lp.setMarginStart(marginStart);
        holder.ivChannelIcon.setLayoutParams(lp);
        if (channelRV.isVisible()) {
            holder.itemView.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setVisibility(View.GONE);
        }
    }

    private void onBindChannelGroupViewHolder(@NonNull ChannelGroupViewHolder holder, int position) {
        ChannelGroupViewHolder groupHolder = holder;
        ChannelGroupRV channelGroup = (ChannelGroupRV) mDataList.get(position);
        groupHolder.tvCategoryName.setText(channelGroup.getName());
        int indent = channelGroup.getChannel().getIndent() + 1;
        int marginStart = indent * groupHolder.itemView.getContext().getResources().getDimensionPixelSize(
                        R.dimen.channel_list_expand_margin_left);
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) groupHolder.ivCategoryExpandStateIcon.getLayoutParams();
        lp.setMarginStart(marginStart);
        groupHolder.ivCategoryExpandStateIcon.setLayoutParams(lp);
        groupHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ChannelRV> toggleChannels = channelGroup.toggleCollapse();
                if (channelGroup.isCollapsed()) {
                    mDataList.removeAll(toggleChannels);
                    notifyItemRangeRemoved(position+1, toggleChannels.size());
                    notifyItemRangeChanged(position, mDataList.size() - position);
                } else {
                    mDataList.addAll(position+1, toggleChannels);
                    notifyItemRangeInserted(position+1, toggleChannels.size());
                    notifyItemRangeChanged(position, mDataList.size() - position);
                }
            }
        });
        if (channelGroup.isCollapsed()) {
            groupHolder.ivCategoryExpandStateIcon.setImageResource(R.drawable.channel_group_collapse);
        } else {
            groupHolder.ivCategoryExpandStateIcon.setImageResource(R.drawable.channel_group_expand);
        }
        if (channelGroup.isVisible()) {
            groupHolder.itemView.setVisibility(View.VISIBLE);
        } else {
            groupHolder.itemView.setVisibility(View.GONE);
        }
    }

    private void setVoiceChannelStateDesp(ChannelViewHolder holder, VoiceChannel voiceChannel) {
        boolean joined = voiceChannel.isJoined();
        if (!joined) {
            holder.tvChannelDesp.setText(R.string.voice_channel_no_joined_desp);
        } else {
            List<VoiceUpdate> voiceStates = voiceChannel.getVoiceStates();
            GuildMember guildMember1 = voiceChannel.getMembers().get(voiceStates.get(0).getUserId());
            GuildMember guildMember2 = voiceChannel.getMembers().get(voiceStates.get(1).getUserId());
            int channelDespStringResId;
            if (voiceStates.size() <= 2) {
                channelDespStringResId = R.string.voice_channel_joined_desp;
            } else {
                channelDespStringResId = R.string.voice_channel_alot_joined_desp;
            }
            String channelDesp = holder.itemView.getContext().getString(
                    channelDespStringResId,
                    guildMember1.getDisplayName(),
                    guildMember2.getDisplayName());
            holder.tvChannelDesp.setText(channelDesp);
        }
    }

    private String getMessageAuthorName(Context context, String authorId, GuildChannel channel) {
        String displayName = null;
        GuildMember guildMember = channel.getMembers().get(authorId);
        if (guildMember != null) {
            displayName = guildMember.getDisplayName();
        }
        if (TextUtils.isEmpty(displayName)) {
            displayName =context.getResources().getString(R.string.deleted_name);
        }
        return displayName;
    }

    private void setMessageDesp(ChannelViewHolder holder, Message msg, GuildChannel channel) {
        String name = getMessageAuthorName(holder.itemView.getContext(), msg.getAuthorId(), channel);
        if (msg.getType() == MessageType.GUILD_MEMBER_JOIN) {
            holder.tvChannelDesp.setText(holder.itemView.getContext().getString(R.string.user_joined_guild, name));
        } else if (msg.getType() == MessageType.GUILD_OWNER_CHANGE) {
            String newOwnerId = msg.getContent();
            String newOwnerName = getMessageAuthorName(holder.itemView.getContext(), newOwnerId, channel);
            holder.tvChannelDesp.setText(holder.itemView.getContext().getString(R.string.guild_owner_changed, newOwnerName));
        } else if (msg.getStamps().size() > 0 && msg.getType() == MessageType.DEFAULT) {
            holder.tvChannelDesp.setText(holder.itemView.getContext().getString(R.string.msg_desp_stamp));
        } else if (TextUtils.isEmpty(msg.getContent()) && msg.getAttachments().getSize() > 0) {
            for (MessageAttachment attachment : msg.getAttachments()) {
                String content = "";
                if (isImage(attachment.getFileName())) {
                    content = holder.itemView.getContext().getString(R.string.attachment_image);
                } else if (isVideo(attachment.getFileName())) {
                    content = holder.itemView.getContext().getString(R.string.attachment_video);
                } else {
                    content = holder.itemView.getContext().getString(R.string.attachment_file);
                }
                holder.tvChannelDesp.setText(String.format("%s: %s",
                        name
                        , content));
                break;
            }
        } else {
            holder.tvChannelDesp.setText(String.format("%s: %s",
                    name
                    , msg.getContent()));
        }

    }

    private void setTextChannelLatestMsgDespAndTime(ChannelViewHolder holder, TextChannel textChannel) {
        MessageCollection messages = textChannel.getMessages();
        if (messages.getLatestMessage() != null) {
            setMessageDesp(holder,
                    messages.getLatestMessage(),
                    textChannel);
            LocalDateTime timestamp = messages.getLatestMessage().getTimestamp();
            holder.tvChannelTime.setText(LocalDateUtils.timestampConverterSimple(holder.itemView.getContext(), timestamp));
        } else {
            Message message = mLastedMessageCache.get(textChannel.getId());
            if (message != null) {
                setMessageDesp(holder, message, textChannel);
                LocalDateTime timestamp = message.getTimestamp();
                holder.tvChannelTime.setText(LocalDateUtils.timestampConverterSimple(holder.itemView.getContext(), timestamp));
            } else {
                holder.tvChannelDesp.setText("");
                holder.tvChannelTime.setText("");
                messages.fetch(null, null, 1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<Message>>() {
                            @Override
                            public void accept(List<Message> messages) throws Throwable {
                                if (messages.size() == 0) {
                                    return;
                                }
                                Message message = messages.get(0);
                                String msgChannelId = message.getChannelId();
                                if (msgChannelId.equals(textChannel.getId())) {
                                    mLastedMessageCache.put(textChannel.getId(), message);
                                    setMessageDesp(holder, message, textChannel);
                                    LocalDateTime timestamp = message.getTimestamp();
                                    holder.tvChannelTime.setText(LocalDateUtils.timestampConverterSimple(holder.itemView.getContext(), timestamp));
                                }
                            }
                        });
            }

        }
    }

    private boolean isImage(String name) {
        return name.endsWith("jpg") || name.endsWith("bmp") || name.endsWith(
                "gif"
        ) || name.endsWith("png") || name.endsWith("jpeg");
    }

    private boolean isVideo(String name) {
        return name.endsWith("mp4") || name.endsWith("avi") || name.endsWith(
                "3gp"
        );
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).getChannel().getType().getValue();
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {

        ImageView ivUnreadDotStroke;
        ImageView ivUnreadDotContent;
        ImageView ivChannelIcon;
        TextView tvChannelName;
        TextView tvChannelTime;
        TextView tvChannelDesp;


        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUnreadDotStroke = itemView.findViewById(R.id.iv_unread_dot_stroke);
            ivUnreadDotContent = itemView.findViewById(R.id.iv_unread_dot_content);
            ivChannelIcon = itemView.findViewById(R.id.iv_channel);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
            tvChannelDesp = itemView.findViewById(R.id.tv_channel_dscp);
            tvChannelTime = itemView.findViewById(R.id.tv_channel_time);
        }

        public void setUnreadView(boolean unread, boolean mention) {
            if (unread) {
                ivUnreadDotStroke.setVisibility(View.VISIBLE);
                ivUnreadDotContent.setVisibility(View.VISIBLE);
                if (mention) {
                    ivUnreadDotContent.setBackgroundResource(R.drawable.shape_unread_dot_content_alert);
                } else {
                    ivUnreadDotContent.setBackgroundResource(R.drawable.shape_unread_dot_content);
                }
            } else {
                ivUnreadDotStroke.setVisibility(View.INVISIBLE);
                ivUnreadDotContent.setVisibility(View.INVISIBLE);
            }
        }
    }

    static class ChannelGroupViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCategoryExpandStateIcon;
        TextView tvCategoryName;

        public ChannelGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryExpandStateIcon = itemView.findViewById(R.id.iv_category_expand_state);
            tvCategoryName = itemView.findViewById(R.id.tv_categoty_name);
        }
    }
}
