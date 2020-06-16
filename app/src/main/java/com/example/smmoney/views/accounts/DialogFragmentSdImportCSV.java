package com.example.smmoney.views.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.misc.Locales;

public class DialogFragmentSdImportCSV extends DialogFragment {

    public DialogFragmentSdImportCSV() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
        builder.setMessage("The file 'SMMoney.csv' should be placed in the folder '/Download/PocketMoneyBackup'");
        builder.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DialogSdImportCSVListener activity = (DialogSdImportCSVListener) getActivity();
                if (activity != null) {
                    activity.onFinishSdImportCVSDialog(Locales.kLOC_GENERAL_OK);
                }
                dialog.dismiss();
                //AccountsActivity.this.importCSVFromSD();
            }
        });
        builder.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DialogSdImportCSVListener activity = (DialogSdImportCSVListener) getActivity();
                if (activity != null) {
                    activity.onFinishSdImportCVSDialog(Locales.kLOC_GENERAL_CANCEL);
                }
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    public interface DialogSdImportCSVListener {
        void onFinishSdImportCVSDialog(String okCancel);
    }
}
