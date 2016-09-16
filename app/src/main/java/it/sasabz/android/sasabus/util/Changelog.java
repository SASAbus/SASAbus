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

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import it.sasabz.android.sasabus.R;

/**
 * Used to display a changelog dialog when the user updates the app. The changelog gets loaded from
 * the json files located in /assets/changelog.
 *
 * @author Alex Lardschneider
 */
public final class Changelog {

    private Changelog() {
    }

    /**
     * Create a dialog containing (parts of the) change log.
     */
    public static void showDialog(Context context) {
        WebView webView = new WebView(context);
        webView.loadDataWithBaseURL(null, getChangelog(context), "text/html", "UTF-8", null);

        new AlertDialog.Builder(context, R.style.DialogStyle)
                .setTitle(context.getResources().getString(R.string.dialog_changelog_title))
                .setView(webView)
                .setPositiveButton(context.getResources().getString(android.R.string.ok), (dialog, which) -> dialog.dismiss())
                .setNegativeButton(context.getResources().getString(R.string.dialog_button_dont_show_again), (dialog, which) -> {
                    SettingsUtils.disableChangelog(context);
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    /**
     * Returns the changelog.
     */
    private static String getChangelog(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append("<head>");
        sb.append("<style type=\"text/css\">");
        sb.append(context.getString(R.string.changelog_css));
        sb.append("</style>");
        sb.append("</head>");

        sb.append("<body>");

        try {
            String content = context.getString(R.string.changelog_content);
            JSONArray array = new JSONArray(content);

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);

                JSONArray changes = object.getJSONArray("CHANGES");

                sb.append("<p>").append(object.getString("VERSION_NAME")).append("</p>");
                sb.append("<ul>");

                for (int j = 0; j < changes.length(); j++) {
                    sb.append("<li>").append(changes.getString(j)).append("</li>");
                }

                sb.append("</ul>");

                // Only show the last 3 changelog entries.
                if (i == 2) break;
            }

            sb.append("</body>");
            sb.append("</html>");
        } catch (Exception e) {
            Utils.logException(e);
        }

        return sb.toString();
    }
}