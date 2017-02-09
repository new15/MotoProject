package com.example.alex.motoproject.firebase;

import android.util.Log;

import com.example.alex.motoproject.screenOnlineUsers.UsersOnlineAdapter;
import com.example.alex.motoproject.screenOnlineUsers.UsersOnline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



public class FirebaseDatabaseHelper {
    private static final String TAG = "log";
    private final List<UsersOnline> listModels = new ArrayList<>();
    private UsersOnlineAdapter adapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public FirebaseDatabaseHelper() {

    }

    public void createDatabase(String userId,String email,String name){


        DatabaseReference myRef = database.getReference().child("users").child(userId).child("name");
        myRef.setValue(name);

        myRef = database.getReference().child("users").child(userId).child("email");
        myRef.setValue(email);

        myRef = database.getReference().child("users").child(userId).child("friendsList").child("userID");
        myRef.setValue("2363524");
        myRef = database.getReference().child("users").child(userId).child("friendsList").child("userID").child("name");
        myRef.setValue("User");
        myRef = database.getReference().child("users").child(userId).child("friendsList").child("userID").child("email");
        myRef.setValue("test@best.mail.com");

        myRef = database.getReference().child("users").child(userId).child("friendsRequest").child("userID");
        myRef.setValue("234256");
        myRef = database.getReference().child("users").child(userId).child("friendsRequest").child("userID").child("name");
        myRef.setValue("TestUser");
        myRef = database.getReference().child("users").child(userId).child("friendsRequest").child("userID").child("email");
        myRef.setValue("test@best.mail.com");

    }

    public void addToOnline(String userId, String email, String avatar){
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId).child("email");
        myRef.setValue(email);
        myRef = database.getReference().child("onlineUsers").child(userId).child("avatar");
        myRef.setValue(avatar);
        myRef = database.getReference().child("onlineUsers").child(userId).child("status");
        myRef.setValue("public");
    }

    public void removeFromOnline(String userId){
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId);
        myRef.removeValue();
    }

    public void updateOnlineUserLocation(double lat, double lon) {
        String uid = getCurrentUser().getUid();
        Log.d(TAG, "updateOnlineUserLocation: " + uid);
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(uid).child("lat");
        myRef.setValue(lat);
        myRef = database.getReference().child("onlineUsers").child(uid).child("lon");
        myRef.setValue(lon);
    }

    public List<UsersOnline> getAllOnlineUsers() {
        // Read from the database

        DatabaseReference myRef = database.getReference().child("onlineUsers");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listModels.clear();

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    UsersOnline friend = postSnapshot.getValue(UsersOnline.class);
                    Log.d(TAG, "onDataChange: " +friend.getEmail() + " - ");
                    listModels.add(friend);
                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return listModels;
    }
    public void setAdapter(UsersOnlineAdapter adapter){
        this.adapter=adapter;
    }

    private FirebaseUser getCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            return auth.getCurrentUser();
        }
        throw new RuntimeException("Current user is null");
    }
}
