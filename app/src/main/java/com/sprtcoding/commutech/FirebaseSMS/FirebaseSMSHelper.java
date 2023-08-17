package com.sprtcoding.commutech.FirebaseSMS;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class FirebaseSMSHelper {
    private static final String TAG = "FirebaseSMSHelper";

    public static void sendSMS(Activity activity, String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            PendingIntent sentIntent = PendingIntent.getBroadcast(activity, 0, new Intent("SMS_SENT"), 0);
            PendingIntent deliveredIntent = PendingIntent.getBroadcast(activity, 0, new Intent("SMS_DELIVERED"), 0);

            smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent);
            // Show success message
            Toast.makeText(activity, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error sending SMS: " + e.getMessage());
            // Show error message
            Toast.makeText(activity, "Failed to send SMS", Toast.LENGTH_SHORT).show();
        }
    }
}
