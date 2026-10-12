package lk.jiat.bonsaimart_admin.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {

    public static class Slice {
        public String label;
        public float value;
        public int color;

        public Slice(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private List<Slice> slices = new ArrayList<>();
    private Paint paint;
    private RectF oval;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        oval = new RectF();
    }

    public void setData(List<Slice> data) {
        this.slices = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (slices.isEmpty()) return;

        float total = 0;
        for (Slice s : slices) total += s.value;
        if (total == 0) return;

        int size = Math.min(getWidth(), getHeight());
        int padding = 20;
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int radius = (size / 2) - padding;

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius);

        float startAngle = -90f;

        for (Slice s : slices) {
            float sweepAngle = (s.value / total) * 360f;
            paint.setColor(s.color);
            canvas.drawArc(oval, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
    }
}