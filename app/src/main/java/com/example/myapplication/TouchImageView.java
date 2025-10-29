package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatImageView;
import java.util.ArrayList;
import java.util.List;

public class TouchImageView extends AppCompatImageView {

    public static class LabeledRect {
        public RectF rect;
        public String label;

        public LabeledRect(RectF rect, String label) {
            this.rect = rect;
            this.label = label;
        }
    }

    private final List<LabeledRect> labeledRects = new ArrayList<>();
    private final Paint rectPaint = new Paint();
    private final Paint textPaint = new Paint();
    private float startX, startY;
    private boolean drawing = false;
    private String currentLabel = null;

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(5f);

        textPaint.setColor(Color.RED);
        textPaint.setTextSize(40f);
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (LabeledRect lr : labeledRects) {
            canvas.drawRect(lr.rect, rectPaint);
            if (lr.label != null) {
                canvas.drawText(lr.label, lr.rect.left + 10, lr.rect.top + 50, textPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentLabel == null) return false; // don’t draw until a label is chosen

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                drawing = true;
                break;

            case MotionEvent.ACTION_UP:
                if (drawing) {
                    float endX = event.getX();
                    float endY = event.getY();
                    RectF rect = new RectF(startX, startY, endX, endY);
                    labeledRects.add(new LabeledRect(rect, currentLabel));
                    invalidate();
                }
                drawing = false;
                break;
        }
        return true;
    }

    public void setCurrentLabel(String label) {
        this.currentLabel = label;
    }

    public List<LabeledRect> getLabeledRects() {
        return labeledRects;
    }

    public void clearRectangles() {
        labeledRects.clear();
        invalidate();
    }
}
