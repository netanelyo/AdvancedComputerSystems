package com.orinati.advancedsystems.servoble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationVectorSensor {
    private SensorManager       mSensorManager;
    private Sensor              mRotationSensor;
    private RotationListener    mRotationListener;
    private RotationManager     mRotationManager;

    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGatt               mGatt;

    public RotationVectorSensor(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mRotationManager = new RotationManager();
        mRotationListener = new RotationListener();
    }

    public void registerListener(BluetoothGatt gatt,
                                 BluetoothGattCharacteristic characteristic) {
        mGatt = gatt;
        mCharacteristic = characteristic;
        mSensorManager.registerListener(mRotationListener, mRotationSensor,
                                        SensorManager.SENSOR_DELAY_GAME);
    }

    private void update(float[] vectors) {
        SensorManager.getRotationMatrixFromVector(mRotationManager.matrix, vectors);
        int worldAxisY = SensorManager.AXIS_Y;
        int worldAxisZ = SensorManager.AXIS_Z;
        SensorManager.remapCoordinateSystem(mRotationManager.matrix, worldAxisY, worldAxisZ,
                                            mRotationManager.adjustedMatrix);
        SensorManager.getOrientation(mRotationManager.adjustedMatrix, mRotationManager.orientation);

        int pitch   = (int) Math.round(Math.toDegrees(mRotationManager.orientation[1]));
        int roll    = (int) Math.round(Math.toDegrees(mRotationManager.orientation[2]));
        writeRotationValues(pitch, roll);
    }

    private void writeRotationValues(int pitch, int roll) {
        if (roll > 90) {
            roll = 90;
        }
        else if (roll < -90) {
            roll = -90;
        }

        //TODO: complete
//        int toWrite =
//        mCharacteristic.setValue(, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
//        mGatt.writeCharacteristic(mCharacteristic);
    }


    private class RotationListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        @Override
        public void onSensorChanged(SensorEvent event) {
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
