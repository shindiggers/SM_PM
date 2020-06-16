package com.example.smmoney.views.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.misc.Locales;

public class DialogFragmentSdImportOFX extends DialogFragment {

    public DialogFragmentSdImportOFX() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
        builder.setMessage("Place the *.ofx file in the folder '/Download/PocketMoneyBackup'.\n\nWarning: Make sure to select the correct file format in the preferences");
        builder.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DialogSdImportOFXListener activity = (DialogSdImportOFXListener) getActivity();
                if (activity != null) {
                    activity.onFinishSdImportOFXDialog(Locales.kLOC_GENERAL_OK);
                }
                dialog.dismiss();
                //AccountsActivity.this.importOFXFromSD();
            }
        });
        builder.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DialogSdImportOFXListener activity = (DialogSdImportOFXListener) getActivity();
                if (activity != null) {
                    activity.onFinishSdImportOFXDialog(Locales.kLOC_GENERAL_CANCEL);
                }
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    public interface DialogSdImportOFXListener {
        void onFinishSdImportOFXDialog(String okCancel);
    }
}
