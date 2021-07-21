package com.ncorti.slidetoact;

import com.ncorti.slidetoact.utils.AnimationUtils;
import com.ncorti.slidetoact.utils.LogUtil;
import com.ncorti.slidetoact.utils.SlideToActIconUtil;
import com.ncorti.slidetoact.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorGroup;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.element.VectorElement;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.utils.Color;
import ohos.app.Context;
import ohos.multimodalinput.event.TouchEvent;
import ohos.vibrator.agent.VibratorAgent;

/**
 * SlideToActView.
 */
public class SlideToActView extends Component
        implements
        Component.LayoutRefreshedListener,
        Component.DrawTask,
        Component.TouchEventListener,
        Component.EstimateSizeListener {

    private static final String TAG = SlideToActView.class.getSimpleName();

    /* -------------------- LAYOUT BOUNDS -------------------- */
    private static final float DESIRED_SLIDER_HEIGHT_DP = 72F;
    private static final float DESIRED_SLIDER_WIDTH_DP = 280F;
    private int mDesiredSliderHeight = 0;
    private int mDesiredSliderWidth = 0;

    /* -------------------- MEMBERS -------------------- */
    /**
     * Text message.
     */
    private CharSequence text = "";

    /**
     * Border Radius, default to mAreaHeight/2, -1 when not initialized.
     */
    private int borderRadius = -1;

    /**
     * Outer color used by the slider (primary).
     */
    private Color outerColor;

    /**
     * Inner color used by the slider (secondary, icon and border).
     */
    private Color innerColor;

    /**
     * Text color.
     */
    private Color textColor;

    /**
     * Custom Icon color.
     */
    private Color iconColor = new Color(getContext().getColor(ResourceTable.Color_slidetoact_defaultAccent));

    /**
     * Public flag to reverse the slider by 180 degree.
     */
    private boolean isReversed = false;

    /**
     * Public flag to lock the rotation icon.
     */
    private boolean isRotateIcon = true;

    /**
     * Public flag to lock the slider.
     */
    private boolean isLocked = false;

    /**
     * Public flag to enable complete animation.
     */
    private boolean isAnimateCompletion = true;

    /**
     * Private size for the text message.
     */
    private int textSize = 60;

    /**
     * Margin for Icon.
     */
    private int iconMargin = 28;

    /**
     * Margin of the cursor from the outer area.
     */
    private int originAreaMargin = 24;

    /**
     * Duration of the complete and reset animation (in milliseconds).
     */
    private long animDuration = 300;

    /**
     * Duration of vibration after bumping to the end point.
     */
    private long bumpVibration = 0L;

    /**
     * Custom Slider Icon.
     */
    private VectorElement sliderIcon;

    /**
     * Custom Complete Icon.
     */
    private VectorElement completeIcon;

    /* -------------------- REQUIRED FIELDS -------------------- */
    /**
     * Inner rectangle (used for arrow rotation).
     */
    private RectF mInnerRect;

    /**
     * Outer rectangle (used for area drawing).
     */
    private RectF mOuterRect;

    /**
     * Height of the drawing area.
     */
    private int mAreaHeight = 0;
    /**
     * Width of the drawing area.
     */
    private int mAreaWidth = 0;
    /**
     * Actual Width of the drawing area, used for animations.
     */
    private int mActualAreaWidth = 0;

    /**
     * Current angle for Arrow Icon.
     */
    private float mArrowAngle = 0f;

    /**
     * Arrow vector element.
     */
    private VectorElement mDrawableArrow;

    /**
     * Tick vector element.
     */
    private VectorElement mDrawableTick;

    private boolean mFlagDrawTick = false;

    /**
     * Margin for Arrow Icon.
     */
    private int mArrowMargin;

    /**
     * Margin for Tick Icon.
     */
    private int mTickMargin;

    /**
     * Slider cursor effective position. This is used to handle the `reversed` scenario.
     */
    private int mEffectivePosition = 0;

    /**
     * Slider cursor position (between 0 and (`mAreaWidth - mAreaHeight)).
     */
    private int mPosition = 0;

    /**
     * Slider cursor position in percentage (between 0f and 1f).
     */
    private float mPositionPerc = 0f;

    /**
     * 1/mPositionPerc.
     */
    private float mPositionPercInv = 1f;

    /**
     * Grace value, when mPositionPerc > mGraceValue slider will perform the 'complete' operations.
     */
    private float mGraceValue = 0.8F;

    /**
     * Positioning of text.
     */
    private float mTextYposition = -1f;
    private float mTextXposition = -1f;

    /**
     * Flag to understand if user is moving the slider cursor.
     */
    private boolean mFlagMoving = false;

    /**
     * Last X coordinate for the touch event.
     */
    private float mLastX = 0F;

    /**
     * Private flag to check if the slide gesture have been completed.
     */
    private boolean mIsCompleted = false;

    /**
     * Margin of the cursor from the outer area.
     */
    private int mActualAreaMargin;

    /* -------------------- Interfaces -------------------- */
    /**
     * Public Slide event listeners.
     */
    private OnSlideToActAnimationEventListener onSlideToActAnimationEventListener = null;
    private OnSlideCompleteListener onSlideCompleteListener = null;
    private OnSlideResetListener onSlideResetListener = null;
    private OnSlideUserFailedListener onSlideUserFailedListener = null;

    /* -------------------- PAINT & DRAW -------------------- */
    private Paint mOuterPaint;
    private Paint mInnerPaint;
    private Paint mTextPaint;

    public SlideToActView(Context context) {
        super(context);
        init(null);
    }

    public SlideToActView(Context context, AttrSet attrSet) {
        super(context, attrSet);
        init(attrSet);
    }

    public SlideToActView(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
        init(attrSet);
    }

    private void init(AttrSet attrSet) {
        LogUtil.info(TAG, "Init method called");

        Color defaultOuter = new Color(getContext().getColor(ResourceTable.Color_slidetoact_defaultAccent));
        Color defaultWhite = new Color(getContext().getColor(ResourceTable.Color_slidetoact_white));

        mDesiredSliderHeight = Utils.dp2px(DESIRED_SLIDER_HEIGHT_DP);
        mDesiredSliderWidth = Utils.dp2px(DESIRED_SLIDER_WIDTH_DP);

        LogUtil.info(TAG, "Init method mDesiredSliderHeight: " + mDesiredSliderHeight
                + " mDesiredSliderWidth: " + mDesiredSliderWidth);

        if (attrSet != null) {
            mDesiredSliderHeight = attrSet.getAttr(Attribute.SLIDER_HEIGHT).isPresent()
                    ? attrSet.getAttr(Attribute.SLIDER_HEIGHT).get().getIntegerValue()
                    : mDesiredSliderHeight;

            borderRadius = attrSet.getAttr(Attribute.BORDER_RADIUS).isPresent()
                    ? attrSet.getAttr(Attribute.BORDER_RADIUS).get().getIntegerValue()
                    : -1;

            outerColor = attrSet.getAttr(Attribute.OUTER_COLOR).isPresent()
                    ? attrSet.getAttr(Attribute.OUTER_COLOR).get().getColorValue()
                    : defaultOuter;

            innerColor = attrSet.getAttr(Attribute.INNER_COLOR).isPresent()
                    ? attrSet.getAttr(Attribute.INNER_COLOR).get().getColorValue()
                    : defaultWhite;

            // For text color, check if the `text_color` is set.
            // if not the `inner_color` is set.
            textColor = attrSet.getAttr(Attribute.TEXT_COLOR).isPresent()
                    ? attrSet.getAttr(Attribute.TEXT_COLOR).get().getColorValue()
                    : innerColor;

            text = attrSet.getAttr(Attribute.TEXT).isPresent()
                    ? attrSet.getAttr(Attribute.TEXT).get().getStringValue()
                    : "";

            textSize = attrSet.getAttr(Attribute.TEXT_SIZE).isPresent()
                    ? attrSet.getAttr(Attribute.TEXT_SIZE).get().getIntegerValue()
                    : 60;

            isLocked = attrSet.getAttr(Attribute.SLIDER_LOCKED).isPresent()
                    && attrSet.getAttr(Attribute.SLIDER_LOCKED).get().getBoolValue();

            isReversed = attrSet.getAttr(Attribute.SLIDER_REVERSED).isPresent()
                    && attrSet.getAttr(Attribute.SLIDER_REVERSED).get().getBoolValue();

            isRotateIcon = !attrSet.getAttr(Attribute.ROTATE_ICON).isPresent()
                    || attrSet.getAttr(Attribute.ROTATE_ICON).get().getBoolValue();

            isAnimateCompletion = !attrSet.getAttr(Attribute.ANIMATE_COMPLETION).isPresent()
                    || attrSet.getAttr(Attribute.ANIMATE_COMPLETION).get().getBoolValue();

            animDuration = attrSet.getAttr(Attribute.ANIMATION_DURATION).isPresent()
                    ? attrSet.getAttr(Attribute.ANIMATION_DURATION).get().getLongValue()
                    : 300;

            bumpVibration = attrSet.getAttr(Attribute.BUMP_VIBRATION).isPresent()
                    ? attrSet.getAttr(Attribute.BUMP_VIBRATION).get().getLongValue()
                    : 0;

            originAreaMargin = attrSet.getAttr(Attribute.AREA_MARGIN).isPresent()
                    ? attrSet.getAttr(Attribute.AREA_MARGIN).get().getIntegerValue()
                    : 24;

            sliderIcon = attrSet.getAttr(Attribute.SLIDER_ICON).isPresent()
                    ? (VectorElement) attrSet.getAttr(Attribute.SLIDER_ICON).get().getElement()
                    : new VectorElement(getContext(), ResourceTable.Graphic_slidetoact_ic_arrow);

            // For icon color. check if the `slide_icon_color` is set.
            // if not the `outer_color` is set.
            iconColor = attrSet.getAttr(Attribute.SLIDER_ICON_COLOR).isPresent()
                    ? attrSet.getAttr(Attribute.SLIDER_ICON_COLOR).get().getColorValue()
                    : outerColor;

            completeIcon = attrSet.getAttr(Attribute.COMPLETE_ICON).isPresent()
                    ? (VectorElement) attrSet.getAttr(Attribute.COMPLETE_ICON).get().getElement()
                    : new VectorElement(getContext(), ResourceTable.Graphic_slidetoact_ic_check);

            iconMargin = attrSet.getAttr(Attribute.ICON_MARGIN).isPresent()
                    ? attrSet.getAttr(Attribute.ICON_MARGIN).get().getIntegerValue()
                    : 28;
        }

        mArrowMargin = iconMargin;
        mTickMargin = iconMargin;

        mActualAreaMargin = originAreaMargin;

        mDrawableArrow = sliderIcon;
        mDrawableTick = completeIcon;

        SlideToActIconUtil.tintIconCompat(mDrawableArrow, iconColor);

        mOuterRect = new RectF(
                mActualAreaWidth,
                0f,
                mAreaWidth - mActualAreaWidth,
                mAreaHeight
        );

        mInnerRect = new RectF(
                (mActualAreaMargin + mEffectivePosition),
                mActualAreaMargin,
                (mAreaHeight + mEffectivePosition) - mActualAreaMargin,
                mAreaHeight - mActualAreaMargin
        );

        mOuterPaint = new Paint();
        mOuterPaint.setAntiAlias(true);
        mOuterPaint.setColor(outerColor);

        mInnerPaint = new Paint();
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setColor(innerColor);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setAlpha(255 * 1f);
        mTextPaint.setColor(textColor);

        setLayoutRefreshedListener(this);
        setEstimateSizeListener(this);
        setTouchEventListener(this);
        addDrawTask(this);
    }

    @Override
    public boolean onEstimateSize(int widthEstimateConfig, int heightEstimateConfig) {
        LogUtil.info(TAG, "onEstimateSize method called");
        int width = measureDimension(mDesiredSliderWidth, widthEstimateConfig);
        int height = measureDimension(mDesiredSliderHeight, heightEstimateConfig);

        LogUtil.info(TAG, "onEstimateSize method Width: " + width + " Height: " + height);
        //Do Size Estimation here and don't forgot to call setEstimatedSize(width, height)
        setEstimatedSize(EstimateSpec.getSizeWithMode(width, EstimateSpec.PRECISE),
                EstimateSpec.getSizeWithMode(height, EstimateSpec.PRECISE));

        return true;
    }

    private int measureDimension(int defaultSize, int measureSpec) {
        int result = 0;
        int specMode = EstimateSpec.getMode(measureSpec);
        int specSize = EstimateSpec.getSize(measureSpec);
        if (specMode == EstimateSpec.PRECISE) {
            result = specSize;
        } else {
            result = defaultSize; // UNSPECIFIED
            if (specMode == EstimateSpec.NOT_EXCEED) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    public void onDraw(Component component, Canvas canvas) {
        LogUtil.info(TAG, "onDraw method called");
        if (canvas == null) {
            return;
        }

        //Draw outer area
        mOuterRect.set(
                new RectF(
                        mActualAreaWidth,
                        0f,
                        mAreaWidth - mActualAreaWidth,
                        mAreaHeight
                )
        );

        canvas.drawRoundRect(
                mOuterRect,
                borderRadius,
                borderRadius,
                mOuterPaint
        );


        // Text alpha
        mTextPaint.setAlpha(255 * mPositionPercInv);
        String textToDraw = text.toString();
        canvas.drawText(
                mTextPaint,
                textToDraw,
                mTextXposition,
                mTextYposition
        );

        //draw inner view
        // ratio is used to compute the proper border radius for the inner rect (see #8).
        float ratio = (mAreaHeight - 2.0f * mActualAreaMargin) / mAreaHeight;

        mInnerRect.set(
                new RectF(
                        (mActualAreaMargin + mEffectivePosition),
                        mActualAreaMargin,
                        (mAreaHeight + mEffectivePosition) - mActualAreaMargin,
                        mAreaHeight - mActualAreaMargin

                )
        );

        canvas.drawRoundRect(
                mInnerRect,
                borderRadius * ratio,
                borderRadius * ratio,
                mInnerPaint
        );

        // Arrow angle
        // We compute the rotation of the arrow and we apply .rotate transformation on the canvas.
        canvas.save();

        if (isReversed) {
            canvas.scale(-1F, 1F, mInnerRect.centerX(), mInnerRect.centerY());
        }

        if (isRotateIcon) {
            mArrowAngle = -180 * mPositionPerc;
            canvas.rotate(mArrowAngle, mInnerRect.centerX(), mInnerRect.centerY());
            LogUtil.info(TAG, "mArrowAngle: " + mArrowAngle + " mPositionPerc:" + mPositionPerc);
        }

        mDrawableArrow.setBounds(
                (int) mInnerRect.left + mArrowMargin,
                (int) mInnerRect.top + mArrowMargin,
                (int) mInnerRect.right - mArrowMargin,
                (int) mInnerRect.bottom - mArrowMargin
        );

        if (mDrawableArrow.getBounds().left <= mDrawableArrow.getBounds().right
                && mDrawableArrow.getBounds().top <= mDrawableArrow.getBounds().bottom
        ) {
            mDrawableArrow.drawToCanvas(canvas);
        }
        canvas.restore();

        // Tick drawing
        mDrawableTick.setBounds(
                mActualAreaWidth + mTickMargin,
                mTickMargin,
                mAreaWidth - mTickMargin - mActualAreaWidth,
                mAreaHeight - mTickMargin
        );

        SlideToActIconUtil.tintIconCompat(mDrawableTick, innerColor);

        LogUtil.info(TAG, "mFlagDrawTick: " + mFlagDrawTick
                + " mActualAreaWidth: " + mActualAreaWidth
                + " mTickMargin: " + mTickMargin
                + " mAreaWidth: " + mAreaWidth
                + " mAreaHeight: " + mAreaHeight);

        if (mFlagDrawTick) {
            mDrawableTick.drawToCanvas(canvas);
        }
    }

    @Override
    public void onRefreshed(Component component) {
        LogUtil.info(TAG, "onRefreshed method called");
        int w = component.getWidth();
        int h = component.getHeight();
        mAreaWidth = w;
        mAreaHeight = h;

        LogUtil.info(TAG, "onRefreshed method mAreaWidth: " + mAreaWidth + " mAreaHeight: " + mAreaHeight);

        if (borderRadius == -1) {
            // Round if not set up
            borderRadius = h / 2;
        }

        // Text horizontal/vertical positioning (both centered)
        mTextXposition = mAreaWidth / 3.0f;
        mTextYposition = (mAreaHeight / 2.0f)
                - (mTextPaint.descent() + mTextPaint.ascent()) / 2;

        invalidate();
        // Make sure the position is recomputed.
        updatePosition(0);
    }

    @Override
    public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
        if (touchEvent != null && isEnabled()) {
            float x = touchEvent.getPointerPosition(touchEvent.getIndex()).getX();
            float y = touchEvent.getPointerPosition(touchEvent.getIndex()).getY();

            LogUtil.info(TAG, "onTouchEvent method x: " + x + " y: " + y);

            switch (touchEvent.getAction()) {
                case TouchEvent.PRIMARY_POINT_DOWN: {
                    if (checkInsideButton(x, y)) {
                        mFlagMoving = true;
                        mLastX = x;
                    } else {
                        // Clicking outside the area -> User failed, notify the listener.
                        if (onSlideUserFailedListener != null) {
                            onSlideUserFailedListener.onSlideFailed(this, true);
                        }
                    }
                }
                break;
                case TouchEvent.PRIMARY_POINT_UP: {
                    if ((mPosition > 0 && isLocked)
                            || (mPosition > 0 && mPositionPerc < mGraceValue)
                    ) {
                        // Check for grace value
                        AnimatorValue positionAnimator = new AnimatorValue();
                        positionAnimator.setDuration(animDuration);
                        positionAnimator.setValueUpdateListener((animatorValue, v) -> {
                            updatePosition((int) v);
                            invalidate();
                        });
                        positionAnimator.start();
                    } else if (mPosition > 0 && mPositionPerc >= mGraceValue) {
                        setEnabled(false); // Fully disable touch events
                        startAnimationComplete();
                    } else if (mFlagMoving && mPosition == 0) {
                        // mFlagMoving == true means user successfully grabbed the slider,
                        // but mPosition == 0 means that the slider is released at the beginning
                        // so either a Tap or the user slided back.
                        if (onSlideUserFailedListener != null) {
                            onSlideUserFailedListener.onSlideFailed(this, false);
                        }
                    }
                    mFlagMoving = false;
                }
                break;
                case TouchEvent.POINT_MOVE: {
                    if (mFlagMoving) {
                        // True if the cursor was not at the end position before this event
                        boolean wasIncomplete = mPositionPerc < 1f;

                        float diffX = x - mLastX;
                        mLastX = x;
                        increasePosition((int) diffX);
                        invalidate();

                        // If this event brought the cursor to the end position, we can vibrate
                        if (bumpVibration > 0 && wasIncomplete && mPositionPerc == 1f) {
                            handleVibration();
                        }
                    }
                }
                break;
            }
            return true;
        }
        return false;
    }

    /**
     * Private method to check if user has touched the slider cursor.
     *
     * @param x The x coordinate of the touch event
     * @param y The y coordinate of the touch event
     * @return A boolean that informs if user has pressed or not
     */
    private boolean checkInsideButton(float x, float y) {
        return (
                0 < y
                        && y < mAreaHeight
                        && mEffectivePosition < x
                        && x < (mAreaHeight + mEffectivePosition)
            );
    }

    /**
     * Private method for increasing/decreasing the position
     * Ensure that position never exits from its range [0, (mAreaWidth - mAreaHeight)].
     *
     * <p>Please note that the increment is inverted in case of a reversed slider.
     *
     * @param inc Increment to be performed (negative if it's a decrement)
     */
    private void increasePosition(int inc) {
        LogUtil.info(TAG, "increasePosition Before mPosition: " + mPosition + " And Diff: " + inc);
        if (isReversed) {
            updatePosition(mPosition - inc);
        } else {
            updatePosition(mPosition + inc);
        }
        LogUtil.info(TAG, "increasePosition After mPosition: " + mPosition);
        if (mPosition < 0) {
            updatePosition(0);
        }
        if (mPosition > (mAreaWidth - mAreaHeight)) {
            updatePosition(mAreaWidth - mAreaHeight);
        }
    }

    private void updatePosition(int position) {
        this.mPosition = position;
        if (mAreaWidth - mAreaHeight == 0) {
            // Avoid 0 division
            mPositionPerc = 0f;
            mPositionPercInv = 1f;
            return;
        }
        mPositionPerc = position * 1f / (mAreaWidth - mAreaHeight);
        mPositionPercInv = 1 - position * 1f / (mAreaWidth - mAreaHeight);

        LogUtil.info(TAG, "UpdatePosition position: " + position + " mPositionPerc: " + mPositionPerc);
        updateEffectivePosition(mPosition);
    }

    private void updateEffectivePosition(int effectivePosition) {
        if (isReversed) {
            this.mEffectivePosition = (mAreaWidth - mAreaHeight) - effectivePosition;
        } else {
            this.mEffectivePosition = effectivePosition;
        }
    }

    /**
     * Private method that is performed when user completes the slide.
     */
    private void startAnimationComplete() {
        AnimatorGroup animSet = new AnimatorGroup();

        // Animator that moves the cursor
        AnimatorValue finalPositionAnimator = new AnimatorValue();
        finalPositionAnimator.setValueUpdateListener((animatorValue, v) -> {
            float value = AnimationUtils.getAnimatedValue(v, mPosition, mAreaWidth - mAreaHeight);
            updatePosition((int) value);
            invalidate();
        });

        // Animator that bounce away the cursors
        AnimatorValue marginAnimator = new AnimatorValue();
        marginAnimator.setValueUpdateListener((animatorValue, v) -> {
            float value = AnimationUtils.getAnimatedValue(
                    v,
                    mActualAreaMargin,
                    (int) ((mInnerRect.width() / 2) + mActualAreaMargin));
            mActualAreaMargin = (int) value;
            invalidate();
        });
        marginAnimator.setCurveType(Animator.CurveType.ANTICIPATE_OVERSHOOT);

        // Animator that reduces the outer area (to right)
        AnimatorValue areaAnimator = new AnimatorValue();
        areaAnimator.setValueUpdateListener((animatorValue, v) -> {
            float value = AnimationUtils.getAnimatedValue(v, 0, (mAreaWidth - mAreaHeight) / 2);
            mActualAreaWidth = (int) value;
            invalidate();
        });

        final boolean[] startedOnce = {false};
        AnimatorValue.ValueUpdateListener tickListener = (animatorValue, v) -> {
            if (!mFlagDrawTick) {
                LogUtil.info(TAG, "Tick listner call from SlideToActView");
                mFlagDrawTick = true;
                mTickMargin = iconMargin;
            }

            if (!startedOnce[0]) {
                invalidate();
                startedOnce[0] = true;
            }
        };

        AnimatorValue tickAnimator = SlideToActIconUtil.createIconAnimator(tickListener);

        List<Animator> animators = new ArrayList<>();
        if (mPosition < mAreaWidth - mAreaHeight) {
            animators.add(finalPositionAnimator);
        }

        if (isAnimateCompletion) {
            animators.add(marginAnimator);
            animators.add(areaAnimator);
            animators.add(tickAnimator);
        }

        animSet.runSerially(animators.toArray(new Animator[animators.size()]));

        animSet.setDuration(animDuration);

        animSet.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
                if (onSlideToActAnimationEventListener != null) {
                    onSlideToActAnimationEventListener
                            .onSlideCompleteAnimationStarted(SlideToActView.this, mPositionPerc);
                }
            }

            @Override
            public void onStop(Animator animator) {
                // Do nothing
            }

            @Override
            public void onCancel(Animator animator) {
                // Do nothing
            }

            @Override
            public void onEnd(Animator animator) {
                mIsCompleted = true;
                if (onSlideToActAnimationEventListener != null) {
                    onSlideToActAnimationEventListener.onSlideCompleteAnimationEnded(SlideToActView.this);
                }

                if (onSlideCompleteListener != null) {
                    onSlideCompleteListener.onSlideComplete(SlideToActView.this);
                }
            }

            @Override
            public void onPause(Animator animator) {
                // Do nothing
            }

            @Override
            public void onResume(Animator animator) {
                // Do nothing
            }
        });
        animSet.start();
    }

    /**
     * Private method that is performed when you want to reset the cursor.
     */
    private void startAnimationReset() {
        mIsCompleted = false;
        AnimatorGroup animSet = new AnimatorGroup();

        // Animator that reduces the tick size
        AnimatorValue tickAnimator = new AnimatorValue();
        tickAnimator.setValueUpdateListener((animatorValue, v) -> {
            mTickMargin = (int) v;
            invalidate();
        });

        // Animator that enlarges the outer area
        AnimatorValue areaAnimator = new AnimatorValue();
        areaAnimator.setValueUpdateListener((animatorValue, v) -> {
            float value = AnimationUtils.getAnimatedValue(v, mActualAreaWidth, 0);
            // Now we can hide the tick till the next complete
            mFlagDrawTick = false;
            mActualAreaWidth = (int) value;
            invalidate();
        });

        AnimatorValue positionAnimator = new AnimatorValue();
        positionAnimator.setValueUpdateListener((animatorValue, v) -> {
            float value = AnimationUtils.getAnimatedValue(v, mPosition, 0);
            updatePosition((int) value);
            invalidate();
        });

        // Animator that re-draw the cursors
        AnimatorValue marginAnimator = new AnimatorValue();
        marginAnimator.setValueUpdateListener((animatorValue, v) -> {
            float value = AnimationUtils.getAnimatedValue(v, mActualAreaMargin, originAreaMargin);
            mActualAreaMargin = (int) value;
            invalidate();
        });
        marginAnimator.setCurveType(Animator.CurveType.ANTICIPATE_OVERSHOOT);

        // Animator that makes the arrow appear
        AnimatorValue arrowAnimator = new AnimatorValue();
        arrowAnimator.setValueUpdateListener((animatorValue, v) -> {
            float value = AnimationUtils.getAnimatedValue(v, mArrowMargin, iconMargin);
            mArrowMargin = (int) value;
            invalidate();
        });
        marginAnimator.setCurveType(Animator.CurveType.OVERSHOOT);

        if (isAnimateCompletion) {
            animSet.runSerially(
                    tickAnimator,
                    areaAnimator,
                    positionAnimator,
                    marginAnimator,
                    arrowAnimator
            );
        } else {
            animSet.runSerially(positionAnimator);
        }

        animSet.setDuration(animDuration);

        animSet.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
                if (onSlideToActAnimationEventListener != null) {
                    onSlideToActAnimationEventListener.onSlideResetAnimationStarted(SlideToActView.this);
                }
            }

            @Override
            public void onStop(Animator animator) {
                // Do nothing
            }

            @Override
            public void onCancel(Animator animator) {
                // Do nothing
            }

            @Override
            public void onEnd(Animator animator) {
                setEnabled(true);

                if (onSlideToActAnimationEventListener != null) {
                    onSlideToActAnimationEventListener.onSlideResetAnimationEnded(SlideToActView.this);
                }

                if (onSlideResetListener != null) {
                    onSlideResetListener.onSlideReset(SlideToActView.this);
                }
            }

            @Override
            public void onPause(Animator animator) {
                // Do nothing
            }

            @Override
            public void onResume(Animator animator) {
                // Do nothing
            }
        });
        animSet.start();
    }

    /**
     * Private method to handle vibration logic, called when the cursor it moved to the end of
     * it's path.
     */
    private void handleVibration() {
        if (bumpVibration <= 0) {
            return;
        }

        VibratorAgent vibrator = new VibratorAgent();
        vibrator.startOnce((int) bumpVibration);
    }

    /**
     * Method that reset the slider.
     */
    public void resetSlider() {
        if (mIsCompleted) {
            startAnimationReset();
        }
    }

    public CharSequence getText() {
        return text;
    }

    public void setText(CharSequence text) {
        this.text = text;
        invalidate();
    }

    public int getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(int borderRadius) {
        this.borderRadius = borderRadius;
        invalidate();
    }

    public Color getOuterColor() {
        return outerColor;
    }

    public void setOuterColor(Color outerColor) {
        this.outerColor = outerColor;
        mOuterPaint.setColor(outerColor);
        invalidate();
    }

    public Color getInnerColor() {
        return innerColor;
    }

    public void setInnerColor(Color innerColor) {
        this.innerColor = innerColor;
        mInnerPaint.setColor(innerColor);
        invalidate();
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        mTextPaint.setColor(textColor);
        invalidate();
    }

    public Color getIconColor() {
        return iconColor;
    }

    public void setIconColor(Color iconColor) {
        this.iconColor = iconColor;
        SlideToActIconUtil.tintIconCompat(mDrawableArrow, iconColor);
        invalidate();
    }

    public boolean isReversed() {
        return isReversed;
    }

    public void setReversed(boolean reversed) {
        isReversed = reversed;
        // We reassign the position field to trigger the re-computation of the effective position.
        updatePosition(mPosition);
        invalidate();
    }

    public boolean isRotateIcon() {
        return isRotateIcon;
    }

    public void setRotateIcon(boolean rotateIcon) {
        isRotateIcon = rotateIcon;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isAnimateCompletion() {
        return isAnimateCompletion;
    }

    public void setAnimateCompletion(boolean animateCompletion) {
        isAnimateCompletion = animateCompletion;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        mTextPaint.setTextSize(textSize);
        invalidate();
    }

    public int getIconMargin() {
        return iconMargin;
    }

    public void setIconMargin(int iconMargin) {
        this.iconMargin = iconMargin;
        mArrowMargin = iconMargin;
        mTickMargin = iconMargin;
        invalidate();
    }

    public int getOriginAreaMargin() {
        return originAreaMargin;
    }

    public void setOriginAreaMargin(int originAreaMargin) {
        this.originAreaMargin = originAreaMargin;
        mActualAreaMargin = originAreaMargin;
        invalidate();
    }

    public long getAnimDuration() {
        return animDuration;
    }

    public void setAnimDuration(long animDuration) {
        this.animDuration = animDuration;
    }

    public long getBumpVibration() {
        return bumpVibration;
    }

    public void setBumpVibration(long bumpVibration) {
        this.bumpVibration = bumpVibration;
    }

    public VectorElement getSliderIcon() {
        return sliderIcon;
    }

    public void setSliderIcon(VectorElement sliderIcon) {
        this.sliderIcon = sliderIcon;
        mDrawableArrow = sliderIcon;
        SlideToActIconUtil.tintIconCompat(mDrawableArrow, iconColor);
        invalidate();
    }

    public VectorElement getCompleteIcon() {
        return completeIcon;
    }

    public void setCompleteIcon(VectorElement completeIcon) {
        this.completeIcon = completeIcon;
        mDrawableTick = completeIcon;
        SlideToActIconUtil.tintIconCompat(mDrawableTick, iconColor);
        invalidate();
    }

    public void setOnSlideCompleteListener(OnSlideCompleteListener onSlideCompleteListener) {
        this.onSlideCompleteListener = onSlideCompleteListener;
    }

    public void setOnSlideToActAnimationEventListener(
            OnSlideToActAnimationEventListener onSlideToActAnimationEventListener) {
        this.onSlideToActAnimationEventListener = onSlideToActAnimationEventListener;
    }

    public void setOnSlideResetListener(OnSlideResetListener onSlideResetListener) {
        this.onSlideResetListener = onSlideResetListener;
    }

    public void setOnSlideUserFailedListener(OnSlideUserFailedListener onSlideUserFailedListener) {
        this.onSlideUserFailedListener = onSlideUserFailedListener;
    }

    /**
     * Event handler for the SlideToActView animation events.
     * This event handler can be used to react to animation events from the Slide,
     * the event will be fired whenever an animation start/end.
     */
    public interface OnSlideToActAnimationEventListener {

        /**
         * Called when the slide complete animation start. You can perform actions during the
         * complete animations.
         *
         * @param view      The SlideToActView who created the event
         * @param threshold The mPosition (in percentage [0f,1f]) where the user has left the cursor
         */
        void onSlideCompleteAnimationStarted(SlideToActView view, float threshold);

        /**
         * Called when the slide complete animation finish. At this point the slider is stuck in the
         * center of the slider.
         *
         * @param view The SlideToActView who created the event
         */
        void onSlideCompleteAnimationEnded(SlideToActView view);

        /**
         * Called when the slide reset animation start. You can perform actions during the reset
         * animations.
         *
         * @param view The SlideToActView who created the event
         */
        void onSlideResetAnimationStarted(SlideToActView view);

        /**
         * Called when the slide reset animation finish. At this point the slider will be in the
         * ready on the left of the screen and user can interact with it.
         *
         * @param view The SlideToActView who created the event
         */
        void onSlideResetAnimationEnded(SlideToActView view);
    }

    /**
     * Event handler for the slide complete event.
     * Use this handler to react to slide event
     */
    public interface OnSlideCompleteListener {
        /**
         * Called when user performed the slide.
         *
         * @param view The SlideToActView who created the event
         */
        void onSlideComplete(SlideToActView view);
    }

    /**
     * Event handler for the slide react event.
     * Use this handler to inform the user that he can slide again.
     */
    public interface OnSlideResetListener {
        /**
         * Called when slides is again available.
         *
         * @param view The SlideToActView who created the event
         */
        void onSlideReset(SlideToActView view);
    }

    /**
     * Event handler for the user failure with the Widget.
     * You can subscribe to this event to get notified when the user is wrongly
     * interacting with the widget to eventually educate it:
     *
     * <p>- The user clicked outside of the cursor
     * - The user slided but left when the cursor was back to zero
     *
     * <p>You can use this listener to show a Toast or other messages.
     */
    public interface OnSlideUserFailedListener {
        /**
         * Called when user failed to interact with the slider slide.
         *
         * @param view      The SlideToActView who created the event
         * @param isOutside True if user pressed outside the cursor
         */
        void onSlideFailed(SlideToActView view, boolean isOutside);
    }
}
