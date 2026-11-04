package com.example.smmoney.prefs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SecurityPrefsActivity extends PocketMoneyPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(SMMoney.getAppContext().getString(R.string.PREFS_SECURITY));
        loadParentFragment(R.xml.prefs_security);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        String[] delays = new String[]{
                SMMoney.getAppContext().getString(R.string.GENERAL_NONE),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY1MIN),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY5MINS),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY10MINS),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY15MINS),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY30MINS),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY1HOUR),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY2HOURS),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY4HOURS),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY8HOURS),
                SMMoney.getAppContext().getString(R.string.PASSWORDDELAY24HOURS)
        };
        ListPreference delayListPref = fragment.findPreference(Prefs.PASSWORD_DELAY);
        if (delayListPref != null) {
            delayListPref.setEntries(delays);
            delayListPref.setEntryValues(delays);
            if (delayListPref.getValue() == null) {
                delayListPref.setDefaultValue(delays[0]);
            }
            delayListPref.setOnPreferenceChangeListener(getOnChangeListener());
            delayListPref.setSummary(delayListPref.getEntry());
        }

        Preference passwordPref = fragment.findPreference(Prefs.PASSWORD);
        if (passwordPref != null) {
            passwordPref.setOnPreferenceClickListener(preference -> {
                showPasswordDialog();
                return true;
            });
        }
    }

    private void showPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.pref_dialog_password, null);
        TextInputLayout confirmLayout = dialogView.findViewById(R.id.confirm_layout);
        TextInputEditText passwordEdit = dialogView.findViewById(R.id.password_edit);
        TextInputEditText confirmEdit = dialogView.findViewById(R.id.confirm_edit);

        String currentPassword = Prefs.getStringPref(Prefs.PASSWORD);
        if (!currentPassword.isEmpty()) {
            passwordEdit.setText(currentPassword);
            confirmEdit.setText(currentPassword);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(SMMoney.getAppContext().getString(R.string.PREFERENCES_PASSWORD_TITLE))
                .setView(dialogView)
                .setPositiveButton(SMMoney.getAppContext().getString(R.string.GENERAL_OK), null) // Override onClick below to prevent auto-dismiss on error
                .setNegativeButton(SMMoney.getAppContext().getString(R.string.GENERAL_CANCEL), (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String pass = passwordEdit.getText() != null ? passwordEdit.getText().toString() : "";
                String confirm = confirmEdit.getText() != null ? confirmEdit.getText().toString() : "";

                if (!pass.equals(confirm)) {
                    confirmLayout.setError(SMMoney.getAppContext().getString(R.string.PREFERENCES_PASSWORD_NOTMATCH));
                    return;
                }

                confirmLayout.setError(null);
                Prefs.setPref(Prefs.PASSWORD, pass);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private Preference.OnPreferenceChangeListener getOnChangeListener() {
        return (preference, newValue) -> {
            preference.setSummary((String) newValue);
            return true;
        };
    }
}
