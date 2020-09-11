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
import cn.troph.tomon.core.Client;
import cn.troph.tomon.core.MessageType;
import cn.troph.tomon.core.collections.MessageCollection;
import cn.troph.tomon.core.events.GuildVoiceSelectorEvent;
import cn.troph.tomon.core.events.MessageAtMeEvent;
import cn.troph.tomon.core.events.MessageCreateEvent;
import cn.troph.tomon.core.events.MessageReadEvent;
import cn.troph.tomon.core.events.VoiceStateUpdateEvent;
import cn.troph.tomon.core.structures.Base;
import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.core.structures.GuildMember;
import cn.troph.tomon.core.structures.Message;
import cn.troph.tomon.core.structures.MessageAttachment;
import cn.troph.tomon.core.structures.TextChannel;
import cn.troph.tomon.core.structures.VoiceChannel;
import cn.troph.tomon.core.structures.VoiceUpdate;
import cn.troph.tomon.core.utils.Assets;
import cn.troph.tomon.ui.utils.LocalDateUtils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChannelListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChannelRV> mDataList = new ArrayList<>();
    private ArrayMap<String, Message> mLastedMessageCache = new ArrayMap<>();
    private String mCurrentGuildID;
    private OnVoiceChannelItemClickListener mOnVoiceChannelClickListener;

    public ChannelListAdapter(ChannelGroupRV root, String guildId) {
        if (root != null) {
            mDataList = root.flatten();
            mCurrentGuildID = guildId;
        }
    }

    public void setDataAndNotifyChanged(ChannelGroupRV root, String guildID) {
        mDataList = root.flatten();
        mCurrentGuildID = guildID;
        notifyDataSetChanged();
    }

    public void setOnVoiceChannelClickListener(OnVoiceChannelItemClickListener listener) {
        mOnVoiceChannelClickListener = listener;
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
        registerObserverForChannel(position, holder);
        bindChannelForHolder(holder, position);
    }

    private void bindChannelForHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChannelGroupViewHolder && mDataList.get(position).getChannel().getType() == ChannelType.CATEGORY) {
            onBindChannelGroupViewHolder((ChannelGroupViewHolder) holder, position);
        } else if (holder instanceof ChannelViewHolder && mDataList.get(position).getChannel().getType() != ChannelType.CATEGORY) {
            onBindChannelViewHolder((ChannelViewHolder) holder, position);
        }
    }

    private void registerObserverForChannel(int position, RecyclerView.ViewHolder holder) {
        ChannelRV channelRV = mDataList.get(position);
        GuildChannel channel = channelRV.getChannel();
        channel.getObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Base>() {
            @Override
            public void accept(Base base) throws Throwable {
                bindChannelForHolder(holder, position);
            }
        });

        Client.Companion.getGlobal().getEventBus().observeEventsOnUi().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object event) throws Throwable {
                if (event instanceof MessageCreateEvent) {
                    MessageCreateEvent createEvent = (MessageCreateEvent) event;
                    if (createEvent.getMessage().getChannelId().equals(channel.getId())) {
                        mLastedMessageCache.put(channel.getId(), createEvent.getMessage());
                        bindChannelForHolder(holder, position);
                    }
                } else if (event instanceof MessageAtMeEvent) {
                    MessageAtMeEvent atMeEvent = (MessageAtMeEvent) event;
                    if (atMeEvent.getMessage().getChannelId().equals(channel.getId())) {
                        mLastedMessageCache.put(channel.getId(), atMeEvent.getMessage());
                        bindChannelForHolder(holder, position);
                    }
                } else if (event instanceof MessageReadEvent) {
                    MessageReadEvent readEvent = (MessageReadEvent) event;
                    if (readEvent.getMessage().getChannelId().equals(channel.getId())) {
                        mLastedMessageCache.put(channel.getId(), readEvent.getMessage());
                        bindChannelForHolder(holder, position);
                    }
                } else if (event instanceof GuildVoiceSelectorEvent) {
                    GuildVoiceSelectorEvent voiceSelectorEvent = (GuildVoiceSelectorEvent) event;
                    if (voiceSelectorEvent.getChannelId().equals(channel.getId())) {
                        VoiceChannel voiceChannel = (VoiceChannel) channel;
                        voiceChannel.setJoined(true);
                        bindChannelForHolder(holder, position);
                    } else {
                        if (channel instanceof VoiceChannel) {
                            ((VoiceChannel) channel).setJoined(false);
                        }
                    }
                } else if (event instanceof VoiceStateUpdateEvent) {
                    if (!mCurrentGuildID.equals(channel.getGuildId())) {
                        return;
                    }
                    String eventChannelId = ((VoiceStateUpdateEvent) event).getVoiceUpdate().getChannelId();
                    if (TextUtils.isEmpty(eventChannelId) || eventChannelId.equals(mDataList.get(position).getChannel().getId()) ) {
                        bindChannelForHolder(holder, position);
                    }
                }
            }
        });
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
            holder.itemView.setOnClickListener(null);
        } else if (type == ChannelType.VOICE) {
            holder.tvChannelTime.setVisibility(View.INVISIBLE);
            holder.ivChannelIcon.setImageResource(R.drawable.channel_voice_icon);
            holder.setUnreadView(false, false);
            setVoiceChannelStateDesp(holder, (VoiceChannel) channel);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnVoiceChannelClickListener != null) {
                        mOnVoiceChannelClickListener.onVoiceChannelClick((VoiceChannel) channel);
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(null);
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
            GuildMember guildMember2 = null;
            if (voiceStates.size() >= 2) {
                guildMember2 = voiceChannel.getMembers().get(voiceStates.get(1).getUserId());
            }

            int channelDespStringResId;
            if (voiceStates.size() == 1) {
                channelDespStringResId = R.string.voice_channel_joined_one_desp;
            } else if (voiceStates.size() == 2) {
                channelDespStringResId = R.string.voice_channel_joined_two_desp;
            } else {
                channelDespStringResId = R.string.voice_channel_alot_joined_desp;
            }
            String channelDesp;
            if (voiceStates.size() == 1) {
                channelDesp = holder.itemView.getContext().getString(
                        channelDespStringResId,
                        guildMember1.getDisplayName());
            } else if (voiceStates.size() == 2){
                channelDesp = holder.itemView.getContext().getString(
                        channelDespStringResId,
                        guildMember1.getDisplayName(),
                        guildMember2.getDisplayName());
            } else {
                channelDesp = holder.itemView.getContext().getString(
                        channelDespStringResId,
                        guildMember1.getDisplayName(),
                        guildMember2.getDisplayName(),
                        voiceStates.size());
            }
            holder.tvChannelDesp.setText(channelDesp);
        }
    }

    private String getAuthorNameInChannel(Context context, String authorId, GuildChannel channel) {
        String displayName = null;
        GuildMember guildMember = channel.getMembers().get(authorId);
        if (guildMember != null) {
            displayName = guildMember.getDisplayName();
        }
        if (TextUtils.isEmpty(displayName)) {
            displayName =context.getResources().getString(R.string.deleted_name);
        }
        if (displayName.length() > 14) {
            displayName = displayName.substring(0, 14) + "...";
        }
        return displayName;
    }

    private void setMessageDesp(ChannelViewHolder holder, Message msg, TextChannel channel) {
        boolean hasMentionedUnread = channel.getUnread() && channel.getMention() > 0;
        String name = getAuthorNameInChannel(holder.itemView.getContext(), msg.getAuthorId(), channel);
        if (msg.getType() == MessageType.GUILD_MEMBER_JOIN) {
            holder.tvChannelDesp.setText(holder.itemView.getContext().getString(R.string.user_joined_guild, name));
        } else if (msg.getType() == MessageType.GUILD_OWNER_CHANGE) {
            String newOwnerId = msg.getContent();
            String newOwnerName = getAuthorNameInChannel(holder.itemView.getContext(), newOwnerId, channel);
            holder.tvChannelDesp.setText(holder.itemView.getContext().getString(R.string.guild_owner_changed, newOwnerName));
        } else if (msg.getStamps().size() > 0 && msg.getType() == MessageType.DEFAULT) {
            holder.tvChannelDesp.setText(holder.itemView.getContext().getString(R.string.msg_desp_stamp));
        } else if (!TextUtils.isEmpty(msg.getContent()) &&
                (Assets.INSTANCE.getRegexAtUser().containsMatchIn(msg.getContent()) || Assets.INSTANCE.getRegexEmoji().containsMatchIn(msg.getContent()))) {
            Assets.ContentSpan contentSpan = Assets.INSTANCE.contentParser(msg.getContent());
            String tmpMsg = msg.getContent();
            List<Assets.ContentEmoji> contentEmojis = contentSpan.getContentEmoji();
            for (Assets.ContentEmoji contentEmoji : contentEmojis) {
                tmpMsg = tmpMsg.replaceFirst(contentEmoji.getRaw(), "["+contentEmoji.getName()+"]");
            }
            List<Assets.ContentAtUser> contentAtUsers = contentSpan.getContentAtUser();
            for (Assets.ContentAtUser contentAtUser : contentAtUsers) {
                tmpMsg = tmpMsg.replaceFirst("<@"+contentAtUser.getId()+">", "@" + getAuthorNameInChannel(holder.itemView.getContext(), contentAtUser.getId(), channel));
            }
            holder.tvChannelDesp.setText(String.format("%s: %s", name, tmpMsg));
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

        if (hasMentionedUnread) {
            holder.tvChannelDespMention.setText(R.string.msg_desp_mention);
            holder.tvChannelDespMention.setVisibility(View.VISIBLE);
        } else {
            holder.tvChannelDespMention.setText("");
            holder.tvChannelDespMention.setVisibility(View.INVISIBLE);
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
        TextView tvChannelDespMention;


        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUnreadDotStroke = itemView.findViewById(R.id.iv_unread_dot_stroke);
            ivUnreadDotContent = itemView.findViewById(R.id.iv_unread_dot_content);
            ivChannelIcon = itemView.findViewById(R.id.iv_channel);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
            tvChannelDesp = itemView.findViewById(R.id.tv_channel_dscp);
            tvChannelTime = itemView.findViewById(R.id.tv_channel_time);
            tvChannelDespMention = itemView.findViewById(R.id.tv_channel_dscp_mention);
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

    public interface OnVoiceChannelItemClickListener {
        void onVoiceChannelClick(VoiceChannel channel);
    }
}
