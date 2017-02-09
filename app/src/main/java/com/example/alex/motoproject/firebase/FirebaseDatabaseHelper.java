package com.example.alex.motoproject.firebase;

import android.util.Log;

import com.example.alex.motoproject.events.FriendDataReadyEvent;
import com.example.alex.motoproject.events.MapMarkerEvent;
import com.example.alex.motoproject.models.OnlineUser;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class FirebaseDatabaseHelper {
    private static final String TAG = "log";
    private static final String LOG_TAG = FirebaseDatabaseHelper.class.getSimpleName();
    private final List<OnlineUser> onlineUserList = new ArrayList<>();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private ChildEventListener mOnlineUsersLocationListener;
    private ChildEventListener onlineUsersListener;
    private DatabaseReference mOnlineUsersRef;


    public FirebaseDatabaseHelper() {

    }

    public void addUser(String uid, String email, String name, String avatar) {
        DatabaseReference users = mDatabase.getReference().child("users").child(uid);
        users.child("email").setValue(email);
        users.child("name").setValue(name);
        users.child("avatar").setValue(avatar);
    }

    public void setUserOnline(String status) {
        String uid = getCurrentUser().getUid();
        DatabaseReference onlineUsers = mDatabase.getReference().child("onlineUsers").child(uid);
        onlineUsers.setValue(status);
    }

    public void setUserOffline() {
        String uid = getCurrentUser().getUid();
        DatabaseReference onlineUsers = mDatabase.getReference().child("onlineUsers").child(uid);
        onlineUsers.removeValue();
    }

    public void registerOnlineUsersLocationListener() {
        mOnlineUsersRef = mDatabase.getReference().child("onlineUsers");
        mOnlineUsersLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DatabaseReference location =
                        mDatabase.getReference().child("location").child(dataSnapshot.getKey());
                location.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.getKey().equals(getCurrentUser().getUid())) {
                            Double lat = (Double) dataSnapshot.child("lat").getValue();
                            Double lng = (Double) dataSnapshot.child("lng").getValue();
                            if (lat != null && lng != null) { // TODO: 07.02.2017 delete if statement
                                String uid = dataSnapshot.getKey();
                                LatLng latLng = new LatLng(lat, lng);
                                getNameByUid(uid, latLng);
                            }
                        } else {
                            Log.w(LOG_TAG, "data snapshot is null");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mOnlineUsersRef.addChildEventListener(mOnlineUsersLocationListener);
    }

    private void getNameByUid(final String uid, final LatLng latLng) {
        DatabaseReference nameRef = mDatabase.getReference().child("users").child(uid).child("name");
        nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.getValue();
                EventBus.getDefault().post(new MapMarkerEvent(latLng, uid, name));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void unregisterOnlineUsersLocationListener() {
        if (mOnlineUsersLocationListener != null) {
            mOnlineUsersRef = mDatabase.getReference().child("onlineUsers");
            mOnlineUsersRef.removeEventListener(mOnlineUsersLocationListener);
        }
    }

    public void removeFromOnline(String userId) {
        DatabaseReference myRef = mDatabase.getReference().child("onlineUsers").child(userId);
        myRef.removeValue();
    }

    public void updateOnlineUserLocation(double lat, double lng) {
        String uid = getCurrentUser().getUid();
        Log.d(TAG, "updateOnlineUserLocation: " + uid);
        DatabaseReference myRef = mDatabase.getReference().child("location").child(uid);
        myRef.child("lat").setValue(lat);
        myRef.child("lng").setValue(lng);
    }

    public void registerOnlineUsersListener() {
        // Read from the mDatabase
        DatabaseReference myRef = mDatabase.getReference().child("onlineUsers");
        onlineUsersListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onlineUserList.clear();
                String uid = dataSnapshot.getKey();
                Object userStatus = dataSnapshot.getValue();
                if (userStatus instanceof String) // TODO: 08.02.2017 remove this line
                    getOnlineUserDataByUid(uid, (String) userStatus);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String uid = dataSnapshot.getKey();
                Object userStatus = dataSnapshot.getValue();
                if (userStatus instanceof String) // TODO: 08.02.2017 remove this line
                    getOnlineUserDataByUid(uid, (String) userStatus);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        myRef.addChildEventListener(onlineUsersListener);
    }

    private FirebaseUser getCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            return auth.getCurrentUser();
        }
        throw new RuntimeException("Current user is null");
    }

    private void getOnlineUserDataByUid(final String uid, final String userStatus) {
        DatabaseReference ref = mDatabase.getReference().child("users").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String avatar = (String) dataSnapshot.child("avatar").getValue();
                if (name != null) {
                    onlineUserList.add(new OnlineUser(uid, name, avatar, userStatus));
                    EventBus.getDefault().post(new FriendDataReadyEvent());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public List<OnlineUser> getFriends() {
        return onlineUserList;
    }

    public void unregisterOnlineUsersListener() {
        DatabaseReference myRef = mDatabase.getReference().child("onlineUsers");
        myRef.removeEventListener(onlineUsersListener);
    }
}
