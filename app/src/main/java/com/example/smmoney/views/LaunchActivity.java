package com.example.smmoney.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smmoney.database.Database;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.accounts.AccountsActivity;

import java.io.File;

public class LaunchActivity extends AppCompatActivity {
    private final ActivityResultLauncher<Intent> mainLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Log.d("LAUNCHACTIVITY", "mainLauncher has just returned");
        finish();
    });

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LAUNCHACTIVITY", "onCreate() - restoring previous state");
        Prefs.setPref(Prefs.SHUTTINGDOWN, false);
        Database.currentDB();
        Log.d("LAUNCHACTIVITY", "Database.currentDB() has just been called");
        Database.loadDatabasePreferences();
        Log.d("LAUNCHACTIVITY", "Database.loadDatabasePreferences() has just been called");
        Prefs.initialize();
        Log.d("LAUNCHACTIVITY", "Prefs.initialize() has just been called");
        try {
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "PocketMoneyBackup").mkdirs();
        } catch (Exception e) {
            Log.d("LAUNCHACTIVITY", "Exception: Failed to getExternalStorage ");
        }
    }

    public void onResume() {
        super.onResume();
        if (!Prefs.getBooleanPref(Prefs.SHUTTINGDOWN)) {
            Log.d("LAUNCHACTIVITY", "onResume - !Prefs.getBooleanPref(Prefs.SHUTTINGDOWN) = FALSE");
            boolean z;
            Intent i = new Intent(this, AccountsActivity.class);
            String pass = Prefs.getStringPref(Prefs.PASSWORD);
            Log.d("LAUNCHACTIVITY", "String pass = " + pass);
            String str = "showPasswordScreen";
            z = pass != null && pass.length() > 0;
            Log.d("LAUNCHACTIVITY", "Show passowrd screen var 'z' = " + z);
            i.putExtra(str, z);
            mainLauncher.launch(i);
        }
    }

    protected void onDestroy() {
        Log.d("LAUNCHACTIVITY", "onDestroy() has just run");
        super.onDestroy();
    }
}
