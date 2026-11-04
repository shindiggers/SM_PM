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

public class DialogFragmentLocalStorageTransfers extends DialogFragment {

    public DialogFragmentLocalStorageTransfers() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        CharSequence[] items5 = new CharSequence[]{
                SMMoney.getAppContext().getString(R.string.TOOLS_BACKUP_SD),
                SMMoney.getAppContext().getString(R.string.TOOLS_RESTORE_SD),
                SMMoney.getAppContext().getString(R.string.TOOLS_IMPORT_SD),
                SMMoney.getAppContext().getString(R.string.TOOLS_EXPORT_SD)};
        builder = new AlertDialog.Builder(getActivity(), PocketMoneyThemes.dialogTheme());
        builder.setTitle(R.string.FILETRANSFERS_SDCARD);
        builder.setItems(items5, (dialog, item) -> {
            DialogLocalStorageTransferListener activity = (DialogLocalStorageTransferListener) getActivity();
            if (activity != null) {
                switch (item) {
                    case 0 /*Backup...*/:
                        activity.onFinishLocalStorageTransferDialog(0);
                        break;
                    case 1 /*Restore...*/:
                        activity.onFinishLocalStorageTransferDialog(1);
                        break;
                    case 2 /*Import...*/:
                        activity.onFinishLocalStorageTransferDialog(2);
                        break;
                    case 3 /*Export*/:
                        activity.onFinishLocalStorageTransferDialog(3);
                        break;
                    default:
                }
            }
        });
        return builder.create();
    }

    public interface DialogLocalStorageTransferListener {
        void onFinishLocalStorageTransferDialog(int transferType);
    }
}
