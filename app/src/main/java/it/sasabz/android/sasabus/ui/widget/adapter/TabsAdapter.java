package it.sasabz.android.sasabus.ui.widget.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabsAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragments;
    private final List<String> titles;
    private final boolean displayIcon;

    public TabsAdapter(FragmentManager fragmentManager, boolean displayIcon) {
        super(fragmentManager);

        this.displayIcon = displayIcon;
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
    }

    public void addFragment(Fragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return displayIcon ? null : titles.get(position);
    }
}