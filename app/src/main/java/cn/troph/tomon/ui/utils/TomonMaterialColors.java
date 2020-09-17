package cn.troph.tomon.ui.utils;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.core.graphics.ColorUtils;

public final class TomonMaterialColors {
    private TomonMaterialColors(){}

    /**
     * Calculates a color that represents the layering of the {@code overlayColor} (with {@code
     * overlayAlpha} applied) on top of the {@code backgroundColor}.
     */
    @ColorInt
    public static int layer(
            @ColorInt int backgroundColor,
            @ColorInt int overlayColor,
            @FloatRange(from = 0.0, to = 1.0) float overlayAlpha) {
        int computedAlpha = Math.round(Color.alpha(overlayColor) * overlayAlpha);
        int computedOverlayColor = ColorUtils.setAlphaComponent(overlayColor, computedAlpha);
        return layer(backgroundColor, computedOverlayColor);
    }

    /**
     * Calculates a color that represents the layering of the {@code overlayColor} on top of the
     * {@code backgroundColor}.
     */
    @ColorInt
    public static int layer(@ColorInt int backgroundColor, @ColorInt int overlayColor) {
        return ColorUtils.compositeColors(overlayColor, backgroundColor);
    }
}
