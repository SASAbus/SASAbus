package it.sasabz.android.sasabus.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Utility class to form weak hashes like md5 identifier for trips or app signatures.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public final class HashUtils {

    private HashUtils() {
    }

    public static String getHashForIdentifier(Context context, String identifier) {
        return Utils.md5(identifier + ':' +
                Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID) + ':' +
                System.currentTimeMillis()).substring(0, 8);
    }

    private static String byte2HexF(byte... arr) {
        StringBuilder str = new StringBuilder(arr.length << 1);

        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = '0' + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < arr.length - 1) str.append(':');
        }

        return str.toString();
    }
}