package cn.troph.tomon.ui.activities;

import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.Client;
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

    private void initViewModel() {
        mChatVM = new ViewModelProvider(this).get(ChatSharedViewModel.class);
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
                if (mAdapter == null) {
                    mAdapter = new GuildListAdapter(guilds);
                    mGuildListRecyclerView.setAdapter(mAdapter);
                } else {
                    mAdapter.setDataAndNotifyChanged(guilds);
                }
            }
        });
        mChatVM.loadGuildList();
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