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

import android.content.Intent;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.ui.MapActivity;
import it.sasabz.android.sasabus.ui.departure.DepartureActivity;
import it.sasabz.android.sasabus.ui.intro.AppIntro;

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
        colors.add(ContextCompat.getColor(this, R.color.material_blue_500));

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
        Intent intent = new Intent(this, DepartureActivity.class);
        startActivity(intent);

        finish();
    }
}
