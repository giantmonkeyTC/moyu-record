package cn.troph.tomon.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.ChannelType;
import cn.troph.tomon.core.Client;
import cn.troph.tomon.core.collections.GuildChannelCollection;
import cn.troph.tomon.core.structures.Guild;
import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.ui.channel.ChannelGroup;
import cn.troph.tomon.ui.channel.ChannelListAdapter;
import cn.troph.tomon.ui.guild.GuildAvatarUtils;

public class ChannelListFragment extends Fragment {

    public static final String TAG = "ChannelListFragment";
    public static final String GUILD_ID = "guild_id";

    private ImageView mIvGuildAvater;
    private ImageView mIvGuildBanner;
    private ImageView mIvGuildSetting;
    private ImageView mIvGuildBannerMask;
    private TextView mTvGuildName;
    private TextView mTvGuildAvaterTextHolder;
    private RecyclerView mRvChannelList;
    private ChannelListAdapter mChannelListAdapter;
    private List<ChannelGroup> mChannelGroups;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        mChannelGroups = new ArrayList<>();
        updateGuildBanner(getArguments().getString(GUILD_ID));
    }

    private void initView(@NonNull View view) {
        mIvGuildAvater = view.findViewById(R.id.iv_guild_avatar);
        mIvGuildBanner = view.findViewById(R.id.iv_guild_banner);
        mIvGuildBannerMask = view.findViewById(R.id.iv_guild_banner_mask);
        mIvGuildSetting = view.findViewById(R.id.iv_guild_setting);
        mTvGuildName = view.findViewById(R.id.tv_guild_name);
        mTvGuildAvaterTextHolder = view.findViewById(R.id.tv_no_icon_text);
        mRvChannelList = view.findViewById(R.id.rv_channel_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRvChannelList.setLayoutManager(linearLayoutManager);

    }

    public void updateGuildBanner(String guildId) {
        if (TextUtils.isEmpty(guildId)) {
            guildId = Client.Companion.getGlobal().getGuilds().getList().get(0).getId();
        }
        Guild guild = Client.Companion.getGlobal().getGuilds().get(guildId);
        GuildAvatarUtils.setGuildAvatar(mIvGuildAvater, mTvGuildAvaterTextHolder, guild);
        mTvGuildName.setText(guild.getName());
        Glide.with(mIvGuildBanner).clear(mIvGuildBanner);
        Glide.with(mIvGuildBanner)
                .load(guild.getBackgroundUrl())
                .transform(new CenterCrop())
                .placeholder(R.drawable.guild_background_placeholder)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mIvGuildBannerMask.setVisibility(View.INVISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mIvGuildBannerMask.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(mIvGuildBanner);

        GuildChannelCollection channels = guild.getChannels();
        HashMap<String, ChannelGroup> channelGroupHashMap = new HashMap<>();
        mChannelGroups.clear();
        for (GuildChannel channel : channels) {
            ChannelType type = channel.getType();
            if (type == ChannelType.CATEGORY) {
                ChannelGroup channelGroup = new ChannelGroup(channel.getName(), new ArrayList<>());
                channelGroupHashMap.put(channel.getId(), channelGroup);
                mChannelGroups.add(channelGroup);
            } else if (type == ChannelType.TEXT || type == ChannelType.VOICE) {
                ChannelGroup channelGroup1 = channelGroupHashMap.get(channel.getParentId());
                channelGroup1.getItems().add(channel);
            } else {
                Log.d(TAG, "Add an unknown type channel: " + type);
            }
        }

        if (mChannelListAdapter == null) {
            mChannelListAdapter = new ChannelListAdapter(mChannelGroups);
            mRvChannelList.setAdapter(mChannelListAdapter);
        } else {
            mChannelListAdapter.setDataSetAndNotifyChanged(mChannelGroups);
        }

        mChannelListAdapter.expandAll();

    }
}
