package cn.troph.tomon.ui.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import cn.troph.tomon.R;

public final class TomonToast {
    private TomonToast(){}

    public static Toast makeText(Context context, int resId, int duration) {
        return makeText(context, context.getString(resId), duration);
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast toast = _makeText(context, text, duration, R.layout.custom_toast);
        return toast;
    }

    public static Toast makeErrorText(Context context, int resId, int duration) {
        return makeErrorText(context, context.getString(resId), duration);
    }

    public static Toast makeErrorText(Context context, CharSequence text, int duration) {
        Toast toast = _makeText(context, text, duration, R.layout.custom_toast_error);
        return toast;
    }

    @NotNull
    private static Toast _makeText(Context context, CharSequence text, int duration, int layoutId) {
        Toast toast = Toast.makeText(context, text, duration);
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflate.inflate(layoutId, null);
        TextView textView = (TextView) layout.findViewById(R.id.message);
        textView.setText(text);
        toast.setView(layout);
        return toast;
    }
}
