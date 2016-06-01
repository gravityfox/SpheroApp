package net.gravityfox.apcsa.sphero.apcsasphero.apcsasphero;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
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
    private boolean bluetoothAvailable = false;
    private boolean hasPermission = false;
    private float stableX = 0, stableY = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Creating");
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        gv = new GraphicsView(this);
        setContentView(gv);
        SM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onStart() {
        Log.i(TAG, "Starting");
        super.onStart();
        discoveryAgent = DiscoveryAgentLE.getInstance();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
        } else {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAvailable = true;
            } else {
            }
        }
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        } else {
            hasPermission = true;
        }
    }


    @Override
    protected void onResume() {
        Log.i(TAG, "Resuming");
        super.onResume();
        discoveryAgent.addRobotStateListener(this);
        if (hasPermission && bluetoothAvailable) startDiscovery();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Pausing");
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true;
            if (bluetoothAvailable)
                startDiscovery();
        }
    }

    @Override
    public void handleRobotsAvailable(List<Robot> list) {
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {
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
                Log.v(TAG, "Not handling state change notification: " + type);
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
            float heading = (float) (Math.atan2(stableX, -stableY));
            if (heading < 0.0) {
                heading += 2.0 * Math.PI;
            }
            heading *= (180.0 / Math.PI);
            float velocity = (float) (Math.sqrt(stableX * stableX + stableY * stableY) * 0.05);
            if (velocity > 1) velocity = 1;
            this.robot.drive(heading, velocity);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not used
    }

    private void startDiscovery() {
        Log.i(TAG, "Starting discovery...");
        try {
            discoveryAgent.addRobotStateListener(this);
            discoveryAgent.addDiscoveryListener(this);
            discoveryAgent.startDiscovery(this);
            Log.i(TAG, "Started discovery!");
        } catch (DiscoveryException e) {
            Log.e(TAG, "Could not start discovery. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopDiscovery() {
        Log.i(TAG, "Stopping discovery...");
        discoveryAgent.removeDiscoveryListener(this);
        discoveryAgent.stopDiscovery();
    }

    private class GraphicsView extends View {
        private String TAG = "Random Stuff";
        float initialX, initialY;
        private float centerX = 0;
        private float centerY = 0;
        private int radius = 100;
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float x = 0;
        float y = 0;
        private WindowManager wm;
        private Display display;
        private Point size = new Point();
        private int screenWidth = 300;
        private int screenHeight = 300;
        Bitmap btn_Left;
        Bitmap btn_Right;
        Bitmap ball;

        public GraphicsView(Context context) {
            super(context);
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            display = wm.getDefaultDisplay();
            btn_Left = BitmapFactory.decodeResource(getResources(), R.drawable.btn_left);
            btn_Right = BitmapFactory.decodeResource(getResources(), R.drawable.btn_right);
            ball = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ball), radius, radius, true);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            centerX = w / 2;
            centerY = h / 2;
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);

            canvas.drawBitmap(ball, centerX + (x * 50), centerY + (y * 50), paint);
            canvas.drawBitmap(btn_Left, radius + 20, screenHeight / 2, paint);
            canvas.drawBitmap(btn_Right, screenWidth - radius - 20, screenHeight / 2, paint);


        }

/*        public boolean onTouchEvent(MotionEvent event) {
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
        }*/

    }

}
