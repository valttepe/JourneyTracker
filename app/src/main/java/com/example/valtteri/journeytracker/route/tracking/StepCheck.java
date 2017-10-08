package com.example.valtteri.journeytracker.route.tracking;

/**
 * Created by Otto on 1.10.2017.
 */
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class StepCheck implements SensorEventListener {

    public interface StepCounterListener {
        //Passes information to OrienteeringFragment if a step has been taken.
        void stepCountChanged(String sensoriChanged);
    }

    //StepCheck variables.
    private StepCounterListener listener;
    private SensorManager mSensorManager;
    private Sensor countSensor;
    boolean valueChanged;
    boolean activityRunning;
    int stepCount = 0;

    //Get step detector sensor to usage. THIS CAN'T BE PRIVATE BECAUSE IT'S CALLED FROM ORIENTEERING FRAGMENT!
    public StepCheck(Context context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }

    //Register sensor listener. THIS CAN'T BE PRIVATE BECAUSE IT'S CALLED FROM ORIENTEERING FRAGMENT!
    public boolean register(){
        return mSensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
    }

    //Unregister sensor listener. THIS CAN'T BE PRIVATE BECAUSE IT'S CALLED FROM ORIENTEERING FRAGMENT!
    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    //Set sensor listener. THIS CAN'T BE PRIVATE BECAUSE IT'S CALLED FROM ORIENTEERING FRAGMENT!
    public void setListener(StepCounterListener listener) {
        this.listener = listener;
        activityRunning = true;
    }


    //Method to pass information to OrienteeringFragment to tell if a step has been taken.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (activityRunning) {
            setSteps(1);
            valueChanged = true;

            if (listener != null) {
                listener.stepCountChanged(sensorEvent.toString());
            }
        }
    }

    //Sets steps to stepCount variable.
    private void setSteps(int steps){
        this.stepCount = stepCount + steps;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
