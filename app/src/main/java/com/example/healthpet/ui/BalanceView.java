package com.example.healthpet.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BalanceView extends View {

    private float centerX, centerY;
    private float ballX, ballY;
    private float radius;
    private Paint circlePaint, ballPaint;

    public BalanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.parseColor("#000E47"));
        circlePaint.setStyle(Paint.Style.FILL);

        ballPaint = new Paint();
        ballPaint.setColor(Color.WHITE);
        ballPaint.setStyle(Paint.Style.FILL);
    }

    public void updateBalance(float x, float y) {
        if (radius == 0) return;

        float scale = radius / 2f;
        float targetX = centerX - x * scale;
        float targetY = centerY + y * scale;

        float smoothing = 0.1f;
        ballX = ballX + (targetX - ballX) * smoothing;
        ballY = ballY + (targetY - ballY) * smoothing;

        float distance = (float) Math.sqrt(Math.pow(ballX - centerX, 2) + Math.pow(ballY - centerY, 2));

        if (distance <= radius) {
            ballPaint.setColor(Color.BLACK);
        } else {
            ballPaint.setColor(Color.RED);
            if (failListener != null) {
                failListener.onBalanceFail();
            }
        }

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2.2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
        canvas.drawCircle(ballX, ballY, 40, ballPaint);
    }

    public interface BalanceFailListener {
        void onBalanceFail();
    }

    private BalanceFailListener failListener;

    public void setBalanceFailListener(BalanceFailListener listener) {
        this.failListener = listener;
    }
}
