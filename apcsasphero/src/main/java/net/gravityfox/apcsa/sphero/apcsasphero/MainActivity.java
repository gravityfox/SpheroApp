package net.gravityfox.apcsa.sphero.apcsasphero;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.flowpowered.math.TrigMath;
import com.flowpowered.math.imaginary.Quaternionf;
import com.flowpowered.math.vector.Vector3f;
import com.orbotix.ConvenienceRobot;
import com.orbotix.Ollie;
import com.orbotix.common.*;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotLE;

import java.util.List;

import static android.R.attr.type;

public class MainActivity extends Activity implements SensorEventListener, DiscoveryAgentEventListener, RobotChangedStateListener {

    static final String TAG = "APCSA Sphero";

    private DiscoveryAgent discoveryAgent;
    private ConvenienceRobot robot;
    private GraphicsView gv;
    private SensorManager sm;
    private Sensor accelerometer;
    private Sensor rotation;
    private Snackbar snackRD;
    private boolean robotActive = false;
    private boolean bluetoothAvailable = false;
    private boolean hasPermission = false;
    private boolean shouldDiscover = false;
    private boolean isCalibrating = false;
    private float stableX = 0, stableY = 0;
    private float phoneAngle = 0;
    private float phoneAngleOffset = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Creating");
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        gv = new GraphicsView(this);
        setContentView(gv);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "Starting");
        super.onStart();
        discoveryAgent = DiscoveryAgentLE.getInstance();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Snackbar snackBT = Snackbar.make(gv, "Sorry, but your phone does not have a Bluetooth adapter.", Snackbar.LENGTH_INDEFINITE);
            snackBT.show();
        } else if (bluetoothAdapter.isEnabled()) {
            bluetoothAvailable = true;
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
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);
        discoveryAgent.addRobotStateListener(this);
        shouldDiscover = true;
        if (hasPermission && bluetoothAvailable) {
            startDiscovery();

        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Pausing");
        super.onPause();
        sm.unregisterListener(this);
        shouldDiscover = false;
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
        //not used
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
                if (shouldDiscover)
                    startDiscovery();
                break;
            default:
                Log.v(TAG, "Not handling state change notification: " + type);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            float x = event.values[1], y = event.values[0];
            stableX = stableX * 0.95f + x * 0.05f;
            stableY = stableY * 0.95f + y * 0.05f;
            gv.x = stableX;
            gv.y = stableY;
            gv.postInvalidate();
            if (robotActive && !isCalibrating) {
                float heading = (float) (TrigMath.atan2(stableY, stableX));
                heading -= phoneAngle + phoneAngleOffset;
                heading = Util.wrapAngle(heading);
                heading *= (180.0 / Math.PI);
                float velocity = (float) (Math.sqrt(stableX * stableX + stableY * stableY) * 0.1);
                if (velocity > 1) velocity = 1;
                this.robot.drive(heading, velocity);
            }
        } else if (event.sensor == rotation) {
            Quaternionf quat = Util.quatfFromArray(event.values);
            Vector3f pointVector = quat.rotate(0, 1, 0);
            phoneAngle = (float) TrigMath.atan2(pointVector.getY(), pointVector.getX());
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
            snackRD = Snackbar.make(gv, "Please place the back of your device against the Ollie's power port until it lights up.", Snackbar.LENGTH_INDEFINITE);
            snackRD.show();
        } catch (DiscoveryException e) {
            Log.e(TAG, "Could not start discovery. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopDiscovery() {
        Log.i(TAG, "Stopping discovery...");
        if (snackRD != null) snackRD.dismiss();
        discoveryAgent.removeDiscoveryListener(this);
        discoveryAgent.stopDiscovery();
    }

    public void callibrating(boolean value) {
        isCalibrating = value;
        if (robotActive) {
            if (value) {
                this.robot.stop();
                this.robot.calibrating(true);
            } else {
                this.robot.calibrating(false);
            }
        }
        if (!value) {
            this.phoneAngleOffset = -this.phoneAngle - ((float) Math.PI / 2);
        }
    }

}
