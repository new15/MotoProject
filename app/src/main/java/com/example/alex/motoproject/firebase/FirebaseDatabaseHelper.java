package com.example.alex.motoproject.firebase;

import android.util.Log;

import com.example.alex.motoproject.adapters.OnlineUsersAdapter;
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
    private final List<OnlineUser> listModels = new ArrayList<>();
    private OnlineUsersAdapter adapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private ChildEventListener mOnlineUsersLocationListener;
    private DatabaseReference mOnlineUsersRef;
    public FirebaseDatabaseHelper() {

    }

//    public void createDatabase(String userId, String email, String name) {
//
//
//        DatabaseReference myRef = database.getReference().child("users").child(userId).child("name");
//        myRef.setValue(name);
//
//        myRef = database.getReference().child("users").child(userId).child("email");
//        myRef.setValue(email);
//
//        myRef = database.getReference().child("users").child(userId).child("friendsList").child("userID");
//        myRef.setValue("2363524");
//        myRef = database.getReference().child("users").child(userId).child("friendsList").child("userID").child("name");
//        myRef.setValue("User");
//        myRef = database.getReference().child("users").child(userId).child("friendsList").child("userID").child("email");
//        myRef.setValue("test@best.mail.com");
//
//        myRef = database.getReference().child("users").child(userId).child("friendsRequest").child("userID");
//        myRef.setValue("234256");
//        myRef = database.getReference().child("users").child(userId).child("friendsRequest").child("userID").child("name");
//        myRef.setValue("TestUser");
//        myRef = database.getReference().child("users").child(userId).child("friendsRequest").child("userID").child("email");
//        myRef.setValue("test@best.mail.com");
//
//    }

    public void addUser(String uid, String email, String name, String avatar) {
        DatabaseReference users = database.getReference().child("users").child(uid);
        users.child("email").setValue(email);
        users.child("name").setValue(name);
        users.child("avatar").setValue(avatar);
    }

    public void setUserOnline(String status) {
        String uid = getCurrentUser().getUid();
        DatabaseReference onlineUsers = database.getReference().child("onlineUsers").child(uid);
        onlineUsers.setValue(status);
    }

    public void setUserOffline() {
        String uid = getCurrentUser().getUid();
        DatabaseReference onlineUsers = database.getReference().child("onlineUsers").child(uid);
        onlineUsers.removeValue();
    }

    public void registerOnlineUsersLocationListener() {
        mOnlineUsersRef = database.getReference().child("onlineUsers");
        mOnlineUsersLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DatabaseReference location =
                        database.getReference().child("location").child(dataSnapshot.getKey());
                location.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.getKey().equals(getCurrentUser().getUid())) {
                            Double lat = (Double) dataSnapshot.child("lat").getValue();
                            Double lng = (Double) dataSnapshot.child("lng").getValue();
                            if (lat != null && lng != null) { // TODO: 07.02.2017 delete if statement
                                String uid = dataSnapshot.getKey();
                                LatLng latLng = new LatLng(lat, lng);
                                EventBus.getDefault().post(new MapMarkerEvent(latLng, uid, "IBFLD"));
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

    public void unregisterOnlineUsersLocationListener() {
        if (mOnlineUsersLocationListener != null) {
            mOnlineUsersRef = database.getReference().child("onlineUsers");
            mOnlineUsersRef.removeEventListener(mOnlineUsersLocationListener);
        }
    }

    public void removeFromOnline(String userId) {
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId);
        myRef.removeValue();
    }

    public void updateOnlineUserLocation(double lat, double lng) {
        String uid = getCurrentUser().getUid();
        Log.d(TAG, "updateOnlineUserLocation: " + uid);
        DatabaseReference myRef = database.getReference().child("location").child(uid);
        myRef.child("lat").setValue(lat);
        myRef.child("lng").setValue(lng);
    }

    public void getAllOnlineUsers() {
        // Read from the database
        DatabaseReference myRef = database.getReference().child("onlineUsers");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
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
        myRef.addChildEventListener(childEventListener);

//        return listModels;
    }

    public void setAdapter(OnlineUsersAdapter adapter) {
        this.adapter = adapter;
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
        DatabaseReference ref = database.getReference().child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
//                String email = (String) dataSnapshot.child("email").getValue();
                String avatar = (String) dataSnapshot.child("avatar").getValue();
                if (name != null) {
                    listModels.add(new OnlineUser(uid, name, avatar, userStatus));
                    EventBus.getDefault().post(new FriendDataReadyEvent());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public List<OnlineUser> getFriends() {
        return listModels;
    }
}
