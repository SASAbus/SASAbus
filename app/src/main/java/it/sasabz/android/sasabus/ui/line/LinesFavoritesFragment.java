package it.sasabz.android.sasabus.ui.line;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.LinesApi;
import it.sasabz.android.sasabus.network.rest.model.Line;
import it.sasabz.android.sasabus.network.rest.response.LinesAllResponse;
import it.sasabz.android.sasabus.realm.user.FavoriteLine;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.LinesAllAdapter;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Displays all lines which the user added to the favorites. Identical to {@link LinesAllFragment},
 * except the empty state background which will be displayed if the user hasn't added any favorites.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class LinesFavoritesFragment extends RxFragment {

    private RelativeLayout mErrorWifi;
    private RelativeLayout mErrorGeneral;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<Line> mItems;
    private LinesAllAdapter mAdapter;

    private Realm realm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lines_favorites, container, false);

        if (savedInstanceState == null) {
            mItems = new ArrayList<>();
        } else {
            mItems = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);
        }

        realm = Realm.getDefaultInstance();

        mAdapter = new LinesAllAdapter(getActivity(), mItems);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        mErrorWifi = (RelativeLayout) view.findViewById(R.id.error_wifi);
        mErrorGeneral = (RelativeLayout) view.findViewById(R.id.error_general);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this::parseData);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_amber, R.color.primary_red,
                R.color.primary_green, R.color.primary_indigo);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            int errorWifiVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_WIFI);
            int errorGeneralVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_GENERAL);
            int emptyStateVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_EMPTY_STATE);

            //noinspection ResourceType
            mErrorGeneral.setVisibility(errorGeneralVisibility);

            //noinspection ResourceType
            mErrorWifi.setVisibility(errorWifiVisibility);

            return;
        }

        parseData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);
        outState.putInt(Config.BUNDLE_ERROR_WIFI, mErrorWifi.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_GENERAL, mErrorGeneral.getVisibility());
    }

    public void parseData() {
        if (getActivity() == null) return;

        realm.where(FavoriteLine.class).findAllAsync().asObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(RealmResults::isLoaded)
                .first()
                .flatMap((Func1<RealmResults<FavoriteLine>, Observable<LinesAllResponse>>) favoriteLines -> {
                    if (favoriteLines.isEmpty()) {
                        mErrorGeneral.setVisibility(View.GONE);
                        mErrorWifi.setVisibility(View.GONE);

                        mItems.clear();
                        mAdapter.notifyDataSetChanged();

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));

                        return Observable.empty();
                    }

                    if (!NetUtils.isOnline(getActivity())) {
                        mErrorWifi.setVisibility(View.VISIBLE);
                        mErrorGeneral.setVisibility(View.GONE);

                        if (mAdapter != null) {
                            mItems.clear();
                            mAdapter.notifyDataSetChanged();
                        }

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));

                        return Observable.empty();
                    }

                    mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));

                    StringBuilder sb = new StringBuilder();

                    for (FavoriteLine favoriteLine : favoriteLines) {
                        sb.append(favoriteLine.getId()).append(',');
                    }

                    sb.deleteCharAt(sb.length() - 1);

                    String language = getResources().getConfiguration().locale.toString();

                    LinesApi linesApi = RestClient.ADAPTER.create(LinesApi.class);

                    return linesApi.filterLines(language, sb.toString())
                            .compose(bindToLifecycle())
                            .subscribeOn(Schedulers.newThread());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LinesAllResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        mErrorGeneral.setVisibility(View.VISIBLE);
                        mErrorWifi.setVisibility(View.GONE);

                        mItems.clear();
                        mAdapter.notifyDataSetChanged();

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }

                    @Override
                    public void onNext(LinesAllResponse linesAllResponse) {
                        mItems.clear();
                        mItems.addAll(linesAllResponse.lines);

                        mAdapter.notifyDataSetChanged();

                        mErrorWifi.setVisibility(View.GONE);
                        mErrorGeneral.setVisibility(View.GONE);

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }
                });
    }
}