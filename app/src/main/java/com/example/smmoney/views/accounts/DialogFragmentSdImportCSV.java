package com.example.smmoney.views.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.PocketMoneyThemes;

public class DialogFragmentSdImportCSV extends DialogFragment {

    public DialogFragmentSdImportCSV() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), PocketMoneyThemes.dialogTheme());
        builder.setTitle(R.string.TOOLS_FILETRANSFERS);
        builder.setMessage("The file 'SMMoney.csv' should be placed in the folder '/Download/PocketMoneyBackup'");
        builder.setPositiveButton(R.string.GENERAL_OK, (dialog, whichButton) -> {
            DialogSdImportCSVListener activity = (DialogSdImportCSVListener) getActivity();
            if (activity != null) {
                activity.onFinishSdImportCVSDialog(SMMoney.getAppContext().getString(R.string.GENERAL_OK));
            }
            dialog.dismiss();
            //AccountsActivity.this.importCSVFromSD();
        });
        builder.setNegativeButton(R.string.GENERAL_CANCEL, (dialog, whichButton) -> {
            DialogSdImportCSVListener activity = (DialogSdImportCSVListener) getActivity();
            if (activity != null) {
                activity.onFinishSdImportCVSDialog(SMMoney.getAppContext().getString(R.string.GENERAL_CANCEL));
            }
            dialog.dismiss();
        });
        return builder.create();
    }

    public interface DialogSdImportCSVListener {
        void onFinishSdImportCVSDialog(String okCancel);
    }
}
