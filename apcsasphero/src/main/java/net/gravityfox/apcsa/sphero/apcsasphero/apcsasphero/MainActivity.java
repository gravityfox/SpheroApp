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
import com.orbotix.ConvenienceRobot;
import com.orbotix.common.DiscoveryAgent;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;

import java.util.List;

public class MainActivity extends Activity implements SensorEventListener, DiscoveryAgentEventListener, RobotChangedStateListener{

    static final int RED = Color.parseColor("#CD5C5C");

    private SensorManager SM;
    private Sensor sensor;
    private DiscoveryAgent discoveryAgent;
    private ConvenienceRobot robot;
    private GraphicsView gv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gv = new GraphicsView(this);
        setContentView(gv);
        SM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void handleRobotsAvailable(List<Robot> list) {
        
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {

    }


    private class GraphicsView extends View {

        private float centerX = getWidth() / 2;
        private float centerY = getHeight() / 2;
        private int radius = 70;
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float x = getWidth() / 2;
        float y = getHeight() / 2;

        public GraphicsView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            // Use Color.parseColor to define HTML colors
            paint.setColor(RED);

            canvas.drawCircle(centerX + (x * 30), centerY + (y * 30), radius, paint);
        }

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        gv.x = event.values[0];
        gv.y = event.values[1];
        while (gv.x > gv.getWidth() - 70)
            gv.x = gv.x - 2;
        while (gv.y > gv.getHeight() - 70)
            gv.y = gv.y - 2;
        gv.postInvalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not used
    }

}