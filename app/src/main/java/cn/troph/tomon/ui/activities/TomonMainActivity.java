package cn.troph.tomon.ui.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.Client;
import cn.troph.tomon.core.actions.Position;
import cn.troph.tomon.core.events.GuildCreateEvent;
import cn.troph.tomon.core.events.GuildDeleteEvent;
import cn.troph.tomon.core.events.GuildPositionEvent;
import cn.troph.tomon.core.events.GuildUpdateEvent;
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
import cn.troph.tomon.core.utils.KeyboardUtils;
import cn.troph.tomon.core.utils.Url;
import cn.troph.tomon.ui.chat.fragments.DmChannelSelectorFragment;
import cn.troph.tomon.ui.chat.fragments.Invite;
import cn.troph.tomon.ui.chat.fragments.MeFragment;
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel;
import cn.troph.tomon.ui.fragments.ChannelListFragment;
import cn.troph.tomon.ui.fragments.TomonMainPagerAdapter;
import cn.troph.tomon.ui.guild.GuildListAdapter;
import cn.troph.tomon.ui.utils.AppUtils;
import cn.troph.tomon.ui.utils.GuildUtils;
import cn.troph.tomon.ui.widgets.TomonDrawerLayout;
import cn.troph.tomon.ui.widgets.TomonTabButton;
import cn.troph.tomon.ui.widgets.TomonToast;
import cn.troph.tomon.ui.widgets.UserAvatar;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TomonMainActivity extends BaseActivity implements TomonMainPagerAdapter.FragmentSupplier {

    private static final String TAG = "TomonMainActivity";
    public static final int NUM_PAGER = 3;
    public static final int POS_CHANNEL_LIST = 0;
    public static final int POS_DM_LIST = 1;
    public static final int POS_ME = 2;

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
    private UserAvatar mAvatar;
    private RelativeLayout mMyStatus;
    private EditText mEtSearchBar;
    private ViewPager2 mVpFragments;
    private TomonMainPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this).reset().init();
        ImmersionBar.with(this).navigationBarColor(R.color.channel_list_tab_bg).init();
        setContentView(R.layout.activity_tomon_main);
        initViewModel();
        initGuildListAndRegistObserver();
        initGuildEmoji();
        initTab();
        initChannelList(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AppUtils.isFirstRun(TomonMainActivity.this) &&
                Client.Companion.getGlobal().getGuilds().getSize() > 0) {
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.openDrawer(Gravity.LEFT, true);
                    AppUtils.setIsFirstRun(TomonMainActivity.this, false);
                }
            });
        }
    }

    @SuppressLint("WrongConstant")
    private void initChannelList(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return;
        }
        mVpFragments = findViewById(R.id.vp_fragment_container);
        mPagerAdapter = new TomonMainPagerAdapter(this, this);
        mVpFragments.setAdapter(mPagerAdapter);
        mVpFragments.setOffscreenPageLimit(NUM_PAGER);
        mVpFragments.setUserInputEnabled(false);
        mVpFragments.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case POS_CHANNEL_LIST:
                        ImmersionBar.with(TomonMainActivity.this).reset().init();
                        ImmersionBar.with(TomonMainActivity.this).navigationBarColor(R.color.channel_list_tab_bg).init();
                        break;
                    case POS_DM_LIST:
                        ImmersionBar.with(TomonMainActivity.this).statusBarColor(R.color.blackPrimary, 0.0f).navigationBarColor(R.color.channel_list_tab_bg).fitsSystemWindows(true).init();
                        break;
                    case POS_ME:
                        ImmersionBar.with(TomonMainActivity.this).statusBarColor(R.color.blackPrimary, 0.2f).navigationBarColor(R.color.channel_list_tab_bg).fitsSystemWindows(true).init();
                        break;
                }
            }
        });
        mVpFragments.setCurrentItem(POS_CHANNEL_LIST, false);
    }

    private void initTab() {
        mTabBtnChannel = findViewById(R.id.tab_channel_btn);
        mTabBtnDm = findViewById(R.id.tab_dm_btn);
        initMeTabViewAndRegistObserver();
        setChannelSelected();
        mTabBtnChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectChannelListTab();
            }
        });
        mTabBtnDm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDmListTab();
            }
        });
        mTabRlMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMeTab();
            }
        });
    }

    private void selectMeTab() {
        setMeSelected();
        mVpFragments.setCurrentItem(POS_ME, false);
    }

    private void selectDmListTab() {
        setDmSelected();
        mVpFragments.setCurrentItem(POS_DM_LIST, false);
    }

    private void selectChannelListTab() {
        setChannelSelected();
        mVpFragments.setCurrentItem(POS_CHANNEL_LIST, false);
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
        mAvatar.setUser(me);
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

    public ChatSharedViewModel getActivityViewModel() {
        return mChatVM;
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
        initSearchBar();
        initJoinGuildView();
        initRecyclerView();
        observeGuildList();
        observeGuildCreated();
        observeGuildPosition();
        observeGuildUpdate();
        observeGuildDeleted();
        observeMessageCreate();
        observeMessageRead();
        observeMessageAtMe();
        observeMessageDelete();
        observeMessageUpdate();
        observeDmUnread();
        mChatVM.loadGuildList();
    }

    private void observeGuildUpdate() {
        mChatVM.getGuildUpdateLD().observe(this, new Observer<GuildUpdateEvent>() {
            @Override
            public void onChanged(GuildUpdateEvent guildUpdateEvent) {
                mAdapter.setDataAndNotifyChanged(Client.Companion.getGlobal().getGuilds().getList().toList(),
                        mEtSearchBar.getText().toString().trim());
            }
        });
    }

    private void initSearchBar() {
        mEtSearchBar = findViewById(R.id.et_guild_list_search);
        mEtSearchBar.clearFocus();
        mEtSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapter != null) {
                    mAdapter.setDataAndNotifyChanged(Client.Companion.getGlobal().getGuilds().getList().toList(),
                            mEtSearchBar.getText().toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mDrawerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.hideKeyBoard(TomonMainActivity.this);
                return false;
            }
        });
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
                if (Client.Companion.getGlobal().getGuilds().getSize() == 0) {
                    showEmptyGuildListView();
                    return;
                } else {
                    enableDrawerSlide();
                }
                mAdapter.setDataAndNotifyChanged(Client.Companion.getGlobal().getGuilds().getList().toList(),
                        mEtSearchBar.getText().toString().trim());
                if (mAdapter.getCurrentGuildId().equals(guildDeleteEvent.getGuild().getId())) {
                    Guild guild = mAdapter.getGuildList().get(0);
                    mAdapter.setCurrentGuildId(guild.getId());
                    updateGuildChannelList(guild.getId());
                }

            }
        });
    }

    private void enableDrawerSlide() {
        mDrawerLayout.setDrawerEnabledInTouch(Gravity.START, true);
        mDrawerLayout.setDrawerEnabledInTouch(Gravity.END, true);
    }

    private void disableDrawerSlide() {
        mDrawerLayout.setDrawerEnabledInTouch(Gravity.START, false);
        mDrawerLayout.setDrawerEnabledInTouch(Gravity.END, false);
    }

    private void showEmptyGuildListView() {
        ChannelListFragment channelListFragment = (ChannelListFragment) mPagerAdapter.getFragment(POS_CHANNEL_LIST);
        showEmptyGuildListView(channelListFragment);
    }

    private void showEmptyGuildListView(ChannelListFragment channelListFragment) {
        mDrawerLayout.closeDrawer(true);
        disableDrawerSlide();
        if (channelListFragment != null) {
            channelListFragment.showEmptyGuildsView();
            channelListFragment.setOnJoinGuildClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showJoinGuildBottomSheet();
                }
            });
        }
    }

    private void observeGuildCreated() {
        mChatVM.getGuildCreateLD().observe(this, new Observer<GuildCreateEvent>() {
            @Override
            public void onChanged(GuildCreateEvent guildCreateEvent) {
                if (mAdapter == null) {
                    return;
                }

                if (mAdapter.isInviting()) {
                    mAdapter.setCurrentGuildId(guildCreateEvent.getGuild().getId());
                }
                if (!mDrawerLayout.isDrawerSlidable(Gravity.START)) {
                    enableDrawerSlide();
                    ChannelListFragment channelListFragment = (ChannelListFragment) mPagerAdapter.getFragment(POS_CHANNEL_LIST);
                    if (channelListFragment != null) {
                        channelListFragment.hideEmptyGuildsView();
                        mAdapter.setCurrentGuildId(guildCreateEvent.getGuild().getId());
                        updateGuildChannelList(guildCreateEvent.getGuild().getId());
                    }
                }
                mAdapter.setDataAndNotifyChanged(
                        Client.Companion.getGlobal().getGuilds().getList().toList(),
                        mEtSearchBar.getText().toString().trim());
                if (mAdapter.isInviting()) {
                    mAdapter.setIsInviting(false);
                    mGuildListRecyclerView.scrollToPosition(
                            mAdapter.getGuildList().indexOf(guildCreateEvent.getGuild()));
                    updateGuildChannelList(guildCreateEvent.getGuild().getId());
                    if (AppUtils.isFirstRun(TomonMainActivity.this)) {
                        mDrawerLayout.openDrawer(Gravity.LEFT, true);
                        AppUtils.setIsFirstRun(TomonMainActivity.this, false);
                    } else {
                        mDrawerLayout.closeDrawer(true);
                    }
                }
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
                mAdapter.setDataAndNotifyChanged(rearrangedGuildList, mEtSearchBar.getText().toString().trim());
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
                List<Guild> guildList = Client.Companion.getGlobal().getGuilds().getList().toList();
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
                List<Guild> guildList = Client.Companion.getGlobal().getGuilds().getList().toList();
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
                List<Guild> guildList = Client.Companion.getGlobal().getGuilds().getList().toList();
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
                List<Guild> guildList = Client.Companion.getGlobal().getGuilds().getList().toList();
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
                List<Guild> guildList = Client.Companion.getGlobal().getGuilds().getList().toList();
                Guild msgFromGuild = msgCreateEv.getMessage().getGuild();
                if (guildList.contains(msgFromGuild)) {
                    if (
                            msgFromGuild.updateUnread()
                            &&
                            !msgCreateEv.getMessage().getAuthorId().equals(getMyId())
                    ) {
                        mAdapter.notifyItemChanged(guildList.indexOf(msgFromGuild), new Object());
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
                    mAdapter = new GuildListAdapter(guilds, mEtSearchBar.getText().toString().trim());
                    mAdapter.setCurrentGuildId(lastGuildId);
                    mGuildListRecyclerView.setAdapter(mAdapter);
                    mAdapter.setOnItemClickListener(new GuildListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, Guild guild) {
                            KeyboardUtils.hideKeyBoard(TomonMainActivity.this);
                            mAdapter.setCurrentGuildId(guild.getId());
                            mAdapter.notifyDataSetChanged();
                            mDrawerLayout.closeDrawer(true);
                            updateGuildChannelList(guild.getId());
                            selectChannelListTab();
                        }
                    });
                } else {
                    mAdapter.setDataAndNotifyChanged(guilds, mEtSearchBar.getText().toString().trim());
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

    private MeFragment showMeFragment() {
        MeFragment mefragment = (MeFragment) mPagerAdapter.getFragment(POS_ME);
        if (mefragment == null) {
            mefragment = new MeFragment();
        }
        return mefragment;
    }

    private DmChannelSelectorFragment showDmFragment() {
        DmChannelSelectorFragment dmfragment = (DmChannelSelectorFragment) mPagerAdapter.getFragment(POS_DM_LIST);
        if (dmfragment == null) {
            dmfragment = new DmChannelSelectorFragment();
        }
        return dmfragment;

    }

    private ChannelListFragment createGuildChannelList(String guildId) {
        ChannelListFragment channelListFragment = new ChannelListFragment();
        Bundle extraData = new Bundle();
        extraData.putString(ChannelListFragment.GUILD_ID, guildId);
        channelListFragment.setArguments(extraData);
        saveLastGuildId(guildId);
        if (Client.Companion.getGlobal().getGuilds().getSize() == 0) {
            showEmptyGuildListView();
        }
        return channelListFragment;
    }

    private void updateGuildChannelList(String guildId) {
        ChannelListFragment channelListFragment = (ChannelListFragment) mPagerAdapter.getFragment(POS_CHANNEL_LIST);
        channelListFragment.updateWholePage(guildId);
        if (Client.Companion.getGlobal().getGuilds().getSize() == 0) {
            showEmptyGuildListView();
            saveLastGuildId("");
        } else {
            saveLastGuildId(guildId);
        }
    }

    private void initJoinGuildView() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mJoinGuild = findViewById(R.id.iv_join_guild);
        RelativeLayout.LayoutParams rlLp = (RelativeLayout.LayoutParams) mJoinGuild.getLayoutParams();
        rlLp.width = (int) (metrics.widthPixels * 0.17);
        rlLp.height = (int) (metrics.widthPixels * 0.17);
        mJoinGuild.setLayoutParams(rlLp);
        mJoinGuild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJoinGuildBottomSheet();
            }
        });
    }

    public void showJoinGuildBottomSheet() {
        View viewBase = LayoutInflater.from(this).inflate(R.layout.coordinator_join_guild, null);
        View bottomSheetView = viewBase.findViewById(R.id.bottom_sheet_join_guild);
        EditText etLink = bottomSheetView.findViewById(R.id.bs_textfield);
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        dialog.setContentView(viewBase);
        View windowBg = dialog.getWindow().findViewById(R.id.design_bottom_sheet);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (windowBg != null) {
            windowBg.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        bottomSheetView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.clearFocus();
                etLink.requestFocus();
                dialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inviteCode = etLink.getText().toString();
                if (inviteCode.matches("[A-Za-z0-9]{6}")) {
                } else if (inviteCode.contains(Url.inviteUrl)) {
                    inviteCode = Url.INSTANCE.parseInviteCode(inviteCode);
                } else {
                    TomonToast.makeErrorText(getApplicationContext(),
                            getString(R.string.invalid_invite),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String finalInviteCode = inviteCode;
                Client.Companion.getGlobal().getGuilds().fetchInvite(inviteCode)
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Invite>() {
                    @Override
                    public void accept(Invite invite) throws Throwable {
                        checkAndAcceptInvite(invite, etLink, finalInviteCode, dialog);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        TomonToast.makeErrorText(getApplicationContext(),
                                getString(R.string.invalid_invite),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogI) {
                etLink.requestFocus();
                KeyboardUtils.hideKeyBoard(TomonMainActivity.this);
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogI) {
                KeyboardUtils.showKeyBoard(etLink, TomonMainActivity.this);
            }
        });

        dialog.show();

    }

    private void checkAndAcceptInvite(Invite invite, EditText etLink, String inviteCode,
                                      BottomSheetDialog dialog) {
        if (invite == null) {
            return;
        }
        if (invite.getJoined()) {
            TomonToast.makeText(
                    getApplicationContext(),
                    getString(R.string.guild_already_joined),
                    Toast.LENGTH_LONG
            ).show();
            etLink.setText("");
            return;
        }
        Client.Companion.getGlobal().getGuilds().join(inviteCode)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Guild>() {
            @Override
            public void accept(Guild guild) throws Throwable {
                dialog.dismiss();
                TomonToast.makeText(getApplicationContext(),
                        getString(R.string.guild_joined_success),
                        Toast.LENGTH_LONG).show();
                KeyboardUtils.hideKeyBoard(TomonMainActivity.this);
                mAdapter.setIsInviting(true);
                for (Guild each : mAdapter.getGuildList()) {
                    if (each.getId().equals(guild.getId())) {
                        mAdapter.setIsInviting(false);
                        mAdapter.setCurrentGuildId(guild.getId());
                        mAdapter.setDataAndNotifyChanged(Client.Companion.getGlobal().getGuilds().getList().toList(),
                                mEtSearchBar.getText().toString().trim());
                        mGuildListRecyclerView.scrollToPosition(
                                mAdapter.getGuildList().indexOf(each));
                        updateGuildChannelList(guild.getId());
                        if (AppUtils.isFirstRun(TomonMainActivity.this)) {
                            mDrawerLayout.openDrawer(Gravity.LEFT, true);
                            AppUtils.setIsFirstRun(TomonMainActivity.this, false);
                        } else {
                            mDrawerLayout.closeDrawer(true);
                        }
                        break;
                    }
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Throwable {
                Log.d(TAG, "join error:", throwable);
            }
        });
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Fragment getFragmentByPosition(int pos) {
        Fragment toReturn = null;
        if (pos == POS_CHANNEL_LIST) {
            toReturn = createGuildChannelList(getLastGuildId());
        } else if (pos == POS_DM_LIST) {
            toReturn = showDmFragment();
        } else if (pos == POS_ME) {
            toReturn = showMeFragment();
        } else {
            throw new RuntimeException("Your pager number is:" + NUM_PAGER + ", current pos is:" + pos);
        }
        return toReturn;
    }

    @Override
    public int getFragmentNum() {
        return NUM_PAGER;
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(Gravity.LEFT, true);
    }
}