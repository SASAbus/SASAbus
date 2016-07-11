package it.sasabz.android.sasabus.ui.intro.data;

import android.content.Intent;
import android.support.v4.content.ContextCompat;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.ui.MapActivity;
import it.sasabz.android.sasabus.ui.intro.AppIntro;

import java.util.ArrayList;

/**
 * Small intro which is used when new plan data is available and the user needs to wait till
 * the download finishes.
 *
 * @see IntroFragmentData
 * @author Alex Lardschneider
 */
public class IntroData extends AppIntro {

    @Override
    public void init() {
        addSlide(new IntroFragmentDataStandalone());

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this, R.color.primary_light_blue));

        setAnimationColors(colors);
    }

    @Override
    public void onDonePressed() {
        finishIntro();
    }

    /**
     * Finishes the intro screen and navigates to {@link MapActivity}.
     */
    private void finishIntro() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);

        finish();
    }
}
