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

package it.sasabz.android.sasabus.ui.intro.data;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.NetUtils;
import it.sasabz.android.sasabus.data.vdv.PlannedData;
import it.sasabz.android.sasabus.ui.intro.AppIntro;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Special intro fragment which handles downloading of the plan data. If the plan data download
 * hasn't succeeded yet, either because it isn't completed yet or has failed, the user cannot
 * swipe past it.
 *
 * @author Alex Lardschneider
 */
public class IntroFragmentData extends Fragment implements Observer<Void>, View.OnClickListener {

    private Subscription subscription;

    ProgressBar progressBar;
    ImageView successImage;
    Button errorButton;

    private boolean error;
    private boolean success;
    private boolean downloadRunning = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_data, container, false);

        setRetainInstance(true);

        TextView title = (TextView) view.findViewById(R.id.intro_title);
        TextView description = (TextView) view.findViewById(R.id.intro_description);

        title.setText(R.string.intro_offline_data_title);
        description.setText(R.string.intro_offline_data_sub);

        progressBar = (ProgressBar) view.findViewById(R.id.intro_data_progress);
        successImage = (ImageView) view.findViewById(R.id.intro_data_done);
        errorButton = (Button) view.findViewById(R.id.intro_data_error);

        successImage.setOnClickListener(this);
        errorButton.setOnClickListener(this);

        if (savedInstanceState == null) {
            startDownload();
        } else {
            if (success) {
                errorButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

                successImage.setVisibility(View.VISIBLE);
            } else if (error) {
                progressBar.setVisibility(View.GONE);
                successImage.setVisibility(View.GONE);

                errorButton.setVisibility(View.VISIBLE);
            } else {
                successImage.setVisibility(View.GONE);
                errorButton.setVisibility(View.GONE);

                progressBar.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    @Override
    public void setMenuVisibility(boolean visible) {
        super.setMenuVisibility(visible);

        if (getActivity() != null) {
            if (visible) {
                if ((downloadRunning || error) && !success) {
                    ((AppIntro) getActivity()).setNextPageSwipeLock(true);
                } else {
                    ((AppIntro) getActivity()).setNextPageSwipeLock(false);
                }
            } else {
                ((AppIntro) getActivity()).setNextPageSwipeLock(false);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.intro_data_done:
            case R.id.intro_data_error:
                if (error) {
                    successImage.setVisibility(View.GONE);
                    errorButton.setVisibility(View.GONE);

                    progressBar.setVisibility(View.VISIBLE);
                    ViewCompat.setAlpha(progressBar, 0);

                    progressBar.animate()
                            .alpha(1)
                            .setStartDelay(150)
                            .setInterpolator(new DecelerateInterpolator())
                            .setDuration(500)
                            .start();

                    new Handler().postDelayed(this::startDownload, 500);

                    error = false;
                }
                break;
        }
    }

    @Override
    public void onNext(Void file) {
    }

    @Override
    public void onCompleted() {
        Timber.e("onCompleted()");

        downloadRunning = false;
        success = true;
        error = false;

        progressBar.animate()
                .alpha(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(400)
                .start();

        successImage.setVisibility(View.VISIBLE);
        successImage.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                R.drawable.ic_check_white_48dp));

        ViewCompat.setAlpha(successImage, 0);
        ViewCompat.setScaleX(successImage, 0);
        ViewCompat.setScaleY(successImage, 0);

        successImage.animate()
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .setStartDelay(450)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .start();

        ((AppIntro) getActivity()).setNextPageSwipeLock(false);
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable != null) {
            Utils.logException(throwable);
        }

        error = true;
        success = false;
        downloadRunning = false;

        progressBar.animate()
                .alpha(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(400)
                .start();

        errorButton.setVisibility(View.VISIBLE);

        ViewCompat.setAlpha(errorButton, 0);

        errorButton.animate()
                .alpha(1)
                .setStartDelay(450)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .start();
    }

    void startDownload() {
        downloadRunning = true;

        if (!NetUtils.isOnline(getActivity())) {
            onError(null);

            return;
        }

        subscription = PlannedData.download(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }
}