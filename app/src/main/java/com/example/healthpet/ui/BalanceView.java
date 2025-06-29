package com.example.healthpet.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * BalanceView is a custom view that displays a circle and a moving ball.
 * The ball's position updates based on balance sensor data.
 */
public class BalanceView extends View {

    private float centerX, centerY;
    private float ballX, ballY;
    private float radius;
    private Paint circlePaint, ballPaint;

    private BalanceFailListener failListener;

    /**
     * Constructor for BalanceView.
     *
     * @param context The Context the view is running in.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public BalanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initializes paint objects for drawing the circle and ball.
     */
    private void init() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.parseColor("#000E47"));
        circlePaint.setStyle(Paint.Style.FILL);

        ballPaint = new Paint();
        ballPaint.setColor(Color.WHITE);
        ballPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Updates the ball's position based on sensor input.
     *
     * @param x X-axis sensor value.
     * @param y Y-axis sensor value.
     */
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
            ballPaint.setColor(Color.WHITE);
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

    /**
     * Sets a listener that is called when balance fails (ball outside circle).
     *
     * @param listener The BalanceFailListener to set.
     */
    public void setBalanceFailListener(BalanceFailListener listener) {
        this.failListener = listener;
    }

    /**
     * Listener interface for balance failure events.
     */
    public interface BalanceFailListener {
        void onBalanceFail();
    }
}
