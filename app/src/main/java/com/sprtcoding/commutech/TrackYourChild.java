package com.sprtcoding.commutech;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sprtcoding.commutech.databinding.ActivityTrackYourChildBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class TrackYourChild extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityTrackYourChildBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTrackYourChildBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        trackLocation();
    }

    private void trackLocation() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        DatabaseReference locationShareRef = FirebaseDatabase.getInstance().getReference("LocationShare");

        locationShareRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String childID = snapshot.child("SenderID").getValue(String.class);

                    assert childID != null;
                    DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child(childID).child("Location");

                    locationRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                                Double longitude = dataSnapshot.child("longitude").getValue(Double.class);

                                if (latitude != null && longitude != null) {
                                    LatLng location = new LatLng(latitude, longitude);
                                    // Clear existing markers on the map
                                    mMap.clear();

                                    // Reverse geocode the coordinates to obtain the location name
                                    Geocoder geocoder = new Geocoder(TrackYourChild.this, Locale.getDefault());
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                        if (addresses.size() > 0) {
                                            Address address = addresses.get(0);
                                            String locationName = address.getAddressLine(0);

                                            // Add a marker with the location name as the title
                                            MarkerOptions markerOptions = new MarkerOptions()
                                                    .position(location)
                                                    .title(locationName);
                                            Marker marker = mMap.addMarker(markerOptions);

                                            // Add a circle
                                            CircleOptions circleOptions = new CircleOptions()
                                                    .center(location)
                                                    .radius(100) // Radius in meters
                                                    .strokeWidth(2)
                                                    .strokeColor(Color.BLUE)
                                                    .fillColor(Color.parseColor("#500084d3")); // Semi-transparent blue color
                                            Circle circle = mMap.addCircle(circleOptions);

                                            // Move the camera to the updated location with zoom level
                                            float zoomLevel = 16.0f; // Adjust the zoom level as needed
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle the database error
                            Toast.makeText(TrackYourChild.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrackYourChild.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}