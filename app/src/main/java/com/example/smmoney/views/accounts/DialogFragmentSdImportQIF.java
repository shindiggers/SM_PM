package com.example.smmoney.views.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.misc.Locales;

public class DialogFragmentSdImportQIF extends DialogFragment {

    public DialogFragmentSdImportQIF() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
        builder.setMessage("Place the *.qif file(s) in the folder '/Download/PocketMoneyBackup'\n\nMake sure to select the correct file format in the preferences");
        builder.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DialogSdImportQIFListener activity = (DialogSdImportQIFListener) getActivity();
                if (activity != null) {
                    activity.onFinishSdImportQIFDialog(Locales.kLOC_GENERAL_OK);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DialogSdImportQIFListener activity = (DialogSdImportQIFListener) getActivity();
                dialog.dismiss();
                if (activity != null) {
                    activity.onFinishSdImportQIFDialog(Locales.kLOC_GENERAL_CANCEL);
                }
            }
        });
        return builder.create();
    }

    public interface DialogSdImportQIFListener {
        void onFinishSdImportQIFDialog(String okCancel);
    }
}
