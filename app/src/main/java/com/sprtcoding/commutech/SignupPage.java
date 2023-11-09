package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.sprtcoding.commutech.FireStoreDB.DBQuery;
import com.sprtcoding.commutech.FireStoreDB.MyCompleteListener;
import com.sprtcoding.commutech.Loading.LoadingDialog;

import java.util.HashMap;
import java.util.Objects;

public class SignupPage extends AppCompatActivity {

    private TextView _loginBtn, _alertMSG, _successAlertMSG;
    private LinearLayout _parentLLPhone;
    private CountryCodePicker _ccp, _ccp_parent;
    private EditText _phone_number, _phone_number_parent;
    private MaterialButton _signUpBtn, _okBtn, _successOkBtn, _continueBtn;
    private RadioGroup _radioGroup;
    private String _selectedOption;
    private TextInputEditText _fullname, _email, _password, _address, _parentsNameET;
    private TextInputLayout _parentNameLL;
    private LoadingDialog loadingDialog;
    private ImageButton _closeDialogBtn, _successCloseBtn;
    private CheckBox _dataPrivacyCB;

    FirebaseAuth mAuth;
    FirebaseDatabase mdb;
    DatabaseReference _userRef, _userTokenRef;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);
        _var();

        _firebase();

        loadingDialog = new LoadingDialog(this);

        DBQuery.g_firestore = FirebaseFirestore.getInstance();

        //success dialog
        View successAlertDialog = LayoutInflater.from(SignupPage.this).inflate(R.layout.success_dialog, null);
        AlertDialog.Builder successAlertDialogBuilder = new AlertDialog.Builder(SignupPage.this);

        successAlertDialogBuilder.setView(successAlertDialog);

        _successAlertMSG = successAlertDialog.findViewById(R.id.successMSG);
        _successCloseBtn = successAlertDialog.findViewById(R.id.closeDialogBtnSuccess);
        _successOkBtn = successAlertDialog.findViewById(R.id.okBtnSuccess);

        final AlertDialog successDialog = successAlertDialogBuilder.create();

        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        successDialog.setCanceledOnTouchOutside(false);
        //end of success dialog

        //warning dialog
        View warningAlertDialog = LayoutInflater.from(SignupPage.this).inflate(R.layout.warning_dialog, null);
        AlertDialog.Builder warningAlertDialogBuilder = new AlertDialog.Builder(SignupPage.this);

        warningAlertDialogBuilder.setView(warningAlertDialog);

        _alertMSG = warningAlertDialog.findViewById(R.id.warningMSG);
        _closeDialogBtn = warningAlertDialog.findViewById(R.id.closeDialogBtn);
        _okBtn = warningAlertDialog.findViewById(R.id.okBtn);

        final AlertDialog warningDialog = warningAlertDialogBuilder.create();

        warningDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        warningDialog.setCanceledOnTouchOutside(false);

        _closeDialogBtn.setOnClickListener(view -> {
            warningDialog.cancel();
        });

        _okBtn.setOnClickListener(view -> {
            warningDialog.cancel();
        });
        //end of warning dialog

        //privacy policy dialog
        View privacyPolicyDialog = LayoutInflater.from(SignupPage.this).inflate(R.layout.privacy_policy_dialog, null);
        AlertDialog.Builder privacyPolicyDialogBuilder = new AlertDialog.Builder(SignupPage.this);

        privacyPolicyDialogBuilder.setView(privacyPolicyDialog);

        _continueBtn = privacyPolicyDialog.findViewById(R.id.continueBtn);

        final AlertDialog privacyAndPolicyDialog = privacyPolicyDialogBuilder.create();

        privacyAndPolicyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        privacyAndPolicyDialog.setCanceledOnTouchOutside(false);

        _continueBtn.setOnClickListener(view -> {
            privacyAndPolicyDialog.dismiss();
        });

        //end of privacy policy dialog

        _radioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            RadioButton radioButton = findViewById(checkedId);
            _selectedOption = radioButton.getText().toString();
            if(_selectedOption.equals("Student")) {
                _parentLLPhone.setVisibility(View.VISIBLE);
                _parentNameLL.setVisibility(View.VISIBLE);
            } else if (_selectedOption.equals("Parents")) {
                _parentLLPhone.setVisibility(View.GONE);
                _parentNameLL.setVisibility(View.GONE);
            }
        });

        _signUpBtn.setOnClickListener(view -> {
            loadingDialog.show();
            String email = Objects.requireNonNull(_email.getText()).toString();
            String password = Objects.requireNonNull(_password.getText()).toString();
            String fullname = Objects.requireNonNull(_fullname.getText()).toString();
            String address = Objects.requireNonNull(_address.getText()).toString();
            String contact = Objects.requireNonNull(_phone_number.getText()).toString();
            String parentsContact = Objects.requireNonNull(_phone_number_parent.getText()).toString();
            String parentsName = Objects.requireNonNull(_parentsNameET.getText()).toString();

            if(_parentLLPhone.getVisibility() == View.VISIBLE) {
                if(TextUtils.isEmpty(parentsContact)) {
                    loadingDialog.dismiss();
                    _alertMSG.setText("Parents Number is empty!");
                    warningDialog.show();
                }
            }

            if(TextUtils.isEmpty(_fullname.getText())) {
                loadingDialog.dismiss();
                _alertMSG.setText("Full Name is empty!");
                warningDialog.show();
            }else if(TextUtils.isEmpty(_email.getText())) {
                loadingDialog.dismiss();
                _alertMSG.setText("Email is empty!");
                warningDialog.show();
            }else if(TextUtils.isEmpty(_password.getText())) {
                loadingDialog.dismiss();
                _alertMSG.setText("Password is empty!");
                warningDialog.show();
            }else if(TextUtils.isEmpty(_address.getText())) {
                loadingDialog.dismiss();
                _alertMSG.setText("Address is empty!");
                warningDialog.show();
            }else if(TextUtils.isEmpty(_phone_number.getText())) {
                loadingDialog.dismiss();
                _alertMSG.setText("Contact Number is empty!");
                warningDialog.show();
            }else if(TextUtils.isEmpty(_selectedOption)) {
                loadingDialog.dismiss();
                _alertMSG.setText("Please select account type!");
                warningDialog.show();
            }else {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if(_selectedOption.equals("Student")) {

                            DBQuery.createUserData(
                                    email,
                                    fullname,
                                    address,
                                    mAuth.getCurrentUser().getUid(),
                                    _selectedOption,
                                    _ccp.getSelectedCountryCode() + contact,
                                    _ccp_parent.getSelectedCountryCode() + parentsContact,
                                    parentsName,
                                    new MyCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    loadingDialog.dismiss();
                                    _successAlertMSG.setText("Account created successfully!");
                                    successDialog.show();
                                    _successCloseBtn.setOnClickListener(view1 -> {
                                        successDialog.cancel();
                                        Intent intent = new Intent(SignupPage.this, LoginPage.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                    _successOkBtn.setOnClickListener(view1 -> {
                                        successDialog.cancel();
                                        Intent intent = new Intent(SignupPage.this, LoginPage.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(SignupPage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else if (_selectedOption.equals("Parents")) {
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task1 -> {
                                        if (!task1.isSuccessful()) {
                                            _alertMSG.setText("Fetching FCM registration token failed" + task1.getException());
                                            warningDialog.show();
                                            return;
                                        }

                                        // Get new FCM registration token
                                        String token = task1.getResult();
                                        _userTokenRef.child(mAuth.getCurrentUser().getUid()).child("token").setValue(token);
                                    });

                            DBQuery.createUserData(
                                    email,
                                    fullname,
                                    address,
                                    mAuth.getCurrentUser().getUid(),
                                    _selectedOption,
                                    _ccp_parent.getSelectedCountryCode() + contact,
                                    null,
                                    null,
                                    new MyCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    loadingDialog.dismiss();
                                    _successAlertMSG.setText("Account created successfully!");
                                    successDialog.show();
                                    _successCloseBtn.setOnClickListener(view1 -> {
                                        successDialog.cancel();
                                        Intent intent = new Intent(SignupPage.this, LoginPage.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                    _successOkBtn.setOnClickListener(view1 -> {
                                        successDialog.cancel();
                                        Intent intent = new Intent(SignupPage.this, LoginPage.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(SignupPage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        _dataPrivacyCB.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if(isChecked) {
                _signUpBtn.setEnabled(true);
                privacyAndPolicyDialog.show();
            }else {
                _signUpBtn.setEnabled(false);
            }
        });

        _loginBtn.setOnClickListener(view -> {
            loadingDialog.show();
            Handler handler = new Handler();
            Runnable runnable = () -> {
                loadingDialog.dismiss();
                Intent gotoSignUp = new Intent(this, LoginPage.class);
                startActivity(gotoSignUp);
                finish();
            };
            handler.postDelayed(runnable, 2000);
        });
    }

    private void _firebase() {
        mAuth = FirebaseAuth.getInstance();
        mdb = FirebaseDatabase.getInstance();
        _userRef = mdb.getReference("Users");
        _userTokenRef = mdb.getReference("UserToken");
    }

    private void _var() {
        _signUpBtn = findViewById(R.id.createAccountBtn);
        _loginBtn = findViewById(R.id.loginRegBtn);
        _fullname = findViewById(R.id.fullNameET);
        _email = findViewById(R.id.emailET);
        _password = findViewById(R.id.passET);
        _address = findViewById(R.id.addressET);
        _radioGroup = findViewById(R.id.radio_group);
        _parentLLPhone = findViewById(R.id.parentLLPhone);
        _ccp = findViewById(R.id.ccp);
        _ccp_parent = findViewById(R.id.ccp_parent);
        _phone_number = findViewById(R.id.phone_number);
        _phone_number_parent = findViewById(R.id.phone_number_parent);
        _dataPrivacyCB = findViewById(R.id.dataPrivacyCB);
        _parentNameLL = findViewById(R.id.parentNameLL);
        _parentsNameET = findViewById(R.id.parentsNameET);
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}