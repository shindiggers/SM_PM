package com.example.smmoney.views.transactions;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.exifinterface.media.ExifInterface;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyActivity;

import java.io.File;

public class PhotoReceiptOptionsActivity extends PocketMoneyActivity {
    public static final int RESULT_DELETED = 1;
    public static final int RESULT_REPLACE = 2;
    private String imageName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.imageName = extras.getString("imageName");
        }
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
        finish();
        return true;
    }

    private void setupView() {
        findViewById(R.id.bottom_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        ImageView imageView = findViewById(R.id.image);

        File photoDir = new File(SMMoney.getAppContext().getFilesDir(), "photos");
        File photoFile = new File(photoDir, this.imageName != null ? this.imageName : "");

        if (photoFile.exists()) {
            Options bmOptions = new Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = 1;
            if (photoW > 1024 || photoH > 1024) {
                scaleFactor = Math.max(photoW / 1024, photoH / 1024);
            }

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
            if (bitmap != null) {
                // Fix rotation for full screen view
                bitmap = rotateImageIfRequired(bitmap, photoFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            }
        }

        TextView replaceTextView = findViewById(R.id.replace);
        replaceTextView.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        replaceTextView.setTextColor(Color.WHITE);
        replaceTextView.setOnClickListener(v -> {
            Intent i = new Intent();
            i.putExtra("imageName", this.imageName);
            setResult(RESULT_REPLACE, i);
            finish();
        });

        TextView deleteTextView = findViewById(R.id.delete);
        deleteTextView.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        deleteTextView.setTextColor(Color.WHITE);
        deleteTextView.setOnClickListener(v -> {
            AlertDialog.Builder b = new AlertDialog.Builder(PhotoReceiptOptionsActivity.this, PocketMoneyThemes.dialogTheme());
            b.setMessage("Are you sure you want to delete this picture?");
            b.setPositiveButton(Locales.kLOC_GENERAL_YES, (dialog, which) -> {
                Intent i = new Intent();
                i.putExtra("imageName", PhotoReceiptOptionsActivity.this.imageName);
                PhotoReceiptOptionsActivity.this.setResult(RESULT_DELETED, i);
                PhotoReceiptOptionsActivity.this.finish();
            });
            b.setNegativeButton(Locales.kLOC_GENERAL_NO, null);
            b.create().show();
        });
    }

    private Bitmap rotateImageIfRequired(Bitmap img, String path) {
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            return switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270);
                default -> img;
            };
        } catch (Exception e) {
            return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
}
