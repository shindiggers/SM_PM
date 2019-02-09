package com.catamount.pocketmoney.views.transactions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import com.catamount.pocketmoney.PocketMoney;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class PhotoReceiptsCell extends View {
    int PHOTO_HEIGHT = 40;
    int PHOTO_SPACE_H = 10;
    int PHOTO_SPACE_W = -1;
    int PHOTO_WIDTH = 40;
    ArrayList<Bitmap> bitmaps = new ArrayList();
    Context context;
    ArrayList<String> imageNames = new ArrayList();
    int itemsPerRow = 3;

    public PhotoReceiptsCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setImageLocationString(String locations) {
        if (locations != null) {
            this.bitmaps.clear();
            this.imageNames.clear();
            for (String name : locations.split(";")) {
                if (name.length() != 0) {
                    try {
                        File f = new File(Environment.getDataDirectory() + "/data/" + PocketMoney.getAppContext().getPackageName() + "/photos/" + name);
                        if (f.exists()) {
                            Options bmOptions = new Options();
                            bmOptions.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(f.getAbsolutePath(), bmOptions);
                            int scaleFactor = Math.min(bmOptions.outWidth / this.PHOTO_WIDTH, bmOptions.outHeight / this.PHOTO_HEIGHT);
                            bmOptions.inJustDecodeBounds = false;
                            bmOptions.inSampleSize = scaleFactor;
                            bmOptions.inPurgeable = true;
                            this.bitmaps.add(BitmapFactory.decodeFile(f.getAbsolutePath(), bmOptions));
                            this.imageNames.add(name);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        int i = 0;
        Iterator it = this.bitmaps.iterator();
        while (it.hasNext()) {
            canvas.drawBitmap((Bitmap) it.next(), null, new Rect(this.PHOTO_SPACE_W + ((i % this.itemsPerRow) * (this.PHOTO_WIDTH + this.PHOTO_SPACE_W)), this.PHOTO_SPACE_H + ((i / this.itemsPerRow) * (this.PHOTO_HEIGHT + this.PHOTO_SPACE_H)), (this.PHOTO_SPACE_W + ((i % this.itemsPerRow) * (this.PHOTO_WIDTH + this.PHOTO_SPACE_W))) + this.PHOTO_WIDTH, (this.PHOTO_SPACE_H + ((i / this.itemsPerRow) * (this.PHOTO_HEIGHT + this.PHOTO_SPACE_H))) + this.PHOTO_HEIGHT), paint);
            i++;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        Log.i(PocketMoney.TAG, "looking for1 got -> " + event.getAction());
        if (event.getAction() == 0) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int i = 0;
            Iterator it = this.bitmaps.iterator();
            while (it.hasNext()) {
                Bitmap bitmap = (Bitmap) it.next();
                if (new Rect(this.PHOTO_SPACE_W + ((i % this.itemsPerRow) * (this.PHOTO_WIDTH + this.PHOTO_SPACE_W)), this.PHOTO_SPACE_H + ((i / this.itemsPerRow) * (this.PHOTO_HEIGHT + this.PHOTO_SPACE_H)), (this.PHOTO_SPACE_W + ((i % this.itemsPerRow) * (this.PHOTO_WIDTH + this.PHOTO_SPACE_W))) + this.PHOTO_WIDTH, (this.PHOTO_SPACE_H + ((i / this.itemsPerRow) * (this.PHOTO_HEIGHT + this.PHOTO_SPACE_H))) + this.PHOTO_HEIGHT).contains(x, y)) {
                    Intent intent = new Intent(this.context, PhotoReceiptOptionsActivity.class);
                    intent.putExtra("imageName", this.imageNames.get(i));
                    ((Activity) this.context).startActivityForResult(intent, 37);
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    private void measureWidth() {
        TypedValue value = new TypedValue();
        boolean b = this.context.getTheme().resolveAttribute(16842829, value, true);
        String s = TypedValue.coerceToString(value.type, value.data);
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) this.context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int rowHeight = (int) value.getDimension(metrics);
        if (rowHeight != 0) {
            int rowWidth = getWidth();
            if (rowWidth != 0) {
                int i = rowHeight - this.PHOTO_SPACE_H;
                this.PHOTO_HEIGHT = i;
                this.PHOTO_WIDTH = i;
                this.itemsPerRow = (rowWidth - this.PHOTO_SPACE_W) / (this.PHOTO_WIDTH + this.PHOTO_SPACE_W);
                this.PHOTO_SPACE_W = (rowWidth - (this.itemsPerRow * this.PHOTO_WIDTH)) / (this.itemsPerRow + 1);
                if (this.PHOTO_SPACE_W < this.PHOTO_SPACE_H) {
                    this.itemsPerRow--;
                    this.PHOTO_SPACE_W = (rowWidth - (this.itemsPerRow * this.PHOTO_WIDTH)) / (this.itemsPerRow + 1);
                }
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.PHOTO_SPACE_W == -1) {
            measureWidth();
        }
        if (this.itemsPerRow != -1) {
            if (this.bitmaps.size() == 0) {
                setMeasuredDimension(0, 0);
                return;
            }
            int rows = ((this.bitmaps.size() - 1) / this.itemsPerRow) + 1;
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.makeMeasureSpec(MeasureSpec.getMode(heightMeasureSpec), ((this.PHOTO_SPACE_H * 2) + (this.PHOTO_HEIGHT * rows)) + ((rows - 1) * this.PHOTO_SPACE_H)));
        }
    }
}
