package com.example.livenessdetector.utility;

import android.annotation.SuppressLint;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.livenessdetector.Detector2;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetector;

public class ImageAnalyzer implements ImageAnalysis.Analyzer {
    FaceDetector detector;

    private final float OPEN_THRESHOLD = 0.85f;
    private final float CLOSE_THRESHOLD = 0.4f;
    Detector2 activity;
    private int state = 0;
    private int blinkCount = 0;

    public ImageAnalyzer(FaceDetector detector, Detector2 activity) {
        this.detector = detector;
        this.activity = activity;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeOptInUsageError") Image mediaImage = image.getImage();
        if (mediaImage != null) {
            var inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
            var result = detector.process(inputImage).addOnSuccessListener(faces -> {
                Log.d("leng", String.valueOf(faces.size()));
                for (Face face : faces) {
                    if (face.getRightEyeOpenProbability() != null && face.getLeftEyeOpenProbability() != null) {
                        float left = face.getLeftEyeOpenProbability();
                        float right = face.getRightEyeOpenProbability();
                        if ((left == com.google.android.gms.vision.face.Face.UNCOMPUTED_PROBABILITY) ||
                                (right == com.google.android.gms.vision.face.Face.UNCOMPUTED_PROBABILITY)) {
                            // One of the eyes was not detected.
                            continue;
                        }

                        float value = Math.min(left, right);
                        blink(value);
                    }
                }
            });
        }
        image.close();
    }

    void blink(float value) {
        Log.i("BlinkTracker", String.valueOf(value));

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
                    if (blinkCount >= 3) {
                        activity.enableCaptureButton();
                    }
                    blinkCount++;
                    state = 0;

                }
                break;
        }


    }

}
