package com.sprtcoding.commutech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sprtcoding.commutech.Adapters.ParentFirestoreAdapter;
import com.sprtcoding.commutech.Model.ParentsModel;

public class SearchParents extends AppCompatActivity {
    private RecyclerView parentsRV;
    private ImageView backBtn;
    FirebaseAuth mAuth;
    ParentFirestoreAdapter parentFirestoreAdapter;
    FirebaseFirestore DB;
    CollectionReference userCollectionRef;
    String _parentsName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_parents);
        _var();

        parentsRV.setHasFixedSize(true);
        parentsRV.setLayoutManager(new LinearLayoutManager(SearchParents.this));

        mAuth = FirebaseAuth.getInstance();

        DB = FirebaseFirestore.getInstance();

        userCollectionRef = DB.collection("USERS");

        _parentsName = getIntent().getStringExtra("PARENT_NAME");

        Query userQuery = userCollectionRef.whereEqualTo("NAME", _parentsName).orderBy("ACCOUNT_TYPE", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ParentsModel> options = new FirestoreRecyclerOptions.Builder<ParentsModel>()
                .setQuery(userQuery, ParentsModel.class)
                .build();

        parentFirestoreAdapter = new ParentFirestoreAdapter(options);

        parentsRV.setAdapter(parentFirestoreAdapter);

        backBtn.setOnClickListener(view -> {
            finish();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        parentFirestoreAdapter.startListening();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onStop() {
        super.onStop();
        parentFirestoreAdapter.stopListening();
    }

    private void _var() {
        parentsRV = findViewById(R.id.parentRecycleView);
        backBtn = findViewById(R.id.backBtn);

    }
}