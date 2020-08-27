package cn.troph.tomon.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.Client;
import cn.troph.tomon.core.actions.Position;
import cn.troph.tomon.core.events.Event;
import cn.troph.tomon.core.events.GuildCreateEvent;
import cn.troph.tomon.core.events.GuildDeleteEvent;
import cn.troph.tomon.core.events.GuildPositionEvent;
import cn.troph.tomon.core.events.MessageAtMeEvent;
import cn.troph.tomon.core.events.MessageCreateEvent;
import cn.troph.tomon.core.events.MessageDeleteEvent;
import cn.troph.tomon.core.events.MessageReadEvent;
import cn.troph.tomon.core.events.MessageUpdateEvent;
import cn.troph.tomon.core.structures.Channel;
import cn.troph.tomon.core.structures.Guild;
import cn.troph.tomon.core.structures.StampPack;
import cn.troph.tomon.core.utils.Assets;
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel;
import cn.troph.tomon.ui.guild.GuildListAdapter;
import cn.troph.tomon.ui.widgets.TomonDrawerLayout;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChannelListActivity extends BaseActivity {

    public static final String NO_GUILD_ID = "";

    public static final String SP_NAME_CHANNEL_LIST_CONFIG = "channel_list_config";
    public static final String SP_KEY_GUILD_ID = "guild_id";
    private Channel mCurrentChannel;
    private ChatSharedViewModel mChatVM;
    private RecyclerView mGuildListRecyclerView;
    private GuildListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TomonDrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);
        initViewModel();
        initGuildList();
        initGuildEmoji();
    }

    private String getLastGuildId() {
        SharedPreferences spChannelListConfig = getSharedPreferences(
                SP_NAME_CHANNEL_LIST_CONFIG, MODE_PRIVATE);
        return spChannelListConfig.getString(SP_KEY_GUILD_ID, NO_GUILD_ID);
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

    private void initGuildList() {
        initSlidePaneLayout();
        initRecyclerView();
        mChatVM.getGuildListLiveData().observe(this, new Observer<List<Guild>>() {
            @Override
            public void onChanged(List<Guild> guilds) {
                String lastGuildId = getLastGuildId();
                if (mAdapter == null) {
                    mAdapter = new GuildListAdapter(guilds);
                    mAdapter.setCurrentGuildId(lastGuildId);
                    mGuildListRecyclerView.setAdapter(mAdapter);
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

        mChatVM.getDmUnReadLiveData().observe(this, new Observer<HashMap<String, Integer>>() {
            @Override
            public void onChanged(HashMap<String, Integer> data) {
                //TODO update dm unreader dot
            }
        });
        mChatVM.loadGuildList();
    }

    private String getMyId () {
        return Client.Companion.getGlobal().getMe().getId();
    }

    private void initRecyclerView() {
        mGuildListRecyclerView = findViewById(R.id.rv_guild_list);
        mLayoutManager = new LinearLayoutManager(this);
        mGuildListRecyclerView.setLayoutManager(mLayoutManager);
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