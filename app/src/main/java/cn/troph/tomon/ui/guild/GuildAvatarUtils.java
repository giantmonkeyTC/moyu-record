package cn.troph.tomon.ui.guild;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import cn.troph.tomon.R;
import cn.troph.tomon.core.structures.Guild;

public final class GuildAvatarUtils {
    private GuildAvatarUtils(){}
    private static final int GUILD_AVATAR_CORNER_RADIUS = 25;

    public static void setGuildAvatar(ImageView toSetView, TextView textHolder, Guild guild) {
        String iconUrl = guild.getIconURL();
        if (TextUtils.isEmpty(iconUrl)) {
            Glide.with(toSetView).load(R.drawable.guild_avatar_placeholder)
                    .transform(new CenterCrop(), new RoundedCorners(GUILD_AVATAR_CORNER_RADIUS))
                    .into(toSetView);
            textHolder.setText(guild.getName().substring(0,1));
            textHolder.setVisibility(View.VISIBLE);

        } else {
            Glide.with(toSetView).load(guild.getIconURL())
                    .transform(new CenterCrop(), new RoundedCorners(GUILD_AVATAR_CORNER_RADIUS))
                    .into(toSetView);
            textHolder.setVisibility(View.GONE);
        }
    }
}
