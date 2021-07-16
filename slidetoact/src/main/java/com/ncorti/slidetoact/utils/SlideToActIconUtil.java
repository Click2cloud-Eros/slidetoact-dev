package com.ncorti.slidetoact.utils;

import com.ncorti.slidetoact.SlideToActView;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.VectorElement;
import ohos.agp.utils.Color;
import ohos.app.Context;

public class SlideToActIconUtil {

    public static VectorElement loadIconCompat(Context context, int value) {
        return new VectorElement(context, value);
    }

    public static void tintIconCompat(Element icon, Color color) {

    }

    /**
     * Creates a [ValueAnimator] to animate the complete icon. Uses the [fallbackToFadeAnimation]
     * to decide if the icon should be animated with a Fade or with using [AnimatedVectorDrawable].
     */
    public static AnimatorValue createIconAnimator(
            SlideToActView view,
            VectorElement icon,
            AnimatorValue.ValueUpdateListener listener
    ) {
        //val tickAnimator = ValueAnimator.ofInt(0, 255)
        AnimatorValue tickAnimator = new AnimatorValue();
        tickAnimator.setValueUpdateListener(listener);
        tickAnimator.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float v) {
                float value = AnimationUtils.getAnimatedColor(v,0, 255);
                icon.setAlpha((int) value);
                view.invalidate();
            }
        });
        return tickAnimator;
    }
}
