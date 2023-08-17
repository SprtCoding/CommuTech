package com.sprtcoding.commutech.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sprtcoding.commutech.FCM.FCMNotificationSender;
import com.sprtcoding.commutech.FireStoreDB.DBQuery;
import com.sprtcoding.commutech.FireStoreDB.MyCompleteListener;
import com.sprtcoding.commutech.LocationTracked;
import com.sprtcoding.commutech.Model.ParentsModel;
import com.sprtcoding.commutech.R;
import com.sprtcoding.commutech.SendNotification;

import java.util.HashMap;

public class ParentFirestoreAdapter extends FirestoreRecyclerAdapter<ParentsModel, ParentFirestoreAdapter.ViewHolder> {

    Context v;
    private ProgressDialog loading;
    String userToken, sender_Name;
    FirebaseFirestore DB;
    CollectionReference userCollectionRef;
    FirebaseAuth mAuth;

    public ParentFirestoreAdapter(@NonNull FirestoreRecyclerOptions<ParentsModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ParentsModel model) {
        loading = new ProgressDialog(v);
        loading.setTitle("Sending");
        loading.setMessage("Please wait...");
        loading.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();

        DB = FirebaseFirestore.getInstance();
        userCollectionRef = DB.collection("USERS");

        //get name from fire store
        userCollectionRef.document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    sender_Name = documentSnapshot.getString("NAME");
                }).addOnFailureListener(e -> Toast.makeText(v, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        holder.fname.setText(model.getNAME());
        holder.number.setText(model.getCONTACT_NUMBER());

        holder.sendBtn.setOnClickListener(view -> {
            loading.show();

            DBQuery.setParentsLocation(
                    model.getUSER_ID(),
                    mAuth.getCurrentUser().getUid(),
                    sender_Name,
                    new MyCompleteListener() {
                @Override
                public void onSuccess() {
                    sendNotification(model.getUSER_ID(), sender_Name);
                    loading.dismiss();
                    Toast.makeText(v, "Location sent successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(v, LocationTracked.class);
                    intent.putExtra("ParentID", model.getUSER_ID());
                    v.startActivity(intent);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(v, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.v = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parent_list_item,parent,false);
        return new ParentFirestoreAdapter.ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fname, number;
        ImageButton sendBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sendBtn = itemView.findViewById(R.id.sendBtn);
            fname = itemView.findViewById(R.id.fname);
            number = itemView.findViewById(R.id.number);
        }
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
                Toast.makeText(v, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            FCMNotificationSender.sendNotification(
                    v,
                    userToken,
                    "CommuTech",
                    StudentName + " ay nagpasa ng kanyang lokasyon.\nBuksan ang iyong application para ma-locate mo ang iyong anak."
            );
        }, 3000);
    }
}
