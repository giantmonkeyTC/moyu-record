package cn.troph.tomon.ui.utils;

import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

public final class TomonViewUtils {
    private TomonViewUtils(){}
    public static float getParentAbsoluteElevation(@NonNull View view) {
        float absoluteElevation = 0;
        ViewParent viewParent = view.getParent();
        while (viewParent instanceof View) {
            absoluteElevation += ViewCompat.getElevation((View) viewParent);
            viewParent = viewParent.getParent();
        }
        return absoluteElevation;
    }
}
