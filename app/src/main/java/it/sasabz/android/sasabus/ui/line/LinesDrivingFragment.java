package it.sasabz.android.sasabus.ui.line;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.line.LineDriving;
import it.sasabz.android.sasabus.model.line.LineDrivingContent;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.RealtimeApi;
import it.sasabz.android.sasabus.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.util.list.LinesDrivingAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Displays all lines where at least one vehicle is currently in service.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class LinesDrivingFragment extends RxFragment implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    @BindView(R.id.error_wifi) RelativeLayout mErrorWifi;
    @BindView(R.id.error_general) RelativeLayout mErrorGeneral;
    @BindView(R.id.error_lines) RelativeLayout mErrorLines;
    @BindView(R.id.refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    private LinesDrivingAdapter mAdapter;

    private final ArrayList<LineDriving> mItems = new ArrayList<>();
    private ArrayList<LineDriving> mItemsTemp = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lines_driving, container, false);

        ButterKnife.bind(this, view);

        mSwipeRefreshLayout.setOnRefreshListener(this::parseData);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_amber, R.color.primary_red,
                R.color.primary_green, R.color.primary_indigo);

        mAdapter = new LinesDrivingAdapter(getActivity(), mItems);

        ListView listview = (ListView) view.findViewById(R.id.lines_driving_listView);
        listview.setOnItemClickListener(this);
        listview.setOnItemLongClickListener(this);
        listview.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            int errorWifiVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_WIFI);
            int errorGeneralVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_GENERAL);
            int errorLinesVisibility = savedInstanceState.getInt("ERROR_LINES");

            if (errorWifiVisibility != View.GONE || errorGeneralVisibility != View.GONE || errorLinesVisibility != View.GONE) {
                //noinspection ResourceType
                mErrorGeneral.setVisibility(errorGeneralVisibility);
                //noinspection ResourceType
                mErrorWifi.setVisibility(errorWifiVisibility);
                //noinspection ResourceType
                mErrorLines.setVisibility(errorLinesVisibility);

                return;
            } else {
                mItemsTemp = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);
                mItems.addAll(mItemsTemp);

                if (mAdapter != null && !mItems.isEmpty()) {
                    mAdapter.notifyDataSetChanged();

                    return;
                }
            }
        }

        new Handler().postDelayed(this::parseData, Config.LINE_FRAGMENTS_POST_DELAY);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        LineDriving item = mItems.get(position);

        Intent intent = new Intent(getActivity(), LineDetailActivity.class);
        intent.putExtra(Config.EXTRA_LINE_ID, item.getId());
        getActivity().startActivityForResult(intent, LinesActivity.INTENT_DISPLAY_FAVORITES);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);
        outState.putInt(Config.BUNDLE_ERROR_WIFI, mErrorWifi.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_GENERAL, mErrorGeneral.getVisibility());
        outState.putInt("ERROR_LINES", mErrorLines.getVisibility());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        LineDriving line = mItems.get(position);

        if (UserRealmHelper.hasFavoriteLine(line.getId())) {
            UserRealmHelper.removeFavoriteLine(line.getId());
            Snackbar.make(((BaseActivity) getActivity()).getMainContent(), getString(R.string.line_favorites_remove,
                    line.getName()), Snackbar.LENGTH_SHORT).show();
        } else {
            UserRealmHelper.addFavoriteLine(line.getId());
            Snackbar.make(((BaseActivity) getActivity()).getMainContent(), getString(R.string.line_favorites_add,
                    line.getName()), Snackbar.LENGTH_SHORT).show();
        }

        ((LinesActivity) getActivity()).invalidateFavorites();

        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);

        return true;
    }

    private void parseData() {
        if (!NetUtils.isOnline(getActivity())) {
            mErrorWifi.setVisibility(View.VISIBLE);
            mErrorGeneral.setVisibility(View.GONE);

            mItems.clear();
            mAdapter.notifyDataSetChanged();

            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));

            return;
        }

        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));

        RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
        realtimeApi.get("de")
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .map(realtimeResponse -> {
                    List<RealtimeBus> list = realtimeResponse.buses;

                    for (RealtimeBus bus : list) {
                        bus.currentStopName = BusStopRealmHelper.getName(bus.busStop);
                    }

                    return realtimeResponse;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mErrorWifi.setVisibility(View.GONE);
                        mErrorGeneral.setVisibility(View.VISIBLE);

                        mItems.clear();
                        mAdapter.notifyDataSetChanged();

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }

                    @Override
                    public void onNext(RealtimeResponse realtimeResponse) {
                        List<RealtimeBus> list = realtimeResponse.buses;

                        String me = getResources().getString(R.string.merano);
                        String bz = getResources().getString(R.string.bolzano);

                        mItemsTemp.clear();

                        Map<Integer, String> map = new HashMap<>();

                        StringBuilder stringBuilder = new StringBuilder();

                        for (RealtimeBus bus : list) {
                            String location = bus.zone.replace("BZ", bz).replace("ME", me)
                                    .replace("TS", bz + ' ' + me);

                            stringBuilder.append(bus.currentStopName)
                                    .append(':').append(bus.delayMin).append('=');

                            String lineString = bus.lineName + ':' + location + ':' +
                                    bus.lineId + '=';

                            map.put(bus.lineId, (map.get(bus.lineId) == null ? lineString : map.get(bus.lineId)) + stringBuilder);

                            stringBuilder.setLength(0);
                        }

                        List<Integer> lines = new ArrayList<>();

                        for (int line : Lines.allLines) {
                            lines.add(line);
                        }

                        List<Integer> lineList = new ArrayList<>(map.keySet());
                        Collections.sort(lineList, (lhs, rhs) -> lines.indexOf(lhs) - lines.indexOf(rhs));

                        for (int key : lineList) {
                            String[] linesArray = map.get(key).split("=");

                            List<LineDrivingContent> contentList = new ArrayList<>();

                            for (int i = 1; i < linesArray.length; i++) {
                                String[] split = linesArray[i].split(":");

                                contentList.add(new LineDrivingContent(split[0], Integer.parseInt(split[1])));
                            }

                            String[] lineInfoSplit = linesArray[0].split(":");

                            mItemsTemp.add(new LineDriving(Integer.parseInt(lineInfoSplit[2]),
                                    lineInfoSplit[0], lineInfoSplit[1],
                                    contentList));
                        }

                        mItems.clear();
                        mItems.addAll(mItemsTemp);

                        mAdapter.notifyDataSetChanged();

                        mErrorWifi.setVisibility(View.GONE);
                        mErrorGeneral.setVisibility(View.GONE);

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }
                });
    }
}