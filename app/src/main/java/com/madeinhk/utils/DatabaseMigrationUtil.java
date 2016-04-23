package com.madeinhk.utils;

import android.content.Context;

import com.madeinhk.model.DictionaryDatabaseHelper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class DatabaseMigrationUtil {
    final static byte[] SQLITE_HEADER = {0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66, 0x6f, 0x72,
            0x6d,
            0x61, 0x74, 0x20, 0x33, 0x00};
    final static byte[] DECRYPT_MAPPING =
            {-85, -93, -6, 121, 62, 43, 77, 112, 74, 3, -77, -44, -75, 90, 104, 53, -74, 12, 35,
                    -94, 20, -31, -71, -124, -7, 116, -37, -116, -88, -103, -13, 97, 16, -8, -67,
                    122, -61, -97, 48, -114, 0, -20, -113, 28, 40, 49, 60, 78, -99, -59, 64, 92, 29,
                    -128, 93, -34, -19, 113, -1, 84, 108, 76, 17, -125, -35, -4, 42, 19, 72, 8, -65,
                    -123, 18, -21, 56, 9, -84, -24, -22, -47, 123, -49, 2, -9, -45, 89, 79, -28,
                    114, 91, 23, -48, -38, 5, 101, 71, -11, -80, 110, -127, -110, -95, 117, 22, 37,
                    15, -23, 118, -32, -3, -72, -81, 75, -64, 103, 52, -25, 67, -111, 115, -115, 31,
                    54, 109, 45, -66, -50, -54, -29, 106, 107, 68, 51, 120, 98, 111, 102, -119, 127,
                    50, -68, -12, -58, 30, 66, -17, -83, -126, 36, 63, 69, -63, 39, -2, -16, 1, 32,
                    -26, -60, -96, -92, -52, -101, -89, 94, 26, 61, 33, -15, -40, -100, 59, -108,
                    47, -117, 81, 100, -82, 38, -53, 21, -70, -121, 13, -90, 70, -112, 10, -41, -30,
                    -118, 85, 6, -5, 126, -104, 34, 87, -87, -107, -78, -36, -43, 73, 95, 83, -106,
                    -10, -55, 57, -57, 4, -86, -62, -39, -27, 80, 86, -122, 11, 96, -46, -98, -109,
                    82, -69, 46, 124, 55, -42, 7, 99, 24, 105, 58, -120, 25, -79, -102, -56, 65, 44,
                    -76, 88, -73, -18, 119, 41, -105, -33, -51, 27, -91, -14, 14, 125};

    private static boolean isMigrationNeeded(Context context) {
        File databaseFile = context.getDatabasePath(DictionaryDatabaseHelper.DATABASE_NAME);
        if (!databaseFile.exists()) {
            return false;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(databaseFile);
            byte[] header = new byte[16];
            fis.read(header);
            boolean same = Arrays.equals(SQLITE_HEADER, header);
            return !same;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static void maybeMigrate(Context context) {
        if (isMigrationNeeded(context)) {
            File databaseFile = context.getDatabasePath(DictionaryDatabaseHelper.DATABASE_NAME);
            File tempFile = new File(context.getCacheDir(), "database.tmp");
            BufferedInputStream bis = null;
            FileOutputStream fileOutputStream = null;
            try {
                bis = new BufferedInputStream(new FileInputStream(databaseFile));
                fileOutputStream = new FileOutputStream(tempFile);
                int byteRead;
                byte[] buffer = new byte[8192];
                while ((byteRead = bis.read(buffer)) > 0) {
                    for (int i = 0; i < byteRead; i++) {
                        final int index = buffer[i] & 0xFF;
                        buffer[i] = DECRYPT_MAPPING[index];
                    }
                    fileOutputStream.write(buffer, 0, byteRead);
                }
            } catch (IOException ex) {
                databaseFile.delete();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            tempFile.renameTo(databaseFile);
        }
    }

}
