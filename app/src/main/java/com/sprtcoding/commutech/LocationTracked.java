package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sprtcoding.commutech.FCM.FCMNotificationSender;

public class LocationTracked extends AppCompatActivity {
    MaterialButton _stopSharingBtn;
    String parentID, userToken, _userName;
    FirebaseAuth mAuth;
    FirebaseUser mUSer;
    FirebaseFirestore db;
    CollectionReference userColRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracked);

        Intent i = getIntent();
        parentID = i.getStringExtra("ParentID");

        db = FirebaseFirestore.getInstance();
        userColRef = db.collection("USERS");

        mAuth = FirebaseAuth.getInstance();
        mUSer = mAuth.getCurrentUser();

        if(mUSer != null) {
            userColRef.document(mUSer.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()) {
                            _userName = documentSnapshot.getString("NAME");
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        _stopSharingBtn = findViewById(R.id.stopBtn);

        _stopSharingBtn.setOnClickListener(view -> {
            SendNotification.fusedLocationProviderClient.removeLocationUpdates(SendNotification.locationCallback);
            sendNotification(parentID, _userName);
            Intent intent = new Intent(this, ScanQR.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void sendNotification(String UserID, String StudentName) {
        FirebaseDatabase.getInstance().getReference("UserToken").child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    userToken = snapshot.child("token").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocationTracked.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            FCMNotificationSender.sendNotification(
                    getApplicationContext(),
                    userToken,
                    "CommuTech",
                    "Ang iyong anak na si " + StudentName + " ay nakauwi na ng ligtas.\nMaraming salamat sa pag gamit ng application."
            );
        }, 3000);
    }
}