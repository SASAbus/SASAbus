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

package it.sasabz.android.sasabus.util;

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public final class Strings {

    public static final String DEFAULT_DELIMITER = ",";

    private Strings() {
    }

    @NonNull
    public static <T> CharSequence arrayToString(@NonNull T[] array, @NonNull String delimiter) {
        Preconditions.checkNotNull(array, "array == null");
        Preconditions.checkNotNull(delimiter, "delimiter == null");

        return TextUtils.join(delimiter, array);
    }

    @NonNull
    public static <T> String listToString(@NonNull List<T> list, @NonNull String delimiter) {
        Preconditions.checkNotNull(list, "List cannot be null");
        Preconditions.checkNotNull(delimiter, "Delimiter cannot be null");

        return TextUtils.join(delimiter, list);
    }

    @NonNull
    public static List<Integer> stringToList(@NonNull String s, @NonNull String delimiter) {
        List<Integer> list = new ArrayList<>();

        if (s.isEmpty()) return list;

        String[] split = s.split(delimiter);

        for (String s1 : split) {
            list.add(Integer.valueOf(s1));
        }

        return list;
    }

    /**
     * Formats a search query to be used with {@link it.sasabz.android.sasabus.ui.widget.SearchSnippet}.
     * A "{" marks the beginning of a found query string and "}" marks the end of it.
     *
     * @param name   the title of the result
     * @param query  the query string to search for and format
     * @param format the format start and end chars, have to be an even length
     * @return the formatted string if {@code title} contains {@code query}, otherwise
     * the unformatted raw {@code title} string.
     */
    @NonNull
    public static String formatQuery(String name, String query, String... format) {
        Preconditions.checkNotNull(name, "Format title cannot be null");
        Preconditions.checkNotNull(query, "Format query cannot be null");

        if (name.toLowerCase().contains(query.toLowerCase())) {
            int indexStart = name.toLowerCase().indexOf(query.toLowerCase());
            int indexEnd = indexStart + query.length() + 1;

            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.insert(indexStart, format[0]);
            sb.insert(indexEnd, format[1]);

            return sb.toString();
        }

        return name;
    }

    /**
     * Removes all non UTF-8 and non-printable characters from a string.
     *
     * @param s the string to sanitize
     * @return a string without non-printable chars
     */
    static CharSequence sanitizeString(String s) {
        return Html.fromHtml(s).toString().replaceAll("\\p{C}", "");
    }
}
