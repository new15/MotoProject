package com.example.alex.motoproject.locationService;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.app.App;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.networkStateReceiver.GpsStateReceiver;
import com.example.alex.motoproject.screenMain.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import static com.example.alex.motoproject.screenProfile.MyProfileFragment.PROFSET;
import static com.example.alex.motoproject.util.ArgKeys.MAP_PENDING_INTENT_CODE;
import static com.example.alex.motoproject.util.ArgKeys.SHOW_MAP_FRAGMENT;


/**
 * The Service that listens for location changes and sends them to Firebase
 */
public class LocationService extends Service implements Runnable,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    public static final String LOCATION_REQUEST_FREQUENCY_HIGH = "high";
    public static final String LOCATION_REQUEST_FREQUENCY_DEFAULT = "default";
    public static final String LOCATION_REQUEST_FREQUENCY_LOW = "low";
    public static final String GPS_RATE = "gpsRate";
    private static final String STOP_SERVICE_EXTRA = "stopService";

    private static final int GET_LOCATION_TIME = 2900;
    private static final int LOCATION_UPDATE_TIME_20_SEC = 20000;
    private static final int LOCATION_UPDATE_TIME_10_SEC = 10000;
    private static final int LOCATION_UPDATE_TIME_3_SEC = 3000;

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    FirebaseAuth mFirebaseAuth;
    int mNotificationId = 3;
    private BroadcastReceiver mGpsStateReceiver = new GpsStateReceiver();
    private Handler handler = new Handler();
    private int updateTime = 10000;
    private GoogleApiClient mGoogleApiClient;

    private String mUserStatus;

    public LocationService() {
        // Required empty public constructor
    }

    @Override
    public void onCreate() {
        App.getCoreComponent().inject(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
        mFirebaseAuth = FirebaseAuth.getInstance();

        showNotification();

        SharedPreferences preferences = getApplicationContext()
                .getSharedPreferences(PROFSET, Context.MODE_PRIVATE);

        SharedPreferences preferencesRate = getApplicationContext()
                .getSharedPreferences(GPS_RATE, Context.MODE_PRIVATE);

        String gpsRate = preferencesRate.getString(
                mFirebaseDatabaseHelper.getCurrentUser().getUid(), null);
        if (gpsRate == null) {
            gpsRate = LOCATION_REQUEST_FREQUENCY_DEFAULT;
        }
        switch (gpsRate) {
            case LOCATION_REQUEST_FREQUENCY_LOW:
                updateTime = LOCATION_UPDATE_TIME_20_SEC;
                break;
            case LOCATION_REQUEST_FREQUENCY_DEFAULT:
                updateTime = LOCATION_UPDATE_TIME_10_SEC;
                break;
            case LOCATION_REQUEST_FREQUENCY_HIGH:
                updateTime = LOCATION_UPDATE_TIME_3_SEC;
                break;
        }

        mUserStatus = preferences.getString(
                mFirebaseDatabaseHelper.getCurrentUser().getUid(), null);

        ((App) getApplication()).setLocationListenerServiceOn(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mGpsStateReceiver, filter);
        mGpsStateReceiver.onReceive(this, null);

        handler.postDelayed(this, 100);

        EventBus.getDefault().register(this);

        super.onCreate();
    }


    @Override
    public void onDestroy() {
        handler.removeCallbacks(this);
        mGoogleApiClient.disconnect();
        cleanupNotifications();
        unregisterReceiver(mGpsStateReceiver);
        ((App) getApplication()).setLocationListenerServiceOn(false);

        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    //The service is not designed for binding
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //stop service if that was the purpose of intent
        if (intent != null && intent.getExtras() != null &&
                intent.getExtras().getBoolean(STOP_SERVICE_EXTRA)) {
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(sticky = true)
    public void onGpsStatusChangedEvent(GpsStatusChangedEvent event) {
        if (event.isGpsOn()) mFirebaseDatabaseHelper.setUserOnline(mUserStatus);
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_motorcycle)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.location_service_notification))
                        .setShowWhen(false);

        //create pending intent used when tapping on the app notification
        //open up ScreenMapFragment
        Intent mapIntent = new Intent(this, MainActivity.class);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mapIntent.putExtra(SHOW_MAP_FRAGMENT, true);
        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(
                        this,
                        MAP_PENDING_INTENT_CODE,
                        mapIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(mapPendingIntent);

        //Send notification
        startForeground(mNotificationId, mBuilder.build());
    }

    private void cleanupNotifications() {
        //Cleanup notifications, no need of them if the app is off
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancelAll();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void run() {
        handler.postDelayed(this, updateTime);
        startLocationUpdates();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopLocationUpdates();

            }
        }, GET_LOCATION_TIME);
    }

    private void startLocationUpdates() {
        //Handle unexpected permission absence
        if (mGoogleApiClient.isConnected()) {
            if (checkLocationPermission()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, createLocationRequest(), this);
            }
        } else {
            mGoogleApiClient.connect();
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setInterval(1000); //1 secs
        mLocationRequest.setFastestInterval(100); //1 secs
        mLocationRequest.setSmallestDisplacement(1f); //1 m

        return mLocationRequest;

    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mFirebaseDatabaseHelper.updateUserLocation(location);
        mFirebaseDatabaseHelper.updateUserLocation(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkLocationPermission()) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (lastLocation != null) {
                mFirebaseDatabaseHelper.updateUserLocation(lastLocation);
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
    }
}