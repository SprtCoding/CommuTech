package com.sprtcoding.commutech.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.List;
import java.util.Objects;

public class ParentsAdapter extends RecyclerView.Adapter<ParentsAdapter.ViewHolder> {
    Context context;
    List<ParentsModel> parentsList;
    private ProgressDialog loading;
    String userToken, sender_Name;
    FirebaseFirestore DB;
    CollectionReference userCollectionRef;
    FirebaseAuth mAuth;

    public ParentsAdapter(Context context, List<ParentsModel> parentsList) {
        this.context = context;
        this.parentsList = parentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parent_list_item,parent,false);
        return new ParentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParentsModel parents = parentsList.get(position);

        loading = new ProgressDialog(context);
        loading.setTitle("Sending");
        loading.setMessage("Please wait...");
        loading.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();

        DB = FirebaseFirestore.getInstance();
        userCollectionRef = DB.collection("USERS");

        //get name from fire store
        userCollectionRef.document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    sender_Name = documentSnapshot.getString("NAME");
                }).addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        holder.fname.setText(parents.getNAME());
        holder.number.setText(parents.getCONTACT_NUMBER());

        holder.sendBtn.setOnClickListener(view -> {
            loading.show();

            DBQuery.setParentsLocation(
                    parents.getUSER_ID(),
                    mAuth.getCurrentUser().getUid(),
                    sender_Name,
                    new MyCompleteListener() {
                        @Override
                        public void onSuccess() {
                            sendNotification(parents.getUSER_ID(), sender_Name);
                            loading.dismiss();
                            Toast.makeText(context, "Location sent successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, LocationTracked.class);
                            intent.putExtra("ParentID", parents.getUSER_ID());
                            context.startActivity(intent);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });

    }

    @Override
    public int getItemCount() {
        return parentsList.size();
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
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            FCMNotificationSender.sendNotification(
                    context,
                    userToken,
                    "CommuTech",
                    StudentName + " ay nagpasa ng kanyang lokasyon.\nBuksan ang iyong application para ma-locate mo ang iyong anak."
            );
        }, 3000);
    }
}
