package com.example.smmoney.views.budgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.LayoutParams;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;

public class BudgetsHeaderHolder extends View {
    private Context context;
    public String label;
    private String xofy;

    public BudgetsHeaderHolder(Context context, String label, String xofy) {
        super(context);
        this.context = context;
        this.label = label;
        this.xofy = xofy;
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT/*-1*/, ViewGroup.LayoutParams.WRAP_CONTENT/*-2*/));
        setMinimumHeight((int) (((double) getPrefferedItemHeight()) * 0.5d));
    }

    private float getPrefferedItemHeight() {
        TypedValue typedValue = new TypedValue();
        DisplayMetrics displayMetrics = this.context.getResources().getDisplayMetrics();
        this.context.getTheme().resolveAttribute(16842829, typedValue, true);
        return typedValue.getDimension(displayMetrics);
    }

    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        Rect r = new Rect(0, 0, width, height);
        Paint p = new Paint();
        p.setColorFilter(new PorterDuffColorFilter(PocketMoneyThemes.currentTintColor(), Mode.SCREEN));
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.categorybar), null, r, p);
        p.setColorFilter(null);
        Bitmap collapseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.collapseandexpand);
        canvas.drawBitmap(collapseBitmap, (float) ((width - 5) - collapseBitmap.getWidth()), (float) ((height / 2) - (collapseBitmap.getHeight() / 2)), p);
        p.setTextAlign(Align.CENTER);
        try {
            p.setTextSize((float) getDPFromPixels(35.0d));
        } catch (Exception e) {
            p.setTextSize(25.0f);
        }
        p.setColor(-16777216);
        p.setTypeface(Typeface.SANS_SERIF);
        p.setAntiAlias(true);
        float y = (((float) height) / 2.0f) + (p.getTextSize() / 2.0f);
        float text_w = p.measureText(this.label);
        float totalTextWidth = ((float) getWidth()) * 0.45f;
        float xscale = 1.0f;
        if (totalTextWidth < text_w) {
            xscale = totalTextWidth / text_w;
        }
        p.setTextScaleX(xscale);
        canvas.drawText(this.label, (p.measureText(this.label) / 2.0f) + 5.0f, y, p);
        text_w = p.measureText(this.xofy);
        totalTextWidth = ((float) getWidth()) * 0.65f;
        xscale = 1.0f;
        if (totalTextWidth < text_w) {
            xscale = totalTextWidth / text_w;
        }
        p.setTextScaleX(xscale);
        canvas.drawText(this.xofy, ((((float) width) - (p.measureText(this.xofy) / 2.0f)) - 10.0f) - ((float) collapseBitmap.getWidth()), y, p);
    }

    private double getDPFromPixels(double pixels) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        switch (metrics.densityDpi) {
            case 120:
                return pixels * 0.75d;
            case 160:
                return pixels * 2.0d;
            case 240:
                return pixels * 3.0d;
            case 320:
                return pixels * 2.0d;
            case 480:
                return pixels * 2.0d;
            case 560:
                return pixels * 2.0d;
            case 640:
                return pixels * 2.0d;
            default:
                return pixels;
        }
    }
}
