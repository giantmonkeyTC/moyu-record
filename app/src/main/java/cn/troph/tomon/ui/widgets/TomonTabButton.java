package cn.troph.tomon.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.troph.tomon.R;

public class TomonTabButton extends LinearLayout {

    private ImageView mIcon;
    private TextView mName;

    public TomonTabButton(Context context) {
        this(context, null);
    }

    public TomonTabButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TomonTabButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TomonTabButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.TomonTabButton, defStyleAttr, 0);
        String name = ta.getString(R.styleable.TomonTabButton_name_text);
        int bgResId = ta.getResourceId(R.styleable.TomonTabButton_icon_background, -1);
        ta.recycle();
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        mIcon = new ImageView(context);
        mName = new TextView(context);
        addView(mIcon);
        addView(mName);
        LinearLayout.LayoutParams iconLp = (LayoutParams) mIcon.getLayoutParams();
        iconLp.width = getResources().getDimensionPixelSize(R.dimen.channel_tab_icon_width);
        iconLp.height = LayoutParams.WRAP_CONTENT;
        mIcon.setLayoutParams(iconLp);
        if (bgResId != -1) {
            mIcon.setBackgroundResource(bgResId);
        }
        mIcon.setFocusable(true);
        LinearLayout.LayoutParams textLp = (LayoutParams) mName.getLayoutParams();
        textLp.width = LayoutParams.WRAP_CONTENT;
        textLp.height = LayoutParams.WRAP_CONTENT;
        textLp.topMargin = getResources().getDimensionPixelSize(R.dimen.channel_tab_text_margin_top);
        mName.setLayoutParams(textLp);
        mName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
        mName.setText(name);
        mName.setTextColor(getResources().getColor(R.color.white_60, null));
    }

    public void setSelected(boolean selected) {
        mIcon.setSelected(selected);
        mIcon.setPressed(selected);
        int color = selected ? R.color.pinkPrimary:R.color.white_60;
        mName.setTextColor(getResources().getColor(color, null));
    }
}
