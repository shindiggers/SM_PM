package com.example.smmoney;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.smmoney.misc.Prefs;

import java.io.File;
import java.io.IOException;

public class SMMoney extends Application {
    public static final String TAG = "com.catamount.pocketmon";

    // Application context reference is safe from memory leaks as it matches the process lifecycle.
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }

    public static boolean isLiteVersion() {
        return context.getPackageName().toLowerCase().contains("lite");
    }

    public static String getID() {
        String uuid = Prefs.getUUID();
        Log.i(TAG, "uuid=" + uuid);
        return uuid;
    }

    public static String getExternalPocketMoneyDirectory() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pocketMoneyDir = new File(downloadsDir, "PocketMoneyBackup");
        if (!pocketMoneyDir.exists() && !pocketMoneyDir.mkdirs()) {
            Log.w(TAG, "Failed to create directory: " + pocketMoneyDir.getAbsolutePath());
        }
        return pocketMoneyDir.getAbsolutePath() + "/";
    }

    public static String getTempFile() {
        String dir = Environment.getDataDirectory() + "/data/" + getAppContext().getPackageName() + "/";
        File dirFile = new File(dir);
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            Log.w(TAG, "Failed to create temp directory: " + dir);
        }
        try {
            File tempFile = new File(dir + "temp.data");
            if (!tempFile.exists() && !tempFile.createNewFile()) {
                Log.w(TAG, "Failed to create temp file: " + tempFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException in getTempFile", e);
        }
        return dir + "temp.data";
    }

    public static boolean IsExternalStorageWritable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }
}
