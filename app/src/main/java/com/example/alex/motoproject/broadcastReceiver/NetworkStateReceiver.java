package com.example.alex.motoproject.broadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.example.alex.motoproject.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NetworkStateReceiver extends BroadcastReceiver {

    public NetworkStateReceiver() {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork == null) { //Internet connection is turned off
            notifyInternetIsOff(context);
        }
    }

    private void notifyInternetIsOff(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("MotoProject")
                        .setContentText("Інтернет вимкнено. Ввімкнути")
                        .setPriority(Notification.PRIORITY_HIGH)
                        //TODO make this notification cause vibration
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true);

        //create pending intent used when tapping on the notification
        Intent callWirelessSettingIntent = new Intent(
                Settings.ACTION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        callWirelessSettingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(pendingIntent);

        // set an ID for the notification
        int mNotificationId = 1;
        // get an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // send notification
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
