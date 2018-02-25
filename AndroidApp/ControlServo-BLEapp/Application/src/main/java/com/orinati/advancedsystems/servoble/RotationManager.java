package com.orinati.advancedsystems.servoble;

public class RotationManager {
    float[] orientation;
    float[] matrix;
    float[] adjustedMatrix;

    public RotationManager() {
        matrix          = new float[9];
        adjustedMatrix  = new float[9];
        orientation     = new float[3];
    }
}
