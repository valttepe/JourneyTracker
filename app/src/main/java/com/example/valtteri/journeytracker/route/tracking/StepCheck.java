package com.example.valtteri.journeytracker.route.tracking;

/**
 * Created by Otto on 1.10.2017.
 */
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class StepCheck implements SensorEventListener {

    public interface StepCounterListener {
        void stepCountChanged(String sensoriChanged);
    }

    private StepCounterListener listener;

    private SensorManager mSensorManager;
    private Sensor countSensor;
    boolean valueChanged;
    boolean activityRunning;

    int stepCount = 0;


    public StepCheck(Context context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

    }
    public boolean register(){
        Log.d("Internal sensor", "listener registered maybe?");
        return mSensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }


    public void setListener(StepCounterListener listener) {
        this.listener = listener;
        activityRunning = true;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("step counter", "do we ever come here? " + sensorEvent.values[0]);
        if (activityRunning) {
            Log.d("AABLYAT", sensorEvent.toString());
            setSteps(1);
            valueChanged = true;
            Log.d("Steps combined: ", Integer.toString(stepCount));
            if (listener != null) {
                listener.stepCountChanged(sensorEvent.toString());
            }

        }
    }
    public boolean getSensorState(){
        return valueChanged;
    }
    private void setSteps(int steps){
        this.stepCount = stepCount + steps;
    }

    public int getSteps(){
        return stepCount;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
