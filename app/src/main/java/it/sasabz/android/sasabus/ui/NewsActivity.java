package it.sasabz.android.sasabus.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.News;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.NewsApi;
import it.sasabz.android.sasabus.network.rest.response.NewsResponse;
import it.sasabz.android.sasabus.ui.widget.adapter.TabsAdapter;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.NewsAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Shows all the currently available news split up into 3 regions: {@code ALL General},
 * {@code BZ Bolzano} and {@code ME Merano}. Will highlight the news if the user clicked on a
 * news notification and was redirected to this activity.
 *
 * @author Alex Lardschneider
 */
public class NewsActivity extends BaseActivity {

    private static final String TAG = "NewsActivity";

    private static final String ZONE_ALL = "ALL";
    private static final String ZONE_BZ = "BZ";
    private static final String ZONE_ME = "ME";

    /**
     * The fragments holding the different categories of news.
     */
    private NewsFragment mNewsAllFragment;
    private NewsFragment mNewsBZFragment;
    private NewsFragment mNewsMEFragment;

    /**
     * Tab adapter to hold the fragments and fragment titles.
     */
    private TabsAdapter mAdapter;

    /**
     * The view pager which allows us to scroll between the fragments.
     */
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    /**
     * Tab layout which allows the user to click on a fragment title and navigate to it without
     * having to swipe.
     */
    @BindView(R.id.tabs)
    TabLayout mTabLayout;

    /**
     * If the user came to this activity after he clicked on a news notification, this bundle
     * will hold the news id and a boolean key to tell the fragment to highlight it.
     */
    private static Bundle sArguments;

    private static int mLoadedCounter;

