package com.example.smmoney.views.charts.views;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import com.example.smmoney.R;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.views.charts.items.ChartItem;

public class ChartPieView extends ChartView {
    private Paint mBgPaints = new Paint();
    private Paint mLinePaints = new Paint();
    double rads;

    public ChartPieView(Context context) {
        super(context);
    }

    public ChartPieView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    private void drawChart(Canvas canvas, int type, boolean halved) {
        if (this.series != null) {
            int mWidth = getWidth();
            int mHeight = getHeight();
            int i;
            int mGapBottom;
            int mGapLeft;
            int mGapRight;
            int mGapTop;
            float pieCenterX;
            float radius;
            if (halved) {
                if (mWidth / 2 > mHeight) {
                    radius = (float) ((mHeight - 20) / 2);
                    i = ((int) (((float) mWidth) - (radius * 4.0f))) / 3;
                    mGapRight = i;
                    mGapLeft = i;
                    mGapBottom = 10;
                    mGapTop = 10;
                } else {
                    radius = (float) ((mWidth - 30) / 4);
                    mGapRight = 10;
                    mGapLeft = 10;
                    i = ((int) (((float) mHeight) - (radius * 2.0f))) / 2;
                    mGapBottom = i;
                    mGapTop = i;
                }
                if (type ==Enums.kChartTypeNegativePie /*-1*/) {
                    mGapRight = (int) (((float) (mGapLeft * 2)) + (radius * 2.0f));
                    pieCenterX = ((float) mGapLeft) + radius;
                } else {
                    mGapLeft = (int) (((float) (mGapRight * 2)) + (radius * 2.0f));
                    pieCenterX = ((float) mGapLeft) + radius;
                }
            } else {
                i = ((mWidth - mHeight) + 20) / 2;
                mGapRight = i;
                mGapLeft = i;
                mGapBottom = 10;
                mGapTop = 10;
                radius = (float) ((mHeight - mGapLeft) - mGapRight);
                pieCenterX = (float) (mWidth / 2);
                float radiusForHeight = (float) ((mHeight - 20) / 2);
                if (radius > radiusForHeight) {
                    radius = radiusForHeight;
                }
            }
            float pieCenterY = (float) (mHeight / 2);
            float leftX = (float) (mWidth - mGapRight);
            RectF mOvals = new RectF((float) mGapLeft, (float) mGapTop, (float) (mWidth - mGapRight), (float) (mHeight - mGapBottom));
            this.mBgPaints.setAntiAlias(true);
            this.mBgPaints.setStyle(Style.FILL);
            this.mBgPaints.setColor(-1996554240);
            this.mBgPaints.setStrokeWidth(0.5f);
            this.mLinePaints.setAntiAlias(true);
            this.mLinePaints.setStyle(Style.STROKE);
            this.mLinePaints.setColor(-16777216);
            this.mLinePaints.setStrokeWidth(0.5f);
            float mSweep;
            float mStart = 0.0f;
            Path path;
            for (Object o : this.series.get(0)) {
                ChartItem item = (ChartItem) o;
                if ((type == Enums.kChartTypePositivePie/*1*/ && item.value > 0.0d) || (type == Enums.kChartTypeNegativePie /*-1*/ && item.value < 0.0d)) {
                    mSweep = 360.0f * ((float) item.percent);
                    path = new Path();
                    path.moveTo(pieCenterX, pieCenterY);
                    path.addArc(mOvals, mStart, mSweep);
                    path.lineTo(pieCenterX, pieCenterY);
                    path.close();
                    item.path = path;
                    this.mBgPaints.setColor(item.color);
                    canvas.drawPath(path, this.mBgPaints);
                    canvas.drawPath(path, this.mLinePaints);
                    mStart += mSweep;
                }
            }
            if (this.selectedItem != null) {
                this.mLinePaints.setColor(-23296);
                this.mLinePaints.setStrokeWidth(2.0f);
                canvas.drawPath(this.selectedItem.path, this.mLinePaints);
            }
            path = new Path();
            path.addCircle(pieCenterX, pieCenterY, 40.0f, Direction.CCW);
            this.mBgPaints.setColor(1140850688);
            canvas.drawPath(path, this.mBgPaints);
            path = new Path();
            path.addCircle(pieCenterX, pieCenterY, 30.0f, Direction.CCW);
            this.mLinePaints.setColor(-16777216);
            this.mLinePaints.setStrokeWidth(1.0f);
            canvas.drawPath(path, this.mLinePaints);
            this.mBgPaints.setColor(type == Enums.kChartTypeNegativePie /*-1*/ ? -65536 : -16711936);
            canvas.drawPath(path, this.mBgPaints);
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(50.0f);
            p.setColor(-16777216);
            p.setTextAlign(Align.CENTER);
            canvas.drawText(type == Enums.kChartTypeNegativePie /*-1*/ ? "-" : "+", pieCenterX, pieCenterY + p.measureText("-"), p);
            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piechartoverlay), null, mOvals, this.mBgPaints);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.negativeTotal < 0.0d && this.positiveTotal > 0.0d) {
            drawChart(canvas, Enums.kChartTypeNegativePie /*-1*/, true);
            drawChart(canvas, Enums.kChartTypePositivePie /*1*/, true);
        } else if (this.negativeTotal < 0.0d) {
            drawChart(canvas, Enums.kChartTypeNegativePie /*-1*/, false);
        } else {
            drawChart(canvas, Enums.kChartTypePositivePie /*1*/, false);
        }
    }
}
