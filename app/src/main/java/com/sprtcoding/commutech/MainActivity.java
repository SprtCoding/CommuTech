package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser _mUSer;
    DatabaseReference _userRef;
    FirebaseDatabase mdb;
    FirebaseFirestore userDB;
    DocumentReference userDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _auth();
    }

    private void _auth() {
        FirebaseAuth _mAuth = FirebaseAuth.getInstance();
        _mUSer = _mAuth.getCurrentUser();

        mdb = FirebaseDatabase.getInstance();
        _userRef = mdb.getReference("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        userDB = FirebaseFirestore.getInstance();
        Handler handler = new Handler();
        if(_mUSer != null) {
            handler.postDelayed(() -> {
                userDocRef = userDB.collection("USERS").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                userDocRef.get().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()) {
                        DocumentSnapshot document = task1.getResult();
                        if(document.contains("ACCOUNT_TYPE")) {
                            String type = document.get("ACCOUNT_TYPE").toString();

                            if(type.equals("Student")) {
                                Intent intent = new Intent(MainActivity.this, ScanQR.class);
                                startActivity(intent);
                                finish();
                            }else if (type.equals("Parents")) {
                                Intent intent = new Intent(MainActivity.this, OpenMaps.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                });
            }, 3000);
        }else {
            handler.postDelayed(() -> {
                Intent i = new Intent(MainActivity.this, LoginPage.class);
                startActivity(i);
                finish();
            }, 3000);
        }
    }
}