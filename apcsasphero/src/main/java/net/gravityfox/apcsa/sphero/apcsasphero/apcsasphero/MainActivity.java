package net.gravityfox.apcsa.sphero.apcsasphero.apcsasphero;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.orbotix.ConvenienceRobot;
import com.orbotix.Ollie;
import com.orbotix.common.*;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotLE;

import java.util.List;

import static android.R.attr.type;

public class MainActivity extends Activity implements SensorEventListener, DiscoveryAgentEventListener, RobotChangedStateListener {

    static final String TAG = "APCSASphero";
    static final int RED = Color.parseColor("#CD5C5C");

    private SensorManager SM;
    private Sensor sensor;
    private DiscoveryAgent discoveryAgent;
    private ConvenienceRobot robot;
    private GraphicsView gv;
    private boolean robotActive = false;
    private float stableX = 0, stableY = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        gv = new GraphicsView(this);
        setContentView(gv);
        SM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        discoveryAgent = DiscoveryAgentLE.getInstance();
        discoveryAgent.addRobotStateListener(this);
        Log.i(TAG, "Started.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDiscovery();
        Log.i(TAG, "Resuming.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (discoveryAgent != null) {
            discoveryAgent.removeRobotStateListener(this);
            for (Robot r : discoveryAgent.getConnectedRobots()) {
                r.sleep();
            }
        }
        stopDiscovery();
    }

    @Override
    public void handleRobotsAvailable(List<Robot> list) {
        Log.v("APCSASphero", list.toString());
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {
        Log.v(TAG, "Robot Changed State: " + robotChangedStateNotificationType);
        switch (robotChangedStateNotificationType) {
            case Online:
                stopDiscovery();
                if (robot instanceof RobotLE) {
                    this.robot = new Ollie(robot);
                }
                this.robot.setLed(0f, 1f, 0f);
                this.robotActive = true;

                break;
            case Disconnected:
                this.robotActive = false;
//                startDiscovery();
                break;
            default:
                Log.v("APCSASphero", "Not handling state change notification: " + type);
        }
    }


    private class GraphicsView extends View {
        private String TAG = "Random Stuff";
        float initialX, initialY;
        private float centerX = 0;
        private float centerY = 0;
        private int radius = 70;
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float x = 0;
        float y = 0;
        Bitmap button;
        Bitmap ball;
        Bitmap wood;

        public GraphicsView(Context context) {
            super(context);
            button = BitmapFactory.decodeResource(getResources(), R.drawable.button);
            ball = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ball), 70, 70, true);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            centerX = w / 2;
            centerY = h / 2;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            paint.setColor(RED);

            //canvas.drawCircle(centerX + (x * 50), centerY + (y * 50), radius, paint);
            canvas.drawBitmap(ball, centerX + (x * 50), centerY + (y * 50), paint);
            canvas.drawBitmap(button, 50, 50, paint);
        }

        public boolean onTouchEvent(MotionEvent event) {
            //mGestureDetector.onTouchEvent(event);

            int action = event.getActionMasked();

            switch (action) {

                case MotionEvent.ACTION_DOWN:
                    initialX = event.getX();
                    initialY = event.getY();

                    Log.d(TAG, "Action was DOWN");
                    break;

                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "Action was MOVE");
                    break;

                case MotionEvent.ACTION_UP:
                    float finalX = event.getX();
                    float finalY = event.getY();

                    Log.d(TAG, "Action was UP");

                    if (initialX < finalX) {
                        Log.d(TAG, "Left to Right swipe performed");
                    }

                    if (initialX > finalX) {
                        Log.d(TAG, "Right to Left swipe performed");
                    }

                    if (initialY < finalY) {
                        Log.d(TAG, "Up to Down swipe performed");
                    }

                    if (initialY > finalY) {
                        Log.d(TAG, "Down to Up swipe performed");
                    }

                    break;

                case MotionEvent.ACTION_CANCEL:
                    Log.d(TAG, "Action was CANCEL");
                    break;

                case MotionEvent.ACTION_OUTSIDE:
                    Log.d(TAG, "Movement occurred outside bounds of current screen element");
                    break;
            }

            return super.onTouchEvent(event);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[1], y = event.values[0];
        stableX = stableX * 0.8f + x * 0.2f;
        stableY = stableY * 0.8f + y * 0.2f;
        gv.x = stableX;
        gv.y = stableY;
        gv.postInvalidate();
        if (robotActive) {
            float heading = (float) Math.atan2(x, y);
            float velocity = (float) Math.sqrt(x * x + y * y);
            //this.robot.drive(heading, velocity);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not used
    }

    private void startDiscovery() {
        try {
            discoveryAgent.addDiscoveryListener(this);
            discoveryAgent.startDiscovery(this);
        } catch (DiscoveryException e) {
            Log.e("APCSASphero", "Could not start discovery. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopDiscovery() {
        discoveryAgent.removeDiscoveryListener(this);
        discoveryAgent.stopDiscovery();
    }

}