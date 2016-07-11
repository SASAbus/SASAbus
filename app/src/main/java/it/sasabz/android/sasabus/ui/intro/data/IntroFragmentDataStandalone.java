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