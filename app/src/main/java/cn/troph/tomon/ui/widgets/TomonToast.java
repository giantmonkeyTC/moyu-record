package cn.troph.tomon.ui.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.troph.tomon.R;

public final class TomonToast {
    private TomonToast(){}

    public static Toast makeText(Context context, int resId, int duration) {
        return makeText(context, context.getString(resId), duration);
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflate.inflate(R.layout.custom_toast, null);
        TextView textView= (TextView) layout.findViewById(R.id.message);
        textView.setText(text);
        toast.setView(layout);
        return toast;
    }
}
