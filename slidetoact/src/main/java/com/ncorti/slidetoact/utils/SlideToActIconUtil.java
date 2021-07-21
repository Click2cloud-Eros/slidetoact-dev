package com.ncorti.slidetoact.utils;

import ohos.agp.animation.AnimatorValue;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.element.VectorElement;
import ohos.agp.render.ColorMatrix;
import ohos.agp.utils.Color;
import ohos.app.Context;

/**
 * SlideToActIconUtil.
 */
public class SlideToActIconUtil {

    private SlideToActIconUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Change icon color using color matrix.
     *
     * @param icon instance of VectorElement.
     * @param color target color.
     */
    public static void tintIconCompat(VectorElement icon, Color color) {
        RgbColor rgbColor = RgbColor.fromArgbInt(color.getValue());
        float r = rgbColor.getRed() / 255.0f;
        float g = rgbColor.getGreen() / 255.0f;
        float b = rgbColor.getBlue() / 255.0f;

        float[] matrix = {
            r, r, r, r, r, //red
            g, g, g, g, g, //green
            b, b, b, b, b, //blue
            1, 1, 1, 1, 1 //alpha
        };

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setMatrix(matrix);

        icon.setColorMatrix(colorMatrix);
    }

    /**
     * Creates a [ValueAnimator] to animate the complete icon. Uses the [fallbackToFadeAnimation]
     * to decide if the icon should be animated with a Fade or with using [AnimatedVectorDrawable].
     *
     * @param listener animator value update listner.
     */
    public static AnimatorValue createIconAnimator(
            AnimatorValue.ValueUpdateListener listener
    ) {
        AnimatorValue tickAnimator = new AnimatorValue();
        tickAnimator.setValueUpdateListener(listener);
        return tickAnimator;
    }
}
