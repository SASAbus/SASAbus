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

package it.sasabz.android.sasabus.ui.intro;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.sasabz.android.sasabus.R;

/**
 * Default intro fragment, which display a title at the top, an image centered in the fragment and
 * a subtitle at the bottom.
 *
 * @author Alex Lardschneider
 */
public class AppIntroFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_DRAWABLE = "drawable";

    private int drawable;

    private CharSequence title;
    private CharSequence description;

    public static AppIntroFragment newInstance(CharSequence title, CharSequence description,
                                                int imageDrawable) {
        AppIntroFragment sampleSlide = new AppIntroFragment();

        Bundle args = new Bundle();
        args.putCharSequence(ARG_TITLE, title);
        args.putCharSequence(ARG_DESC, description);
        args.putInt(ARG_DRAWABLE, imageDrawable);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && !getArguments().isEmpty()) {
            drawable = getArguments().getInt(ARG_DRAWABLE);
            title = getArguments().getCharSequence(ARG_TITLE);
            description = getArguments().getCharSequence(ARG_DESC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro, container, false);

        TextView title = (TextView) view.findViewById(R.id.intro_title);
        TextView description = (TextView) view.findViewById(R.id.intro_description);
        ImageView image = (ImageView) view.findViewById(R.id.intro_image);
        LinearLayout main = (LinearLayout) view.findViewById(R.id.intro_main);

        title.setText(this.title);
        description.setText(this.description);

        image.setImageDrawable(ContextCompat.getDrawable(getActivity(), drawable));
        main.setBackgroundColor(Color.TRANSPARENT);

        return view;
    }

}
