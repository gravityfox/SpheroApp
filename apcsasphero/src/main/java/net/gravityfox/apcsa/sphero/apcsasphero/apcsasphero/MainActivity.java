package net.gravityfox.apcsa.sphero.apcsasphero.apcsasphero;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));
    }

    public class MyView extends View implements SensorEventListener{

        private SensorManager SM;
        private Sensor sensor;
        private float x = getWidth()/2;
        private float y = getHeight()/2;
        private float lastx = 0;
        private float lasty = 0;

        public MyView(Context context) {
            super(context);
            SM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            SM.registerListener(this, sensor , SensorManager.SENSOR_DELAY_GAME);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);
            int radius = 70;
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            // Use Color.parseColor to define HTML colors
            paint.setColor(Color.parseColor("#CD5C5C"));
            canvas.drawCircle(lastx + ((-1*x)/2), lasty + ((-1*y)/2), radius, paint);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            //lastx = x;
            //lasty = y;
            //x = event.values[0];
            //y = event.values[1];
            x--;
            y--;
            postInvalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //not used
        }
    }
}