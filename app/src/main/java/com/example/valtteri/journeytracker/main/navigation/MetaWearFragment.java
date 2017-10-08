package com.example.valtteri.journeytracker.main.navigation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.example.valtteri.journeytracker.R;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.SensorFusionBosch;

import bolts.Continuation;
import bolts.Task;

import static android.content.Context.SENSOR_SERVICE;




public class MetaWearFragment extends Fragment implements SensorEventListener, ServiceConnection {

    // for the bluetooth enable request
    private static final int REQUEST_ENABLE_BT = 1;

    //Internal sensor variables
    private ImageView mPointer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private ImageView mPointerEx;
    private float[] exLastAccelerometer = new float[3];
    private float[] exLastMagnetometer = new float[3];
    private boolean exLastAccelerometerSet = false;
    private boolean exLastMagnetometerSet = false;
    private float[] exR = new float[9];
    private float[] exOrientation = new float[3];
    private float exCurrentDegree = 0f;
    private boolean isBound = false;


    public MetaWearFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Adapter for checking if bluetooth is not on
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            // creating dialogue that asks if you want to enable bluetooth or not
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //Bind the service when the activity is created
        isBound = getActivity().getApplicationContext().bindService(new Intent(getActivity(), BtleService.class),
                this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_metawear, container, false);
        // Sensormanager for the internal sensor
        mSensorManager = (SensorManager)getActivity().getSystemService(SENSOR_SERVICE);
        // If phone has internal accelerometer and magnetic field sensors then it uses them
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null ||
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mPointer = v.findViewById(R.id.pointer);
        }
        mPointerEx = v.findViewById(R.id.ex_pointer);



        return v;
    }


    public void onStart() {
        super.onStart();
        // Registers listeners for the sensors
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void onResume() {
        super.onResume();
        // Registers listeners for the sensors
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

    }

    public void onStop() {
        super.onStop();
        // unregisters listeners for the sensors
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }
    public void onPause(){
        super.onPause();
        // unregisters listeners for the sensors
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // closes service and makes sure that board doesn't use battery when app is closed
        if(isBound){
            board.tearDown();
            getActivity().getApplicationContext().unbindService(this);
        }




    }

    //Internal sensors pointer moving when getting data
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == mAccelerometer) {
            // Copies Accelerometer event values to the mLastAccelerometer array
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            // When accessing first time setting to true
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            // Copies Magnetometer event values to the mLastMagnetometer array
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            // When accessing first time setting to true
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            // Making matrix for the orientation with arrays of the acclerometer and magnetometer
            // values
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            // getting direction in radians and changing it to degrees
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            // Making animation for the compass
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);
            // Starting animation
            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    // When using external sensor
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) iBinder;
        // board variable initialization
        retrieveBoard();
        // asynchronous connection to metawear board
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Route>>() {
            @Override
            public Task<Route> then(Task<Void> task) throws Exception {
                // Creating route for the sensorfusion magnetometer
                final SensorFusionBosch sensorFusion = board.getModule(SensorFusionBosch.class);
                // configuring sensorfusion
                sensorFusion.configure()
                        .mode(SensorFusionBosch.Mode.COMPASS)
                        .commit();
                // Returning route
                return sensorFusion.correctedMagneticField().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                // the data that comes from the metawear is added to movepointer
                                // function
                                movePointer(data, null);
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Route>() {
                    @Override
                    public Route then(Task<Route> task) throws Exception {
                        // Starting sensorfusion
                        sensorFusion.correctedMagneticField().start();
                        sensorFusion.start();
                        return null;
                    }
                });
            }
        });

        // asynchronous connection to metawear board
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Route>>() {
            @Override
            public Task<Route> then(Task<Void> task) throws Exception {
                // Creating route for the sensorfusion accelerometer
                final SensorFusionBosch sensorFusionBosch = board.getModule(SensorFusionBosch.class);
                // configuring sensorfusion
                sensorFusionBosch.configure()
                        .mode(SensorFusionBosch.Mode.COMPASS)
                        .commit();
                return sensorFusionBosch.correctedAcceleration().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                // the data that comes from the metawear is added to movepointer
                                // function
                                movePointer(null, data);
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Route>() {
                    @Override
                    public Route then(Task<Route> task) throws Exception {
                        // Starting sensorfusion
                        sensorFusionBosch.correctedAcceleration().start();
                        sensorFusionBosch.start();
                        return null;
                    }
                });
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    public void retrieveBoard() {
        // Connecting to the metawear with bluetooth
        final BluetoothManager btManager=
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        // Using hard coded mac address because we didn't have time to make scanner
        String MW_MAC_ADDRESS = "CB:AA:89:01:48:20";
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);
    }
    // Moving external compass
    public void movePointer(Data magnetic, Data accel) {
        if(magnetic != null && accel == null){
            // Creating float array for the incoming data
            float magnetometer[] = new float[3];
            // Changing Tesla to microTesla because values are too close to zero otherwise
            float changeToMicros = 1000000;
            magnetometer[0] = magnetic.value(SensorFusionBosch.CorrectedMagneticField.class).x()* changeToMicros;
            magnetometer[1] = magnetic.value(SensorFusionBosch.CorrectedMagneticField.class).y()* changeToMicros;
            magnetometer[2] = magnetic.value(SensorFusionBosch.CorrectedMagneticField.class).z()* changeToMicros;
            // Copied magnetometer array to exLastMagnetometer array
            System.arraycopy(magnetometer, 0, exLastMagnetometer, 0, magnetometer.length);
            // First time setting to true
            exLastMagnetometerSet = true;
        }

        else if(accel != null && magnetic == null){
            // Creating float array for the incoming data
            float accelerometer[] = new float[3];
            // Changing g values to acceleration because that is needed
            float changeToaccel = 9.81f;
            accelerometer[0] = accel.value(SensorFusionBosch.CorrectedAcceleration.class).x()* changeToaccel;
            accelerometer[1] = accel.value(SensorFusionBosch.CorrectedAcceleration.class).y()* changeToaccel;
            accelerometer[2] = accel.value(SensorFusionBosch.CorrectedAcceleration.class).z()* changeToaccel;
            // Copied accelerometer array to exLastAccelerometer array
            System.arraycopy(accelerometer, 0, exLastAccelerometer, 0, accelerometer.length);
            // First time setting to true
            exLastAccelerometerSet = true;
        }

        if (exLastAccelerometerSet && exLastMagnetometerSet) {
            // Same thing as in sensorchanged function to making compass move
            SensorManager.getRotationMatrix(exR, null, exLastAccelerometer, exLastMagnetometer);
            SensorManager.getOrientation(exR, exOrientation);
            float azimuthInRadians = exOrientation[0];
            float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            RotateAnimation ra = new RotateAnimation(
                    exCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointerEx.startAnimation(ra);
            exCurrentDegree = -azimuthInDegress;
        }


    }


}
