package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.sprtcoding.commutech.Loading.LoadingDialog;

public class ForgotPasswordPage extends AppCompatActivity {
    private MaterialButton _resetBtn, _backBtn, _successOkBtn;
    private TextInputEditText _emailET;
    private LoadingDialog loadingDialog;
    private TextView _successAlertMSG;
    private ImageButton _successCloseBtn;
    FirebaseAuth mAuth;
    String email;
    AlertDialog successDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_page);
        _var();

        //success dialog
        View successAlertDialog = LayoutInflater.from(ForgotPasswordPage.this).inflate(R.layout.success_dialog, null);
        AlertDialog.Builder successAlertDialogBuilder = new AlertDialog.Builder(ForgotPasswordPage.this);

        successAlertDialogBuilder.setView(successAlertDialog);

        _successAlertMSG = successAlertDialog.findViewById(R.id.successMSG);
        _successCloseBtn = successAlertDialog.findViewById(R.id.closeDialogBtnSuccess);
        _successOkBtn = successAlertDialog.findViewById(R.id.okBtnSuccess);

        successDialog = successAlertDialogBuilder.create();

        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        successDialog.setCanceledOnTouchOutside(false);
        //end of success dialog

        mAuth = FirebaseAuth.getInstance();

        loadingDialog = new LoadingDialog(this);

        _resetBtn.setOnClickListener(view -> {
            loadingDialog.show();
            email = _emailET.getText().toString().trim();
            if(!TextUtils.isEmpty(email)) {
                ResetPassword();
            } else {
                _emailET.setError("Email field can't empty!");
                loadingDialog.dismiss();
            }
        });

        _backBtn.setOnClickListener(view -> {
            loadingDialog.show();
            Handler handler = new Handler();
            Runnable runnable = () -> {
                loadingDialog.dismiss();
                finish();
            };
            handler.postDelayed(runnable, 1500);
        });
    }

    @SuppressLint("SetTextI18n")
    private void ResetPassword() {
        _resetBtn.setVisibility(View.INVISIBLE);

        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(unused -> {
            loadingDialog.dismiss();
            _successAlertMSG.setText("Reset Password link has been sent to your registered Email.");
            successDialog.show();
            _successCloseBtn.setOnClickListener(view1 -> {
                successDialog.cancel();
                finish();
            });
            _successOkBtn.setOnClickListener(view1 -> {
                successDialog.cancel();
                finish();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: - " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
            _resetBtn.setVisibility(View.VISIBLE);
        });
    }

    private void _var() {
        _resetBtn = findViewById(R.id.resetBtn);
        _backBtn = findViewById(R.id.backBtn);
        _emailET = findViewById(R.id.emailET);
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}