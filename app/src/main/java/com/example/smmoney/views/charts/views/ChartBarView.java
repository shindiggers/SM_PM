package com.example.smmoney.views.charts.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.util.AttributeSet;

import com.example.smmoney.views.charts.items.ChartItem;

public class ChartBarView extends ChartView {
    private Paint mBgPaints = new Paint();
    private Paint mLinePaints = new Paint();
    private float maxBarWidth;

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
            float leftEdge = 10.0f;
            float rightEdge = (float) (getWidth() - 10);
            float topEdge = 10.0f;
            float bottomEdge = (float) (getHeight() - 10);
            float chartHeight = (float) (getHeight() - 20);
            float chartWidth = (float) (getWidth() - 20);
            float posNegVector = (float) (this.positiveMaxValue - this.negativeMaxValue);
            if (posNegVector != 0.0f) {
                d = this.positiveMaxValue / ((double) posNegVector);
            } else {
                d = this.positiveMaxValue != 0.0d ? 0.0f : 0.5f;
            }
            float ratioPosNeg = (float) d;
            float verticalScale = posNegVector != 0.0f ? chartHeight / posNegVector : 0.0f;
            float centerLine = topEdge + Math.min((float) Math.max(Math.round(chartHeight * ratioPosNeg), 0), bottomEdge - topEdge);
            float currentBar = leftEdge + 2.0f;
            float barWidth = chartWidth / ((float) (this.series.size() != 0 ? Math.max(this.series.get(0).size(), 1) : 1));
            if (this.maxBarWidth != 0.0f) {
                barWidth = Math.min(barWidth, this.maxBarWidth);
            }
            this.mBgPaints.setAntiAlias(true);
            this.mBgPaints.setStyle(Style.FILL);
            this.mBgPaints.setColor(-1996554240);
            this.mBgPaints.setStrokeWidth(0.5f);
            this.mLinePaints.setAntiAlias(true);
            this.mLinePaints.setStyle(Style.STROKE);
            this.mLinePaints.setColor(-16777216);
            this.mLinePaints.setStrokeWidth(0.5f);
            for (Object o : this.series.get(0)) {
                item = (ChartItem) o;
                path = new Path();
                barHeight = (float) Math.abs(item.value * ((double) verticalScale));
                if (item.value > 0.0d || this.allNegative) {
                    path.addRect(currentBar, centerLine - barHeight, (currentBar + barWidth) - 2.0f, centerLine, Direction.CW);
                } else {
                    path.addRect(currentBar, centerLine, (currentBar + barWidth) - 2.0f, centerLine + barHeight, Direction.CW);
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
                currentBar += barWidth;
            }
            currentBar = leftEdge + 2.0f;
            if (this.series.size() >= 2) {
                for (Object o : this.series.get(1)) {
                    item = (ChartItem) o;
                    path = new Path();
                    barHeight = (float) Math.abs(item.value * ((double) verticalScale));
                    if (item.value > 0.0d) {
                        path.addRect(currentBar, centerLine - barHeight, (currentBar + barWidth) - 2.0f, centerLine, Direction.CW);
                    } else {
                        path.addRect(currentBar, centerLine, (currentBar + barWidth) - 2.0f, centerLine + barHeight, Direction.CW);
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
                    currentBar += barWidth;
                }
            }
            if (this.series.size() == 3) {
                Point lastPoint = null;
                currentBar = leftEdge + ((barWidth - 2.0f) / 2.0f);
                boolean firstPoint = true;
                for (Object o : this.series.get(2)) {
                    Point nextPoint;
                    item = (ChartItem) o;
                    path = new Path();
                    barHeight = (float) Math.abs(item.value * ((double) verticalScale));
                    if (item.value > 0.0d) {
                        nextPoint = new Point((int) currentBar, (int) (centerLine - barHeight));
                    } else {
                        nextPoint = new Point((int) currentBar, (int) (centerLine + barHeight));
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
                    currentBar += barWidth;
                }
            }
            drawLine(canvas, new Point((int) leftEdge, (int) centerLine), new Point((int) rightEdge, (int) centerLine), 1.0f, -256);
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
