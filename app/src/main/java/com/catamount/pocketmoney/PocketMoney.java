package com.catamount.pocketmoney;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.catamount.pocketmoney.misc.Prefs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class PocketMoney extends Application {
    public static final String TAG = "com.catamount.pocketmon";
    private static Context context;
    private static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

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
        String udid = null;
        if (context.getPackageManager().hasSystemFeature("android.hardware.telephony")) {

            udid = Settings.Secure.getString(getAppContext().getContentResolver(), Settings.Secure.ANDROID_ID);

            if (udid == null || Prefs.getBooleanPref(Prefs.USINGUUID)) {
                udid = Prefs.getUUID();
                Prefs.setPref(Prefs.USINGUUID, true);
            }
            Log.i(TAG, "uuid=" + udid);
            return udid;
        }
        return udid;
    }

        public static boolean hasCamera() {
            return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        }

        public static String getTempPocketMoneyDirectory(){
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir == null) {
                context.getCacheDir();
            }
            return cacheDir.getAbsolutePath();
        }

        public static String getExternalPocketMoneyDirectory () {
            File dir = new File(Prefs.getStringPref(Prefs.EXPORT_STOREDEVICE).concat("/data/PocketMoney/"));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir.toString() + "/";
        }

        public static String[] getExternalMounts () {
            try {
                ArrayList<String> out = new ArrayList();
                String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
                String s = "";
                try {
                    Process process = new ProcessBuilder().command("mount").redirectErrorStream(true).start();
                    process.waitFor();
                    InputStream is = process.getInputStream();
                    byte[] buffer = new byte[1024];
                    while (is.read(buffer) != -1) {
                        s = new StringBuilder(String.valueOf(s)).append(new String(buffer)).toString();
                    }
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (String line : s.split("\n")) {
                    if (!line.toLowerCase(Locale.US).contains("asec") && line.matches(reg)) {
                        for (String part : line.split(" ")) {
                            if (part.startsWith("/") && !part.toLowerCase(Locale.US).contains("vold")) {
                                out.add(part);
                            }
                        }
                    }
                }
                String[] ret = new String[out.size()];
                Iterator it = out.iterator();
                int i = 0;
                while (it.hasNext()) {
                    int i2 = i + 1;
                    ret[i] = (String) it.next();
                    i = i2;
                }
                return ret;
            } catch (Exception e2) {
                return new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()};
            }
        }

        public static String getTempFile () {
            String dir = "";
            dir = Environment.getDataDirectory() + "/data/" + getAppContext().getPackageName() + "/";
            new File(dir).mkdirs();
            try {
                new File(new StringBuilder(String.valueOf(dir)).append("temp.data").toString()).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new StringBuilder(String.valueOf(dir)).append("temp.data").toString();
        }

        public static boolean IsExternalStorageWritable () {
            return "mounted".equals(Environment.getExternalStorageState());
        }
    }
