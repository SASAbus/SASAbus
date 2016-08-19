package it.sasabz.android.sasabus.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility methods and constants used for writing and reading to from streams and files.
 *
 * @author Alex Lardschneider
 */
public final class IOUtils {

    private static final String TAG = "IOUtils";

    private static File storageDir;

    private IOUtils() {
    }

    /**
     * Reads a {@link File} as a String
     *
     * @param file The file to be read in.
     * @return Returns the contents of the File as a String.
     * @throws IOException
     */
    public static String readFileAsString(File file) throws IOException {
        Preconditions.checkNotNull(file, "file == null");

        return readAsString(new FileInputStream(file));
    }

    /**
     * Reads an {@link InputStream} into a String using the UTF-8 encoding.
     * Note that this method closes the InputStream passed to it.
     *
     * @param inputStream The InputStream to be read.
     * @return The contents of the InputStream as a String.
     * @throws IOException
     */
    private static String readAsString(InputStream inputStream) throws IOException {
        Preconditions.checkNotNull(inputStream, "inputStream == null");

        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();

        try {
            String line;
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Utils.handleException(e);
                }
            }
        }

        inputStream.close();

        return sb.toString();
    }

    /**
     * Unzip a zip file. Will overwrite existing files.
     *
     * @param zipFile  Full path of the zip file you'd like to unzipFile.
     * @param location Full path of the directory you'd like to unzipFile to (will be created if it doesn't exist).
     */
    @SuppressLint("NewApi")
    public static boolean unzipFile(String zipFile, String location) throws IOException {
        int size;
        byte[] buffer = new byte[8192];

        if (!(!location.isEmpty() && location.charAt(location.length() - 1) == '/')) {
            location += "/";
        }

        File f = new File(location);
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                return false;
            }
        }

        try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(location + zipFile), 8192))) {

            ZipEntry ze;

            while ((ze = zin.getNextEntry()) != null) {
                String path = location + ze.getName();
                File unzipFile = new File(path);

                if (ze.isDirectory()) {
                    if (!unzipFile.isDirectory()) {
                        if (!unzipFile.mkdirs()) {
                            return false;
                        }
                    }
                } else {
                    File parentDir = unzipFile.getParentFile();
                    if (parentDir != null) {
                        if (!parentDir.isDirectory()) {
                            if (!parentDir.mkdirs()) {
                                return false;
                            }
                        }
                    }

                    FileOutputStream out = new FileOutputStream(unzipFile, false);
                    BufferedOutputStream outputStream = new BufferedOutputStream(out, 8192);
                    try {
                        while ((size = zin.read(buffer, 0, 8192)) != -1) {
                            outputStream.write(buffer, 0, size);
                        }

                        zin.closeEntry();
                    } finally {
                        try {
                            outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            Utils.handleException(e);
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Finds a suitable storage dir to save files like plan data and timetables. Prefers
     * external storage over internal storage. If no storage dir is found it will throw
     * a {@link IllegalStateException} as the app cannot work without usable storage.
     *
     * @param context Context to access various storage directories.
     * @return a {@link File} representing the storage dir.
     */
    @NonNull
    private static File findStorageDir(Context context) {
        if (storageDir != null) {
            return storageDir;
        }

        Preconditions.checkNotNull(context, "context == null");

        List<File> files = Arrays.asList(
                context.getExternalFilesDir(null),
                context.getFilesDir()
        );

        for (File file : files) {
            if (file != null && file.canRead() && file.canWrite()) {
                LogUtils.w(TAG, "Using " + file.getAbsolutePath() + " as storage");

                storageDir = file;
                return file;
            }
        }

        throw new IllegalStateException("Cannot find suitable storage dir");
    }

    /**
     * Returns the directory where the plan data should be saved.
     *
     * @param context Context to access {@link #findStorageDir(Context)}.
     * @return a {@link File} which points to the data dir.
     */
    @NonNull
    public static File getDataDir(Context context) {
        return new File(findStorageDir(context), "/data/");
    }

    /**
     * Returns the directory where the timetables should be saved.
     *
     * @param context Context to access {@link #findStorageDir(Context)}.
     * @return a {@link File} which points to the timetables dir.
     */
    @NonNull
    public static File getTimetablesDir(Context context) {
        return new File(findStorageDir(context), "/timetables/");
    }
}