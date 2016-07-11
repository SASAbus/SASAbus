package it.sasabz.android.sasabus.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.SparseArray;

public final class Buses {

    private static final String[] VENDORS = {
            "Solaris",
            "Mercedes-Benz",
            "BredaMenarinibus",
            "Iveco",
            "MAN"
    };

    private static final String[] MODELS = {
            "Urbino 18",
            "Urbino 12",
            "Citaro O 530 BZ",
            "Vivacity+ 231 MU/3P/E5 EEV",
            "Citaro O 530 GN",
            "Citaro O 530 K",
            "M 240 R/LU/3P/CNG",
            "Irisbus 491E.10.22 CityClass CNG",
            "M 221/1/LU/3P",
            "313 NG/CNG Lion's City",
            "M 231/2/CU/2P E3 CNG",
            "Vivacity 231 MU/3P/E5",
            "Avancity NU/3P/CNG",
            "Citaro O 530 GN",
            "Citaro O 530 N",
            "313 NG/CNG",
            "Irisbus 491E.12.22 CityClass",
            "NL 313",
            "NG 313",
            "M 240 LS/3P"
    };

    private static final String[] FUEL_IT = {
            "Idrogeno",
            "Diesel",
            "Metano"
    };

    private static final String[] FUEL_DE = {
            "Wasserstoff",
            "Diesel",
            "Methan"
    };

    private static final String[] FUEL_EN = {
            "Hydrogen",
            "Diesel",
            "Methane"
    };

    private static final String[] COLOR_IT = {
            "Giallo",
            "Arancione",
            "Bianco e viola"
    };

    private static final String[] COLOR_DE = {
            "Gelb",
            "Orange",
            "Weiß und violett"
    };

    private static final String[] COLOR_EN = {
            "Yellow",
            "Orange",
            "White and purple"
    };

    private static final SparseArray<Bus> BUSES = new SparseArray<>();
    
