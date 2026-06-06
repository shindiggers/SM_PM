package com.example.smmoney.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;

public class PocketMoneyProgressDialog {
    private final AlertDialog dialog;
    private final ProgressBar progressBar;
    private final TextView messageView;

    public PocketMoneyProgressDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, PocketMoneyThemes.dialogTheme());
        View view = LayoutInflater.from(context).inflate(R.layout.progress_dialog_layout, null);
        progressBar = view.findViewById(R.id.progress_bar);
        messageView = view.findViewById(R.id.progress_message);
        builder.setView(view);
        builder.setCancelable(false);
        dialog = builder.create();
    }

    public void setMessage(CharSequence message) {
        messageView.setText(message);
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }

    public void setIndeterminate(boolean indeterminate) {
        progressBar.setIndeterminate(indeterminate);
    }

    public void setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public void setMax(int max) {
        progressBar.setMax(max);
    }
}
