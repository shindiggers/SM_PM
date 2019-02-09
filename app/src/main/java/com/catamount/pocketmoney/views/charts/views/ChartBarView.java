package com.catamount.pocketmoney.views.charts.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.util.AttributeSet;
import com.catamount.pocketmoney.views.charts.items.ChartItem;
import java.util.ArrayList;
import java.util.Iterator;

public class ChartBarView extends ChartView {
    float barWidth;
    float bottomEdge;
    float centerLine;
    float chartHeight;
    float chartWidth;
    float currentBar;
    float leftEdge;
    Paint mBgPaints = new Paint();
    Paint mLinePaints = new Paint();
    float maxBarWidth;
    float posNegVector;
    float ratioPosNeg;
    float rightEdge;
    float topEdge;
    float verticalScale;

    public ChartBarView(Context context) {
        super(context);
    }

    public ChartBarView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.series != null && this.series.size() != 0) {
            double d;
            ChartItem item;
            Path path;
            float barHeight;
            this.leftEdge = 10.0f;
            this.rightEdge = (float) (getWidth() - 10);
            this.topEdge = 10.0f;
            this.bottomEdge = (float) (getHeight() - 10);
            this.chartHeight = (float) (getHeight() - 20);
            this.chartWidth = (float) (getWidth() - 20);
            this.posNegVector = (float) (this.positiveMaxValue - this.negativeMaxValue);
            if (this.posNegVector != 0.0f) {
                d = this.positiveMaxValue / ((double) this.posNegVector);
            } else {
                d = (double) (this.positiveMaxValue != 0.0d ? 0.0f : 0.5f);
            }
            this.ratioPosNeg = (float) d;
            this.verticalScale = this.posNegVector != 0.0f ? this.chartHeight / this.posNegVector : 0.0f;
            this.centerLine = this.topEdge + Math.min((float) Math.max(Math.round(this.chartHeight * this.ratioPosNeg), 0), this.bottomEdge - this.topEdge);
            this.currentBar = this.leftEdge + 2.0f;
            this.barWidth = this.chartWidth / ((float) (this.series.size() != 0 ? Math.max(this.series.get(0).size(), 1) : 1));
            if (this.maxBarWidth != 0.0f) {
                this.barWidth = Math.min(this.barWidth, this.maxBarWidth);
            }
            this.mBgPaints.setAntiAlias(true);
            this.mBgPaints.setStyle(Style.FILL);
            this.mBgPaints.setColor(-1996554240);
            this.mBgPaints.setStrokeWidth(0.5f);
            this.mLinePaints.setAntiAlias(true);
            this.mLinePaints.setStyle(Style.STROKE);
            this.mLinePaints.setColor(-16777216);
            this.mLinePaints.setStrokeWidth(0.5f);
            Iterator it = ((ArrayList) this.series.get(0)).iterator();
            while (it.hasNext()) {
                item = (ChartItem) it.next();
                path = new Path();
                barHeight = (float) Math.abs(item.value * ((double) this.verticalScale));
                if (item.value > 0.0d || this.allNegative) {
                    path.addRect(this.currentBar, this.centerLine - barHeight, (this.currentBar + this.barWidth) - 2.0f, this.centerLine, Direction.CW);
                } else {
                    path.addRect(this.currentBar, this.centerLine, (this.currentBar + this.barWidth) - 2.0f, this.centerLine + barHeight, Direction.CW);
                }
                path.close();
                item.path = path;
                this.mBgPaints.setColor(item.color);
                canvas.drawPath(path, this.mBgPaints);
                canvas.drawPath(path, this.mLinePaints);
                if (item.selected) {
                    this.mLinePaints.setColor(-23296);
                    this.mLinePaints.setStrokeWidth(2.0f);
                    canvas.drawPath(item.path, this.mLinePaints);
                    this.mLinePaints.setColor(-16777216);
                    this.mLinePaints.setStrokeWidth(0.5f);
                }
                this.currentBar += this.barWidth;
            }
            this.currentBar = this.leftEdge + 2.0f;
            if (this.series.size() >= 2) {
                it = ((ArrayList) this.series.get(1)).iterator();
                while (it.hasNext()) {
                    item = (ChartItem) it.next();
                    path = new Path();
                    barHeight = (float) Math.abs(item.value * ((double) this.verticalScale));
                    if (item.value > 0.0d) {
                        path.addRect(this.currentBar, this.centerLine - barHeight, (this.currentBar + this.barWidth) - 2.0f, this.centerLine, Direction.CW);
                    } else {
                        path.addRect(this.currentBar, this.centerLine, (this.currentBar + this.barWidth) - 2.0f, this.centerLine + barHeight, Direction.CW);
                    }
                    path.close();
                    item.path = path;
                    this.mBgPaints.setColor(item.color);
                    canvas.drawPath(path, this.mBgPaints);
                    canvas.drawPath(path, this.mLinePaints);
                    if (item.selected) {
                        this.mLinePaints.setColor(-23296);
                        this.mLinePaints.setStrokeWidth(2.0f);
                        canvas.drawPath(item.path, this.mLinePaints);
                        this.mLinePaints.setColor(-16777216);
                        this.mLinePaints.setStrokeWidth(0.5f);
                    }
                    this.currentBar += this.barWidth;
                }
            }
            if (this.series.size() == 3) {
                Point lastPoint = null;
                this.currentBar = this.leftEdge + ((this.barWidth - 2.0f) / 2.0f);
                boolean firstPoint = true;
                Iterator it2 = ((ArrayList) this.series.get(2)).iterator();
                while (it2.hasNext()) {
                    Point nextPoint;
                    item = (ChartItem) it2.next();
                    path = new Path();
                    barHeight = (float) Math.abs(item.value * ((double) this.verticalScale));
                    if (item.value > 0.0d) {
                        nextPoint = new Point((int) this.currentBar, (int) (this.centerLine - barHeight));
                    } else {
                        nextPoint = new Point((int) this.currentBar, (int) (this.centerLine + barHeight));
                    }
                    if (firstPoint) {
                        lastPoint = new Point(nextPoint.x, nextPoint.y);
                        firstPoint = false;
                    } else {
                        drawLine(canvas, lastPoint, nextPoint, 2.0f, item.color);
                        lastPoint = nextPoint;
                        firstPoint = false;
                    }
                    path.addCircle((float) lastPoint.x, (float) lastPoint.y, 8.0f, Direction.CCW);
                    item.path = path;
                    this.mBgPaints.setColor(item.color);
                    if (item.selected) {
                        this.mBgPaints.setColor(-256);
                        canvas.drawPath(item.path, this.mBgPaints);
                        this.mBgPaints.setColor(-16777216);
                    }
                    this.currentBar += this.barWidth;
                }
            }
            drawLine(canvas, new Point((int) this.leftEdge, (int) this.centerLine), new Point((int) this.rightEdge, (int) this.centerLine), 1.0f, -256);
        }
    }

    private void drawLine(Canvas canvas, Point lastPoint, Point nextPoint, float width, int color) {
        Path p = new Path();
        p.moveTo((float) lastPoint.x, (float) lastPoint.y);
        p.lineTo((float) nextPoint.x, (float) nextPoint.y);
        p.close();
        this.mLinePaints.setColor(color);
        this.mLinePaints.setStrokeWidth(width);
        canvas.drawPath(p, this.mLinePaints);
        this.mLinePaints.setColor(-16777216);
        this.mLinePaints.setStrokeWidth(0.5f);
    }
}
