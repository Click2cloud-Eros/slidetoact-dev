package com.ncorti.slidetoact.utils;

import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.element.VectorElement;
import ohos.agp.render.ColorMatrix;
import ohos.agp.utils.Color;

/**
 * SlideToActIconUtil.
 */
public class SlideToActIconUtil {

    private SlideToActIconUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Change icon color using color matrix.
     * reference from https://stackoverflow.com/a/11171509
     *
     * @param icon instance of VectorElement.
     * @param color target color.
     */
    public static void tintIconCompat(VectorElement icon, Color color) {
        int iColor = color.getValue();

        int red   = (iColor & 0xFF0000) / 0xFFFF;
        int green = (iColor & 0xFF00) / 0xFF;
        int blue  = iColor & 0xFF;

        float[] matrix = {
                0, 0, 0, 0, red,
                0, 0, 0, 0, green,
                0, 0, 0, 0, blue,
                0, 0, 0, 1, 0 };

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
