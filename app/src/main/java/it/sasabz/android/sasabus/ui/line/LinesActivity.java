/*
 * Copyright (C) 2016 David Dejori, Alex Lardschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.sasabz.android.sasabus.ui.line;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.LinesApi;
import it.sasabz.android.sasabus.network.rest.model.Line;
import it.sasabz.android.sasabus.network.rest.response.LinesAllResponse;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.line.fragment.LinesFavoritesFragment;
import it.sasabz.android.sasabus.ui.line.fragment.LinesFragment;
import it.sasabz.android.sasabus.ui.widget.adapter.TabsAdapter;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Holds all the line fragments.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class LinesActivity extends BaseActivity {

    private static final String FRAGMENT_FAVORITES = "FAVORITES";
    private static final String FRAGMENT_BOLZANO = "BOLZANO";
    private static final String FRAGMENT_MERANO = "MERANO";

    private static final String TAG = "LinesActivity";

    static final int RESULT_DISPLAY_FAVORITES = 12341;
    public static final int INTENT_DISPLAY_FAVORITES = 12342;

    private LinesFavoritesFragment mLinesFavoritesFragment;
    private LinesFragment mLinesBzFragment;
    private LinesFragment mLinesMeFragment;

    @BindView(R.id.viewpager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;

    private ArrayList<Line> mLines;

    private TabsAdapter mAdapter;

    public static int loadedCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lines);
        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        loadedCount = 0;

        mAdapter = new TabsAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(2);

        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.white));

        if (savedInstanceState != null) {
            mLines = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);
        } else {
            mLines = new ArrayList<>();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLinesFavoritesFragment = (LinesFavoritesFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, FRAGMENT_FAVORITES);

            mLinesBzFragment = (LinesFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, FRAGMENT_BOLZANO);

            mLinesMeFragment = (LinesFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, FRAGMENT_MERANO);
        }

        if (mLinesFavoritesFragment == null) {
            mLinesFavoritesFragment = new LinesFavoritesFragment();
        }

        if (mLinesBzFragment == null) {
            mLinesBzFragment = new LinesFragment();
        }

        if (mLinesMeFragment == null) {
            mLinesMeFragment = new LinesFragment();
        }

        mAdapter.addFragment(mLinesFavoritesFragment, getString(R.string.favorites));
        mAdapter.addFragment(mLinesBzFragment, getString(R.string.bolzano));
        mAdapter.addFragment(mLinesMeFragment, getString(R.string.merano));

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
                invalidateFavorites();

                if (resultCode == RESULT_DISPLAY_FAVORITES) {
                    mViewPager.post(() -> {
                        mViewPager.setCurrentItem(0);
                        mTabLayout.setupWithViewPager(mViewPager);
                    });
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mLines);

        try {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_FAVORITES, mLinesFavoritesFragment);
            getSupportFragmentManager().putFragment(outState, FRAGMENT_BOLZANO, mLinesBzFragment);
            getSupportFragmentManager().putFragment(outState, FRAGMENT_MERANO, mLinesMeFragment);
        } catch (IllegalStateException e) {
            Utils.logException(e);
        }
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_LINES;
    }


    public void parseData(boolean manualRefresh) {
        if (!NetUtils.isOnline(this)) {
            mLinesFavoritesFragment.showErrorWifi();

            mLinesBzFragment.showErrorWifi();
            mLinesMeFragment.showErrorWifi();

            return;
        }

        LinesApi linesApi = RestClient.ADAPTER.create(LinesApi.class);
        linesApi.allLines()
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LinesAllResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        mLinesFavoritesFragment.showErrorGeneral();

                        mLinesBzFragment.showErrorGeneral();
                        mLinesMeFragment.showErrorGeneral();
                    }

                    @Override
                    public void onNext(LinesAllResponse response) {
                        mLines.clear();
                        mLines.addAll(response.lines);

                        mLinesFavoritesFragment.setLines(mLines, manualRefresh);

                        Collection<Line> bz = new ArrayList<>();
                        Collection<Line> me = new ArrayList<>();

                        boolean isBz = true;
                        for (int i = 0, mItemsSize = mLines.size(); i < mItemsSize; i++) {
                            Line line = mLines.get(i);

                            if (line.city != null && line.city.toLowerCase().startsWith("me")) {
                                isBz = false;
                            }

                            if (isBz) {
                                bz.add(line);
                            } else {
                                me.add(line);
                            }
                        }

                        mLinesBzFragment.setLines(bz, manualRefresh);
                        mLinesMeFragment.setLines(me, manualRefresh);
                    }
                });
    }

    public void invalidateFavorites() {
        if (mLinesFavoritesFragment != null) {
            mLinesFavoritesFragment.setLines(mLines, true);
        }
    }
}