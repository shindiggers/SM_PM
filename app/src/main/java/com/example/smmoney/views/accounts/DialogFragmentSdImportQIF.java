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

public class DialogFragmentSdImportQIF extends DialogFragment {

    public DialogFragmentSdImportQIF() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), PocketMoneyThemes.dialogTheme());
        builder.setTitle(R.string.TOOLS_FILETRANSFERS);
        builder.setMessage("Place the *.qif file(s) in the folder '/Download/PocketMoneyBackup'\n\nMake sure to select the correct file format in the preferences");
        builder.setPositiveButton(R.string.GENERAL_OK, (dialog, whichButton) -> {
            DialogSdImportQIFListener activity = (DialogSdImportQIFListener) getActivity();
            if (activity != null) {
                activity.onFinishSdImportQIFDialog(SMMoney.getAppContext().getString(R.string.GENERAL_OK));
            }
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.GENERAL_CANCEL, (dialog, whichButton) -> {
            DialogSdImportQIFListener activity = (DialogSdImportQIFListener) getActivity();
            dialog.dismiss();
            if (activity != null) {
                activity.onFinishSdImportQIFDialog(SMMoney.getAppContext().getString(R.string.GENERAL_CANCEL));
            }
        });
        return builder.create();
    }

    public interface DialogSdImportQIFListener {
        void onFinishSdImportQIFDialog(String okCancel);
    }
}
