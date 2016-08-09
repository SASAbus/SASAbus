package it.sasabz.android.sasabus.network.auth.jjwt.lang;

public abstract class Objects {

    /**
     * Returns {@code true} if the specified byte array is null or of zero length, {@code false} otherwise.
     *
     * @param array the byte array to check
     * @return {@code true} if the specified byte array is null or of zero length, {@code false} otherwise.
     */
    public static boolean isEmpty(byte... array) {
        return array == null || array.length == 0;
    }
}
