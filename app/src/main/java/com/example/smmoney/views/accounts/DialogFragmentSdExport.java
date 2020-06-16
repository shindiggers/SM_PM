package com.example.smmoney.views.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.misc.Locales;

public class DialogFragmentSdExport extends DialogFragment {

    public DialogFragmentSdExport() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        CharSequence[] items6 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX"};
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(Locales.kLOC_TOOLS_EXPORT_SD);
        builder.setItems(items6, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                DialogSdExportListener activity = (DialogSdExportListener) getActivity();
                if (activity != null) {
                    switch (item) {
                        case 0 /*QIF*/:
                            activity.onFinishSdExportDialog(0);
                            break;
                        case 1 /*TDF*/:
                            activity.onFinishSdExportDialog(1);
                            break;
                        case 2 /*CSV*/:
                            activity.onFinishSdExportDialog(2);
                            break;
                        case 3 /*OFX/QFX*/:
                            activity.onFinishSdExportDialog(3);
                            break;
                        default:
                    }
                }
            }
        });
        return builder.create();
    }

    public interface DialogSdExportListener {
        void onFinishSdExportDialog(int exportType);
    }
}
