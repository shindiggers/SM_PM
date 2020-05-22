package com.example.smmoney.views.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.example.smmoney.misc.Locales;

public class DialogFragmentFileTransfer extends DialogFragment {


    public DialogFragmentFileTransfer() {
        // empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        CharSequence[] items = new CharSequence[3];
        items[0] = Locales.kLOC_TOOLS_FILETRANSFERS_EMAIL;
        items[1] = Locales.kLOC_TOOLS_FILETRANSFERS_SDCARD;
        items[2] = Locales.kLOC_DESKTOPSYNC_TITLE;
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                DialogFileTransferListener activity = (DialogFileTransferListener) getActivity();
                if (activity != null) {
                    switch (item) {
                        case 0 /*Email Transferss...*/:
                            activity.onFinishFileTransferDialog(0);
                            break;
                        case 1 /*Local Storage Trasnfers...*/:
                            activity.onFinishFileTransferDialog(1);
                            break;
                        case 2 /*SMMoney Sync...*/:
                            activity.onFinishFileTransferDialog(2);
                            break;
                        default:
                    }
                }
            }
        });
        return builder.create();
    }

    public interface DialogFileTransferListener {
        void onFinishFileTransferDialog(int transferType);
    }
}
