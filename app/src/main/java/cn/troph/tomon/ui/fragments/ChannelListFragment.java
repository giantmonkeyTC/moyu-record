package cn.troph.tomon.ui.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.ChannelType;
import cn.troph.tomon.core.Client;
import cn.troph.tomon.core.collections.GuildChannelCollection;
import cn.troph.tomon.core.collections.GuildMemberCollection;
import cn.troph.tomon.core.events.ChannelSyncEvent;
import cn.troph.tomon.core.events.GuildVoiceSelectorEvent;
import cn.troph.tomon.core.events.VoiceSpeakEvent;
import cn.troph.tomon.core.network.socket.GatewayOp;
import cn.troph.tomon.core.structures.Guild;
import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.core.structures.GuildMember;
import cn.troph.tomon.core.structures.Speaking;
import cn.troph.tomon.core.structures.VoiceChannel;
import cn.troph.tomon.core.structures.VoiceConnectSend;
import cn.troph.tomon.core.structures.VoiceConnectStateReceive;
import cn.troph.tomon.core.structures.VoiceIdentify;
import cn.troph.tomon.core.structures.VoiceLeaveConnect;
import cn.troph.tomon.core.structures.VoiceUpdate;
import cn.troph.tomon.core.utils.Collection;
import cn.troph.tomon.ui.channel.ChannelGroupRV;
import cn.troph.tomon.ui.channel.ChannelListAdapter;
import cn.troph.tomon.ui.channel.ChannelRV;
import cn.troph.tomon.ui.chat.fragments.VoiceBottomSheet;
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel;
import cn.troph.tomon.ui.guild.GuildAvatarUtils;
import cn.troph.tomon.ui.widgets.TomonToast;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

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
    private Guild mCurrentGuild;
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
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void initAgoraEngine() {
        if (mRtcEngine == null) {
            try {
                mRtcEngine = RtcEngine.create(getContext(), getString(R.string.agora_app_id), mRtcEngineEventHandler);
                mRtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
                mRtcEngine.enableAudioVolumeIndication(200, 3, true);
            } catch (Exception e) {
                Log.e(TAG, "RtcEngine create error:", e);
                TomonToast.makeText(
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
        mIvGuildSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGuildSettings();
            }
        });
    }

    private void showGuildSettings() {
        if (mCurrentGuild == null) {
            return;
        }
        View viewBase = LayoutInflater.from(getContext()).inflate(R.layout.coordinator_guild_settings, null);
        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View windowBg = dialog.getWindow().findViewById(R.id.design_bottom_sheet);
        dialog.setContentView(viewBase);
        if (windowBg != null) {
            windowBg.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        View bottomSheetView = viewBase.findViewById(R.id.bottom_sheet_guild_settings);
        initGuildSettingsHeader(bottomSheetView);
        initNickNameButton(bottomSheetView);
        initLeaveGroupButton(dialog, bottomSheetView);
        initMuteButton(bottomSheetView);
        setPeekHeight(dialog, bottomSheetView);
        dialog.show();
    }

    private void initMuteButton(View bottomSheetView) {
        ConstraintLayout clMute = bottomSheetView.findViewById(R.id.cl_guild_mute);
        clMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setPeekHeight(BottomSheetDialog dialog, View bottomSheetView) {
        int marginBottom = getContext().getResources().getDimensionPixelSize(R.dimen.btn_leave_group_margin_bottom);
        int height = getContext().getResources().getDimensionPixelSize(R.dimen.btn_leave_group_height);
        int hideHeight = (int) (marginBottom + (3.0 * height) / 4);
        bottomSheetView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bottomSheetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int sheetHeight = bottomSheetView.getHeight();
                BottomSheetBehavior<FrameLayout> behavior = dialog.getBehavior();
                behavior.setPeekHeight(sheetHeight - hideHeight, true);
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            behavior.setHideable(true);
                            behavior.setSkipCollapsed(true);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
            }
        });
    }

    private void initNickNameButton(View bottomSheetView) {
        TextView tvNickName = bottomSheetView.findViewById(R.id.tv_guild_nickname);
        GuildMember meMember = mCurrentGuild.getMembers().get(Client.Companion.getGlobal().getMe().getId());
        tvNickName.setText(meMember.getDisplayName());
        ConstraintLayout clGuildNickName = bottomSheetView.findViewById(R.id.cl_guild_nickname);
        clGuildNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeNickNameDialog();
            }
        });
    }

    private void initLeaveGroupButton(BottomSheetDialog dialog, View bottomSheetView) {
        Button leaveGroup = bottomSheetView.findViewById(R.id.btn_leave_group);
        leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentGuild.getOwnerId().equals(Client.Companion.getGlobal().getMe().getId())) {
                    showOwnerDialog();
                } else {
                    showLeaveAlertDialog();
                }
                dialog.dismiss();
            }
        });
    }

    private void initGuildSettingsHeader(View bottomSheetView) {
        ImageView guildAvatar = bottomSheetView.findViewById(R.id.iv_guild_avatar);
        TextView textHolder = bottomSheetView.findViewById(R.id.tv_no_icon_text);
        GuildAvatarUtils.setGuildAvatar(guildAvatar, textHolder, mCurrentGuild);
        TextView guildName = bottomSheetView.findViewById(R.id.tv_guild_settings_guild_name);
        guildName.setText(mCurrentGuild.getName());
        TextView memberInfo = bottomSheetView.findViewById(R.id.tv_member_info);
        GuildMemberCollection members = mCurrentGuild.getMembers();
        memberInfo.setText(getString(R.string.guild_member_number, members.getSize()));
    }

    private void showChangeNickNameDialog() {
        TomonToast.makeText(getContext().getApplicationContext(), "没开发呢", Toast.LENGTH_SHORT).show();
    }

    private void showLeaveAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewBase = View.inflate(getContext(), R.layout.leave_alert_dialog, null);
        Button btnOk = viewBase.findViewById(R.id.btn_ok);
        Button btnCancel = viewBase.findViewById(R.id.btn_cancel);
        builder.setView(viewBase);
        AlertDialog dialog = builder.create();
        dialog.getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLeaveGroup(dialog);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showOwnerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewBase = View.inflate(getContext(), R.layout.owner_dialog, null);
        Button btnOk = viewBase.findViewById(R.id.btn_ok);
        builder.setView(viewBase);
        AlertDialog dialog = builder.create();
        dialog.getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void requestLeaveGroup(AlertDialog dialog) {
        Client.Companion.getGlobal().getRest().getGuildService().leaveGuild(
                mCurrentGuild.getId(),
                Client.Companion.getGlobal().getAuth())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Response<Integer>>() {
                    @Override
                    public void accept(Response<Integer> response) throws Throwable {
                        TomonToast.makeText(getContext().getApplicationContext(), "成功离开群组", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        TomonToast.makeText(getContext().getApplicationContext(), "请重试", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "error:", throwable);
                    }
                });
    }

    public void updateGuildBanner(String guildId) {
        if (TextUtils.isEmpty(guildId)) {
            guildId = Client.Companion.getGlobal().getGuilds().getList().get(0).getId();
        }
        Guild guild = Client.Companion.getGlobal().getGuilds().get(guildId);
        if (guild == null) {
            return;
        }
        mCurrentGuild = guild;
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
            mChannelListAdapter.setChatSharedVM(mChatVM);
            setOnVoiceChannelClickListener();
            registerObserver();
            mRvChannelList.setAdapter(mChannelListAdapter);
            connectVoiceChannelIfNeeded(guildId);
        } else {
            mChannelListAdapter.setDataAndNotifyChanged(mChannelTreeRoot, guild.getId());
        }
    }

    public void connectVoiceChannelIfNeeded(String guildId) {
        Guild guild = Client.Companion.getGlobal().getGuilds().get(guildId);
        if (guild == null) {
            return;
        }
        for (GuildChannel channel : guild.getChannels().clone()) {
            if (channel instanceof VoiceChannel && ((VoiceChannel) channel).isJoined()) {
                VoiceChannel voiceChannel = (VoiceChannel) channel;
                boolean isMeJoined = false;
                List<VoiceUpdate> voiceStates = voiceChannel.getVoiceStates();
                for (VoiceUpdate voiceUpdate : voiceStates) {
                    if (voiceUpdate.getUserId().equals(Client.Companion.getGlobal().getMe().getId())) {
                        isMeJoined = true;
                        break;
                    }
                }
                if (isMeJoined) {
                    mChannelListAdapter.getOnVoiceChannelClickListener().onVoiceChannelClick(voiceChannel);
                }
                break;
            }
        }
    }

    private void registerObserver() {
        mChatVM.getChannelSyncLD().observe(getViewLifecycleOwner(), new Observer<ChannelSyncEvent>() {
            @Override
            public void onChanged(ChannelSyncEvent channelSyncEvent) {
                Guild guild = channelSyncEvent.getGuild();
                if (guild == null) {
                    return;
                }
                String lastGuildId = null;
                if (mCurrentGuild != null) {
                    lastGuildId = mCurrentGuild.getId();
                }
                if (TextUtils.isEmpty(lastGuildId)) {
                    lastGuildId = getArguments().getString(GUILD_ID);
                }
                if (TextUtils.isEmpty(lastGuildId)) {
                    lastGuildId = Client.Companion.getGlobal().getGuilds().getList().get(0).getId();
                }
                String syncGuildId = guild.getId();
                if (syncGuildId.equals(lastGuildId)) {
                    updateGuildBanner(syncGuildId);
                }
            }
        });

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
        connectToVoiceChannel(mRtcEngineEventHandler.getCurrentVoiceChannel());
    }

    private void connectToVoiceChannel(VoiceChannel channel) {
        if (channel.isJoined() && mChatVM.getSelectedCurrentVoiceChannel().getValue() == null) {
            Client.Companion.getGlobal().getSocket().send(GatewayOp.VOICE, new Gson().toJsonTree(new VoiceLeaveConnect()));
            Client.Companion.getGlobal().getVoiceSocket().open();
        }
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
        TomonToast.makeText(
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
                            new Gson().toJsonTree(new Speaking(speaking, Client.Companion.getGlobal().getMe().getId())));
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
                    if (mWakeLock.isHeld()) {
                        mWakeLock.release();
                    }
                    Sensey.getInstance().stopProximityDetection(mProximityListener);
                }
            });

        }

        @Override
        public void onJoinChannelSuccess(String s, int i, int i1) {
            super.onJoinChannelSuccess(s, i, i1);
            if (selectedVoiceChannel == null) {
                return;
            }
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