    static {
        BUSES.put(438, new Bus(0, 0, 1, 0, 3));
        BUSES.put(437, new Bus(0, 0, 1, 0, 3));
        BUSES.put(436, new Bus(0, 1, 1, 0, 2));
        BUSES.put(435, new Bus(0, 1, 1, 0, 2));
        BUSES.put(434, new Bus(0, 1, 1, 0, 2));
        BUSES.put(433, new Bus(0, 1, 1, 0, 2));
        BUSES.put(432, new Bus(1, 2, 0, 2, 1));
        BUSES.put(431, new Bus(1, 2, 0, 2, 1));
        BUSES.put(430, new Bus(1, 2, 0, 2, 1));
        BUSES.put(429, new Bus(1, 2, 0, 2, 1));
        BUSES.put(428, new Bus(1, 2, 0, 2, 1));
        BUSES.put(427, new Bus(2, 3, 1, 0, 5));
        BUSES.put(426, new Bus(2, 3, 1, 0, 5));
        BUSES.put(425, new Bus(2, 3, 1, 0, -1));
        BUSES.put(424, new Bus(2, 3, 1, 0, -1));
        BUSES.put(423, new Bus(2, 3, 1, 0, 5));
        BUSES.put(422, new Bus(2, 3, 1, 0, 5));
        BUSES.put(421, new Bus(2, 3, 1, 0, 5));
        BUSES.put(419, new Bus(1, 4, 1, 0, 9));
        BUSES.put(418, new Bus(1, 4, 1, 0, 9));
        BUSES.put(417, new Bus(1, 4, 1, 0, 9));
        BUSES.put(416, new Bus(1, 4, 1, 0, 9));
        BUSES.put(415, new Bus(1, 4, 1, 0, 9));
        BUSES.put(414, new Bus(1, 4, 1, 0, 9));
        BUSES.put(413, new Bus(0, 1, 1, 0, 2));
        BUSES.put(412, new Bus(0, 1, 1, 0, 2));
        BUSES.put(411, new Bus(0, 1, 1, 0, 2));
        BUSES.put(410, new Bus(0, 1, 1, 0, 2));
        BUSES.put(409, new Bus(0, 1, 1, 0, 2));
        BUSES.put(408, new Bus(0, 1, 1, 0, 2));
        BUSES.put(407, new Bus(0, 1, 1, 0, 2));
        BUSES.put(406, new Bus(0, 1, 1, 0, 2));
        BUSES.put(405, new Bus(0, 1, 1, 0, 2));
        BUSES.put(398, new Bus(1, 5, 1, 0, 10));
        BUSES.put(397, new Bus(1, 5, 1, 0, 10));
        BUSES.put(396, new Bus(1, 5, 1, 0, 10));
        BUSES.put(395, new Bus(1, 5, 1, 0, 10));
        BUSES.put(394, new Bus(1, 5, 1, 0, 10));
        BUSES.put(393, new Bus(1, 5, 1, 0, 10));
        BUSES.put(382, new Bus(2, 11, 1, 1, 14));
        BUSES.put(381, new Bus(2, 12, 2, 1, 19));
        BUSES.put(372, new Bus(2, 12, 2, 1, 13));
        BUSES.put(371, new Bus(2, 12, 2, 1, 13));
        BUSES.put(369, new Bus(4, 15, 2, 1, 7));
        BUSES.put(364, new Bus(2, 6, 2, 1, 11));
        BUSES.put(363, new Bus(2, 6, 2, 1, 11));
        BUSES.put(362, new Bus(2, 6, 2, 1, 11));
        BUSES.put(361, new Bus(2, 6, 2, 1, 11));
        BUSES.put(360, new Bus(2, 6, 2, 1, 11));
        BUSES.put(359, new Bus(2, 6, 2, 1, 11));
        BUSES.put(358, new Bus(2, 6, 2, 1, 11));
        BUSES.put(357, new Bus(2, 6, 2, 1, 11));
        BUSES.put(356, new Bus(2, 6, 2, 1, 11));
        BUSES.put(355, new Bus(2, 6, 2, 1, 11));
        BUSES.put(354, new Bus(4, 9, 2, 1, -1));
        BUSES.put(353, new Bus(4, 9, 2, 1, -1));
        BUSES.put(352, new Bus(4, 9, 2, 1, -1));
        BUSES.put(351, new Bus(4, 9, 2, 1, -1));
        BUSES.put(350, new Bus(4, 9, 2, 1, -1));
        BUSES.put(349, new Bus(4, 9, 2, 1, -1));
        BUSES.put(348, new Bus(2, 6, 2, 1, 11));
        BUSES.put(347, new Bus(2, 6, 2, 1, 11));
        BUSES.put(346, new Bus(2, 6, 2, 1, 11));
        BUSES.put(345, new Bus(2, 6, 2, 1, 11));
        BUSES.put(344, new Bus(2, 6, 2, 1, 11));
        BUSES.put(343, new Bus(2, 6, 2, 1, 11));
        BUSES.put(342, new Bus(2, 6, 2, 1, 11));
        BUSES.put(337, new Bus(2, 6, 2, 1, 11));
        BUSES.put(334, new Bus(2, 6, 2, 1, 11));
        BUSES.put(333, new Bus(2, 6, 2, 1, 11));
        BUSES.put(331, new Bus(2, 6, 2, 1, 11));
        BUSES.put(330, new Bus(2, 6, 2, 1, 11));
        BUSES.put(328, new Bus(3, 7, 2, 1, 12));
        BUSES.put(327, new Bus(3, 7, 2, 1, 12));
        BUSES.put(326, new Bus(3, 7, 2, 1, 12));
        BUSES.put(325, new Bus(3, 7, 2, 1, 12));
        BUSES.put(324, new Bus(3, 7, 2, 1, 12));
        BUSES.put(323, new Bus(3, 7, 2, 1, 12));
        BUSES.put(322, new Bus(3, 7, 2, 1, 12));
        BUSES.put(321, new Bus(3, 7, 2, 1, 12));
        BUSES.put(317, new Bus(3, 7, 2, 1, 12));
        BUSES.put(316, new Bus(3, 7, 2, 1, 12));
        BUSES.put(315, new Bus(3, 7, 2, 1, 12));
        BUSES.put(314, new Bus(3, 7, 2, 1, 12));
        BUSES.put(313, new Bus(3, 7, 2, 1, 12));
        BUSES.put(308, new Bus(3, 7, 2, 1, 12));
        BUSES.put(306, new Bus(3, 7, 2, 1, 12));
        BUSES.put(305, new Bus(3, 7, 2, 1, 12));
        BUSES.put(304, new Bus(3, 7, 2, 1, 12));
        BUSES.put(303, new Bus(3, 7, 2, 1, 12));
        BUSES.put(302, new Bus(3, 7, 2, 1, 12));
        BUSES.put(301, new Bus(3, 7, 2, 1, 12));
        BUSES.put(300, new Bus(3, 7, 2, 1, 12));
        BUSES.put(45, new Bus(4, 17, 1, 1, -1));
        BUSES.put(44, new Bus(4, 17, 1, 1, -1));
    }

    private Buses() {
    }

    @Nullable
    public static Vehicle getBus(Context context, int id) {
        if (BUSES.indexOfKey(id) < 0) {
            return null;
        }

        Bus bus = BUSES.get(id);

        String locale = context.getResources().getConfiguration().locale.toString()
                .toLowerCase().substring(0, 2);

        switch (locale) {
            case "de":
                return new Vehicle(
                        VENDORS[bus.getVendor()],
                        MODELS[bus.getModel()],
                        FUEL_DE[bus.getFuel()],
                        COLOR_DE[bus.getColor()],
                        bus.getGroup()
                );
            case "en":
                return new Vehicle(
                        VENDORS[bus.getVendor()],
                        MODELS[bus.getModel()],
                        FUEL_EN[bus.getFuel()],
                        COLOR_EN[bus.getColor()],
                        bus.getGroup()
                );
            default:
                return new Vehicle(
                        VENDORS[bus.getVendor()],
                        MODELS[bus.getModel()],
                        FUEL_IT[bus.getFuel()],
                        COLOR_IT[bus.getColor()],
                        bus.getGroup()
                );
        }
    }

    private static final class Bus {

        private final int vendor;
        private final int model;
        private final int fuel;
        private final int color;
        private final int group;

        private Bus(int vendor, int model, int fuel, int color, int group) {
            this.vendor = vendor;
            this.model = model;
            this.fuel = fuel;
            this.color = color;
            this.group = group;
        }
        
        public int getVendor() {
            return vendor;
        }

        public int getModel() {
            return model;
        }

        public int getFuel() {
            return fuel;
        }

        public int getColor() {
            return color;
        }

        public int getGroup() {
            return group;
        }
    }
}
