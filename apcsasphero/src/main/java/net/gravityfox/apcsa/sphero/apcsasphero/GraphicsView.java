package net.gravityfox.apcsa.sphero.apcsasphero;

import android.content.Context;
import android.graphics.*;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Seth on 5/16/2016.
 * Project: APCSASphero
 */
public class GraphicsView extends View {
    private int LIGHTGREEN = Color.parseColor("#01ce5b");
    private float centerX = 0;
    private float centerY = 0;
    private int radius = 200;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    float x = 0;
    float y = 0;
    private WindowManager wm;
    private Display display;
    private Point size = new Point();
    private MainActivity mainActivityInstance;
    Bitmap ball;
    Bitmap cal;
    Bitmap crosshair;

    public GraphicsView(MainActivity context) {
        super(context);
        mainActivityInstance = context;
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        ball = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ball), radius, radius, true);
        cal = BitmapFactory.decodeResource(getResources(), R.drawable.calibrate_small);
        crosshair = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.crosshair), radius * 2, radius * 2, true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2 - (radius / 2);
        centerY = h / 2 - (radius / 2);
        display.getSize(size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(LIGHTGREEN);
        canvas.drawPaint(paint);
        canvas.drawBitmap(crosshair, centerX - radius / 2, centerY - radius / 2, paint);
        canvas.drawBitmap(ball, centerX + (x * 50), centerY + (y * 50), paint);
        canvas.drawBitmap(cal, 0, 0, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            mainActivityInstance.callibrating(true);
            return true;
        } else if (e.getAction() == MotionEvent.ACTION_UP) {
            mainActivityInstance.callibrating(false);
            return true;
        }
        return false;
    }

}
