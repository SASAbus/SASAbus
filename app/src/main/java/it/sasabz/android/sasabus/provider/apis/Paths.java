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

package it.sasabz.android.sasabus.provider.apis;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.sasabz.android.sasabus.provider.model.BusStop;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Represents the path (list of bus stops) a bus drives. The path is different for each variant of a
 * line.
 */
public final class Paths {

    public static final HashMap<Integer, Integer> VARIANTS = new HashMap<>();
    private static final HashMap<Integer, HashMap<Integer, List<BusStop>>> PATHS = new HashMap<>();

    private Paths() {
    }

    static void loadPaths(File dir) {
        try {
            JSONArray jPaths = new JSONArray(IOUtils.readFileAsString(new File(dir.getAbsolutePath(), "/LID_VERLAUF.json")));

            // iterates through all the lines
            for (int i = 0; i < jPaths.length(); i++) {

                JSONArray jVariants = jPaths.getJSONObject(i).getJSONArray("varlist");
                int line = Integer.parseInt(jPaths.getJSONObject(i).getString("LI_NR"));

                // HashMap with variants
                HashMap<Integer, List<BusStop>> variants = new HashMap<>();

                // iterates through all the variants
                for (int j = 0; j < jVariants.length(); j++) {

                    List<BusStop> path = new ArrayList<>();
                    JSONArray jPath = jVariants.getJSONObject(j).getJSONArray("routelist");

                    for (int k = 0; k < jPath.length(); k++) {
                        path.add(new BusStop(jPath.getInt(k)));
                    }

                    variants.put(j + 1, path);
                }

                VARIANTS.put(line, variants.size());
                PATHS.put(line, variants);
            }
        } catch (JSONException | IOException e) {
            Utils.logException(e);
        }

        //noinspection CallToSystemGC
        System.gc();
    }

    public static HashMap<Integer, HashMap<Integer, List<BusStop>>> getPaths() {
        return PATHS;
    }

    public static List<BusStop> getPath(int line, int variant) {
        return PATHS.get(line).get(variant);
    }
}
