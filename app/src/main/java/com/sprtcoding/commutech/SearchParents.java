package com.sprtcoding.commutech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sprtcoding.commutech.Adapters.ParentFirestoreAdapter;
import com.sprtcoding.commutech.Model.ParentsModel;

import java.util.Objects;

public class SearchParents extends AppCompatActivity {
    private RecyclerView parentsRV;
    private ImageView backBtn;
    private LinearLayout no_data, with_data;
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

        // Documents with the specified "NAME" value exist.
        FirestoreRecyclerOptions<ParentsModel> options = new FirestoreRecyclerOptions.Builder<ParentsModel>()
                .setQuery(userQuery, ParentsModel.class)
                .build();

        // Check if any documents match the query
        userQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    parentFirestoreAdapter = new ParentFirestoreAdapter(options);

                    parentsRV.setAdapter(parentFirestoreAdapter);

                    parentFirestoreAdapter.startListening();
                } else {
                    // No documents with the specified "NAME" value were found.
                    no_data.setVisibility(View.VISIBLE);
                    with_data.setVisibility(View.GONE);
                }
            } else {
                // Handle the case where the query was not successful.
                Log.d("FIRESTORE_ERROR", Objects.requireNonNull(task.getException().getMessage()));
                Toast.makeText(SearchParents.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        backBtn.setOnClickListener(view -> {
            finish();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (parentFirestoreAdapter != null) {
            parentFirestoreAdapter.stopListening();
        }
    }

    private void _var() {
        parentsRV = findViewById(R.id.parentRecycleView);
        backBtn = findViewById(R.id.backBtn);
        no_data = findViewById(R.id.no_data);
        with_data = findViewById(R.id.with_data);
    }
}