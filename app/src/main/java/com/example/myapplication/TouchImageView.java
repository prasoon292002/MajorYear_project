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
        public RectF  rect;
        public String label;

        public LabeledRect(RectF rect, String label) {
            this.rect  = rect;
            this.label = label;
        }
    }

    private final List<LabeledRect> labeledRects = new ArrayList<>();
    private final Paint rectPaint  = new Paint();
    private final Paint fillPaint  = new Paint();
    private final Paint textPaint  = new Paint();
    private final Paint bgPaint    = new Paint();

    private float  startX, startY;
    private boolean drawing = false;
    private String  currentLabel = null;

    // Colors for different labels (cycles through these)
    private final int[] COLORS = {
            Color.parseColor("#F44336"), // red
            Color.parseColor("#2196F3"), // blue
            Color.parseColor("#4CAF50"), // green
            Color.parseColor("#FF9800"), // orange
            Color.parseColor("#9C27B0"), // purple
            Color.parseColor("#00BCD4"), // cyan
    };

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(6f);
        rectPaint.setAntiAlias(true);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36f);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < labeledRects.size(); i++) {
            LabeledRect lr    = labeledRects.get(i);
            int         color = COLORS[i % COLORS.length];

            // Stroke rectangle
            rectPaint.setColor(color);
            canvas.drawRect(lr.rect, rectPaint);

            // Semi-transparent fill
            fillPaint.setColor(color);
            fillPaint.setAlpha(40);
            canvas.drawRect(lr.rect, fillPaint);

            // Label background pill
            if (lr.label != null) {
                float textWidth  = textPaint.measureText(lr.label);
                float textHeight = textPaint.getTextSize();
                float padding    = 10f;
                RectF labelBg = new RectF(
                        lr.rect.left,
                        lr.rect.top,
                        lr.rect.left + textWidth + padding * 2,
                        lr.rect.top  + textHeight + padding);
                bgPaint.setColor(color);
                bgPaint.setAlpha(220);
                canvas.drawRoundRect(labelBg, 8f, 8f, bgPaint);

                // Label text
                canvas.drawText(lr.label,
                        lr.rect.left + padding,
                        lr.rect.top  + textHeight,
                        textPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentLabel == null) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX  = event.getX();
                startY  = event.getY();
                drawing = true;
                break;

            case MotionEvent.ACTION_UP:
                if (drawing) {
                    float endX = event.getX();
                    float endY = event.getY();

                    // Normalise so rect is always top-left → bottom-right
                    RectF rect = new RectF(
                            Math.min(startX, endX),
                            Math.min(startY, endY),
                            Math.max(startX, endX),
                            Math.max(startY, endY));

                    // Ignore tiny accidental taps
                    if (rect.width() > 20 && rect.height() > 20) {
                        labeledRects.add(new LabeledRect(rect, currentLabel));
                        invalidate();
                    }
                }
                drawing = false;
                break;
        }
        return true;
    }

    public void setCurrentLabel(String label) { this.currentLabel = label; }

    public List<LabeledRect> getLabeledRects() { return labeledRects; }

    /** Remove the most recently added label */
    public boolean deleteLastLabel() {
        if (!labeledRects.isEmpty()) {
            labeledRects.remove(labeledRects.size() - 1);
            invalidate();
            return true;
        }
        return false;
    }

    /** Remove a label by its name (first match) */
    public boolean deleteLabelByName(String name) {
        for (int i = 0; i < labeledRects.size(); i++) {
            if (labeledRects.get(i).label.equals(name)) {
                labeledRects.remove(i);
                invalidate();
                return true;
            }
        }
        return false;
    }

    /** Clear all labels */
    public void clearRectangles() {
        labeledRects.clear();
        invalidate();
    }

    /** Load labels from outside (used when restoring saved state) */
    public void setLabeledRects(List<LabeledRect> rects) {
        labeledRects.clear();
        labeledRects.addAll(rects);
        invalidate();
    }
}