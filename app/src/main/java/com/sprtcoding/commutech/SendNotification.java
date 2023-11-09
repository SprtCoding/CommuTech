package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sprtcoding.commutech.FireStoreDB.DBQuery;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SendNotification extends AppCompatActivity {

    private TextView _sName, _dName, _dLNo, _vRegNo, _franchiseNo, _Location;
    private ImageView _backBtn, drivers_pic;
    private MaterialButton _sendBtn, _viewBtn;
    FirebaseAuth mAuth;
    FirebaseDatabase mdb;
    DatabaseReference _userRef, _driversRef;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    public static FusedLocationProviderClient fusedLocationProviderClient;
    public static Location locationRequest;
    public static LocationCallback locationCallback;
    private static double lat = 0;
    private static double lon = 0;
    private static String locationName, parentsName, sender_Name, parent_id;
    FirebaseFirestore db;
    CollectionReference userCollectionRef, driverCollectionRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);
        _var();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        db = FirebaseFirestore.getInstance();

        DBQuery.g_firestore = FirebaseFirestore.getInstance();

        userCollectionRef = db.collection("USERS");
        driverCollectionRef = db.collection("DRIVERS");

        mAuth = FirebaseAuth.getInstance();
        mdb = FirebaseDatabase.getInstance();
        _userRef = mdb.getReference("Users");
        _driversRef = mdb.getReference("Drivers").child("DriversInfo");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("qr_code")) {
            String qrCode = intent.getStringExtra("qr_code");

            //get name from fire store
            userCollectionRef.document(mAuth.getCurrentUser().getUid()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                sender_Name = documentSnapshot.getString("NAME");

                                if(documentSnapshot.contains("PARENTS_NAME")) {
                                    parentsName = documentSnapshot.getString("PARENTS_NAME");
                                }
                                _sName.setText(sender_Name);
                            }).addOnFailureListener(e -> Toast.makeText(SendNotification.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            //get driver's information from fire store
            driverCollectionRef.document(qrCode).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()) {
                            String driverName = documentSnapshot.getString("DRIVERS_NAME");
                            String driverLNo = documentSnapshot.getString("LICENSE_NO");
                            String driverVRegNo = documentSnapshot.getString("REG_NO");
                            String driverFranchiseNo = documentSnapshot.getString("FRANCHISE_NO");
                            String driverPhoto = documentSnapshot.getString("DRIVER_PHOTO");

                            _dName.setText(driverName);
                            _dLNo.setText(driverLNo);
                            _vRegNo.setText(driverVRegNo);
                            _franchiseNo.setText(driverFranchiseNo);
                            //Picasso.get().load(driverPhoto).fit().into(drivers_pic);
                        }
                    }).addOnFailureListener(e -> Toast.makeText(SendNotification.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        }

        // Define the location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Save the location to Firebase Realtime Database
                    saveLocationToDatabase(location);
                }
            }
        };

        _sendBtn.setOnClickListener(view -> {
            Intent i = new Intent(this, SendSMS.class);
            i.putExtra("StudentName", _sName.getText().toString());
            i.putExtra("DriverName", _dName.getText().toString());
            i.putExtra("DriverLicense", _dLNo.getText().toString());
            i.putExtra("DriverRegNo", _vRegNo.getText().toString());
            i.putExtra("DriverFranchise", _franchiseNo.getText().toString());
            i.putExtra("Location", locationName);
            i.putExtra("Lat", String.valueOf(lat));
            i.putExtra("Long", String.valueOf(lon));
            startActivity(i);
        });

        _backBtn.setOnClickListener(view -> {
            Intent i = new Intent(this, ScanQR.class);
            startActivity(i);
            finish();

        });

        _viewBtn.setOnClickListener(view -> {
            Intent i = new Intent(this, SearchParents.class);
            i.putExtra("PARENT_NAME", parentsName);
            startActivity(i);
        });
    }

    private void _var() {
        _sName = findViewById(R.id.studentNameLbl);
        _dName = findViewById(R.id.driverNameLbl);
        _dLNo = findViewById(R.id.dLNoLbl);
        _vRegNo = findViewById(R.id.vRegNoLbl);
        _franchiseNo = findViewById(R.id.franchiseLbl);
        _Location = findViewById(R.id.locationLbl);

        _backBtn = findViewById(R.id.backBtn);
        _sendBtn = findViewById(R.id.sendInfoBtn);
        _viewBtn = findViewById(R.id.viewBtn);
        drivers_pic = findViewById(R.id.drivers_pic);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermission();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, start getting the location
            getLocation();
            startLocationUpdates();
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void saveLocationToDatabase(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float locationBearing = location.getBearing();

        // Save the latitude and longitude to Firebase Realtime Database
//        _userRef.child(mAuth.getCurrentUser().getUid()).child("Location").child("latitude").setValue(latitude);
//        _userRef.child(mAuth.getCurrentUser().getUid()).child("Location").child("longitude").setValue(longitude);
//        _userRef.child(mAuth.getCurrentUser().getUid()).child("Location").child("locationBearing").setValue(locationBearing);

        if(mAuth.getCurrentUser() != null) {
            DBQuery.setLocation(mAuth.getCurrentUser().getUid(), latitude, longitude, locationBearing);
        }
    }

    public void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000) // Update location every 5 seconds (adjust as needed)
                .setFastestInterval(2000) // Fastest update interval
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @SuppressLint("SetTextI18n")
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Location retrieved successfully
                        lat = location.getLatitude();
                        lon = location.getLongitude();

                        locationRequest = location;

                        Geocoder geocoder = new Geocoder(SendNotification.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                            if (addresses.size() > 0) {
                                Address address = addresses.get(0);
                                locationName = address.getAddressLine(0);
                                // Update the TextView with the location name
                                _Location.setText(locationName);
                            } else {
                                // No address found
                                _Location.setText("No address found");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Location unavailable
                        _Location.setText("Location unavailable");
                    }
                })
                .addOnFailureListener(this, e -> {
                    // Failed to get location
                    _Location.setText("Failed to get location");
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public static void StopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted
                getLocation();
            } else {
                // Location permission denied
                _Location.setText("Location permission denied");
            }
        }
    }
}