package com.sprtcoding.commutech;

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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sprtcoding.commutech.GetStarted.GetStartedPage;
import com.sprtcoding.commutech.Loading.LoadingDialog;

import java.util.Objects;

public class LoginPage extends AppCompatActivity {

    private TextView _signUpBtn, _alertMSG, _successAlertMSG, _forgotPasswordBtn;
    private MaterialButton _loginBtn, _okBtn, _successOkBtn;
    private TextInputEditText _email, _password;
    private LoadingDialog loadingDialog;
    private ImageButton _closeDialogBtn, _successCloseBtn;
    FirebaseAuth mAuth;
    DatabaseReference _userRef, _userTokenRef;
    FirebaseFirestore userDB;
    DocumentReference userDocRef;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        _var();
        loadingDialog = new LoadingDialog(this);

        userDB = FirebaseFirestore.getInstance();

        //success dialog
        View successAlertDialog = LayoutInflater.from(LoginPage.this).inflate(R.layout.success_dialog, null);
        AlertDialog.Builder successAlertDialogBuilder = new AlertDialog.Builder(LoginPage.this);

        successAlertDialogBuilder.setView(successAlertDialog);

        _successAlertMSG = successAlertDialog.findViewById(R.id.successMSG);
        _successCloseBtn = successAlertDialog.findViewById(R.id.closeDialogBtnSuccess);
        _successOkBtn = successAlertDialog.findViewById(R.id.okBtnSuccess);

        final AlertDialog successDialog = successAlertDialogBuilder.create();

        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        successDialog.setCanceledOnTouchOutside(false);
        //end of success dialog

        //warning dialog
        View warningAlertDialog = LayoutInflater.from(LoginPage.this).inflate(R.layout.warning_dialog, null);
        AlertDialog.Builder warningAlertDialogBuilder = new AlertDialog.Builder(LoginPage.this);

        warningAlertDialogBuilder.setView(warningAlertDialog);

        _alertMSG = warningAlertDialog.findViewById(R.id.warningMSG);
        _closeDialogBtn = warningAlertDialog.findViewById(R.id.closeDialogBtn);
        _okBtn = warningAlertDialog.findViewById(R.id.okBtn);

        final AlertDialog warningDialog = warningAlertDialogBuilder.create();

        Objects.requireNonNull(warningDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        warningDialog.setCanceledOnTouchOutside(false);

        _closeDialogBtn.setOnClickListener(view -> {
            warningDialog.cancel();
        });

        _okBtn.setOnClickListener(view -> {
            warningDialog.cancel();
        });
        //end of warning dialog

        mAuth = FirebaseAuth.getInstance();

        _userRef = FirebaseDatabase.getInstance().getReference("Users");
        _userTokenRef = FirebaseDatabase.getInstance().getReference("UserToken");

        _loginBtn.setOnClickListener(view -> {
            loadingDialog.show();
            String email = Objects.requireNonNull(_email.getText()).toString().trim();
            String password = Objects.requireNonNull(_password.getText()).toString();
            if(TextUtils.isEmpty(email)) {
                loadingDialog.dismiss();
                _alertMSG.setText("Email is empty!");
                warningDialog.show();
            }else if(TextUtils.isEmpty(password)) {
                loadingDialog.dismiss();
                _alertMSG.setText("Please enter your password!");
                warningDialog.show();
            }else {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {

                        userDocRef = userDB.collection("USERS").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        userDocRef.get().addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()) {
                                DocumentSnapshot document = task1.getResult();
                                if(document.contains("ACCOUNT_TYPE")) {
                                    String type = Objects.requireNonNull(document.get("ACCOUNT_TYPE")).toString();

                                    if(type.equals("Student")) {
                                        loadingDialog.dismiss();
                                        _successAlertMSG.setText("Login Successfully!");
                                        successDialog.show();
                                        _successCloseBtn.setOnClickListener(view1 -> {
                                            successDialog.cancel();
                                            Intent intent = new Intent(LoginPage.this, GetStartedPage.class);
                                            startActivity(intent);
                                            finish();
                                        });
                                        _successOkBtn.setOnClickListener(view1 -> {
                                            successDialog.cancel();
                                            Intent intent = new Intent(LoginPage.this, GetStartedPage.class);
                                            startActivity(intent);
                                            finish();
                                        });
                                    }else if (type.equals("Parents")) {
                                        loadingDialog.dismiss();

                                        FirebaseMessaging.getInstance().getToken()
                                                .addOnCompleteListener(task2 -> {
                                                    if (!task2.isSuccessful()) {
                                                        _alertMSG.setText("Fetching FCM registration token failed" + task2.getException());
                                                        warningDialog.show();
                                                        return;
                                                    }

                                                    // Get new FCM registration token
                                                    String token = task2.getResult();
                                                    _userTokenRef.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("token").setValue(token);
                                                });

                                        _successAlertMSG.setText("Login Successfully!");
                                        successDialog.show();
                                        _successCloseBtn.setOnClickListener(view1 -> {
                                            successDialog.cancel();
                                            Intent intent = new Intent(LoginPage.this, GetStartedPage.class);
                                            startActivity(intent);
                                            finish();
                                        });
                                        _successOkBtn.setOnClickListener(view1 -> {
                                            successDialog.cancel();
                                            Intent intent = new Intent(LoginPage.this, GetStartedPage.class);
                                            startActivity(intent);
                                            finish();
                                        });
                                    }
                                }
                            }
                        });
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(LoginPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                });
            }
        });

        _signUpBtn.setOnClickListener(view -> {
            loadingDialog.show();
            Handler handler = new Handler();
            Runnable runnable = () -> {
                loadingDialog.dismiss();
                Intent gotoSignUp = new Intent(this, SignupPage.class);
                startActivity(gotoSignUp);
                finish();
            };
            handler.postDelayed(runnable, 2000);
        });

        _forgotPasswordBtn.setOnClickListener(view -> {
            loadingDialog.show();
            Handler handler = new Handler();
            Runnable runnable = () -> {
                loadingDialog.dismiss();
                Intent gotoForgotPassword = new Intent(this, ForgotPasswordPage.class);
                startActivity(gotoForgotPassword);
            };
            handler.postDelayed(runnable, 2000);
        });
    }

    private void _var() {
        _signUpBtn = findViewById(R.id.signUpBtn);
        _loginBtn = findViewById(R.id.loginBtn);
        _email = findViewById(R.id.emailET);
        _password = findViewById(R.id.passET);
        _forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}