package com.catamount.pocketmoney.views.transactions;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import java.io.File;

public class PhotoReceiptOptionsActivity extends Activity {
    public final int DELETED = 1;
    TextView deleteTextView;
    ImageView image;
    String imageName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.imageName = getIntent().getExtras().getString("imageName");
        setContentView(R.layout.photo_receipt_option);
        setupView();
    }

    private void setupView() {
        Options opts;
        ((TextView) findViewById(R.id.title_text_view)).setTextColor(PocketMoneyThemes.toolbarTextColor());
        findViewById(R.id.the_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        findViewById(R.id.bottom_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.image = findViewById(R.id.image);
        File f = new File(Environment.getDataDirectory() + "/data/" + PocketMoney.getAppContext().getPackageName() + "/photos/" + this.imageName);
        Options bmOptions = new Options();
        bmOptions.inJustDecodeBounds = true;
        bmOptions.inSampleSize = 8;
        Bitmap thumb = BitmapFactory.decodeFile(f.getAbsolutePath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        try {
            this.image.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
        } catch (OutOfMemoryError e) {
            try {
                opts = new Options();
                opts.inSampleSize = 2;
                this.image.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeFile(f.getAbsolutePath(), opts), photoW / 2, photoH / 2, false));
            } catch (OutOfMemoryError e2) {
                opts = new Options();
                opts.inSampleSize = 4;
                this.image.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeFile(f.getAbsolutePath(), opts), photoW / 4, photoH / 4, false));
            }
        }
        this.deleteTextView = findViewById(R.id.delete);
        this.deleteTextView.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        this.deleteTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Builder b = new Builder(PhotoReceiptOptionsActivity.this);
                b.setMessage("Are you sure you want to delete this picture?");
                b.setPositiveButton(Locales.kLOC_GENERAL_YES, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent();
                        i.putExtra("imageName", PhotoReceiptOptionsActivity.this.imageName);
                        PhotoReceiptOptionsActivity.this.setResult(1, i);
                        PhotoReceiptOptionsActivity.this.finish();
                    }
                });
                b.setNegativeButton(Locales.kLOC_GENERAL_NO, null);
                b.create().show();
            }
        });
    }
}
