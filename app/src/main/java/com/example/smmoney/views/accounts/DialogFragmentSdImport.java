package com.example.smmoney.views.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.misc.Locales;

public class DialogFragmentSdImport extends DialogFragment {

    public DialogFragmentSdImport() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        CharSequence[] items7 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX"};
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(Locales.kLOC_TOOLS_IMPORT_SD);
        builder.setItems(items7, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                DialogSdImportListener activity = (DialogSdImportListener) getActivity();
                if (activity != null) {
                    switch (item) {
                        case 0 /*QIF*/:
                            activity.onFinishSdImportDialog(0);
                            break;
                        case 1 /*TDF*/:
                            activity.onFinishSdImportDialog(1);
                            break;
                        case 2 /*CSV*/:
                            activity.onFinishSdImportDialog(2);
                            break;
                        case 3 /*OFX/QFX*/:
                            activity.onFinishSdImportDialog(3);
                            break;
                        default:
                    }
                }
            }
        });
        return builder.create();
    }

    public interface DialogSdImportListener {
        void onFinishSdImportDialog(int importType);
    }
}
