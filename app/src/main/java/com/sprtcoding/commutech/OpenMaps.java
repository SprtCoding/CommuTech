package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sprtcoding.commutech.Loading.LoadingDialog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class OpenMaps extends FragmentActivity implements OnMapReadyCallback {
    GoogleMap gMap;
    FrameLayout map;
    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    ImageView logOut;
    private LoadingDialog loadingDialog;
    private TextView locationTxt;
    private FloatingActionButton aboutBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    DocumentReference locationDocumentRef, userDocumentRef;
    CollectionReference locationCollectionRef, userCollectionRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_maps);

        loadingDialog = new LoadingDialog(this);

        locationTxt = findViewById(R.id.location);
        aboutBtn = findViewById(R.id.aboutBtn);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        map = findViewById(R.id.map);

        db = FirebaseFirestore.getInstance();

        locationCollectionRef = db.collection("LOCATION_SHARE");
        userCollectionRef = db.collection("USERS");

        locationDocumentRef = locationCollectionRef.document(user.getUid());

        logOut = findViewById(R.id.logoutPBtn);

        logOut.setOnClickListener(view -> {
            loadingDialog.show();
            Handler handler = new Handler();
            Runnable runnable = () -> {
                loadingDialog.dismiss();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Intent intent = new Intent(OpenMaps.this, LoginPage.class);
                startActivity(intent);
                finish();
            };
            handler.postDelayed(runnable, 3000);
        });

        aboutBtn.setOnClickListener(view -> {
            loadingDialog.show();
            Handler handler = new Handler();
            Runnable runnable = () -> {
                loadingDialog.dismiss();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Intent intent = new Intent(OpenMaps.this, AboutPage.class);
                startActivity(intent);
            };
            handler.postDelayed(runnable, 3000);
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;

        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            trackLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We can show user a dialog why this permission is necessary
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            }

        }
    }

    private void trackLocation() {
        locationDocumentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String sender_id = documentSnapshot.getString("SENDER_ID");

                userDocumentRef = userCollectionRef.document("LOCATION"+sender_id);

                userDocumentRef.addSnapshotListener((value, error) -> {
                    assert value != null;
                    if(value.exists()) {
                        if(value.contains("ID")) {
                            String id = value.getString("ID");
                            assert id != null;
                            if(id.equals(sender_id)) {
                                Double latitude = value.getDouble("LAT");
                                Double longitude = value.getDouble("LNG");
                                float locationBearing = value.getLong("LOC_BEARING");

                                if (latitude != null && longitude != null) {
                                    LatLng location = new LatLng(latitude, longitude);
                                    // Clear existing markers on the map
                                    gMap.clear();

                                    // Reverse geocode the coordinates to obtain the location name
                                    Geocoder geocoder = new Geocoder(OpenMaps.this, Locale.getDefault());
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                        if (addresses.size() > 0) {
                                            Address address = addresses.get(0);
                                            String locationName = address.getAddressLine(0);

                                            locationTxt.setText(locationName);

                                            // Add a marker with the location name as the title
                                            MarkerOptions markerOptions = new MarkerOptions()
                                                    .position(location)
                                                    .title(locationName)
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_top))
                                                    .rotation(locationBearing)
                                                    .anchor((float) 0.5, (float) 0.5);
                                            Marker marker = gMap.addMarker(markerOptions);

                                            // Add a circle
                                            CircleOptions circleOptions = new CircleOptions()
                                                    .center(location)
                                                    .radius(100) // Radius in meters
                                                    .strokeWidth(2)
                                                    .strokeColor(Color.BLUE)
                                                    .fillColor(Color.parseColor("#500084d3")); // Semi-transparent blue color
                                            Circle circle = gMap.addCircle(circleOptions);

                                            // Move the camera to the updated location with zoom level
                                            float zoomLevel = 17.0f; // Adjust the zoom level as needed
                                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                });
            }
        });
    }

    private void enableUserLocation() {
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
        gMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                trackLocation();
            } else {
                //We can show a dialog that permission is not granted...
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}