package it.sasabz.android.sasabus.util.recycler;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.BusStopDetail;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.ui.MapActivity;
import it.sasabz.android.sasabus.ui.line.LineCourseActivity;
import it.sasabz.android.sasabus.ui.line.LineDetailActivity;
import it.sasabz.android.sasabus.ui.widget.animation.AnimationListenerAdapter;
import it.sasabz.android.sasabus.util.Utils;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class BusStopDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<BusStopDetail> mItems;
    private final Collection<ViewHolderBus> mViews = new ArrayList<>();
    private final int mBusStopFamily;

    private static final int BUS_STOP_DETAIL_HEADER = 0;
    private static final int BUS_STOP_DETAIL_BUS = 1;
    private static final int BUS_STOP_DETAIL_DATA = 2;

    public BusStopDetailsAdapter(Context context, int family, List<BusStopDetail> items) {
        mItems = items;
        mBusStopFamily = family;
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        BusStopDetail item = mItems.get(position);

        if (item.getAdditionalData() == null) {
            return BUS_STOP_DETAIL_BUS;
        }

        switch (item.getAdditionalData()) {
            case "data":
                return BUS_STOP_DETAIL_DATA;
            default:
                return BUS_STOP_DETAIL_HEADER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;

        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case BUS_STOP_DETAIL_DATA:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.include_error_data, viewGroup, false);

                viewHolder = new ViewHolderError(view);
                break;
            case BUS_STOP_DETAIL_BUS:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_item_bus_stop_details_bus, viewGroup, false);

                viewHolder = new ViewHolderBus(view);

                mViews.add((ViewHolderBus) viewHolder);
                break;
            case BUS_STOP_DETAIL_HEADER:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_item_bus_stop_details_header, viewGroup, false);

                viewHolder = new ViewHolderHeader(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BusStopDetail item = mItems.get(position);

        int itemType = getItemViewType(position);

        if (itemType == BUS_STOP_DETAIL_HEADER) {
            ViewHolderHeader header = (ViewHolderHeader) holder;

            String[] split = item.getAdditionalData().split("#");

            header.overviewMunic.setText(split[0]);
            header.overviewLines.setText(split[1]);
        } else if (itemType == BUS_STOP_DETAIL_BUS) {
            ViewHolderBus bus = (ViewHolderBus) holder;

            bus.lineCard.setVisibility(View.VISIBLE);

            if (item.isReveal()) {
                bus.lineCard.findViewById(R.id.list_stations_detail_card_line_reveal).setVisibility(View.VISIBLE);
                bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_1).setVisibility(View.VISIBLE);
                bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_2).setVisibility(View.VISIBLE);
                bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_3).setVisibility(View.VISIBLE);
            } else {
                bus.lineCard.findViewById(R.id.list_stations_detail_card_line_reveal).setVisibility(View.GONE);
                bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_1).setVisibility(View.GONE);
                bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_2).setVisibility(View.GONE);
                bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_3).setVisibility(View.GONE);
            }

            if (item.getDelay() == Config.BUS_STOP_DETAILS_OPERATION_RUNNING) {
                setVisibilityIfNeeded(bus.delayProgress, View.VISIBLE);
                setVisibilityIfNeeded(bus.delay, View.GONE);
            } else {
                setVisibilityIfNeeded(bus.delayProgress, View.GONE);
                setVisibilityIfNeeded(bus.delay, View.VISIBLE);
            }

            if (item.getDelay() > 3) {
                bus.delay.setTextColor(ContextCompat.getColor(mContext, R.color.primary_red));
            } else if (item.getDelay() > 0) {
                bus.delay.setTextColor(ContextCompat.getColor(mContext, R.color.primary_amber_dark));
            } else {
                bus.delay.setTextColor(ContextCompat.getColor(mContext, R.color.primary_green));
            }

            if (item.getDelay() == Config.BUS_STOP_DETAILS_NO_DELAY) {
                bus.delay.setText("");
            } else {
                bus.delay.setText(item.getDelay() + "'");
            }

            if (item.getVehicle() == 0) {
                ((ImageView) bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_3_image)).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_timeline));
                ((TextView) bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_3_text)).setText(mContext.getString(R.string.station_menu_course));
            } else {
                ((ImageView) bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_3_image)).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_map_white_48dp));
                ((TextView) bus.lineCard.findViewById(R.id.list_stations_detail_card_line_button_3_text)).setText(mContext.getString(R.string.map));
            }

            bus.line.setText(item.getLine());
            bus.heading.setText(mContext.getString(R.string.station_heading, item.getLastStation()));
            bus.departureTime.setText(item.getTime());

            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(item.getTime().split(":")[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(item.getTime().split(":")[1]));

                long difference = calendar.getTime().getTime() - new Date().getTime();
                difference = difference / 1000 / 60;

                if (difference < -10) difference += 1440;
                else if (difference < 0) difference = 0;

                if (difference < 60) {
                    bus.departureMinutes.setText(String.format(Locale.ITALY, "%d'", difference));
                } else {
                    bus.departureMinutes.setText("");
                }
            } catch (Exception e) {
                Utils.handleException(e);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private void setVisibilityIfNeeded(View view, int visibility) {
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }


    static final class ViewHolderHeader extends RecyclerView.ViewHolder {

        @BindView(R.id.stations_detail_munic)
        TextView overviewMunic;
        @BindView(R.id.stations_detail_lines)
        TextView overviewLines;

        private ViewHolderHeader(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    final class ViewHolderBus extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_stations_detail_line)
        TextView line;
        @BindView(R.id.list_stations_detail_delay)
        TextView delay;
        @BindView(R.id.list_stations_detail_delay_progress)
        ProgressBar delayProgress;
        @BindView(R.id.list_stations_detail_heading)
        TextView heading;
        @BindView(R.id.list_stations_detail_departure_time)
        TextView departureTime;
        @BindView(R.id.list_stations_detail_departure_minutes)
        TextView departureMinutes;

        @BindView(R.id.list_stations_detail_card_line)
        CardView lineCard;

        @BindView(R.id.list_stations_detail_card_line_reveal)
        LinearLayout reveal;
        @BindView(R.id.list_stations_detail_card_line_button_1)
        LinearLayout button1;
        @BindView(R.id.list_stations_detail_card_line_button_2)
        LinearLayout button2;
        @BindView(R.id.list_stations_detail_card_line_button_3)
        LinearLayout button3;

        private ViewHolderBus(View view) {
            super(view);

            ButterKnife.bind(this, view);

            lineCard.setOnClickListener(this);
            button1.setOnClickListener(this);
            button2.setOnClickListener(this);
            button3.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            BusStopDetail item = mItems.get(position);

            switch (v.getId()) {
                case R.id.list_stations_detail_card_line_button_1:
                    hideReveal(reveal);
                    item.setReveal(false);
                    break;
                case R.id.list_stations_detail_card_line_button_2:
                    Intent intent1 = new Intent(mContext, LineDetailActivity.class);
                    intent1.putExtra(Config.EXTRA_LINE_ID, item.getLineId());
                    mContext.startActivity(intent1);
                    break;
                case R.id.list_stations_detail_card_line_button_3:
                    Intent intent;

                    if (item.getVehicle() == 0) {
                        intent = new Intent(mContext, LineCourseActivity.class)
                                .putExtra(Config.EXTRA_STATION_ID, toIntArray(BusStopRealmHelper
                                        .getBusStopIdsFromGroup(mBusStopFamily)))
                                .putExtra("time", item.getTime())
                                .putExtra(Config.EXTRA_LINE_ID, item.getLineId());
                    } else {
                        intent = new Intent(mContext, MapActivity.class)
                                .putExtra(Config.EXTRA_VEHICLE, item.getVehicle())
                                .putExtra(Config.EXTRA_DISPLAY_BUS, true);
                    }

                    mContext.startActivity(intent);
                    break;
                default:
                    if (!item.isReveal()) {
                        showReveal(v.findViewById(R.id.list_stations_detail_card_line_reveal));

                        for (ViewHolderBus holder : mViews) {
                            if (holder.lineCard != null && !holder.lineCard.equals(v)) {
                                try {
                                    hideReveal(holder.lineCard.findViewById(
                                            R.id.list_stations_detail_card_line_reveal));
                                } catch (Exception ignored) {
                                }
                            }
                        }

                        for (BusStopDetail tempItem : mItems) {
                            tempItem.setReveal(false);
                        }

                        item.setReveal(true);
                    }
                    break;
            }
        }

        private void showReveal(View v) {
            if (v == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Animator anim = ViewAnimationUtils.createCircularReveal(v, 0, 0, 0, lineCard.getWidth());
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showIcon(button1, 0);
                        showIcon(button2, 1);
                        showIcon(button3, 2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        v.setVisibility(View.GONE);
                        v.findViewById(R.id.list_stations_detail_card_line_button_1).setVisibility(View.GONE);
                        v.findViewById(R.id.list_stations_detail_card_line_button_2).setVisibility(View.GONE);
                        v.findViewById(R.id.list_stations_detail_card_line_button_3).setVisibility(View.GONE);
                    }
                });

                v.setVisibility(View.VISIBLE);
                anim.start();
                return;
            }

            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(200);
            alphaAnimation.setInterpolator(new DecelerateInterpolator());
            alphaAnimation.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    showIcon(button1, 0);
                    showIcon(button2, 1);
                    showIcon(button3, 2);
                }
            });

            v.setVisibility(View.VISIBLE);
            v.startAnimation(alphaAnimation);
        }

        private void hideReveal(View v) {
            if (v == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Animator anim = ViewAnimationUtils.createCircularReveal(v, 0, 0, lineCard.getWidth(), 0);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                        v.findViewById(R.id.list_stations_detail_card_line_button_1).setVisibility(View.GONE);
                        v.findViewById(R.id.list_stations_detail_card_line_button_2).setVisibility(View.GONE);
                        v.findViewById(R.id.list_stations_detail_card_line_button_3).setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        v.setVisibility(View.GONE);
                        v.findViewById(R.id.list_stations_detail_card_line_button_1).setVisibility(View.GONE);
                        v.findViewById(R.id.list_stations_detail_card_line_button_2).setVisibility(View.GONE);
                        v.findViewById(R.id.list_stations_detail_card_line_button_3).setVisibility(View.GONE);
                    }
                });

                anim.start();

                return;
            }

            v.setVisibility(View.GONE);
            v.findViewById(R.id.list_stations_detail_card_line_button_1).setVisibility(View.GONE);
            v.findViewById(R.id.list_stations_detail_card_line_button_2).setVisibility(View.GONE);
            v.findViewById(R.id.list_stations_detail_card_line_button_3).setVisibility(View.GONE);
        }

        void showIcon(View view, int n) {
            view.setVisibility(View.INVISIBLE);

            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(70);

            TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 100.0f, 0.0f);
            translateAnimation.setDuration(220);

            AnimationSet animationSet = new AnimationSet(true);
            animationSet.setInterpolator(new DecelerateInterpolator());
            animationSet.addAnimation(alphaAnimation);
            animationSet.addAnimation(translateAnimation);
            animationSet.setFillAfter(false);
            animationSet.setStartOffset(n * 60L);
            animationSet.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setVisibility(View.VISIBLE);
                }
            });

            view.startAnimation(animationSet);
        }
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    static final class ViewHolderError extends RecyclerView.ViewHolder {

        private ViewHolderError(View view) {
            super(view);
        }
    }
}