package com.sprtcoding.commutech.FireStoreDB;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DBQuery {
    public static FirebaseFirestore g_firestore;

    public static void createUserData(String email, String name, String address, String id, String accountType, String number, String parentNo, String parentName, MyCompleteListener myCompleteListener) {
        Map<String, Object> userData = new HashMap<>();

        userData.put("USER_ID", id);
        userData.put("EMAIL_ID", email);
        userData.put("NAME", name);
        userData.put("ADDRESS", address);
        userData.put("ACCOUNT_TYPE", accountType);
        userData.put("CONTACT_NUMBER", number);
        userData.put("PARENTS_NUMBER", parentNo);
        userData.put("PARENTS_NAME", parentName);

        DocumentReference userDoc = g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userDoc.set(userData)
                .addOnSuccessListener(unused -> {
                    myCompleteListener.onSuccess();
                }).addOnFailureListener(e -> {
                    myCompleteListener.onFailure(e);
                });
    }

    public static void setParentsLocation(String id, String SenderID, String senderName, MyCompleteListener myCompleteListener) {
        Map<String, Object> locationShareData = new HashMap<>();

        locationShareData.put("ID", id);
        locationShareData.put("SENDER_ID", SenderID);
        locationShareData.put("SENDER_NAME", senderName);

        DocumentReference locationShareDoc = g_firestore.collection("LOCATION_SHARE").document(id);

        locationShareDoc.set(locationShareData)
                .addOnSuccessListener(unused -> {
                    myCompleteListener.onSuccess();
                }).addOnFailureListener(e -> {
                    myCompleteListener.onFailure(e);
                });
    }

    public static void setLocation(String id, double lat, double lng, float loc_bearing) {
        Map<String, Object> locationShareData = new HashMap<>();

        locationShareData.put("ID", id);
        locationShareData.put("LAT", lat);
        locationShareData.put("LNG", lng);
        locationShareData.put("LOC_BEARING", loc_bearing);

        DocumentReference locationShareDoc = g_firestore.collection("USERS").document("LOCATION"+id);

        locationShareDoc.set(locationShareData);
    }

    public static void setSMSRecentHistory(String id, String studentName, String location, String driverName, String DriverLicense,
                                           String DriverRegNo, String DriverFranchise, String Date, String Time) {
        Map<String, Object> smsRecordsData = new HashMap<>();

        smsRecordsData.put("ID", id);
        smsRecordsData.put("STUDENT_NAME", studentName);
        smsRecordsData.put("LOCATION", location);
        smsRecordsData.put("DRIVER_NAME", driverName);
        smsRecordsData.put("DRIVER_LICENSE", DriverLicense);
        smsRecordsData.put("DRIVER_REG_NO", DriverRegNo);
        smsRecordsData.put("DRIVER_FRANCHISE_NO", DriverFranchise);
        smsRecordsData.put("DATE", Date);
        smsRecordsData.put("TIME", Time);

        DocumentReference smsRecordsDoc = g_firestore.collection("SMS_RECORDS").document(id);

        smsRecordsDoc.set(smsRecordsData);
    }
}
