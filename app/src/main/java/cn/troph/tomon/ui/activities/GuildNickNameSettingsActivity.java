package cn.troph.tomon.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import cn.troph.tomon.R;
import cn.troph.tomon.core.Client;
import cn.troph.tomon.core.network.services.GuildService;
import cn.troph.tomon.core.structures.Guild;
import cn.troph.tomon.core.utils.KeyboardUtils;
import cn.troph.tomon.ui.guild.GuildAvatarUtils;
import cn.troph.tomon.ui.widgets.TomonToast;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GuildNickNameSettingsActivity extends BaseActivity {

    public static final String KEY_GUILD_ID = "guild_id";

    private ImageView mIvBack;
    private TextView mTvGuildName;
    private ImageView mGuildAvatar;
    private EditText mEtGuildNickName;
    private Button mBtnChangeNickName;
    private TextView mGuildAvatarHolder;
    private ImageView mIvClear;
    private String mOrigNickName;
    private Guild mCurrentGuild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guild_nick_name_settings);
        String guildId = getIntent().getStringExtra(KEY_GUILD_ID);
        if (TextUtils.isEmpty(guildId)) {
            throw new IllegalArgumentException("You must transfer a guild id.");
        }
        mCurrentGuild = Client.Companion.getGlobal().getGuilds().get(guildId);
        initView();
    }

    private void initView() {
        mIvBack = findViewById(R.id.iv_back);
        mTvGuildName = findViewById(R.id.tv_guild_name);
        mGuildAvatar = findViewById(R.id.iv_guild_avatar);
        mEtGuildNickName = findViewById(R.id.et_guild_nick_name);
        mBtnChangeNickName = findViewById(R.id.btn_change_nickname);
        mGuildAvatarHolder = findViewById(R.id.tv_no_icon_text);
        mIvClear = findViewById(R.id.iv_clear);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mIvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtGuildNickName.setText("");
            }
        });
        mTvGuildName.setText(mCurrentGuild.getName());
        mOrigNickName = mCurrentGuild.getMembers().get(
                Client.Companion.getGlobal().getMe().getId()).getDisplayName();
        mEtGuildNickName.setText(mOrigNickName);
        mEtGuildNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFinishButtonStyle(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        GuildAvatarUtils.setGuildAvatar(mGuildAvatar, mGuildAvatarHolder, mCurrentGuild);
        updateFinishButtonStyle(mEtGuildNickName.getText().toString().trim());
        mBtnChangeNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNickName();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyboardUtils.showKeyBoard(mEtGuildNickName, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyboardUtils.hideKeyBoard(this);
    }

    private void changeNickName() {
        String newNickName = mEtGuildNickName.getText().toString().trim();
        Client.Companion.getGlobal().getRest().getGuildService().setNickName(
                mCurrentGuild.getId(),
                Client.Companion.getGlobal().getAuth(),
                new GuildService.SetNickNameRequest(newNickName))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<JsonObject>() {
                    @Override
                    public void accept(JsonObject jsonObject) throws Throwable {
                        TomonToast.makeText(
                                getApplicationContext(),
                                getString(R.string.set_nickname_success),
                                Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        TomonToast.makeErrorText(
                                getApplicationContext(),
                                getString(R.string.set_nickname_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFinishButtonStyle(CharSequence s) {
        if (s.length() > 16 || s.length() < 1 || s.toString().equals(mOrigNickName)) {
            mBtnChangeNickName.setEnabled(false);
            mBtnChangeNickName.setTextColor(getColor(R.color.white_70));
        } else {
            mBtnChangeNickName.setEnabled(true);
            mBtnChangeNickName.setTextColor(getColor(R.color.white));
        }
    }
}