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

package it.sasabz.android.sasabus.data.vdv.data;

import android.annotation.SuppressLint;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.sasabz.android.sasabus.data.vdv.model.VdvBusStop;
import timber.log.Timber;

/**
 * Represents the path (list of bus stops) a bus drives. The path is different for each variant of a
 * line.
 *
 * @author David Dejori
 */
public final class VdvPaths {

    private static Map<Integer, List<List<VdvBusStop>>> PATHS;

    private VdvPaths() {
    }

    static void loadPaths(JSONArray jPaths) throws Exception {
        @SuppressLint("UseSparseArrays")
        Map<Integer, List<List<VdvBusStop>>> pathsMap = new HashMap<>();

        // iterates through all the lines
        for (int i = 0; i < jPaths.length(); i++) {

            JSONArray jVariants = jPaths.getJSONObject(i).getJSONArray("variants");
            int line = jPaths.getJSONObject(i).getInt("line_id");

            // HashMap with variants
            List<List<VdvBusStop>> variants = new ArrayList<>();

            // iterates through all the variants
            for (int j = 0; j < jVariants.length(); j++) {
                List<VdvBusStop> path = new ArrayList<>();
                JSONArray jPath = jVariants.getJSONObject(j).getJSONArray("path");

                for (int k = 0; k < jPath.length(); k++) {
                    path.add(new VdvBusStop(jPath.getInt(k)));
                }

                variants.add(Collections.unmodifiableList(path));
            }

            pathsMap.put(line, variants);
        }

        PATHS = Collections.unmodifiableMap(pathsMap);
    }

    public static Map<Integer, List<List<VdvBusStop>>> getPaths() {
        VdvHandler.blockTillLoaded();

        return PATHS;
    }

    public static List<VdvBusStop> getPath(int lineId, int variantId) {
        VdvHandler.blockTillLoaded();

        List<List<VdvBusStop>> path = PATHS.get(lineId);
        if (path == null) {
            Timber.e("Requesting path failed: line=%s, variant=%s", lineId, variantId);
            return Collections.emptyList();
        }

        return new ArrayList<>(path.get(variantId - 1));
    }
}
