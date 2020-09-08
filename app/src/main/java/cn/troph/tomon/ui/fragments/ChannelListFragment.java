package cn.troph.tomon.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
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

import cn.troph.tomon.R;
import cn.troph.tomon.core.ChannelType;
import cn.troph.tomon.core.Client;
import cn.troph.tomon.core.collections.GuildChannelCollection;
import cn.troph.tomon.core.structures.Guild;
import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.ui.channel.ChannelGroupRV;
import cn.troph.tomon.ui.channel.ChannelListAdapter;
import cn.troph.tomon.ui.channel.ChannelRV;
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
    private ChannelGroupRV mChannelTreeRoot;
    private ArrayMap<String, ChannelGroupRV> mChannelGroupCache = new ArrayMap<>();
    private ArrayMap<String, ArrayList<GuildChannel>> mChannelOrphans = new ArrayMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
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
        if (guild == null) {
            return;
        }
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
        if (channels != null && channels.getSize() > 0) {
            mChannelTreeRoot = new ChannelGroupRV(null, null, new ArrayList<>());
        }
        for (GuildChannel channel : channels) {
            if (channel.getParent() == null) {
                insertChannelAndCacheGroup(mChannelTreeRoot, channel);
            } else {
                ChannelGroupRV channelGroupRV = mChannelGroupCache.get(channel.getParentId());
                if (channelGroupRV == null) {
                    ArrayList<GuildChannel> orphans = mChannelOrphans.get(channel.getParentId());
                    if (orphans == null) {
                        orphans = new ArrayList<>();
                        mChannelOrphans.put(channel.getParentId(), orphans);
                    }
                    orphans.add(channel);
                } else {
                    insertChannelAndCacheGroup(channelGroupRV, channel);
                }
            }
        }

        if (mChannelListAdapter == null) {
            mChannelListAdapter = new ChannelListAdapter(mChannelTreeRoot);
            mRvChannelList.setAdapter(mChannelListAdapter);
        } else {
            mChannelListAdapter.setDataAndNotifyChanged(mChannelTreeRoot);
        }


    }

    private void insertChannelAndCacheGroup(ChannelGroupRV root, GuildChannel channel) {
        if (channel.getType() == ChannelType.CATEGORY) {
            ChannelGroupRV group = new ChannelGroupRV(root, channel, new ArrayList<>());
            root.addSortedByPostion(group);
            mChannelGroupCache.put(channel.getId(), group);
            ArrayList<GuildChannel> orphans = mChannelOrphans.get(channel.getId());
            if (orphans == null) {
                return;
            }
            for (GuildChannel orphan : orphans) {
                insertChannelAndCacheGroup(group, orphan);
            }
        } else {
            root.addSortedByPostion(new ChannelRV(root, channel));
        }
    }
}
