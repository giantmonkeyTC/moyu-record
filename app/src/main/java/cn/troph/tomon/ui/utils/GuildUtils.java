package cn.troph.tomon.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class GuildUtils {
    private GuildUtils(){}

    public static final String NO_GUILD_ID = "";
    public static final String SP_NAME_CHANNEL_LIST_CONFIG = "channel_list_config";
    public static final String SP_NAME_GUILD_COLLAPSE_CONFIG = "guild_collapse_config";
    public static final String SP_KEY_GUILD_ID = "guild_id";

    public static String getLastGuildId(Context context) {
        SharedPreferences spChannelListConfig = context.getSharedPreferences(
                SP_NAME_CHANNEL_LIST_CONFIG, Context.MODE_PRIVATE);
        return spChannelListConfig.getString(SP_KEY_GUILD_ID, NO_GUILD_ID);
    }

    public static void saveLastGuildId(Context context, String id) {
        context.getSharedPreferences(SP_NAME_CHANNEL_LIST_CONFIG, Context.MODE_PRIVATE)
                .edit()
                .putString(SP_KEY_GUILD_ID, id)
                .apply();
    }

    public static void saveCollapseChannelId(Context context, String id, boolean collapsed) {
        context.getSharedPreferences(SP_NAME_GUILD_COLLAPSE_CONFIG, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(id, collapsed)
                .apply();
    }

    public static boolean isCollapsedChannelId(Context context, String id) {
        SharedPreferences spChannelListConfig = context.getSharedPreferences(
                SP_NAME_GUILD_COLLAPSE_CONFIG, Context.MODE_PRIVATE);
        return spChannelListConfig.getBoolean(id, false);
    }
}
