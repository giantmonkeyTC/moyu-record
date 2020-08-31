package cn.troph.tomon.ui.channel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.ChannelType;
import cn.troph.tomon.core.collections.MessageCollection;
import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.core.structures.GuildMember;
import cn.troph.tomon.core.structures.Message;
import cn.troph.tomon.core.structures.TextChannel;
import cn.troph.tomon.core.structures.VoiceChannel;
import cn.troph.tomon.core.structures.VoiceUpdate;
import cn.troph.tomon.ui.utils.LocalDateUtils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tomon.customview.expandablerecyclerview.ExpandCollapseController;
import tomon.customview.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import tomon.customview.expandablerecyclerview.models.ExpandableGroup;
import tomon.customview.expandablerecyclerview.models.ExpandableList;
import tomon.customview.expandablerecyclerview.viewholders.ChildViewHolder;
import tomon.customview.expandablerecyclerview.viewholders.GroupViewHolder;

public class ChannelListAdapter extends ExpandableRecyclerViewAdapter<ChannelListAdapter.ChannelCategoryViewHolder, ChannelListAdapter.ChannelViewHolder> {

    private HashMap<String, Message> mLastedMessageCache = new HashMap<>();

    public ChannelListAdapter(List<ChannelGroup> channelGroups) {
        super(channelGroups);
    }

    public void setDataSetAndNotifyChanged(List<ChannelGroup> channelGroups) {
        this.expandableList = new ExpandableList(channelGroups);
        this.expandCollapseController = new ExpandCollapseController(expandableList, this);
        notifyDataSetChanged();
    }

    @Override
    public ChannelCategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View groupView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channel_category, parent, false);
        return new ChannelCategoryViewHolder(groupView);
    }

    @Override
    public ChannelViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View channelView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channel_list, parent, false);
        return new ChannelViewHolder(channelView);
    }

    @Override
    public void onBindChildViewHolder(ChannelViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        ChannelGroup channelGroup = (ChannelGroup) group;
        GuildChannel channel = channelGroup.getItems().get(childIndex);
        holder.tvChannelName.setText(channel.getName());
        ChannelType type = channel.getType();
        if (type == ChannelType.TEXT) {
            holder.tvChannelTime.setVisibility(View.VISIBLE);
            holder.ivChannelIcon.setBackgroundResource(R.drawable.channel_text_icon);
            TextChannel textChannel = (TextChannel) channel;
            holder.setUnreadView(textChannel.getUnread(), textChannel.getMention() > 0);
            setTextChannelLatestMsgDesp(holder, textChannel);
        } else if (type == ChannelType.VOICE) {
            holder.tvChannelTime.setVisibility(View.INVISIBLE);
            holder.ivChannelIcon.setBackgroundResource(R.drawable.channel_voice_icon);
            holder.setUnreadView(false, false);
            setVoiceChannelStateDesp(holder, (VoiceChannel) channel);
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

    private void setTextChannelLatestMsgDesp(ChannelViewHolder holder, TextChannel textChannel) {
        MessageCollection messages = textChannel.getMessages();
        if (messages.getLatestMessage() != null) {
            holder.tvChannelDesp.setText(messages.getLatestMessage().getContent());
            LocalDateTime timestamp = messages.getLatestMessage().getTimestamp();
            holder.tvChannelTime.setText(LocalDateUtils.timestampConverter(holder.itemView.getContext(), timestamp));
        } else {
            Message message = mLastedMessageCache.get(textChannel.getId());
            if (message != null) {
                holder.tvChannelDesp.setText(message.getContent());
                LocalDateTime timestamp = message.getTimestamp();
                holder.tvChannelTime.setText(LocalDateUtils.timestampConverter(holder.itemView.getContext(), timestamp));
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
                                    holder.tvChannelDesp.setText(message.getContent());
                                    LocalDateTime timestamp = message.getTimestamp();
                                    holder.tvChannelTime.setText(LocalDateUtils.timestampConverter(holder.itemView.getContext(), timestamp));
                                }
                            }
                        });
            }

        }
    }

    @Override
    public void onBindGroupViewHolder(ChannelCategoryViewHolder holder, int flatPosition, ExpandableGroup group) {
        ChannelGroup channelGroup = (ChannelGroup) group;
        holder.tvCategoryName.setText(channelGroup.getTitle());
    }

    public static class ChannelCategoryViewHolder extends GroupViewHolder {

        ImageView ivCategoryExpandStateIcon;
        TextView tvCategoryName;
        public ChannelCategoryViewHolder(View itemView) {
            super(itemView);
            ivCategoryExpandStateIcon = itemView.findViewById(R.id.iv_category_expand_state);
            tvCategoryName = itemView.findViewById(R.id.tv_categoty_name);
        }
    }

    public static class ChannelViewHolder extends ChildViewHolder {

        ImageView ivUnreadDotStroke;
        ImageView ivUnreadDotContent;
        ImageView ivChannelIcon;
        TextView tvChannelName;
        TextView tvChannelTime;
        TextView tvChannelDesp;

        public ChannelViewHolder(View itemView) {
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
}
