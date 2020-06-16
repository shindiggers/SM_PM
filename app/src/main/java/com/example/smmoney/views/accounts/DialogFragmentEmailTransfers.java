package com.example.smmoney.views.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.misc.Locales;

public class DialogFragmentEmailTransfers extends DialogFragment {
    private final int EMAIL_CSV = 2;
    private final int EMAIL_OFX = 3;
    private final int EMAIL_QIF = 0;
    private final int EMAIL_TDF = 1;
    private final int EMAIL_BACKUP = 4;

    public DialogFragmentEmailTransfers() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        CharSequence[] items3 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX", Locales.kLOC_TOOLS_BACKUPFILES};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS_EMAIL);
        builder.setItems(items3, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                DialogEmailTransferListener activity = (DialogEmailTransferListener) getActivity();
                if (activity != null) {
                    switch (item) {
                        case EMAIL_QIF /*0*/:
                            activity.onFinishEmailDialog(EMAIL_QIF);
                            break;
                        case EMAIL_TDF /*1*/:
                            activity.onFinishEmailDialog(EMAIL_TDF);
                            break;
                        case EMAIL_CSV /*2*/:
                            activity.onFinishEmailDialog(EMAIL_CSV);
                            break;
                        case EMAIL_OFX /*3*/:
                            activity.onFinishEmailDialog(EMAIL_OFX);
                            break;
                        case EMAIL_BACKUP /*4*/ /*Email database backup file*/:
                            activity.onFinishEmailDialog(EMAIL_BACKUP);
                            break;
                        default:
                    }
                }
            }
        });
        return builder.create();
    }

    public interface DialogEmailTransferListener {
        void onFinishEmailDialog(int emailType);
    }
}
