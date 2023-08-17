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

        // Add a listener to the FirestoreRecyclerAdapter to determine if it's empty
//        parentFirestoreAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//                // Called when items are inserted into the adapter
//                // This indicates that the collection is not empty
//                _no_post_prePostTest_ll.setVisibility(View.GONE);
//                _prePostTestRV.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onItemRangeRemoved(int positionStart, int itemCount) {
//                super.onItemRangeRemoved(positionStart, itemCount);
//                // Called when items are removed from the adapter
//                // You can check if the adapter is now empty and handle it accordingly
//                if (testAdapter.getItemCount() == 0) {
//                    // Collection is empty
//                    // You can handle this case here
//                    _no_post_prePostTest_ll.setVisibility(View.VISIBLE);
//                    _prePostTestRV.setVisibility(View.GONE);
//                }else {
//                    _no_post_prePostTest_ll.setVisibility(View.GONE);
//                    _prePostTestRV.setVisibility(View.VISIBLE);
//                }
//            }
//        });


//        userQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()) {
//                    parentsModels.clear();
//                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        ParentsModel parents = dataSnapshot.getValue(ParentsModel.class);
//                        parentsModels.add(parents);
//                    }
//                    parentsAdapter = new ParentsAdapter(SearchParents.this, parentsModels);
//                    parentsRV.setAdapter(parentsAdapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(SearchParents.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });

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