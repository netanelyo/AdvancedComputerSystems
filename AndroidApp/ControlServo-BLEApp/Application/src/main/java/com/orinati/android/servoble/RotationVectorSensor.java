package com.orinati.android.servoble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationVectorSensor {
    private static final int SAMPLE_PERIOD = 120 * 1000; // Sample rotation vector every 120ms
    private SensorManager       mSensorManager;
    private Sensor              mRotationSensor;
    private RotationListener    mRotationListener;
    private RotationManager     mRotationManager;

    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGatt               mGatt;

    boolean paused  = true;
    boolean round   = false;

    public RotationVectorSensor(SensorManager sensorManager) {
        mSensorManager      = sensorManager;
        mRotationSensor     = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mRotationManager    = new RotationManager();
        mRotationListener   = new RotationListener();
    }

    public void registerListener(BluetoothGatt gatt,
                                 BluetoothGattCharacteristic characteristic) {
        if (!paused) {
            return;
        }
        mGatt = gatt;
        mCharacteristic = characteristic;
        paused = false;
        // Register rotation vector listener
        mSensorManager.registerListener(mRotationListener, mRotationSensor, SAMPLE_PERIOD);
    }

    public void unregisterListener() {
        if (paused) {
            return;
        }
        paused = true;
        // Unregister listener
        mSensorManager.unregisterListener(mRotationListener, mRotationSensor);
    }

    private void update(float[] vectors) {
        SensorManager.getRotationMatrixFromVector(mRotationManager.matrix, vectors);
        int worldAxisY = SensorManager.AXIS_Y;
        int worldAxisZ = SensorManager.AXIS_Z;
        SensorManager.remapCoordinateSystem(mRotationManager.matrix, worldAxisY, worldAxisZ,
                                            mRotationManager.adjustedMatrix);
        SensorManager.getOrientation(mRotationManager.adjustedMatrix, mRotationManager.orientation);

        int deg;
        if (round) {
            deg = (int) Math.round(Math.toDegrees(mRotationManager.orientation[1]));
        }
        else {
            deg = (int) Math.round(Math.toDegrees(mRotationManager.orientation[2])) + 90;
        }
        writeRotationValues(deg);
        round = !round;
    }

    private void writeRotationValues(int deg) {
        if (deg >= 60) {
            deg = 60;
        }
        else if (deg <= -60) {
            deg = -60;
        }
        deg += 60;
        if (round) {
            deg |= 0x80;
        }
        mCharacteristic.setValue(deg, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        mGatt.writeCharacteristic(mCharacteristic);
    }


    private class RotationListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (paused) {
                return;
            }
            if (event.sensor == mRotationSensor) {
                if (event.values.length > 4) {
                    float[] truncatedRotationVector = new float[4];
                    System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                    update(truncatedRotationVector);
                } else {
                    update(event.values);
                }
            }
        }
    }
}
