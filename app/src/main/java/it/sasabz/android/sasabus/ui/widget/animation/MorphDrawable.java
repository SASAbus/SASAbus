package it.sasabz.android.sasabus.ui.widget.animation;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.Property;

/**
 * A drawable that can morph size, shape (via it's corner radius) and color.  Specifically this is
 * useful for animating between a FAB and a dialog.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class MorphDrawable extends Drawable {

    private float cornerRadius;

    /**
     * An implementation of {@link Property} to be used specifically with fields of
     * type
     * {@code float}. This type-specific subclass enables performance benefit by allowing
     * calls to a {@code set()} function that takes the primitive
     * {@code float} type and avoids autoboxing and other overhead associated with the
     * {@code Float} class.
     */
    static final Property<MorphDrawable, Float> CORNER_RADIUS =
            new Property<MorphDrawable, Float>(Float.class, "cornerRadius") {

        void setValue(MorphDrawable morphDrawable, float value) {
            morphDrawable.setCornerRadius(value);
        }

        @Override
        public void set(MorphDrawable object, Float value) {
            setValue(object, value);
        }

        @Override
        public Float get(MorphDrawable morphDrawable) {
            return morphDrawable.getCornerRadius();
        }
    };

    /**
     * An implementation of {@link Property} to be used specifically with fields of
     * type
     * {@code int}. This type-specific subclass enables performance benefit by allowing
     * calls to a {@code set()} function that takes the primitive
     * {@code int} type and avoids autoboxing and other overhead associated with the
     * {@code Integer} class.
     */
    static final Property<MorphDrawable, Integer> COLOR =
            new Property<MorphDrawable, Integer>(Integer.class, "color") {

                void setValue(MorphDrawable morphDrawable, int value) {
                    morphDrawable.setColor(value);
                }

                @Override
                public void set(MorphDrawable object, Integer value) {
                    setValue(object, value);
                }

                @Override
                public Integer get(MorphDrawable morphDrawable) {
                    return morphDrawable.getColor();
                }
            };

    private final Paint paint;

    MorphDrawable(@ColorInt int color, float cornerRadius) {
        this.cornerRadius = cornerRadius;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
    }

    private float getCornerRadius() {
        return cornerRadius;
    }

    private void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        invalidateSelf();
    }

    private int getColor() {
        return paint.getColor();
    }

    private void setColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(getBounds().left, getBounds().top, getBounds().right, getBounds()
                .bottom, cornerRadius, cornerRadius, paint);
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        outline.setRoundRect(getBounds(), cornerRadius);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return paint.getAlpha();
    }
}