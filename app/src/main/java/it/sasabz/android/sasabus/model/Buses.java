package it.sasabz.android.sasabus.model;

import android.support.annotation.Nullable;
import android.util.SparseArray;

public final class Buses {

    private static final SparseArray<Bus> BUSES = new SparseArray<>();

    static {
        Vehicle spr = new Vehicle("Mercedes-Benz", "Sprinter O 513 NFXL", 1, 1, 90, "spr");
        Vehicle u18 = new Vehicle("Solaris", "Urbino 18", 1, 1, 90, "u18");
        Vehicle u12 = new Vehicle("Solaris", "Urbino 12", 1, 1, 90, "u12");
        Vehicle h2 = new Vehicle("Mercedes-Benz", "Citaro O 530 BZ", 0, 0, 0, "h2");
        Vehicle vc = new Vehicle("BredaMenarinibus", "Vivacity+ 231 MU/3P/E5 EEV", 1, 1, 90, "vc");
        Vehicle c18 = new Vehicle("Mercedes-Benz", "Citaro O 530 GN", 1, 1, 90, "c18");
        Vehicle c10 = new Vehicle("Mercedes-Benz", "Citaro O 530 K", 1, 1, 90, "c10");
        Vehicle vco = new Vehicle("BredaMenarinibus", "Vivacity 231 MU/3P/E5", 1, 2, 110, "vco");
        Vehicle v88 = new Vehicle("Mercedes-Benz", "Citaro O 530 N", 1, 2, 90, "v88");
        Vehicle v87 = new Vehicle("Mercedes-Benz", "Citaro O 530 GN", 1, 2, 90, "v87");
        Vehicle mbgn = new Vehicle("Mercedes-Benz", "Citaro O 530 GN", 1, 2, 90, "mbgn");
        Vehicle manlc = new Vehicle("MAN", "Lion\'s City 313 NG/CNG", 2, 2, 110, "manlc");
        Vehicle ac = new Vehicle("BredaMenarinibus", "Avancity NU/3P/CNG", 2, 2, 110, "ac");
        Vehicle bmb = new Vehicle("BredaMenarinibus", "Monocar 231/2/CU/2P E3 CNG", 2, 2, 110, "bmb");
        Vehicle v69 = new Vehicle("MAN", "313 NG/CNG", 2, 2, 110, "v69");
        Vehicle m240 = new Vehicle("BredaMenarinibus", "Monocar 240", 2, 2, 110, "m240");
        Vehicle ib = new Vehicle("Iveco", "Irisbus 491E CityClass CNG", 2, 2, 110, "ib");
        Vehicle s4x = new Vehicle("MAN", "NL 313", 1, 2, 160, "s4x");
        Vehicle l4x = new Vehicle("MAN", "NG 313", 1, 2, 160, "l4x");

        BUSES.put(439, new Bus(439, spr));
        BUSES.put(438, new Bus(438, u18));
        BUSES.put(437, new Bus(437, u18));
        BUSES.put(436, new Bus(436, u12));
        BUSES.put(435, new Bus(435, u12));
        BUSES.put(434, new Bus(434, u12));
        BUSES.put(433, new Bus(433, u12));
        BUSES.put(432, new Bus(432, h2));
        BUSES.put(431, new Bus(431, h2));
        BUSES.put(430, new Bus(430, h2));
        BUSES.put(429, new Bus(429, h2));
        BUSES.put(428, new Bus(428, h2));
        BUSES.put(427, new Bus(427, vc));
        BUSES.put(426, new Bus(426, vc));
        BUSES.put(425, new Bus(425, vc));
        BUSES.put(424, new Bus(424, vc));
        BUSES.put(423, new Bus(423, vc));
        BUSES.put(422, new Bus(422, vc));
        BUSES.put(421, new Bus(421, vc));
        BUSES.put(420, new Bus(420, vc));
        BUSES.put(419, new Bus(419, c18));
        BUSES.put(418, new Bus(418, c18));
        BUSES.put(417, new Bus(417, c18));
        BUSES.put(416, new Bus(416, c18));
        BUSES.put(415, new Bus(415, c18));
        BUSES.put(414, new Bus(414, c18));
        BUSES.put(413, new Bus(413, u12));
        BUSES.put(412, new Bus(412, u12));
        BUSES.put(411, new Bus(411, u12));
        BUSES.put(410, new Bus(410, u12));
        BUSES.put(409, new Bus(409, u12));
        BUSES.put(408, new Bus(408, u12));
        BUSES.put(407, new Bus(407, u12));
        BUSES.put(406, new Bus(406, u12));
        BUSES.put(405, new Bus(405, u12));
        BUSES.put(404, new Bus(404, u12));
        BUSES.put(403, new Bus(403, u12));
        BUSES.put(402, new Bus(402, u12));
        BUSES.put(401, new Bus(401, u12));
        BUSES.put(400, new Bus(400, u12));
        BUSES.put(399, new Bus(399, u12));
        BUSES.put(398, new Bus(398, c10));
        BUSES.put(397, new Bus(397, c10));
        BUSES.put(396, new Bus(396, c10));
        BUSES.put(395, new Bus(395, c10));
        BUSES.put(394, new Bus(394, c10));
        BUSES.put(393, new Bus(393, c10));
        BUSES.put(389, new Bus(389, vco));
        BUSES.put(388, new Bus(388, v88));
        BUSES.put(387, new Bus(387, v87));
        BUSES.put(384, new Bus(384, vco));
        BUSES.put(383, new Bus(383, vco));
        BUSES.put(382, new Bus(382, vco));
        BUSES.put(381, new Bus(381, vco));
        BUSES.put(380, new Bus(380, vco));
        BUSES.put(379, new Bus(379, manlc));
        BUSES.put(378, new Bus(378, manlc));
        BUSES.put(377, new Bus(377, manlc));
        BUSES.put(376, new Bus(376, manlc));
        BUSES.put(373, new Bus(373, ac));
        BUSES.put(372, new Bus(372, ac));
        BUSES.put(371, new Bus(371, ac));
        BUSES.put(370, new Bus(370, bmb));
        BUSES.put(369, new Bus(369, v69));
        BUSES.put(368, new Bus(368, m240));
        BUSES.put(367, new Bus(367, m240));
        BUSES.put(366, new Bus(366, m240));
        BUSES.put(365, new Bus(365, m240));
        BUSES.put(364, new Bus(364, m240));
        BUSES.put(363, new Bus(363, m240));
        BUSES.put(362, new Bus(362, m240));
        BUSES.put(361, new Bus(361, m240));
        BUSES.put(360, new Bus(360, m240));
        BUSES.put(359, new Bus(359, m240));
        BUSES.put(358, new Bus(358, m240));
        BUSES.put(357, new Bus(357, m240));
        BUSES.put(356, new Bus(356, m240));
        BUSES.put(355, new Bus(355, m240));
        BUSES.put(354, new Bus(354, manlc));
        BUSES.put(353, new Bus(353, manlc));
        BUSES.put(352, new Bus(352, manlc));
        BUSES.put(351, new Bus(351, manlc));
        BUSES.put(350, new Bus(350, manlc));
        BUSES.put(349, new Bus(349, manlc));
        BUSES.put(348, new Bus(348, m240));
        BUSES.put(347, new Bus(347, m240));
        BUSES.put(346, new Bus(346, m240));
        BUSES.put(345, new Bus(345, m240));
        BUSES.put(344, new Bus(344, m240));
        BUSES.put(343, new Bus(343, m240));
        BUSES.put(342, new Bus(342, m240));
        BUSES.put(341, new Bus(341, bmb));
        BUSES.put(340, new Bus(340, bmb));
        BUSES.put(339, new Bus(339, bmb));
        BUSES.put(338, new Bus(338, bmb));
        BUSES.put(337, new Bus(337, bmb));
        BUSES.put(336, new Bus(336, m240));
        BUSES.put(335, new Bus(335, m240));
        BUSES.put(334, new Bus(334, m240));
        BUSES.put(333, new Bus(333, m240));
        BUSES.put(332, new Bus(332, m240));
        BUSES.put(331, new Bus(331, m240));
        BUSES.put(328, new Bus(328, ib));
        BUSES.put(327, new Bus(327, ib));
        BUSES.put(326, new Bus(326, ib));
        BUSES.put(325, new Bus(325, ib));
        BUSES.put(324, new Bus(324, ib));
        BUSES.put(323, new Bus(323, ib));
        BUSES.put(322, new Bus(322, ib));
        BUSES.put(321, new Bus(321, ib));
        BUSES.put(320, new Bus(320, mbgn));
        BUSES.put(319, new Bus(319, mbgn));
        BUSES.put(318, new Bus(318, ib));
        BUSES.put(317, new Bus(317, ib));
        BUSES.put(316, new Bus(316, ib));
        BUSES.put(315, new Bus(315, ib));
        BUSES.put(314, new Bus(314, ib));
        BUSES.put(313, new Bus(313, ib));
        BUSES.put(308, new Bus(308, ib));
        BUSES.put(307, new Bus(307, ib));
        BUSES.put(306, new Bus(306, ib));
        BUSES.put(305, new Bus(305, ib));
        BUSES.put(304, new Bus(304, ib));
        BUSES.put(303, new Bus(303, ib));
        BUSES.put(302, new Bus(302, ib));
        BUSES.put(301, new Bus(301, ib));
        BUSES.put(300, new Bus(300, ib));
        BUSES.put(299, new Bus(299, ib));
        BUSES.put(45, new Bus(45, s4x));
        BUSES.put(44, new Bus(44, s4x));
        BUSES.put(43, new Bus(43, s4x));
        BUSES.put(42, new Bus(42, l4x));
        BUSES.put(41, new Bus(41, l4x));
    }

    private Buses() {
    }

    @Nullable
    public static Bus getBus(int id) {
        if (BUSES.indexOfKey(id) < 0) {
            return null;
        }

        return BUSES.get(id);
    }
}