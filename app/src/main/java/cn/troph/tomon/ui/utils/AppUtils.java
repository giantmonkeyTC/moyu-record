package cn.troph.tomon.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppUtils {
    private static final String SP_NAME_APP_INFO = "app_info";
    public static final String SP_KEY_IS_APP_FIRST_RUN = "is_app_first_run";

    private AppUtils(){}

    public static boolean isFirstRun(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME_APP_INFO, Context.MODE_PRIVATE);
        return sp.getBoolean(SP_KEY_IS_APP_FIRST_RUN, true);
    }

    public static void setIsFirstRun(Context context, boolean isFristRun) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME_APP_INFO, Context.MODE_PRIVATE);
        sp.edit().putBoolean(SP_KEY_IS_APP_FIRST_RUN, isFristRun).apply();
    }
}
