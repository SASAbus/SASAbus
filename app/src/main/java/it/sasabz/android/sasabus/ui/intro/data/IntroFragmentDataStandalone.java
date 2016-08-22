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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.ui.intro.AppIntro;

/**
 * Fragment which can be used as a standalone fragment to download plan data when an update is
 * available and only this fragment needs to be shown in the intro.
 *
 * @see IntroData
 * @see IntroFragmentData
 *
 * @author Alex Lardschneider
 */
public class IntroFragmentDataStandalone extends IntroFragmentData {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_data, container, false);

        setRetainInstance(true);

        ((AppIntro) getActivity()).hideButton();

        TextView title = (TextView) view.findViewById(R.id.intro_title);
        TextView description = (TextView) view.findViewById(R.id.intro_description);

        title.setText(R.string.intro_offline_data_update_title);
        description.setText(R.string.intro_offline_data_update_sub);

        progressBar = (ProgressBar) view.findViewById(R.id.intro_data_progress);
        successImage = (ImageView) view.findViewById(R.id.intro_data_done);
        errorButton = (Button) view.findViewById(R.id.intro_data_error);

        successImage.setOnClickListener(this);

        if (savedInstanceState == null) {
            startDownload();
        }

        return view;
    }
}