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

public class DialogFragmentSdImportOFX extends DialogFragment {

    public DialogFragmentSdImportOFX() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), PocketMoneyThemes.dialogTheme());
        builder.setTitle(R.string.TOOLS_FILETRANSFERS);
        builder.setMessage("Place the *.ofx file in the folder '/Download/PocketMoneyBackup'.\n\nWarning: Make sure to select the correct file format in the preferences");
        builder.setPositiveButton(R.string.GENERAL_OK, (dialog, whichButton) -> {
            DialogSdImportOFXListener activity = (DialogSdImportOFXListener) getActivity();
            if (activity != null) {
                activity.onFinishSdImportOFXDialog(SMMoney.getAppContext().getString(R.string.GENERAL_OK));
            }
            dialog.dismiss();
            //AccountsActivity.this.importOFXFromSD();
        });
        builder.setNegativeButton(R.string.GENERAL_CANCEL, (dialog, whichButton) -> {
            DialogSdImportOFXListener activity = (DialogSdImportOFXListener) getActivity();
            if (activity != null) {
                activity.onFinishSdImportOFXDialog(SMMoney.getAppContext().getString(R.string.GENERAL_CANCEL));
            }
            dialog.dismiss();
        });
        return builder.create();
    }

    public interface DialogSdImportOFXListener {
        void onFinishSdImportOFXDialog(String okCancel);
    }
}
