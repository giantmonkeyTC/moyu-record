package cn.troph.tomon.core.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public final class KeyboardUtils {
    private KeyboardUtils(){}

    public static void hideKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        view.clearFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
