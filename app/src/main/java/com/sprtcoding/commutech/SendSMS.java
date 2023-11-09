package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sprtcoding.commutech.FireStoreDB.DBQuery;
import com.sprtcoding.commutech.FirebaseSMS.FirebaseSMSHelper;
import com.sprtcoding.commutech.Loading.LoadingDialog;
//import com.vonage.client.VonageClient;
//import com.vonage.client.sms.MessageStatus;
//import com.vonage.client.sms.SmsSubmissionResponse;
//import com.vonage.client.sms.messages.TextMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;

public class SendSMS extends AppCompatActivity {
    private ImageView _backBtn;
    private CheckBox _emergencyTxtCheck;
    private EditText _smsDetails;
    private MaterialButton _sendSmsBtn;
    private static String lat, lon;
    private DatabaseReference _recordRef, _userRef;
    private FirebaseDatabase mdb;
    private FirebaseAuth mAuth;
    FirebaseFirestore userDB;
    DocumentReference userDocRef;
    private static String _sName,_dName,_dDLNo,_dRegNo,_dFranchise,_location,_parentCNo;
    private LoadingDialog loadingDialog;
    private static final String SEMAPHORE_API_KEY = "504f64d26e9cc6f6142730dd83808f54";
    private static final String SENDER_NAME = "SEMAPHORE";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);
        _var();

        mAuth = FirebaseAuth.getInstance();

        userDB = FirebaseFirestore.getInstance();

        DBQuery.g_firestore = FirebaseFirestore.getInstance();

        userDocRef = userDB.collection("USERS").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) {
                if(documentSnapshot.contains("PARENTS_NUMBER")) {
                    _parentCNo = documentSnapshot.getString("PARENTS_NUMBER");
                }
            }
        });

        loadingDialog = new LoadingDialog(this);

        mdb = FirebaseDatabase.getInstance();
        _recordRef = mdb.getReference("Records");
        _userRef = mdb.getReference("Users");

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("StudentName") && intent.hasExtra("DriverName") && intent.hasExtra("DriverLicense") && intent.hasExtra("DriverRegNo") && intent.hasExtra("DriverFranchise")
                && intent.hasExtra("Location") && intent.hasExtra("Lat") && intent.hasExtra("Long")) {
             _sName = intent.getStringExtra("StudentName");
             _dName = intent.getStringExtra("DriverName");
             _dDLNo = intent.getStringExtra("DriverLicense");
             _dRegNo = intent.getStringExtra("DriverRegNo");
             _dFranchise = intent.getStringExtra("DriverFranchise");
             _location = intent.getStringExtra("Location");
            lat = intent.getStringExtra("Lat");
            lon = intent.getStringExtra("Long");

            _smsDetails.setText("Hi there! Good Day. \nYour Son/Daughter named " + _sName + " is with Driver named " + _dName + "!\nwith Drivers License Number of " + _dDLNo + " and Vehicle Registration Number of "
            + _dRegNo + " then the Franchise Number is " + _dFranchise + ".\nCurrent Location is " + _location + ".\nOpen in Google Maps: https://www.google.com/maps?q="
                   + lat + "," + lon + "\nHave a Good Day!\nThank you!");
        }

        _sendSmsBtn.setOnClickListener(view -> {
            loadingDialog.show();
            //sendSMS();
            sendTextMessage();

        });

        _backBtn.setOnClickListener(view -> {
            finish();
        });

        _emergencyTxtCheck.setOnClickListener(view -> {
            if(_emergencyTxtCheck.isChecked()) {
                String emergencyText = "Emergency Alert\n\nYour son/Daughter " + _sName + " is in an emergency situation.\n\n" +
                        "This is the driver’s information:\n" + "Driver's Name:\n" + _dName + "\nDriver's License Number:\n" +
                        _dDLNo + "\nDriver's Registration Number:\n" + _dRegNo + "\nDriver's Franchise Number:\n" + _dFranchise
                        + "\n Location:\n" + _location+ ".\nOpen in Google Maps: https://www.google.com/maps?q="
                        + lat + "," + lon;
                _sendSmsBtn.setEnabled(false);
                viewEmergencyDialog();
                _smsDetails.setText(emergencyText);
            }else {
                _sendSmsBtn.setEnabled(true);

                _smsDetails.setText("Hi there! Good Day. \nYour Son/Daughter named " + _sName + " is with Driver named " + _dName + "!\nwith Drivers License Number of " + _dDLNo + " and Vehicle Registration Number of "
                        + _dRegNo + " then the Franchise Number is " + _dFranchise + ".\nCurrent Location is " + _location + ".\nOpen in Google Maps: https://www.google.com/maps?q="
                        + lat + "," + lon + "\nHave a Good Day!\nThank you!");
            }
        });
    }

    private void viewEmergencyDialog() {
        View privacyPolicyDialog = LayoutInflater.from(SendSMS.this).inflate(R.layout.emergency_alert_dialog, null);
        AlertDialog.Builder privacyPolicyDialogBuilder = new AlertDialog.Builder(SendSMS.this);

        privacyPolicyDialogBuilder.setView(privacyPolicyDialog);

        MaterialButton _yesBtn = privacyPolicyDialog.findViewById(R.id.yesBtn);
        MaterialButton _noBtn = privacyPolicyDialog.findViewById(R.id.noBtn);

        final AlertDialog privacyAndPolicyDialog = privacyPolicyDialogBuilder.create();

        privacyAndPolicyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        privacyAndPolicyDialog.setCanceledOnTouchOutside(false);

        _yesBtn.setOnClickListener(view -> {
            privacyAndPolicyDialog.dismiss();
            _sendSmsBtn.setEnabled(true);
        });

        _noBtn.setOnClickListener(view -> {
            privacyAndPolicyDialog.dismiss();
            Toast.makeText(this, "You cannot send emergency sms!", Toast.LENGTH_SHORT).show();
            _sendSmsBtn.setEnabled(false);
        });

        privacyAndPolicyDialog.show();
    }

    private void sendTextMessage() {
        new SendSmsTask().execute();
    }

    private class SendSmsTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                URL url = new URL("https://semaphore.co/api/v4/messages");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // ... Set up your parameters here ...
                String postData = "apikey=" + SEMAPHORE_API_KEY +
                        "&number=" + _parentCNo +
                        "&message=" + _smsDetails.getText().toString() +
                        "&sendername=" + SENDER_NAME;

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read and process the response if needed
                    return responseCode;
                } else {
                    // Handle error response
                    Toast.makeText(SendSMS.this, "Error.", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                loadingDialog.dismiss();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            // Update UI or show a Toast indicating that the message was sent
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Show a Toast on the main UI thread
                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat hoursFormat = new SimpleDateFormat("hh:mm:ss a");

                String formattedDate = dateFormat.format(calendar.getTime());
                String formattedTime = hoursFormat.format(calendar.getTime());

                String id = _recordRef.push().getKey();
                String sms_type = "";

                if(_emergencyTxtCheck.isChecked()) {
                    sms_type = "emergency";
                }else {
                    sms_type = "normal";
                }

                DBQuery.setSMSRecentHistory(
                        id,
                        _sName,
                        sms_type,
                        _location,
                        _dName,
                        _dDLNo,
                        _dRegNo,
                        _dFranchise,
                        formattedDate,
                        formattedTime);

                HashMap<String, Object> map = new HashMap<>();
                map.put("UID", id);
                map.put("StudentName", _sName);
                map.put("Sms_type", sms_type);
                map.put("Location", _location);
                map.put("DriverName", _dName);
                map.put("DriverLicense", _dDLNo);
                map.put("DriverRegNo", _dRegNo);
                map.put("DriverFranchise", _dFranchise);
                map.put("Date", formattedDate);
                map.put("Time", formattedTime);

                assert id != null;
                _recordRef.child(id).setValue(map).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(SendSMS.this, "SMS sent successfully!", Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                }).addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(SendSMS.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                // Show a Toast for an error case
                Toast.makeText(SendSMS.this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        }
    }

    private void _var() {
        _backBtn = findViewById(R.id.backBtn);
        _smsDetails = findViewById(R.id.msgDetails);
        _sendSmsBtn = findViewById(R.id.sendSmsBtn);
        _emergencyTxtCheck = findViewById(R.id.emergencyTxtCheck);
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}