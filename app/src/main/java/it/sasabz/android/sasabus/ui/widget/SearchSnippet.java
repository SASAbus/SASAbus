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

package it.sasabz.android.sasabus.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An extension to TextView which marks up search result snippets.
 * <p>
 * It looks for search terms surrounded by curly brace tokens e.g. “blah {Android} blah” and marks
 * the search term up as bold.
 */
public class SearchSnippet extends TextView {

    private static final Pattern PATTERN_SEARCH_TERM = Pattern.compile("(\\{[^\\}]+\\})", Pattern.DOTALL);

    public SearchSnippet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text)) {
            Matcher matcher = PATTERN_SEARCH_TERM.matcher(text);

            Editable ssb = new SpannableStringBuilder(text);
            Collection<String> hits = new ArrayList<>();

            while (matcher.find()) {
                hits.add(matcher.group(1));
            }

            for (String hit : hits) {
                int start = ssb.toString().indexOf(hit);
                int end = start + hit.length();

                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
                // delete the markup tokens
                ssb.delete(end - 1, end);
                ssb.delete(start, start + 1);
            }
            text = ssb;
        }

        super.setText(text, type);
    }
}
