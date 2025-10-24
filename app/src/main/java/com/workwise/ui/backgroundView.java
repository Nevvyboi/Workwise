package com.workwise.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;

import java.util.Random;

public class backgroundView extends View implements Choreographer.FrameCallback {

    private static final int LAYERS = 3;
    private static final float MAX_FREQ = 2.0f;
    private static final float MIN_FREQ = 1.0f;
    private static final float H_SPEED_MIN = 0.18f;
    private static final float H_SPEED_MAX = 0.55f;
    private static final float V_SPEED_MIN = 0.20f;
    private static final float V_SPEED_MAX = 0.55f;

    private final Wave[] bottom = new Wave[LAYERS];
    private final Wave[] top    = new Wave[LAYERS];
    private final Paint[] pBot  = new Paint[LAYERS];
    private final Paint[] pTop  = new Paint[LAYERS];
    private final Path path = new Path();
    private final Random rng = new Random(42);

    private long lastFrame = 0L;
    private float w, h, dp;

    public backgroundView(Context c){ super(c); init(); }
    public backgroundView(Context c, AttributeSet a){ super(c,a); init(); }
    public backgroundView(Context c, AttributeSet a, int d){ super(c,a,d); init(); }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        dp = getResources().getDisplayMetrics().density;

        for (int i = 0; i < LAYERS; i++) {
            bottom[i] = new Wave();
            top[i]    = new Wave();

            pBot[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            pTop[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            pBot[i].setStyle(Paint.Style.FILL);
            pTop[i].setStyle(Paint.Style.FILL);
        }
    }

    @Override protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        w = width; h = height;

        for (int i = 0; i < LAYERS; i++) {
            float depth = (i + 1f) / LAYERS;

            Wave b = bottom[i];
            b.freq = lerp(MIN_FREQ, MAX_FREQ, depth);
            b.amp  = lerp(h * 0.018f, h * 0.07f, depth);
            b.phase = rng.nextFloat() * TAU();
            b.hSpeed = lerp(H_SPEED_MIN, H_SPEED_MAX, depth);
            b.baseY  = lerp(h * 0.72f, h * 0.90f, depth);
            b.bobAmp = lerp(h * 0.006f, h * 0.025f, depth);
            b.bobPhase = rng.nextFloat() * TAU();
            b.vSpeed = lerp(V_SPEED_MIN, V_SPEED_MAX, 1f - depth);

            int aBot = (int) lerp(28, 96, depth);
            int botColor = 0xFF2A7DFF;
            pBot[i].setColor((aBot << 24) | (botColor & 0x00FFFFFF));

            Wave t = top[i];
            t.freq = lerp(MIN_FREQ, MAX_FREQ, depth);
            t.amp  = lerp(h * 0.015f, h * 0.06f, depth);
            t.phase = rng.nextFloat() * TAU();
            t.hSpeed = lerp(H_SPEED_MIN, H_SPEED_MAX, depth) * 0.9f;
            t.baseY  = lerp(h * 0.10f, h * 0.22f, 1f - depth);
            t.bobAmp = lerp(h * 0.006f, h * 0.020f, depth);
            t.bobPhase = rng.nextFloat() * TAU();
            t.vSpeed = lerp(V_SPEED_MIN, V_SPEED_MAX, 1f - depth) * 0.9f;

            int aTop = (int) lerp(20, 84, depth);
            int topColor = 0xFF48A6FF;
            pTop[i].setColor((aTop << 24) | (topColor & 0x00FFFFFF));
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lastFrame = 0L;
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override protected void onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(this);
        super.onDetachedFromWindow();
    }

    @Override public void doFrame(long frameTimeNanos) {
        if (lastFrame != 0L) {
            float dt = (frameTimeNanos - lastFrame) / 1_000_000_000f;
            step(dt);
            invalidate();
        }
        lastFrame = frameTimeNanos;
        Choreographer.getInstance().postFrameCallback(this);
    }

    private void step(float dt) {
        for (int i = 0; i < LAYERS; i++) {
            bottom[i].phase += dt * bottom[i].hSpeed * TAU();
            bottom[i].bobPhase += dt * bottom[i].vSpeed * TAU();

            top[i].phase += dt * top[i].hSpeed * TAU();
            top[i].bobPhase += dt * top[i].vSpeed * TAU();
        }
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        final int stepPx = Math.max(4, (int)(5 * dp));

        for (int i = 0; i < LAYERS; i++) {
            Wave wv = top[i];
            float baseline = wv.baseY + (float)Math.sin(wv.bobPhase) * wv.bobAmp;
            path.reset();
            path.moveTo(0, baseline);
            for (int x = 0; x <= w; x += stepPx) {
                float t = (float) x / w;
                float y = (float) Math.sin(t * TAU() * wv.freq + wv.phase) * wv.amp;
                path.lineTo(x, baseline + y);
            }

            path.lineTo(w, 0);
            path.lineTo(0, 0);
            path.close();
            c.drawPath(path, pTop[i]);
        }

        for (int i = 0; i < LAYERS; i++) {
            Wave wv = bottom[i];
            float baseline = wv.baseY + (float)Math.sin(wv.bobPhase) * wv.bobAmp;
            path.reset();
            path.moveTo(0, baseline);
            for (int x = 0; x <= w; x += stepPx) {
                float t = (float) x / w;
                float y = (float) Math.sin(t * TAU() * wv.freq + wv.phase) * wv.amp;
                path.lineTo(x, baseline + y);
            }
            path.lineTo(w, h);
            path.lineTo(0, h);
            path.close();
            c.drawPath(path, pBot[i]);
        }
    }

    private static float TAU() { return (float)(Math.PI * 2.0); }
    private float lerp(float a, float b, float t) { return a + (b - a) * t; }

    private static class Wave {
        float freq, amp, phase, hSpeed;
        float baseY, bobAmp, bobPhase, vSpeed;
    }
}
