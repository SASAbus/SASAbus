package it.sasabz.android.sasabus.ui.line;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.widget.adapter.TabsAdapter;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.DeviceUtils;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Holds all the line fragments.
 *
 * @author Alex Lardschneider
 */
public class LinesActivity extends BaseActivity {

    private static final String TAG = "LinesActivity";

    static final int RESULT_DISPLAY_FAVORITES = 12341;
    public static final int INTENT_DISPLAY_FAVORITES = 12342;

    private LinesFavoritesFragment mLinesFavoritesFragment;
    private LinesDrivingFragment mLinesDrivingFragment;
    private LinesAllFragment mLinesAllFragment;
    private LinesHydrogenFragment mLinesHydrogenFragment;

    @BindView(R.id.viewpager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;

    private TabsAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lines);

        AnalyticsHelper.sendScreenView(TAG);

        ButterKnife.bind(this);

        mAdapter = new TabsAdapter(getSupportFragmentManager(), false);
        mViewPager.setOffscreenPageLimit(3);

        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.white));
        mTabLayout.post(() -> {
            int tabLayoutWidth = mTabLayout.getWidth();

            if (tabLayoutWidth < DeviceUtils.getScreenWidth(this)) {
                mTabLayout.setTabMode(TabLayout.MODE_FIXED);
                mTabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
            } else {
                mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLinesFavoritesFragment = (LinesFavoritesFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, LinesFavoritesFragment.class.getName());

            mLinesDrivingFragment = (LinesDrivingFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, LinesDrivingFragment.class.getName());

            mLinesAllFragment = (LinesAllFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, LinesAllFragment.class.getName());

            mLinesHydrogenFragment = (LinesHydrogenFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, LinesHydrogenFragment.class.getName());
        }

        if (mLinesFavoritesFragment == null) {
            mLinesFavoritesFragment = new LinesFavoritesFragment();
        }

        if (mLinesDrivingFragment == null) {
            mLinesDrivingFragment = new LinesDrivingFragment();
        }

        if (mLinesAllFragment == null) {
            mLinesAllFragment = new LinesAllFragment();
        }

        if (mLinesHydrogenFragment == null) {
            mLinesHydrogenFragment = new LinesHydrogenFragment();
        }

        mAdapter.addFragment(mLinesFavoritesFragment, getString(R.string.favorites));
        mAdapter.addFragment(mLinesDrivingFragment, getString(R.string.lines_driving));
        mAdapter.addFragment(mLinesAllFragment, getString(R.string.lines_all));
        mAdapter.addFragment(mLinesHydrogenFragment, getString(R.string.hydrogen_buses));

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        if (!UserRealmHelper.hasFavoriteLines()) {
            mViewPager.setCurrentItem(1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_DISPLAY_FAVORITES:
                if (resultCode == RESULT_DISPLAY_FAVORITES) {
                    mViewPager.post(() -> {
                        mViewPager.setCurrentItem(0);
                        mTabLayout.setupWithViewPager(mViewPager);
                    });
                } else if (resultCode == RESULT_OK) {
                    invalidateFavorites();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            getSupportFragmentManager().putFragment(outState, LinesFavoritesFragment.class.getName(), mLinesFavoritesFragment);
            getSupportFragmentManager().putFragment(outState, LinesDrivingFragment.class.getName(), mLinesDrivingFragment);
            getSupportFragmentManager().putFragment(outState, LinesAllFragment.class.getName(), mLinesAllFragment);
            getSupportFragmentManager().putFragment(outState, LinesHydrogenFragment.class.getName(), mLinesHydrogenFragment);
        } catch (IllegalStateException e) {
            Utils.handleException(e);
        }
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_LINES;
    }

    public void invalidateFavorites() {
        if (mLinesFavoritesFragment != null) {
            mLinesFavoritesFragment.parseData();
        }
    }
}