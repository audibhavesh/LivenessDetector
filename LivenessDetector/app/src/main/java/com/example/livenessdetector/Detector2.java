package com.example.livenessdetector;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.livenessdetector.utility.ImageAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.concurrent.ExecutionException;

public class Detector2 extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    PreviewView previewView;
    FaceDetector detector;
    ImageButton captureButton;
    ImageButton switchCamera;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector2);
        initView();
        buildDetector();
    }

    private void initView() {
        previewView = findViewById(R.id.cameraPreview);
        captureButton = findViewById(R.id.camera_capture_button);
        switchCamera = findViewById(R.id.switchCamera);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindImageAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
        switchCamera.setOnClickListener(view ->
        {
            flipCamera();
        });
    }

    private void flipCamera() {
        if (lensFacing == CameraSelector.LENS_FACING_FRONT)
            lensFacing = CameraSelector.LENS_FACING_BACK;
        else if (lensFacing == CameraSelector.LENS_FACING_BACK)
            lensFacing = CameraSelector.LENS_FACING_FRONT;
        cameraProvider.unbindAll();
        bindImageAnalysis(cameraProvider);

    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalyzer(detector, this));

//        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
//            @Override
//            public void onOrientationChanged(int orientation) {
////                textView.setText(Integer.toString(orientation));
//            }
//        };
//        orientationEventListener.enable();
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector,
                imageAnalysis, preview);
    }

    private void buildDetector() {
        FaceDetectorOptions faceDetectorOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        detector = FaceDetection.getClient(faceDetectorOptions);
//        detector = new FaceDetector.Builder(getApplicationContext())
//                .setProminentFaceOnly(true) // optimize for single, relatively large face
//                .setTrackingEnabled(true) // enable face tracking
//                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
//                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
//                .build();
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
        captureButton.setEnabled(true);
    }
}