package it.sasabz.android.sasabus.ui.widget;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class MaterialProgressDrawable extends Drawable implements Animatable {
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final TimeInterpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();

    private static final float FULL_ROTATION = 1080.0f;

    @Retention(RetentionPolicy.CLASS)
    @IntDef({LARGE, DEFAULT})
    public @interface ProgressDrawableSize {
    }

    private static final int LARGE = 0;

    private static final int DEFAULT = 1;

    private static final int CIRCLE_DIAMETER = 40;
    private static final float CENTER_RADIUS = 8.75f;
    private static final float STROKE_WIDTH = 2.5f;

    private static final int CIRCLE_DIAMETER_LARGE = 56;
    private static final float CENTER_RADIUS_LARGE = 12.5f;
    private static final float STROKE_WIDTH_LARGE = 3f;

    private static final float COLOR_START_DELAY_OFFSET = 0.75f;
    private static final float END_TRIM_START_DELAY_OFFSET = 0.5f;
    private static final float START_TRIM_DURATION_OFFSET = 0.5f;

    private static final int ANIMATION_DURATION = 1332;

    private static final float NUM_POINTS = 5f;

    private final ArrayList<Animation> mAnimators = new ArrayList<>();

    private final Ring mRing;

    private float mRotation;

    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_HEIGHT = 5;
    private static final float ARROW_OFFSET_ANGLE = 5;

    private static final int ARROW_WIDTH_LARGE = 12;
    private static final int ARROW_HEIGHT_LARGE = 6;
    private static final float MAX_PROGRESS_ARC = .8f;

    private final Resources mResources;
    private final View mParent;
    private Animation mAnimation;
    private float mRotationCount;
    private double mWidth;
    private double mHeight;
    private boolean mFinishing;

    public MaterialProgressDrawable(Context context, View parent) {
        mParent = parent;
        mResources = context.getResources();

        Callback mCallback = new Callback() {
            @Override
            public void invalidateDrawable(Drawable d) {
                invalidateSelf();
            }

            @Override
            public void scheduleDrawable(Drawable d, Runnable what, long when) {
                scheduleSelf(what, when);
            }

            @Override
            public void unscheduleDrawable(Drawable d, Runnable what) {
                unscheduleSelf(what);
            }
        };
        mRing = new Ring(mCallback);
        int[] COLORS = {
                Color.BLACK
        };
        mRing.setColors(COLORS);

        updateSizes();
        setupAnimators();
    }

    private void setSizeParameters() {
        DisplayMetrics metrics = mResources.getDisplayMetrics();
        float screenDensity = metrics.density;

        mWidth = (double) MaterialProgressDrawable.CIRCLE_DIAMETER * screenDensity;
        mHeight = (double) MaterialProgressDrawable.CIRCLE_DIAMETER * screenDensity;

        Ring ring = mRing;
        ring.setStrokeWidth((float) (double) MaterialProgressDrawable.STROKE_WIDTH * screenDensity);
        ring.setCenterRadius((double) MaterialProgressDrawable.CENTER_RADIUS * screenDensity);
        ring.setColorIndex(0);
        ring.setArrowDimensions((float) MaterialProgressDrawable.ARROW_WIDTH * screenDensity,
                (float) MaterialProgressDrawable.ARROW_HEIGHT * screenDensity);
        ring.setInsets((int) mWidth, (int) mHeight);
    }

    private void updateSizes() {
        setSizeParameters(
        );
    }

    public void setArrowScale(float scale) {
        mRing.setArrowScale(scale);
    }

    public void setBackgroundColor() {
        mRing.setBackgroundColor(NestedSwipeRefreshLayout.CIRCLE_BG_LIGHT);
    }

    public void setColorSchemeColors(int... colors) {
        mRing.setColors(colors);
        mRing.setColorIndex(0);
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) mWidth;
    }

    @Override
    public void draw(Canvas c) {
        Rect bounds = getBounds();
        int saveCount = c.save();
        c.rotate(mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        mRing.draw(c, bounds);
        c.restoreToCount(saveCount);
    }

    @Override
    public void setAlpha(int alpha) {
        mRing.setAlpha(alpha);
    }

    @Override
    public int getAlpha() {
        return mRing.getAlpha();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mRing.setColorFilter(colorFilter);
    }

    private void setRotation(float rotation) {
        mRotation = rotation;
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean isRunning() {
        ArrayList<Animation> animators = mAnimators;
        int N = animators.size();
        for (int i = 0; i < N; i++) {
            Animation animator = animators.get(i);
            if (animator.hasStarted() && !animator.hasEnded()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        mAnimation.reset();
        mRing.storeOriginals();

        if (mRing.getEndTrim() != mRing.getStartTrim()) {
            mFinishing = true;
            mAnimation.setDuration(ANIMATION_DURATION / 2);
            mParent.startAnimation(mAnimation);
        } else {
            mRing.setColorIndex(0);
            mRing.resetOriginals();
            mAnimation.setDuration(ANIMATION_DURATION);
            mParent.startAnimation(mAnimation);
        }
    }

    @Override
    public void stop() {
        mParent.clearAnimation();
        setRotation(0);
        mRing.setShowArrow();
        mRing.setColorIndex(0);
        mRing.resetOriginals();
    }

    private float getMinProgressArc(Ring ring) {
        return (float) Math.toRadians(
                ring.getStrokeWidth() / (2 * Math.PI * ring.getCenterRadius()));
    }

    private int evaluateColorChange(float fraction, int startValue, int endValue) {
        int startA = startValue >> 24 & 0xff;
        int startR = startValue >> 16 & 0xff;
        int startG = startValue >> 8 & 0xff;
        int startB = startValue & 0xff;

        int endA = endValue >> 24 & 0xff;
        int endR = endValue >> 16 & 0xff;
        int endG = endValue >> 8 & 0xff;
        int endB = endValue & 0xff;

        return startA + (int) (fraction * (endA - startA)) << 24 |
                startR + (int) (fraction * (endR - startR)) << 16 |
                startG + (int) (fraction * (endG - startG)) << 8 |
                startB + (int) (fraction * (endB - startB));
    }

    private void updateRingColor(float interpolatedTime, Ring ring) {
        if (interpolatedTime > COLOR_START_DELAY_OFFSET) {


            ring.setColor(evaluateColorChange((interpolatedTime - COLOR_START_DELAY_OFFSET)
                            / (1.0f - COLOR_START_DELAY_OFFSET), ring.getStartingColor(),
                    ring.getNextColor()));
        }
    }

    private void applyFinishTranslation(float interpolatedTime, Ring ring) {
        updateRingColor(interpolatedTime, ring);
        float targetRotation = (float) (Math.floor(ring.getStartingRotation() / MAX_PROGRESS_ARC)
                + 1f);
        float minProgressArc = getMinProgressArc(ring);
        float startTrim = ring.getStartingStartTrim()
                + (ring.getStartingEndTrim() - minProgressArc - ring.getStartingStartTrim())
                * interpolatedTime;
        ring.setStartTrim(startTrim);
        ring.setEndTrim(ring.getStartingEndTrim());
        float rotation = ring.getStartingRotation()
                + (targetRotation - ring.getStartingRotation()) * interpolatedTime;
        ring.setRotation(rotation);
    }

    private void setupAnimators() {
        Ring ring = mRing;
        Animation animation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                if (mFinishing) {
                    applyFinishTranslation(interpolatedTime, ring);
                } else {


                    float minProgressArc = getMinProgressArc(ring);
                    float startingEndTrim = ring.getStartingEndTrim();
                    float startingTrim = ring.getStartingStartTrim();
                    float startingRotation = ring.getStartingRotation();

                    updateRingColor(interpolatedTime, ring);


                    if (interpolatedTime <= START_TRIM_DURATION_OFFSET) {


                        float scaledTime = interpolatedTime
                                / (1.0f - START_TRIM_DURATION_OFFSET);
                        float startTrim = startingTrim
                                + (MAX_PROGRESS_ARC - minProgressArc) * MATERIAL_INTERPOLATOR
                                .getInterpolation(scaledTime);
                        ring.setStartTrim(startTrim);
                    }


                    if (interpolatedTime > END_TRIM_START_DELAY_OFFSET) {


                        float minArc = MAX_PROGRESS_ARC - minProgressArc;
                        float scaledTime = (interpolatedTime - START_TRIM_DURATION_OFFSET)
                                / (1.0f - START_TRIM_DURATION_OFFSET);
                        float endTrim = startingEndTrim
                                + minArc * MATERIAL_INTERPOLATOR.getInterpolation(scaledTime);
                        ring.setEndTrim(endTrim);
                    }

                    float rotation = startingRotation + 0.25f * interpolatedTime;
                    ring.setRotation(rotation);

                    float groupRotation = FULL_ROTATION / NUM_POINTS * interpolatedTime
                            + FULL_ROTATION * mRotationCount / NUM_POINTS;
                    setRotation(groupRotation);
                }
            }
        };
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(LINEAR_INTERPOLATOR);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mRotationCount = 0;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                ring.storeOriginals();
                ring.goToNextColor();
                ring.setStartTrim(ring.getEndTrim());
                if (mFinishing) {


                    mFinishing = false;
                    animation.setDuration(ANIMATION_DURATION);
                    ring.setShowArrow();
                } else {
                    mRotationCount = (mRotationCount + 1) % NUM_POINTS;
                }
            }
        });
        mAnimation = animation;
    }

    private static class Ring {
        private final RectF mTempBounds = new RectF();
        private final Paint mPaint = new Paint();
        private final Paint mArrowPaint = new Paint();

        private final Callback mCallback;

        private float mStartTrim;
        private float mEndTrim;
        private float mRotation;
        private float mStrokeWidth = 5.0f;
        private float mStrokeInset = 2.5f;

        private int[] mColors;


        private int mColorIndex;
        private float mStartingStartTrim;
        private float mStartingEndTrim;
        private float mStartingRotation;
        private boolean mShowArrow;
        private Path mArrow;
        private float mArrowScale;
        private double mRingCenterRadius;
        private int mArrowWidth;
        private int mArrowHeight;
        private int mAlpha;
        private final Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int mBackgroundColor;
        private int mCurrentColor;

        public Ring(Callback callback) {
            mCallback = callback;

            mPaint.setStrokeCap(Paint.Cap.SQUARE);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Style.STROKE);

            mArrowPaint.setStyle(Paint.Style.FILL);
            mArrowPaint.setAntiAlias(true);
        }

        public void setBackgroundColor(int color) {
            mBackgroundColor = color;
        }

        public void setArrowDimensions(float width, float height) {
            mArrowWidth = (int) width;
            mArrowHeight = (int) height;
        }

        public void draw(Canvas c, Rect bounds) {
            RectF arcBounds = mTempBounds;
            arcBounds.set(bounds);
            arcBounds.inset(mStrokeInset, mStrokeInset);

            float startAngle = (mStartTrim + mRotation) * 360;
            float endAngle = (mEndTrim + mRotation) * 360;
            float sweepAngle = endAngle - startAngle;

            mPaint.setColor(mCurrentColor);
            c.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint);

            drawTriangle(c, startAngle, sweepAngle, bounds);

            if (mAlpha < 255) {
                mCirclePaint.setColor(mBackgroundColor);
                mCirclePaint.setAlpha(255 - mAlpha);
                c.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), bounds.width() / 2,
                        mCirclePaint);
            }
        }

        private void drawTriangle(Canvas c, float startAngle, float sweepAngle, Rect bounds) {
            if (mShowArrow) {
                if (mArrow == null) {
                    mArrow = new Path();
                    mArrow.setFillType(Path.FillType.EVEN_ODD);
                } else {
                    mArrow.reset();
                }

                float inset = (int) mStrokeInset / 2 * mArrowScale;
                float x = (float) (mRingCenterRadius * 1.0 + bounds.exactCenterX());
                float y = (float) (mRingCenterRadius * 0.0 + bounds.exactCenterY());

                mArrow.moveTo(0, 0);
                mArrow.lineTo(mArrowWidth * mArrowScale, 0);
                mArrow.lineTo(mArrowWidth * mArrowScale / 2, mArrowHeight
                        * mArrowScale);
                mArrow.offset(x - inset, y);
                mArrow.close();

                mArrowPaint.setColor(mCurrentColor);
                c.rotate(startAngle + sweepAngle - ARROW_OFFSET_ANGLE, bounds.exactCenterX(),
                        bounds.exactCenterY());
                c.drawPath(mArrow, mArrowPaint);
            }
        }

        public void setColors(@NonNull int... colors) {
            mColors = colors;

            setColorIndex(0);
        }

        public void setColor(int color) {
            mCurrentColor = color;
        }

        public void setColorIndex(int index) {
            mColorIndex = index;
            mCurrentColor = mColors[mColorIndex];
        }

        public int getNextColor() {
            return mColors[getNextColorIndex()];
        }

        private int getNextColorIndex() {
            return (mColorIndex + 1) % mColors.length;
        }

        public void goToNextColor() {
            setColorIndex(getNextColorIndex());
        }

        public void setColorFilter(ColorFilter filter) {
            mPaint.setColorFilter(filter);
            invalidateSelf();
        }

        public void setAlpha(int alpha) {
            mAlpha = alpha;
        }

        public int getAlpha() {
            return mAlpha;
        }

        public void setStrokeWidth(float strokeWidth) {
            mStrokeWidth = strokeWidth;
            mPaint.setStrokeWidth(strokeWidth);
            invalidateSelf();
        }

        public float getStrokeWidth() {
            return mStrokeWidth;
        }

        public void setStartTrim(float startTrim) {
            mStartTrim = startTrim;
            invalidateSelf();
        }

        public float getStartTrim() {
            return mStartTrim;
        }

        public float getStartingStartTrim() {
            return mStartingStartTrim;
        }

        public float getStartingEndTrim() {
            return mStartingEndTrim;
        }

        public int getStartingColor() {
            return mColors[mColorIndex];
        }

        public void setEndTrim(float endTrim) {
            mEndTrim = endTrim;
            invalidateSelf();
        }

        public float getEndTrim() {
            return mEndTrim;
        }

        public void setRotation(float rotation) {
            mRotation = rotation;
            invalidateSelf();
        }

        public float getRotation() {
            return mRotation;
        }

        public void setInsets(int width, int height) {
            float minEdge = Math.min(width, height);
            float insets;
            if (mRingCenterRadius <= 0 || minEdge < 0) {
                insets = (float) Math.ceil(mStrokeWidth / 2.0f);
            } else {
                insets = (float) (minEdge / 2.0f - mRingCenterRadius);
            }
            mStrokeInset = insets;
        }

        public float getInsets() {
            return mStrokeInset;
        }

        public void setCenterRadius(double centerRadius) {
            mRingCenterRadius = centerRadius;
        }

        public double getCenterRadius() {
            return mRingCenterRadius;
        }

        public void setShowArrow() {
            if (mShowArrow) {
                mShowArrow = false;
                invalidateSelf();
            }
        }

        public void setArrowScale(float scale) {
            if (scale != mArrowScale) {
                mArrowScale = scale;
                invalidateSelf();
            }
        }

        public float getStartingRotation() {
            return mStartingRotation;
        }

        public void storeOriginals() {
            mStartingStartTrim = mStartTrim;
            mStartingEndTrim = mEndTrim;
            mStartingRotation = mRotation;
        }

        public void resetOriginals() {
            mStartingStartTrim = 0;
            mStartingEndTrim = 0;
            mStartingRotation = 0;
            setStartTrim(0);
            setEndTrim(0);
            setRotation(0);
        }

        private void invalidateSelf() {
            mCallback.invalidateDrawable(null);
        }
    }
}