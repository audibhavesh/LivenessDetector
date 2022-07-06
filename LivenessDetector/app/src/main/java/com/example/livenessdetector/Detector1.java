package com.example.livenessdetector;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.livenessdetector.utility.GraphicFaceTracker;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

public class Detector1 extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    FaceDetector detector;
    SurfaceView cameraView;
    CameraSource cameraSource;
    Detector1 activity;
    ImageButton captureButton;
    TextView blinkText;
    Boolean isCaptureEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector1);
        activity = this;
        cameraView = (SurfaceView) findViewById(R.id.surfaceView);
        blinkText = findViewById(R.id.blinkText);
        captureButton = findViewById(R.id.camera_capture_button);
//        captureButton.setEnabled(false);
        buildDetector();

        captureButton.setOnClickListener(view -> {
            if (isCaptureEnabled) {
                Toast.makeText(getApplicationContext(), "Image Clicked", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Not Verfied", Toast.LENGTH_SHORT).show();
            }

        });
        cameraSource = new CameraSource.Builder(getApplicationContext(), (Detector<?>) detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(1280, 720)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, REQUEST_CAMERA_PERMISSION);
                        return;
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });
    }

    private void buildDetector() {
        detector = new FaceDetector.Builder(getApplicationContext())
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
                .build();
        Detector.Processor<Face> processor;
        processor = new LargestFaceFocusingProcessor.Builder(detector, new GraphicFaceTracker(activity, blinkText)).build();
        detector.setProcessor(processor);

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void enableCaptureButton() {
        activity.runOnUiThread(() -> {
            isCaptureEnabled = true;
            blinkText.setText("Verfied Succefully");
        });
    }
}

