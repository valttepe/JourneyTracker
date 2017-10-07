package com.example.valtteri.journeytracker.main.navigation;

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
import android.util.Log;
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


/**
 * Created by Valtteri on 19.9.2017.
 */

public class MetaWearFragment extends Fragment implements SensorEventListener, ServiceConnection {

    MainActivity mainActivity;

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

    // External sensor variables
    private final String MW_MAC_ADDRESS = "CB:AA:89:01:48:20";
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
    private float changeToMicros = 1000000;
    private float changeToaccel = 9.81f;
    private boolean isBound = false;


    public MetaWearFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Bind the service when the activity is created
       isBound = getActivity().getApplicationContext().bindService(new Intent(getActivity(), BtleService.class),
                this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_metawear, container, false);
        mSensorManager = (SensorManager)getActivity().getSystemService(SENSOR_SERVICE);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null ||
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mPointer = (ImageView)v.findViewById(R.id.pointer);
        }
        mPointerEx = (ImageView)v.findViewById(R.id.ex_pointer);



        return v;
    }


    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

    }

    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }
    public void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isBound == true){
            board.tearDown();
            getActivity().getApplicationContext().unbindService(this);
        }




    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) iBinder;
        retrieveBoard();
        Log.i("oonks ees täällä ", "enpä tiedä");
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Route>>() {
            @Override
            public Task<Route> then(Task<Void> task) throws Exception {
                final SensorFusionBosch sensorFusion = board.getModule(SensorFusionBosch.class);
                sensorFusion.configure()
                        .mode(SensorFusionBosch.Mode.COMPASS)
                        .commit();
                Log.i("hei mä oon täällä", "joo nii oot ");
                return sensorFusion.correctedMagneticField().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                Log.i("MAGNETICPER....", data.value(SensorFusionBosch.CorrectedMagneticField.class).toString());
                                movePointer(data, null);
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Route>() {
                    @Override
                    public Route then(Task<Route> task) throws Exception {
                        sensorFusion.correctedMagneticField().start();
                        sensorFusion.start();
                        return null;
                    }
                });
            }
        });


        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Route>>() {
            @Override
            public Task<Route> then(Task<Void> task) throws Exception {
                final SensorFusionBosch sensorFusionBosch = board.getModule(SensorFusionBosch.class);
                sensorFusionBosch.configure()
                        .mode(SensorFusionBosch.Mode.COMPASS)
                        .commit();
                return sensorFusionBosch.correctedAcceleration().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                Log.i("Acceleratorperk....", data.value(SensorFusionBosch.CorrectedAcceleration.class).toString());
                                movePointer(null, data);
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Route>() {
                    @Override
                    public Route then(Task<Route> task) throws Exception {
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
        final BluetoothManager btManager=
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);
    }

    public void movePointer(Data magnetic, Data accel) {
        if(magnetic != null && accel == null){
            float test[] = new float[3];
            test[0] = magnetic.value(SensorFusionBosch.CorrectedMagneticField.class).x()*changeToMicros;
            test[1] = magnetic.value(SensorFusionBosch.CorrectedMagneticField.class).y()*changeToMicros;
            test[2] = magnetic.value(SensorFusionBosch.CorrectedMagneticField.class).z()*changeToMicros;
            System.arraycopy(test, 0, exLastMagnetometer, 0, test.length);
            exLastMagnetometerSet = true;

            //Log.i("VITUNMAGNEETTI",magnetic.value(CorrectedMagneticField.class).toString());
        }


        else if(accel != null && magnetic == null){
            //Log.i("PERKELEENKIIHTYVYYS", accel.value(CorrectedAcceleration.class).toString());
            float test2[] = new float[3];
            test2[0] = accel.value(SensorFusionBosch.CorrectedAcceleration.class).x()*changeToaccel;
            test2[1] = accel.value(SensorFusionBosch.CorrectedAcceleration.class).y()*changeToaccel;
            test2[2] = accel.value(SensorFusionBosch.CorrectedAcceleration.class).z()*changeToaccel;
            System.arraycopy(test2, 0, exLastAccelerometer, 0, test2.length);

            exLastAccelerometerSet = true;
        }

        if (exLastAccelerometerSet && exLastMagnetometerSet) {
            Log.i("tuleeks se tänne", "mahdollisesti");
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
