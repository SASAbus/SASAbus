package it.sasabz.android.sasabus.provider.apis;

import it.sasabz.android.sasabus.provider.model.BusStop;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            Utils.handleException(e);
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
