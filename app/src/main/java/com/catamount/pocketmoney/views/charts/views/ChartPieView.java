package com.catamount.pocketmoney.views.charts.views;

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
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.views.charts.items.ChartItem;
import java.util.ArrayList;
import java.util.Iterator;

public class ChartPieView extends ChartView {
    float leftX;
    float leftY;
    private Paint mBgPaints = new Paint();
    private int mGapBottom;
    private int mGapLeft;
    private int mGapRight;
    private int mGapTop;
    private int mHeight;
    private Paint mLinePaints = new Paint();
    RectF mOvals;
    private float mStart;
    private float mSweep;
    private int mWidth;
    Path path;
    float pieCenterX;
    float pieCenterY;
    float radius;
    double rads;

    public ChartPieView(Context context) {
        super(context);
    }

    public ChartPieView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    private void drawChart(Canvas canvas, int type, boolean halved) {
        if (this.series != null) {
            this.mWidth = getWidth();
            this.mHeight = getHeight();
            int i;
            if (halved) {
                if (this.mWidth / 2 > this.mHeight) {
                    this.radius = (float) ((this.mHeight - 20) / 2);
                    i = ((int) (((float) this.mWidth) - (this.radius * 4.0f))) / 3;
                    this.mGapRight = i;
                    this.mGapLeft = i;
                    this.mGapBottom = 10;
                    this.mGapTop = 10;
                } else {
                    this.radius = (float) ((this.mWidth - 30) / 4);
                    this.mGapRight = 10;
                    this.mGapLeft = 10;
                    i = ((int) (((float) this.mHeight) - (this.radius * 2.0f))) / 2;
                    this.mGapBottom = i;
                    this.mGapTop = i;
                }
                if (type == -1) {
                    this.mGapRight = (int) (((float) (this.mGapLeft * 2)) + (this.radius * 2.0f));
                    this.pieCenterX = ((float) this.mGapLeft) + this.radius;
                } else {
                    this.mGapLeft = (int) (((float) (this.mGapRight * 2)) + (this.radius * 2.0f));
                    this.pieCenterX = ((float) this.mGapLeft) + this.radius;
                }
            } else {
                i = ((this.mWidth - this.mHeight) + 20) / 2;
                this.mGapRight = i;
                this.mGapLeft = i;
                this.mGapBottom = 10;
                this.mGapTop = 10;
                this.radius = (float) ((this.mHeight - this.mGapLeft) - this.mGapRight);
                this.pieCenterX = (float) (this.mWidth / 2);
                float radiusForHeight = (float) ((this.mHeight - 20) / 2);
                if (this.radius > radiusForHeight) {
                    this.radius = radiusForHeight;
                }
            }
            this.pieCenterY = (float) (this.mHeight / 2);
            this.leftX = (float) (this.mWidth - this.mGapRight);
            this.leftY = this.pieCenterY;
            this.mOvals = new RectF((float) this.mGapLeft, (float) this.mGapTop, (float) (this.mWidth - this.mGapRight), (float) (this.mHeight - this.mGapBottom));
            this.mBgPaints.setAntiAlias(true);
            this.mBgPaints.setStyle(Style.FILL);
            this.mBgPaints.setColor(-1996554240);
            this.mBgPaints.setStrokeWidth(0.5f);
            this.mLinePaints.setAntiAlias(true);
            this.mLinePaints.setStyle(Style.STROKE);
            this.mLinePaints.setColor(-16777216);
            this.mLinePaints.setStrokeWidth(0.5f);
            this.mSweep = 0.0f;
            this.mStart = 0.0f;
            Iterator it = ((ArrayList) this.series.get(0)).iterator();
            while (it.hasNext()) {
                ChartItem item = (ChartItem) it.next();
                if ((type == 1 && item.value > 0.0d) || (type == -1 && item.value < 0.0d)) {
                    this.mSweep = 360.0f * ((float) item.percent);
                    this.path = new Path();
                    this.path.moveTo(this.pieCenterX, this.pieCenterY);
                    this.path.addArc(this.mOvals, this.mStart, this.mSweep);
                    this.path.lineTo(this.pieCenterX, this.pieCenterY);
                    this.path.close();
                    item.path = this.path;
                    this.mBgPaints.setColor(item.color);
                    canvas.drawPath(this.path, this.mBgPaints);
                    canvas.drawPath(this.path, this.mLinePaints);
                    this.mStart += this.mSweep;
                }
            }
            if (this.selectedItem != null) {
                this.mLinePaints.setColor(-23296);
                this.mLinePaints.setStrokeWidth(2.0f);
                canvas.drawPath(this.selectedItem.path, this.mLinePaints);
            }
            this.path = new Path();
            this.path.addCircle(this.pieCenterX, this.pieCenterY, 40.0f, Direction.CCW);
            this.mBgPaints.setColor(1140850688);
            canvas.drawPath(this.path, this.mBgPaints);
            this.path = new Path();
            this.path.addCircle(this.pieCenterX, this.pieCenterY, 30.0f, Direction.CCW);
            this.mLinePaints.setColor(-16777216);
            this.mLinePaints.setStrokeWidth(1.0f);
            canvas.drawPath(this.path, this.mLinePaints);
            this.mBgPaints.setColor(type == -1 ? -65536 : -16711936);
            canvas.drawPath(this.path, this.mBgPaints);
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(50.0f);
            p.setColor(-16777216);
            p.setTextAlign(Align.CENTER);
            canvas.drawText(type == -1 ? "-" : "+", this.pieCenterX, this.pieCenterY + p.measureText("-"), p);
            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.piechartoverlay), null, this.mOvals, this.mBgPaints);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.negativeTotal < 0.0d && this.positiveTotal > 0.0d) {
            drawChart(canvas, -1, true);
            drawChart(canvas, 1, true);
        } else if (this.negativeTotal < 0.0d) {
            drawChart(canvas, -1, false);
        } else {
            drawChart(canvas, 1, false);
        }
    }
}