    private List<News> mItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news);

        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        mAdapter = new TabsAdapter(getSupportFragmentManager(), false);

        Intent intent = getIntent();

        mLoadedCounter = 0;
        sArguments = new Bundle();
        sArguments.putBoolean(Config.EXTRA_SHOW_NEWS, intent.getBooleanExtra(Config.EXTRA_SHOW_NEWS, false));
        sArguments.putInt(Config.EXTRA_NEWS_ID, intent.getIntExtra(Config.EXTRA_NEWS_ID, 0));
        sArguments.putCharSequence(Config.EXTRA_NEWS_ZONE, intent.getCharSequenceExtra(Config.EXTRA_NEWS_ZONE));

        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.white));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mNewsAllFragment = (NewsFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, ZONE_ALL);

            mNewsBZFragment = (NewsFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, ZONE_BZ);

            mNewsMEFragment = (NewsFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, ZONE_ME);
        }

        if (mNewsAllFragment == null) {
            mNewsAllFragment = NewsFragment.getInstance(ZONE_ALL);
        }

        if (mNewsBZFragment == null) {
            mNewsBZFragment = NewsFragment.getInstance(ZONE_BZ);
        }

        if (mNewsMEFragment == null) {
            mNewsMEFragment = NewsFragment.getInstance(ZONE_ME);
        }

        mAdapter.addFragment(mNewsAllFragment, getString(R.string.general));
        mAdapter.addFragment(mNewsBZFragment, getString(R.string.bolzano));
        mAdapter.addFragment(mNewsMEFragment, getString(R.string.merano));

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        if (sArguments != null && sArguments.getBoolean(Config.EXTRA_SHOW_NEWS, false)) {
            gotoZone(sArguments);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            getSupportFragmentManager().putFragment(outState, ZONE_ALL, mNewsAllFragment);
            getSupportFragmentManager().putFragment(outState, ZONE_BZ, mNewsBZFragment);
            getSupportFragmentManager().putFragment(outState, ZONE_ME, mNewsMEFragment);
        } catch (IllegalStateException e) {
            Utils.handleException(e);
        }
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_NEWS;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        sArguments = new Bundle();
        sArguments.putBoolean(Config.EXTRA_SHOW_NEWS, intent.getBooleanExtra(Config.EXTRA_SHOW_NEWS, false));
        sArguments.putInt(Config.EXTRA_NEWS_ID, intent.getIntExtra(Config.EXTRA_NEWS_ID, 0));
        sArguments.putCharSequence(Config.EXTRA_NEWS_ZONE, intent.getCharSequenceExtra(Config.EXTRA_NEWS_ZONE));

        gotoZone(sArguments);
        callFragments(mItems);
    }


    /**
     * Parses the news from the internet.
     */
    private void parseContent() {
        if (!NetUtils.isOnline(this)) {
            String error = "nointernet";

            mNewsAllFragment.onFailure(error);
            mNewsBZFragment.onFailure(error);
            mNewsMEFragment.onFailure(error);

            return;
        }

        NewsApi newsApi = RestClient.ADAPTER.create(NewsApi.class);
        newsApi.getNews(locale())
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NewsResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        mNewsAllFragment.onFailure("error");
                        mNewsBZFragment.onFailure("error");
                        mNewsMEFragment.onFailure("error");
                    }

                    @Override
                    public void onNext(NewsResponse newsResponse) {
                        mItems = newsResponse.news;
                        callFragments(mItems);
                    }
                });
    }

    /**
     * Calls the fragment to notify them that the content has been loaded
     * or updated and it should revalidate the recycler items.
     *
     * @param items the items which hold the news.
     */
    private void callFragments(Iterable<News> items) {
        if (mNewsAllFragment != null) {
            mNewsAllFragment.setItems(items);
        }

        if (mNewsBZFragment != null) {
            mNewsBZFragment.setItems(items);
        }

        if (mNewsMEFragment != null) {
            mNewsMEFragment.setItems(items);
        }
    }

    /**
     * Scrolls to a position in the {@link ViewPager}.
     *
     * @param arguments bundle which holds the arguments with all the news.
     */
    private void gotoZone(Bundle arguments) {
        int item;

        String zone = arguments.getCharSequence(Config.EXTRA_NEWS_ZONE, "").toString();

        switch (zone) {
            case ZONE_ALL:
                item = 0;
                break;
            case ZONE_BZ:
                item = 1;
                break;
            case ZONE_ME:
                item = 2;
                break;
            default:
                return;
        }

        mViewPager.setCurrentItem(item);
    }


    /**
     * Fragments which get added to the {@link ViewPager} and hold the news. Call it by using
     * {@link NewsFragment#getInstance(String)} and pass the region.
     */
    public static class NewsFragment extends Fragment {

        private RecyclerView mRecyclerView;

        private RelativeLayout mErrorGeneral;
        private RelativeLayout mErrorWifi;

        private SwipeRefreshLayout mSwipeRefreshLayout;

        private boolean mHighlightNews;
        private int mNewsId;

        private ArrayList<News> mItems;
        private NewsAdapter mAdapter;

        private String mZone;

        public static NewsFragment getInstance(String zone) {
            Bundle bundle = new Bundle();
            bundle.putString("ZONE", zone);

            NewsFragment fragment = new NewsFragment();
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_news_tab, container, false);

            if (getArguments() != null) {
                mZone = getArguments().getString("ZONE");
            }

            mErrorWifi = (RelativeLayout) view.findViewById(R.id.error_wifi);
            mErrorGeneral = (RelativeLayout) view.findViewById(R.id.error_general);

            if (savedInstanceState == null) {
                mItems = new ArrayList<>();
            } else {
                mItems = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);
            }

            mAdapter = new NewsAdapter(getActivity(), mItems);

            mRecyclerView = (RecyclerView) view.findViewById(R.id.news_recycler);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(mAdapter);

            mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
            mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_amber, R.color.primary_red, R.color.primary_green, R.color.primary_indigo);
            mSwipeRefreshLayout.setOnRefreshListener(() -> ((NewsActivity) getActivity()).parseContent());

            if (savedInstanceState == null) {
                mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
            }

            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (savedInstanceState != null) {
                int errorWifiVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_WIFI);
                int errorGeneralVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_GENERAL);

                //noinspection ResourceType
                mErrorGeneral.setVisibility(errorGeneralVisibility);
                //noinspection ResourceType
                mErrorWifi.setVisibility(errorWifiVisibility);
            } else {
                mLoadedCounter++;

                if (mLoadedCounter == 3) {
                    ((NewsActivity) getActivity()).parseContent();
                }
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);
            outState.putInt(Config.BUNDLE_ERROR_WIFI, mErrorWifi.getVisibility());
            outState.putInt(Config.BUNDLE_ERROR_GENERAL, mErrorGeneral.getVisibility());
        }

        void setItems(Iterable<News> items) {
            mItems.clear();

            if (sArguments != null) {
                mHighlightNews = sArguments.getBoolean(Config.EXTRA_SHOW_NEWS);
                mNewsId = sArguments.getInt(Config.EXTRA_NEWS_ID);
            }

            for (News item : items) {
                if (item.getZone().equalsIgnoreCase(mZone)) {
                    if (mHighlightNews && item.getId() == mNewsId) {
                        item.setHighlight();
                    }

                    mItems.add(item);
                }
            }

            mAdapter.notifyDataSetChanged();

            if (mHighlightNews) {
                for (int i = 0; i < mItems.size(); i++) {
                    if (mItems.get(i).isHighlighted()) {
                        mRecyclerView.smoothScrollToPosition(i);
                    }
                }
            }

            mErrorGeneral.setVisibility(View.GONE);
            mErrorWifi.setVisibility(View.GONE);

            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
        }

        void onFailure(String content) {
            if (content.equals("nointernet")) {
                mErrorWifi.setVisibility(View.VISIBLE);
                mErrorGeneral.setVisibility(View.GONE);
            } else if (content.equals("error")) {
                mErrorWifi.setVisibility(View.GONE);
                mErrorGeneral.setVisibility(View.VISIBLE);
            }

            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
        }
    }
}
