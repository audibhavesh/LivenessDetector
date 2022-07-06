package com.example.livenessdetector.utility;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.livenessdetector.Detector1;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class GraphicFaceTracker extends Tracker<Face> {

    private final float OPEN_THRESHOLD = 0.85f;
    private final float CLOSE_THRESHOLD = 0.4f;
    Detector1 activity;
    private int state = 0;
    private int blinkCount = 0;
    TextView blinkText;

    public GraphicFaceTracker(Detector1 activity, TextView blinkText) {
        this.activity = activity;
        this.blinkText = blinkText;
    }

    void blink(float value) {
        switch (state) {
            case 0:
                if (value > OPEN_THRESHOLD) {
                    // Both eyes are initially open
                    state = 1;
                }
                break;

            case 1:
                if (value < CLOSE_THRESHOLD) {
                    // Both eyes become closed
                    state = 2;
                }
                break;

            case 2:
                if (value > OPEN_THRESHOLD) {
                    // Both eyes are open again
                    Log.i("BlinkTracker", "blink occurred!");
                    if (blinkCount == 2) {
                        activity.enableCaptureButton();
                        state = 3;
                    } else {
                        if (state == 2) {
                            activity.runOnUiThread(() -> {
                                blinkText.setText("Blink " + blinkCount);
                            });
                            blinkCount++;
                            state = 0;
                        }
                    }

                }
                break;
            default:
                break;
        }


    }

    @Override
    public void onUpdate(@NonNull FaceDetector.Detections<Face> detectionResults, Face face) {

        float left = face.getIsLeftEyeOpenProbability();
        float right = face.getIsRightEyeOpenProbability();
        if ((left == Face.UNCOMPUTED_PROBABILITY) ||
                (right == Face.UNCOMPUTED_PROBABILITY)) {
            // One of the eyes was not detected.
            return;
        }
        float smile = face.getIsSmilingProbability();
//        Log.d("smileProb", String.valueOf(smile));
        float value = Math.min(left, right);
        blink(value);
    }
}
