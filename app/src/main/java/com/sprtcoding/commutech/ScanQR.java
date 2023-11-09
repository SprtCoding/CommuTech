package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraManager;
import com.sprtcoding.commutech.Loading.LoadingDialog;

import java.util.List;

public class ScanQR extends AppCompatActivity implements SurfaceHolder.Callback {

    private ImageView _logoutBtn;
    private LoadingDialog loadingDialog;
    FirebaseAuth mAuth;
    boolean isLoggedOut;

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private SurfaceView cameraPreview;
    private CameraManager cameraManager;
    private BeepManager beepManager;
    private DecoratedBarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);
        _var();

        loadingDialog = new LoadingDialog(this);

        mAuth = FirebaseAuth.getInstance();

        _logoutBtn.setOnClickListener(view -> {
            loadingDialog.show();
            Handler handler = new Handler();
            Runnable runnable = () -> {
                loadingDialog.dismiss();
                mAuth.signOut();
                Intent intent = new Intent(this, LoginPage.class);
                startActivity(intent);
                finish();
            };
            handler.postDelayed(runnable, 3000);
        });

        SurfaceHolder surfaceHolder = cameraPreview.getHolder();
        surfaceHolder.addCallback(this);

        checkCameraPermission();
    }

    private void _var() {
        _logoutBtn = findViewById(R.id.logoutBtn);
        cameraPreview = findViewById(R.id.camera_preview);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        barcodeView = findViewById(R.id.barcode_scanner);

        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null) {
                    String qrCode = result.getText();
                    // Start the ResultActivity and pass the QR code as an extra
                    Intent intent = new Intent(ScanQR.this, SendNotification.class);
                    intent.putExtra("qr_code", qrCode);
                    startActivity(intent);
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Optional callback for possible result points
            }
        });

        beepManager = new BeepManager(this);

        barcodeView.setStatusText("");
        barcodeView.setTorchListener(new DecoratedBarcodeView.TorchListener() {
            @Override
            public void onTorchOn() {
                // Optional: Perform any actions when the torch is turned on.
            }

            @Override
            public void onTorchOff() {
                // Optional: Perform any actions when the torch is turned off.
            }
        });

        barcodeView.resume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
// Release camera resources.
        if (cameraManager != null) {
            cameraManager.stopPreview();
            cameraManager = null;
        }
    }
}