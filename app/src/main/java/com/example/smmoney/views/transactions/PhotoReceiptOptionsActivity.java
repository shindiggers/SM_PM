package com.example.smmoney.views.transactions;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyActivity;

import java.io.File;
import java.util.Objects;

public class PhotoReceiptOptionsActivity extends PocketMoneyActivity {
    private final int DELETED = 1;
    private String imageName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.imageName = Objects.requireNonNull(getIntent().getExtras()).getString("imageName");
        setContentView(R.layout.photo_receipt_option);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_PHOTO_RECEIPT_TITLE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupView();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void setupView() {
        findViewById(R.id.bottom_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        ImageView imageView = findViewById(R.id.image);

        File photoDir = new File(SMMoney.getAppContext().getFilesDir(), "photos");
        File photoFile = new File(photoDir, this.imageName);

        Log.d("PhotoReceiptOptions", "Loading image from: " + photoFile.getAbsolutePath());

        if (photoFile.exists()) {
            Options bmOptions = new Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Simple scaling logic to avoid OOM
            int scaleFactor = 1;
            if (photoW > 1024 || photoH > 1024) {
                scaleFactor = Math.max(photoW / 1024, photoH / 1024);
            }

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Log.e("PhotoReceiptOptions", "Failed to decode bitmap");
            }
        } else {
            Log.e("PhotoReceiptOptions", "File does not exist: " + photoFile.getAbsolutePath());
        }

        TextView deleteTextView = findViewById(R.id.delete);
        deleteTextView.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        deleteTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Builder b = new Builder(PhotoReceiptOptionsActivity.this);
                b.setMessage("Are you sure you want to delete this picture?");
                b.setPositiveButton(Locales.kLOC_GENERAL_YES, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent();
                        i.putExtra("imageName", PhotoReceiptOptionsActivity.this.imageName);
                        PhotoReceiptOptionsActivity.this.setResult(DELETED, i);
                        PhotoReceiptOptionsActivity.this.finish();
                    }
                });
                b.setNegativeButton(Locales.kLOC_GENERAL_NO, null);
                b.create().show();
            }
        });
    }
}
