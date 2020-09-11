package cn.troph.tomon.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ActivityViewModelLazyKt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.nisrulz.sensey.ProximityDetector;
import com.github.nisrulz.sensey.Sensey;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

import cn.troph.tomon.R;
import cn.troph.tomon.core.ChannelType;
import cn.troph.tomon.core.Client;
import cn.troph.tomon.core.collections.GuildChannelCollection;
import cn.troph.tomon.core.events.GuildVoiceSelectorEvent;
import cn.troph.tomon.core.events.VoiceLeaveChannelEvent;
import cn.troph.tomon.core.events.VoiceSpeakEvent;
import cn.troph.tomon.core.network.socket.GatewayOp;
import cn.troph.tomon.core.structures.Guild;
import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.core.structures.Speaking;
import cn.troph.tomon.core.structures.VoiceChannel;
import cn.troph.tomon.core.structures.VoiceConnectSend;
import cn.troph.tomon.core.structures.VoiceConnectStateReceive;
import cn.troph.tomon.core.structures.VoiceIdentify;
import cn.troph.tomon.core.structures.VoiceLeaveConnect;
import cn.troph.tomon.core.utils.Collection;
import cn.troph.tomon.ui.activities.TomonMainActivity;
import cn.troph.tomon.ui.channel.ChannelGroupRV;
import cn.troph.tomon.ui.channel.ChannelListAdapter;
import cn.troph.tomon.ui.channel.ChannelRV;
import cn.troph.tomon.ui.chat.fragments.VoiceBottomSheet;
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel;
import cn.troph.tomon.ui.guild.GuildAvatarUtils;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class ChannelListFragment extends Fragment implements PermissionListener {

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
    private RtcEngine mRtcEngine;
    private ChatSharedViewModel mChatVM;
    private PowerManager.WakeLock mWakeLock;
    private RtcEngineEventHandler mRtcEngineEventHandler = new RtcEngineEventHandler();
    private ArrayMap<String, ChannelGroupRV> mChannelGroupCache = new ArrayMap<>();
    private ArrayMap<String, ArrayList<GuildChannel>> mChannelOrphans = new ArrayMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "tomon:wakelock_screen_off");
        Sensey.getInstance().init(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mChatVM = new ViewModelProvider(requireActivity()).get(ChatSharedViewModel.class);
        mChatVM.setUpEvents();
        return inflater.inflate(R.layout.fragment_channel_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAgoraEngine();
        initView(view);
        updateGuildBanner(getArguments().getString(GUILD_ID));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRtcEngine.leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
        Sensey.getInstance().stop();
        Client.Companion.getGlobal().getSocket().send(GatewayOp.VOICE, new Gson().toJsonTree(new VoiceLeaveConnect()));
        mWakeLock.release();
        mWakeLock = null;
    }

    private void initAgoraEngine() {
        if (mRtcEngine == null) {
            try {
                mRtcEngine = RtcEngine.create(getContext(), getString(R.string.agora_app_id), mRtcEngineEventHandler);
                mRtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
                mRtcEngine.enableAudioVolumeIndication(200, 3, true);
            } catch (Exception e) {
                Log.e(TAG, "RtcEngine create error:", e);
                Toast.makeText(
                        getContext().getApplicationContext(),
                        R.string.join_voice_fail,
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
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
        Collection<GuildChannel> guildChannels = channels.clone();
        for (GuildChannel channel : guildChannels) {
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
            mChannelListAdapter = new ChannelListAdapter(mChannelTreeRoot, guild.getId());
            setOnVoiceChannelClickListener();
            registerObserver();
            mRvChannelList.setAdapter(mChannelListAdapter);
        } else {
            mChannelListAdapter.setDataAndNotifyChanged(mChannelTreeRoot, guild.getId());
        }
    }

    private void registerObserver() {
        mChatVM.getVoiceSocketLeaveLD().observe(getViewLifecycleOwner(), new Observer<VoiceConnectStateReceive>() {
            @Override
            public void onChanged(VoiceConnectStateReceive voiceConnectStateReceive) {
                Client.Companion.getGlobal().getVoiceSocket().close();
            }
        });
        mChatVM.getVoiceSelfDeafLD().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDeaf) {
                mRtcEngine.muteAllRemoteAudioStreams(isDeaf);
                GuildChannel channel = mChatVM.getSelectedCurrentVoiceChannel().getValue();
                if (channel != null) {
                    AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
                    Client.Companion.getGlobal().getSocket().send(GatewayOp.VOICE,
                            new Gson().toJsonTree(new VoiceConnectSend(
                                    channel.getId(),
                                    isDeaf,
                                    audioManager.isMicrophoneMute()
                            )));
                }
            }
        });
        mChatVM.getVoiceSocketJoinLD().observe(getViewLifecycleOwner(), new Observer<VoiceConnectStateReceive>() {
            @Override
            public void onChanged(VoiceConnectStateReceive state) {
                Client.Companion.getGlobal().getVoiceSocket().open();
                joinVoiceChannel(state.getTokenAgora(), state.getVoiceUserIdAgora(), state.getChannelId());
            }
        });
        mChatVM.getVoiceLeaveClick().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Client.Companion.getGlobal().getSocket().send(GatewayOp.VOICE, new Gson().toJsonTree(new VoiceLeaveConnect()));
            }
        });
        mChatVM.getVoiceSocketStateLD().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean open) {
                if (open) {
                    Client.Companion.getGlobal().getVoiceSocket().send(GatewayOp.DISPATCH,
                            new Gson().toJsonTree(new VoiceIdentify(
                                    Client.Companion.getGlobal().getMe().getId(),
                                    Client.Companion.getGlobal().getSocket().getSesstion(),
                                    mChatVM.getVoiceSocketJoinLD().getValue().getVoiceUserIdAgora()
                            )));
                } else {
                    mRtcEngine.leaveChannel();
                }
            }
        });
    }

    private void joinVoiceChannel(String tokenAgora, int voiceUserIdAgora, String channelId) {
        mRtcEngine.joinChannel(tokenAgora, channelId, "Extra Optional Data", voiceUserIdAgora);
    }

    private void setOnVoiceChannelClickListener() {
        mChannelListAdapter.setOnVoiceChannelClickListener(new ChannelListAdapter.OnVoiceChannelItemClickListener() {
            @Override
            public void onVoiceChannelClick(VoiceChannel channel) {
                mRtcEngineEventHandler.setCurrentVoiceChannel(channel);
                Dexter.withContext(getContext()).withPermission(Manifest.permission.RECORD_AUDIO)
                        .withListener(ChannelListFragment.this).check();
            }
        });
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

    private ProximityDetector.ProximityListener mProximityListener = new ProximityDetector.ProximityListener() {
        @Override
        public void onFar() {
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire(3600 * 1000);
            }
            AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setSpeakerphoneOn(true);
        }

        @Override
        public void onNear() {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setSpeakerphoneOn(false);
        }
    };

    @Override
    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
        VoiceChannel channel = mRtcEngineEventHandler.getCurrentVoiceChannel();
        if (mChatVM.getSelectedCurrentVoiceChannel().getValue() == null) {
            mChatVM.getSwitchingChannelVoiceLD().setValue(false);
            AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            Client.Companion.getGlobal().getSocket().send(GatewayOp.VOICE,
                    new Gson().toJsonTree(new VoiceConnectSend(
                            channel.getId(),
                            audioManager.isStreamMute(AudioManager.STREAM_MUSIC),
                            audioManager.isMicrophoneMute())));
        } else {
            if (!mChatVM.getSelectedCurrentVoiceChannel().getValue().getId().equals(channel.getId())) {
                mChatVM.getVoiceLeaveClick().setValue(true);
                mChatVM.getSwitchingChannelVoiceLD().setValue(true);
            } else {
                mChatVM.getSelectedCurrentVoiceChannel().setValue(mChatVM.getSelectedCurrentVoiceChannel().getValue());
                new VoiceBottomSheet().show(getParentFragmentManager(), null);
            }
        }
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
        Toast.makeText(
                getContext(),
                R.string.join_permission_msg,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
    }

    private class RtcEngineEventHandler extends IRtcEngineEventHandler {

        private static final int LOCAL_USER_IS_NOT_SPEAKING = 0;
        private static final int LOCAL_USER_IS_SPEAKING = 1;
        private Handler mainThread = new Handler(Looper.getMainLooper());
        private VoiceChannel selectedVoiceChannel;

        public void setCurrentVoiceChannel(VoiceChannel voiceChannel) {
            selectedVoiceChannel = voiceChannel;
        }

        public VoiceChannel getCurrentVoiceChannel() {
            return selectedVoiceChannel;
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] audioVolumeInfos, int i) {
            super.onAudioVolumeIndication(audioVolumeInfos, i);
            for (AudioVolumeInfo info : audioVolumeInfos) {
                if (info.vad == LOCAL_USER_IS_SPEAKING) {
                    int speaking = -1;
                    if (info.volume > 50) {
                        speaking = LOCAL_USER_IS_SPEAKING;
                    } else {
                        speaking = LOCAL_USER_IS_NOT_SPEAKING;
                    }
                    Client.Companion.getGlobal().getVoiceSocket().send(
                            GatewayOp.SPEAK,
                            new Gson().toJsonTree(new Speaking(speaking, "")));
                    Client.Companion.getGlobal().getEventBus().postEvent(
                            new VoiceSpeakEvent(
                                    new Speaking(speaking, Client.Companion.getGlobal().getMe().getId())));
                }
            }
        }

        @Override
        public void onUserJoined(int i, int i1) {
            super.onUserJoined(i, i1);
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    MediaPlayer.create(getContext(), R.raw.user_join).start();
                }
            });
        }

        @Override
        public void onLeaveChannel(RtcStats rtcStats) {
            super.onLeaveChannel(rtcStats);
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    MediaPlayer.create(getContext(), R.raw.user_offline).start();
                    Boolean value = mChatVM.getSwitchingChannelVoiceLD().getValue();
                    if (value != null && value) {
                        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
                        Client.Companion.getGlobal().getSocket().send(GatewayOp.VOICE,
                                new Gson().toJsonTree(new VoiceConnectSend(
                                        selectedVoiceChannel.getId(),
                                        audioManager.isStreamMute(AudioManager.STREAM_MUSIC),
                                        audioManager.isMicrophoneMute())));
                    }
                    mChatVM.getSwitchingChannelVoiceLD().setValue(false);
                    mChatVM.getSelectedCurrentVoiceChannel().setValue(null);
                    Client.Companion.getGlobal().getEventBus().postEvent(new GuildVoiceSelectorEvent(""));
                    mWakeLock.release();
                    Sensey.getInstance().stopProximityDetection(mProximityListener);
                }
            });

        }

        @Override
        public void onJoinChannelSuccess(String s, int i, int i1) {
            super.onJoinChannelSuccess(s, i, i1);
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    mRtcEngine.setEnableSpeakerphone(true);
                    mChatVM.getSelectedCurrentVoiceChannel().setValue(selectedVoiceChannel);
                    Client.Companion.getGlobal().getEventBus().postEvent(new GuildVoiceSelectorEvent(selectedVoiceChannel.getId()));
                    new VoiceBottomSheet().show(getParentFragmentManager(), null);
                }
            });
            Sensey.getInstance().startProximityDetection(mProximityListener);
        }

        @Override
        public void onError(int i) {
            super.onError(i);
            Log.e(TAG, "an error during Angora SDK runtime: " + i);
        }
    }
}
