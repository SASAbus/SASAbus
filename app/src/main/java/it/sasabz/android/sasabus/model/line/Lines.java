package it.sasabz.android.sasabus.model.line;

import android.content.Context;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.util.LogUtils;

/**
 * @author David Dejori
 */
public final class Lines {

    private Lines() {
    }

    public static String lidToName(int id) {
        if (id >= 1000 && id != 1071 && id != 1072 && id != 1101 && id != 1102) {
            return String.valueOf(id - 1000);
        } else if (id == 1071) {
            return "7A";
        } else if (id == 1072) {
            return "7B";
        } else if (id == 1101) {
            return "10A";
        } else if (id == 1102) {
            return "10B";
        }

        return String.valueOf(id);
    }

    public static final int[] lineIds = {
            1001,
            1003,
            1005,
            1006,
            1071,
            1072,
            1008,
            1009,
            1101,
            1102,
            1011,
            1012,
            1014,
            110,
            111,
            112,
            116,
            117,
            1153,
            183,
            201,
            202,
            1,
            2,
            3,
            4,
            6,
            146,
            211,
            212,
            213,
            214,
            215,
            221,
            222
    };

    public static final int[] checkBoxesId = {
            100001,
            100002,
            1001,
            1003,
            1005,
            1006,
            1071,
            1072,
            1008,
            1009,
            1101,
            1102,
            1011,
            1012,
            1014,
            110,
            111,
            112,
            116,
            117,
            1153,
            183,
            201,
            202,
            1,
            2,
            3,
            4,
            6,
            146,
            211,
            212,
            213,
            214,
            215,
            221,
            222
    };

    private static String[] sNames;

    public static String[] getNames(Context context) {
        if (sNames != null) {
            return sNames;
        }
        sNames = new String[]{
                context.getString(R.string.all),
                context.getString(R.string.none),
                "1",
                "3",
                "5",
                "6",
                "7A",
                "7B",
                "8",
                "9",
                "10A",
                "10B",
                "11",
                "12",
                "14",
                "110",
                "111",
                "112",
                "116",
                "117",
                "153",
                "183",
                "201",
                "202",
                "1",
                "2",
                "3",
                "4",
                "6",
                "146",
                "211",
                "212",
                "213",
                "214",
                "215",
                "221",
                "222"
        };

        return sNames;
    }

    public static final String[] lineColors = {
            "FF9800", // All
            "FF9800", // None
            "B23E3E", // 1001   1 BZ
            "8E84B7", // 1003   3 BZ
            "795548", // 1005   5 BZ
            "007485", // 1006   6 BZ
            "E77817", // 1071  7A BZ
            "E77817", // 1072  7B BZ
            "7BC4A0", // 1008   8 BZ
            "B8DB7C", // 1009   9 BZ
            "DA251D", // 1101 10A BZ
            "DA251D", // 1102 10B BZ
            "F8C300", // 1011  11 BZ
            "69406E", // 1012  12 BZ
            "00923F", // 1014  14 BZ
            "000000", //  110 110 BZ
            "000000", //  111 111 BZ
            "000000", //  112 112 BZ
            "000000", //  116 116 BZ
            "000000", //  117 117 BZ
            "4D485B", // 1153 153 BZ
            "000000", //  183 183 BZ
            "000000", //  201 201 BZ
            "000000", //  202 202 BZ
            "E77817", //    1   1 ME
            "44916C", //    2   2 ME
            "03A3FB", //    3   3 ME
            "F8C300", //    4   4 ME
            "996188", //    6   6 ME
            "4D485B", //  146 146 ME
            "DA251D", //  211 211 ME
            "595D9C", //  212 212 ME
            "007485", //  213 213 ME
            "00496B", //  214 214 ME
            "E7B000", //  215 215 ME
            "00496B", //  221 221 ME
            "00496B"  //  222 222 BZ
    };

    public static String getColorForId(int id) {
        switch (id) {
            case 1001:
                return lineColors[2];
            case 1003:
                return lineColors[3];
            case 1005:
                return lineColors[4];
            case 1006:
                return lineColors[5];
            case 1071:
                return lineColors[6];
            case 1072:
                return lineColors[7];
            case 1008:
                return lineColors[8];
            case 1009:
                return lineColors[9];
            case 1101:
                return lineColors[10];
            case 1102:
                return lineColors[11];
            case 1011:
                return lineColors[12];
            case 1012:
                return lineColors[13];
            case 1014:
                return lineColors[14];
            case 110:
                return lineColors[15];
            case 111:
                return lineColors[16];
            case 112:
                return lineColors[17];
            case 116:
                return lineColors[18];
            case 117:
                return lineColors[19];
            case 1153:
                return lineColors[20];
            case 183:
                return lineColors[21];
            case 201:
                return lineColors[22];
            case 202:
                return lineColors[23];
            case 211:
                return lineColors[24];
            case 212:
                return lineColors[25];
            case 213:
                return lineColors[26];
            case 214:
                return lineColors[27];
            case 215:
                return lineColors[28];
            case 221:
                return lineColors[29];
            case 222:
                return lineColors[30];
            case 1:
                return lineColors[31];
            case 2:
                return lineColors[32];
            case 3:
                return lineColors[33];
            case 4:
                return lineColors[34];
            case 6:
                return lineColors[35];
            case 146:
                return lineColors[36];
            default:
                LogUtils.e("Lines", "Unknown line id " + id);
                return "FF9800";
        }
    }


    // ======================================== TIMETABLES =======================================

    public static final String[] timetableLines = {
            "BZ_MAP",
            "ME_MAP",
            "BZ_1",
            "BZ_3",
            "BZ_5",
            "BZ_6",
            "BZ_7",
            "BZ_8",
            "BZ_9",
            "BZ_10",
            "BZ_11",
            "BZ_12",
            "BZ_14",
            "BZ_110",
            "BZ_111",
            "BZ_112",
            "BZ_116",
            "BZ_117",
            "BZ_153",
            "BZ_183",
            "ME_201",
            "BZ_202",
            "ME_1",
            "ME_2",
            "ME_3",
            "ME_4",
            "ME_6",
            "ME_146",
            "ME_211",
            "ME_212",
            "ME_213",
            "ME_214",
            "ME_215",
            "ME_221",
            "ME_222",
            "ME_223",
            "ME_224",
            "ME_225",
            "ME_248"
    };

    public static final int[] notTracked = {
            223,
            224,
            225,
            248
    };
}
