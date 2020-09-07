package cn.troph.tomon.ui.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.Client;
import cn.troph.tomon.core.actions.Position;
import cn.troph.tomon.core.events.GuildCreateEvent;
import cn.troph.tomon.core.events.GuildDeleteEvent;
import cn.troph.tomon.core.events.GuildPositionEvent;
import cn.troph.tomon.core.events.MessageAtMeEvent;
import cn.troph.tomon.core.events.MessageCreateEvent;
import cn.troph.tomon.core.events.MessageDeleteEvent;
import cn.troph.tomon.core.events.MessageReadEvent;
import cn.troph.tomon.core.events.MessageUpdateEvent;
import cn.troph.tomon.core.events.PresenceUpdateEvent;
import cn.troph.tomon.core.structures.Guild;
import cn.troph.tomon.core.structures.Me;
import cn.troph.tomon.core.structures.Presence;
import cn.troph.tomon.core.structures.StampPack;
import cn.troph.tomon.core.utils.Assets;
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel;
import cn.troph.tomon.ui.fragments.ChannelListFragment;
import cn.troph.tomon.ui.guild.GuildListAdapter;
import cn.troph.tomon.ui.utils.GuildUtils;
import cn.troph.tomon.ui.widgets.TomonDrawerLayout;
import cn.troph.tomon.ui.widgets.TomonTabButton;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TomonMainActivity extends BaseActivity {

    private ChatSharedViewModel mChatVM;
    private RecyclerView mGuildListRecyclerView;
    private GuildListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TomonDrawerLayout mDrawerLayout;
    private ImageView mJoinGuild;
    private TomonTabButton mTabBtnChannel;
    private TomonTabButton mTabBtnDm;
    private RelativeLayout mTabRlMe;
    private ImageView mMeSelectedRing;
    private ImageView mAvatar;
    private RelativeLayout mMyStatus;
    private String mCurrentFragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomon_main);
        initViewModel();
        initGuildListAndRegistObserver();
        initGuildEmoji();
        initTab();
        initChannelList(savedInstanceState);
    }

    private void initChannelList(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return;
        }
        showGuildChannelList(getLastGuildId());
    }

    private void initTab() {
        mTabBtnChannel = findViewById(R.id.tab_channel_btn);
        mTabBtnDm = findViewById(R.id.tab_dm_btn);
        initMeTabViewAndRegistObserver();
        setChannelSelected();
        mTabBtnChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChannelSelected();
            }
        });
        mTabBtnDm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDmSelected();
            }
        });
        mTabRlMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMeSelected();
            }
        });
    }

    private void setMeSelected() {
        mTabBtnChannel.setSelected(false);
        mTabBtnDm.setSelected(false);
        tabBtnMeSetSelected(true);
    }

    private void setDmSelected() {
        mTabBtnChannel.setSelected(false);
        mTabBtnDm.setSelected(true);
        tabBtnMeSetSelected(false);
    }

    private void setChannelSelected() {
        mTabBtnChannel.setSelected(true);
        mTabBtnDm.setSelected(false);
        tabBtnMeSetSelected(false);
    }

    private void tabBtnMeSetSelected(boolean selected) {
        if (selected) {
            mMeSelectedRing.setVisibility(View.VISIBLE);
        } else {
            mMeSelectedRing.setVisibility(View.INVISIBLE);
        }
    }

    private void initMeTabViewAndRegistObserver() {
        mTabRlMe = findViewById(R.id.rl_tab_me);
        mAvatar = mTabRlMe.findViewById(R.id.iv_avatar);
        mMyStatus = mTabRlMe.findViewById(R.id.rl_my_status);
        mMeSelectedRing = mTabRlMe.findViewById(R.id.iv_ring);
        observePresenceUpdate();
        updateMeStatus();
    }

    private void updateMeStatus() {
        Me me = Client.Companion.getGlobal().getMe();
        Glide.with(mAvatar).load(me.getAvatarURL())
                .transform(new CenterCrop(), new CircleCrop())
                .into(mAvatar);
        Presence myPresence = Client.Companion.getGlobal().getPresences().get(me.getId());
        if (myPresence == null) {
            mMyStatus.setVisibility(View.INVISIBLE);
            return;
        }
        String myStatus = myPresence.getStatus();
        if ("online".equals(myStatus)) {
            mMyStatus.setVisibility(View.VISIBLE);
        } else {
            mMyStatus.setVisibility(View.INVISIBLE);
        }
    }

    private String getLastGuildId() {
        return GuildUtils.getLastGuildId(this);
    }

    private void saveLastGuildId(String id) {
        GuildUtils.saveLastGuildId(this, id);
    }

    private void initViewModel() {
        mChatVM = new ViewModelProvider(this).get(ChatSharedViewModel.class);
        mChatVM.setUpEvents();
    }

    private void initGuildEmoji() {
        Client.Companion.getGlobal().getRest().getGuildEmojiService().fetchStampPack(
                Assets.defaultStampPackId,
                Client.Companion.getGlobal().getAuth()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<JsonObject>() {
                    @Override
                    public void accept(JsonObject jsonObject) throws Throwable {
                        Client.Companion.getGlobal().getStamps()
                                .add(new Gson().fromJson(jsonObject, StampPack.class));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        com.orhanobut.logger.Logger.d(throwable.getMessage());
                    }
                });
    }

    private void initGuildListAndRegistObserver() {
        initSlidePaneLayout();
        initJoinGuildView();
        initRecyclerView();
        observeGuildList();
        observeGuildCreated();
        observeGuildPosition();
        observeGuildDeleted();
        observeMessageCreate();
        observeMessageRead();
        observeMessageAtMe();
        observeMessageDelete();
        observeMessageUpdate();
        observeDmUnread();
        mChatVM.loadGuildList();
    }

    private void observePresenceUpdate() {
        mChatVM.getPresenceUpdateLV().observe(this, new Observer<PresenceUpdateEvent>() {
            @Override
            public void onChanged(PresenceUpdateEvent presenceUpdateEvent) {
                updateMeStatus();
            }
        });
    }

    private void observeDmUnread() {
        mChatVM.getDmUnReadLiveData().observe(this, new Observer<HashMap<String, Integer>>() {
            @Override
            public void onChanged(HashMap<String, Integer> data) {
                //TODO update dm unreader dot
            }
        });
    }

    private void observeGuildDeleted() {
        mChatVM.getGuildDeleteLD().observe(this, new Observer<GuildDeleteEvent>() {
            @Override
            public void onChanged(GuildDeleteEvent guildDeleteEvent) {
                if (mAdapter == null) {
                    return;
                }
                List<Guild> guildList = mAdapter.getGuildList();
                List<Guild> toDeleteGuilds = new ArrayList<>();
                for (Guild g : guildList) {
                    if (g.getId().equals(guildDeleteEvent.getGuild().getId())) {
                        toDeleteGuilds.add(g);
                    }
                }
                if (toDeleteGuilds.size() > 0) {
                    mAdapter.getGuildList().removeAll(toDeleteGuilds);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void observeGuildCreated() {
        mChatVM.getGuildCreateLD().observe(this, new Observer<GuildCreateEvent>() {
            @Override
            public void onChanged(GuildCreateEvent guildCreateEvent) {
                if (mAdapter == null) {
                    return;
                }
                mAdapter.getGuildList().add(guildCreateEvent.getGuild());
                mAdapter.notifyItemInserted(mAdapter.getGuildList().size() - 1);
            }
        });
    }

    private void observeGuildPosition() {
        mChatVM.getGuildPositionLD().observe(this, new Observer<GuildPositionEvent>() {
            @Override
            public void onChanged(GuildPositionEvent guildPositionEvent) {
                if (mAdapter == null) {
                    return;
                }
                ArrayList<Guild> rearrangedGuildList = new ArrayList<>();
                for (Position position : guildPositionEvent.getGuilds()) {
                    Guild newGuild = null;
                    for (Guild g : mAdapter.getGuildList()) {
                        if (g.getId().equals(position.getId())) {
                            newGuild = g;
                            break;
                        }
                    }
                    if (newGuild != null) {
                        rearrangedGuildList.add(position.getPosition(), newGuild);
                    }
                }
                mAdapter.setDataAndNotifyChanged(rearrangedGuildList);
            }
        });
    }

    private void observeMessageUpdate() {
        mChatVM.getMessageUpdateLD().observe(this, new Observer<MessageUpdateEvent>() {
            @Override
            public void onChanged(MessageUpdateEvent msgUpdateEv) {
                if (mAdapter == null) {
                    return;
                }
                Guild guildFromUpdate = msgUpdateEv.getMessage().getGuild();
                List<Guild> guildList = mChatVM.getGuildListLiveData().getValue();
                if (guildList.contains(guildFromUpdate)) {
                    if (guildFromUpdate.updateMention()) {
                        mAdapter.notifyItemChanged(guildList.indexOf(guildFromUpdate));
                    }
                }
            }
        });
    }

    private void observeMessageDelete() {
        mChatVM.getMessageDeleteLD().observe(this, new Observer<MessageDeleteEvent>() {
            @Override
            public void onChanged(MessageDeleteEvent msgDeleteEv) {
                if (mAdapter == null) {
                    return;
                }
                Guild guildFromAtMe = msgDeleteEv.getMessage().getGuild();
                List<Guild> guildList = mChatVM.getGuildListLiveData().getValue();
                if (guildList.contains(guildFromAtMe)) {
                    if (guildFromAtMe.updateUnread()) {
                        mAdapter.notifyItemChanged(guildList.indexOf(guildFromAtMe));
                    }
                }
            }
        });
    }

    private void observeMessageAtMe() {
        mChatVM.getMessageAtMeLD().observe(this, new Observer<MessageAtMeEvent>() {
            @Override
            public void onChanged(MessageAtMeEvent msgAtMeEv) {
                if (mAdapter == null) {
                    return;
                }
                Guild guildFromAtMe = msgAtMeEv.getMessage().getGuild();
                List<Guild> guildList = mChatVM.getGuildListLiveData().getValue();
                if (guildList.contains(guildFromAtMe)) {
                    if (guildFromAtMe.updateMention() && !msgAtMeEv.getMessage().getAuthorId().equals(getMyId())) {
                        mAdapter.notifyItemChanged(guildList.indexOf(guildFromAtMe));
                    }
                }
            }
        });
    }

    private void observeMessageRead() {
        mChatVM.getMessageReadLD().observe(this, new Observer<MessageReadEvent>() {
            @Override
            public void onChanged(MessageReadEvent msgReadEv) {
                if (mAdapter == null) {
                    return;
                }
                List<Guild> guildList = mChatVM.getGuildListLiveData().getValue();
                Guild msgFromRead = msgReadEv.getMessage().getGuild();
                if (guildList.contains(msgFromRead)) {
                    if (!msgFromRead.updateUnread() || msgFromRead.updateMention()){
                        mAdapter.notifyItemChanged(guildList.indexOf(msgFromRead));
                    }
                }
            }
        });
    }

    private void observeMessageCreate() {
        mChatVM.getMessageCreateLD().observe(this, new Observer<MessageCreateEvent>() {
            @Override
            public void onChanged(MessageCreateEvent msgCreateEv) {
                if (mAdapter == null) {
                    return;
                }
                List<Guild> guildList = mChatVM.getGuildListLiveData().getValue();
                Guild msgFromGuild = msgCreateEv.getMessage().getGuild();
                if (guildList.contains(msgFromGuild)) {
                    if (
                            msgFromGuild.updateUnread()
                            &&
                            !msgCreateEv.getMessage().getAuthorId().equals(getMyId())
                    ) {
                        mAdapter.notifyItemChanged(guildList.indexOf(msgFromGuild));
                    }
                }
            }
        });
    }

    private void observeGuildList() {
        mChatVM.getGuildListLiveData().observe(this, new Observer<List<Guild>>() {
            @Override
            public void onChanged(List<Guild> guilds) {
                String lastGuildId = getLastGuildId();
                if (mAdapter == null) {
                    mAdapter = new GuildListAdapter(guilds);
                    mAdapter.setCurrentGuildId(lastGuildId);
                    mGuildListRecyclerView.setAdapter(mAdapter);
                    mAdapter.setOnItemClickListener(new GuildListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, Guild guild) {
                            mAdapter.setCurrentGuildId(guild.getId());
                            mAdapter.notifyDataSetChanged();
                            mDrawerLayout.closeDrawer(true);
                            showGuildChannelList(guild.getId());
                        }
                    });
                } else {
                    mAdapter.setDataAndNotifyChanged(guilds);
                }
                int index = 0;
                for (Guild g : mAdapter.getGuildList()) {
                    if (g.getId().equals(lastGuildId)) {
                        mGuildListRecyclerView.scrollToPosition(index);
                        break;
                    } else {
                        index++;
                    }
                }
            }
        });
    }

    private void showGuildChannelList(String guildId) {
        ChannelListFragment channelListFragment = (ChannelListFragment) getSupportFragmentManager().findFragmentByTag(ChannelListFragment.TAG);
        if (channelListFragment == null) {
            channelListFragment = new ChannelListFragment();
        }
        Bundle extraData = new Bundle();
        extraData.putString(ChannelListFragment.GUILD_ID, guildId);

        if (ChannelListFragment.TAG.equals(mCurrentFragmentTag)) {
            channelListFragment.updateGuildBanner(guildId);
        } else {
            channelListFragment.setArguments(extraData);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, channelListFragment, ChannelListFragment.TAG)
                    .commit();
        }
        mCurrentFragmentTag = ChannelListFragment.TAG;
        saveLastGuildId(guildId);
    }

    private void initJoinGuildView() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mJoinGuild = findViewById(R.id.iv_join_guild);
        RelativeLayout.LayoutParams rlLp = (RelativeLayout.LayoutParams) mJoinGuild.getLayoutParams();
        rlLp.width = (int) (metrics.widthPixels * 0.17);
        rlLp.height = (int) (metrics.widthPixels * 0.17);
        mJoinGuild.setLayoutParams(rlLp);
    }

    private String getMyId () {
        return Client.Companion.getGlobal().getMe().getId();
    }

    private void initRecyclerView() {
        mGuildListRecyclerView = findViewById(R.id.rv_guild_list);
        mLayoutManager = new LinearLayoutManager(this);
        mGuildListRecyclerView.setLayoutManager(mLayoutManager);
        ObjectAnimator dismissAnim = ObjectAnimator.ofFloat(mJoinGuild, "alpha", 0);
        dismissAnim.setDuration(200);
        ObjectAnimator occurAnim = ObjectAnimator.ofFloat(mJoinGuild, "alpha", 1);
        occurAnim.setDuration(200);
        mGuildListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING
                        || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    if (!dismissAnim.isRunning()) {
                        dismissAnim.start();
                    }
                } else {
                    if (!occurAnim.isRunning()) {
                        occurAnim.start();
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }


    private void initSlidePaneLayout() {
        mDrawerLayout = findViewById(R.id.channel_list_drawerlayout);
        mDrawerLayout.setStartDrawerWidthPercent(0.8f);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDrawerLayout.setContentSensitiveEdgeSize(metrics.widthPixels);
        mDrawerLayout.setContentFadeColor(getColor(R.color.black_50));
    }


}