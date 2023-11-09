package com.sprtcoding.commutech.GetStarted;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sprtcoding.commutech.Adapters.ViewPagerAdapter;
import com.sprtcoding.commutech.OpenMaps;
import com.sprtcoding.commutech.R;
import com.sprtcoding.commutech.ScanQR;

import java.util.Objects;

public class GetStartedPage extends AppCompatActivity {
    private TextView skip_btn, next_btn, back_btn;
    private ViewPager my_viewPager;
    private LinearLayout mDotLayout;
    TextView[] dots;
    ViewPagerAdapter viewPagerAdapter;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore db;
    private CollectionReference mUserColRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started_page);
        _init();

        _setClickListener();

        viewPagerAdapter = new ViewPagerAdapter(this);
        my_viewPager.setAdapter(viewPagerAdapter);
        setIndicator(0);
        my_viewPager.addOnPageChangeListener(viewListener);
    }

    public void setIndicator(int position) {
        dots = new TextView[8];
        mDotLayout.removeAllViews();

        for(int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.grey,getApplicationContext().getTheme()));
            mDotLayout.addView(dots[i]);
        }

        dots[position].setTextColor(getResources().getColor(R.color.startColor,getApplicationContext().getTheme()));
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setIndicator(position);

            if(position > 0) {
                back_btn.setVisibility(View.VISIBLE);
            }else {
                back_btn.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private int getItem(int i) {
        return my_viewPager.getCurrentItem() + i;
    }

    private void _setClickListener() {
        back_btn.setOnClickListener(view -> {
            if(getItem(0) > 0) {
                my_viewPager.setCurrentItem(getItem(-1), true);
            }
        });
        next_btn.setOnClickListener(view -> {
            if(getItem(0) < 7) {
                my_viewPager.setCurrentItem(getItem(1), true);
            }else {
                signInUser();
            }
        });
        skip_btn.setOnClickListener(view -> {
            signInUser();
        });
    }

    private void signInUser() {
        if(mUser != null) {
            mUserColRef.document(mUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.contains("ACCOUNT_TYPE")) {
                                String type = Objects.requireNonNull(document.get("ACCOUNT_TYPE")).toString();

                                if(type.equals("Student")) {
                                    Intent intent = new Intent(this, ScanQR.class);
                                    startActivity(intent);
                                    finish();
                                }else if (type.equals("Parents")) {
                                    Intent intent = new Intent(this, OpenMaps.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void _init() {
        skip_btn = findViewById(R.id.skip_btn);
        next_btn = findViewById(R.id.next_btn);
        back_btn = findViewById(R.id.back_btn);
        my_viewPager = findViewById(R.id.my_viewpager);
        mDotLayout = findViewById(R.id.indicator_ll);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        mUserColRef = db.collection("USERS");
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}