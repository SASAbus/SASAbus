package it.sasabz.android.sasabus.ui.intro;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import it.sasabz.android.sasabus.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract activity class which provides utility methods to display an intro.
 *
 * @author Alex Lardschneider
 */
public abstract class AppIntro extends AppCompatActivity {

    static final int DEFAULT_SCROLL_DURATION_FACTOR = 1;

    private PagerAdapter mPagerAdapter;
    private AppIntroViewPager pager;
    private final List<Fragment> fragments = new ArrayList<>();
    private IndicatorController mController;

    private View nextButton;
    private View doneButton;
    private ArrayList<Integer> transitionColors;
    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    private boolean baseProgressButtonEnabled = true;
    private boolean progressButtonEnabled = true;

    private int slidesNumber;
    private int savedCurrentItem;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);

        nextButton = findViewById(R.id.next);
        doneButton = findViewById(R.id.done);

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);

        pager = (AppIntroViewPager) findViewById(R.id.view_pager);
        pager.setAdapter(mPagerAdapter);

        if (savedInstanceState != null) {
            restoreLockingState(savedInstanceState);
        }

        nextButton.setOnClickListener(v -> pager.setCurrentItem(pager.getCurrentItem() + 1));
        doneButton.setOnClickListener(v -> onDonePressed());

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (transitionColors != null) {
                    if (position < pager.getAdapter().getCount() - 1 && position < transitionColors.size() - 1) {
                        pager.setBackgroundColor((Integer) argbEvaluator.evaluate(positionOffset, transitionColors.get(position), transitionColors.get(position + 1)));
                    } else {
                        pager.setBackgroundColor(transitionColors.get(transitionColors.size() - 1));
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (slidesNumber > 1) {
                    mController.selectPosition(position);
                }

                // Allow the swipe to be re-enabled if a user swipes to a previous slide. Restore
                // state of progress button depending on global progress button setting
                if (!pager.isNextPagingEnabled()) {
                    if (pager.getCurrentItem() != pager.getLockPage()) {
                        setProgressButtonEnabled(true);
                        pager.setNextPagingEnabled(true);
                    } else {
                        setProgressButtonEnabled(progressButtonEnabled);
                    }
                } else {
                    setProgressButtonEnabled(progressButtonEnabled);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pager.setCurrentItem(savedCurrentItem); // required for triggering onPageSelected for first page
        pager.setScrollDurationFactor();

        init();

        slidesNumber = fragments.size();

        if (slidesNumber == 1) {
            setProgressButtonEnabled(progressButtonEnabled);
        } else {
            initController();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("baseProgressButtonEnabled", baseProgressButtonEnabled);
        outState.putBoolean("progressButtonEnabled", progressButtonEnabled);
        //outState.putBoolean("skipButtonEnabled", skipButtonEnabled);
        outState.putBoolean("nextEnabled", pager.isPagingEnabled());
        outState.putBoolean("nextPagingEnabled", pager.isNextPagingEnabled());
        outState.putInt("lockPage", pager.getLockPage());
        outState.putInt("currentItem", pager.getCurrentItem());
    }

    @Override
    public boolean onKeyDown(int code, KeyEvent keyEvent) {
        if (code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_BUTTON_A ||
                code == KeyEvent.KEYCODE_DPAD_CENTER) {
            ViewPager vp = (ViewPager) findViewById(R.id.view_pager);

            if (vp.getCurrentItem() == vp.getAdapter().getCount() - 1) {
                onDonePressed();
            } else {
                vp.setCurrentItem(vp.getCurrentItem() + 1);
            }
            return false;
        }
        return super.onKeyDown(code, keyEvent);
    }

    private void restoreLockingState(Bundle savedInstanceState) {
        onRestoreInstanceState(savedInstanceState);

        baseProgressButtonEnabled = savedInstanceState.getBoolean("baseProgressButtonEnabled");
        progressButtonEnabled = savedInstanceState.getBoolean("progressButtonEnabled");
        savedCurrentItem = savedInstanceState.getInt("currentItem");
        pager.setPagingEnabled(savedInstanceState.getBoolean("nextEnabled"));
        pager.setNextPagingEnabled(savedInstanceState.getBoolean("nextPagingEnabled"));
        pager.setLockPage(savedInstanceState.getInt("lockPage"));
    }

    private void initController() {
        if (mController == null) {
            mController = new IndicatorController();
        }

        FrameLayout indicatorContainer = (FrameLayout) findViewById(R.id.indicator_container);
        indicatorContainer.addView(mController.newInstance(this));

        mController.initialize(slidesNumber);
    }

    protected void addSlide(Fragment fragment) {
        fragments.add(fragment);
        mPagerAdapter.notifyDataSetChanged();
    }

    protected void setOffscreenPageLimit(int limit) {
        pager.setOffscreenPageLimit(limit);
    }

    public void hideButton() {
        nextButton.setVisibility(View.INVISIBLE);
        doneButton.setVisibility(View.INVISIBLE);
    }

    private void setButtonState(View button, boolean show) {
        if (show) {
            button.setClickable(true);

/*            if (button.getVisibility() != View.VISIBLE) {
                button.setVisibility(View.VISIBLE);*/

            button.setVisibility(View.VISIBLE);

            //ViewCompat.animate(button).cancel();
            //ViewCompat.setAlpha(button, 0);

            ViewCompat.animate(button)
                    .alpha(1)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            //}
        } else {
            button.setClickable(false);

            //if (button.getVisibility() != View.INVISIBLE) {
            //ViewCompat.animate(button).cancel();
            ViewCompat.animate(button)
                    .alpha(0)
                    .setDuration(200)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();

            new Handler().postDelayed(() -> button.setVisibility(View.INVISIBLE), 150);
            //}
        }
    }

    protected abstract void init();

    /**
     * Setting to to display or hide the Next or Done button. This is a static setting and
     * button state is maintained across slides until explicitly changed.
     *
     * @param progressButtonEnabled Set true to display. False to hide.
     */
    private void setProgressButtonEnabled(boolean progressButtonEnabled) {
        this.progressButtonEnabled = progressButtonEnabled;

        if (progressButtonEnabled) {
            if (pager.getCurrentItem() == slidesNumber - 1) {
                setButtonState(nextButton, false);
                setButtonState(doneButton, true);
            } else {
                setButtonState(nextButton, true);
                setButtonState(doneButton, false);
            }
        } else {
            setButtonState(nextButton, false);
            setButtonState(doneButton, false);
        }
    }

    /**
     * For color transition, will be shown only if color values are properly set and
     * Size of the color array must be equal to the number of slides added
     *
     * @param colors Set color values
     */
    protected void setAnimationColors(ArrayList<Integer> colors) {
        transitionColors = colors;
    }

    protected abstract void onDonePressed();

    public void setNextPageSwipeLock(boolean lockEnable) {
        if (lockEnable) {
            // if locking, save current progress button visibility
            baseProgressButtonEnabled = progressButtonEnabled;
            setProgressButtonEnabled(false);
        } else {
            // if unlocking, restore original button visibility
            setProgressButtonEnabled(baseProgressButtonEnabled);
        }

        pager.setNextPagingEnabled(!lockEnable);
    }
}
