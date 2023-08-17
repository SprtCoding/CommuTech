package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
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
    private static String _sName,_dName,_dDLNo,_dRegNo,_dFranchise,_location,_parentCNo, _smsRecordsID;
    private LoadingDialog loadingDialog;
    private static final String SEMAPHORE_API_KEY = "504f64d26e9cc6f6142730dd83808f54";

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
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    if(documentSnapshot.contains("PARENTS_NUMBER")) {
                        _parentCNo = documentSnapshot.getString("PARENTS_NUMBER");
                    }
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
            sendSMS();
            //String smsDetails = _smsDetails.getText().toString();
//            new Thread(() -> {
//                try {
//                    sendTextMessage("09560040607", smsDetails);
//                } catch (JSONException e) {
//                    runOnUiThread(() -> Toast.makeText(this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//                }
//            }).start();

        });

        _backBtn.setOnClickListener(view -> {
            finish();
        });

        _emergencyTxtCheck.setOnClickListener(view -> {
            if(_emergencyTxtCheck.isChecked()) {
                String emergencyText = "Emergency Alert\n\nI need help please report this information to the nearest police station.\n" +
                        "Student's name:\n" + _sName + "\nDriver's Name:\n" + _dName + "\nDriver's License Number:\n" +
                        _dDLNo + "\nDriver's Registration Number:\n" + _dRegNo + "\nDriver's Franchise Number:\n" + _dFranchise
                        + "\n Location:\n" + _location+ ".\nOpen in Google Maps: https://www.google.com/maps?q="
                        + lat + "," + lon;
                _smsDetails.setText(emergencyText);
            }else {
                _smsDetails.setText("Hi there! Good Day. \nYour Son/Daughter named " + _sName + " is with Driver named " + _dName + "!\nwith Drivers License Number of " + _dDLNo + " and Vehicle Registration Number of "
                        + _dRegNo + " then the Franchise Number is " + _dFranchise + ".\nCurrent Location is " + _location + ".\nOpen in Google Maps: https://www.google.com/maps?q="
                        + lat + "," + lon + "\nHave a Good Day!\nThank you!");
            }
        });
    }

//    private void sendTextMessage(String parentCNo, String smsDetails) throws JSONException {
//        OkHttpClient client = new OkHttpClient();
//        MediaType mediaType = MediaType.parse("application/json");
//
//        JSONObject jsonBody = new JSONObject();
//        JSONArray destinations = new JSONArray();
//        destinations.put(new JSONObject().put("to", parentCNo));
//
//        try {
//            JSONObject messageObject = new JSONObject();
//            messageObject.put("from", "CommuTech");
//            messageObject.put("text", smsDetails);
//            messageObject.put("destinations", destinations);
//
//            JSONArray messages = new JSONArray();
//            messages.put(messageObject);
//
//            jsonBody.put("messages", messages);
//        }
//        catch (Exception e) {
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//
//        RequestBody body = RequestBody.create(mediaType, jsonBody.toString());
//
//        Request request = new Request.Builder()
//                .url("https://api.semaphore.co/api/v4/messages")
//                .method("POST", body)
//                .addHeader("Authorization", "Bearer " + SEMAPHORE_API_KEY)
//                .addHeader("Content-Type", "application/json")
//                .build();
//
//        try {
//            Response response = client.newCall(request).execute();
//            runOnUiThread(() -> {
//                if (response.isSuccessful()) {
//                    Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "Not sent", Toast.LENGTH_SHORT).show();
//                }
//            });
//            // Handle response
//        } catch (IOException e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }

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

    private void sendSMS() {
        String smsDetails = _smsDetails.getText().toString();

        Intent i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", _parentCNo, null));
        i.putExtra("sms_body", smsDetails);
        startActivity(i);

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat hoursFormat = new SimpleDateFormat("hh:mm:ss a");

        String formattedDate = dateFormat.format(calendar.getTime());
        String formattedTime = hoursFormat.format(calendar.getTime());

        String id = _recordRef.push().getKey();

        DBQuery.setSMSRecentHistory(
                id,
                _sName,
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
                loadingDialog.dismiss();
            }
        }).addOnFailureListener(e -> {
            loadingDialog.dismiss();
            Toast.makeText(SendSMS.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}