package com.example.alex.motoproject.screenOnlineUsers;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ScreenOnlineUsersFragment extends Fragment
        implements FirebaseDatabaseHelper.OnlineUsersUpdateReceiver {
    List<OnlineUser> mOnlineUsers = new ArrayList<>();
    OnlineUsersAdapter mAdapter = new OnlineUsersAdapter(mOnlineUsers);

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    public ScreenOnlineUsersFragment() {

        // Required empty public constructor

    }


    @Override
    public void onStop() {
        mFirebaseDatabaseHelper.unregisterOnlineUsersDataListener();
        mOnlineUsers.clear();
        mAdapter.notifyDataSetChanged();
        super.onStop();
    }

    @Override
    public void onStart() {
        mFirebaseDatabaseHelper.registerOnlineUsersListener(this);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        App.getCoreComponent().inject(this);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users_online, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.navigation_friends_list_recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(mAdapter);
    }

    @Override
    public void onUserAdded(OnlineUser onlineUser) {
        mOnlineUsers.add(onlineUser);
        mAdapter.notifyItemInserted(mOnlineUsers.indexOf(onlineUser));
    }

    @Override
    public void onUserChanged(OnlineUser onlineUser) {
        String thisUserId = onlineUser.getUid();
        for (OnlineUser iteratedUser : mOnlineUsers) {
            if (iteratedUser.getUid().equals(thisUserId)) {
                mOnlineUsers.set(mOnlineUsers.indexOf(iteratedUser), onlineUser);
                mAdapter.notifyItemChanged(mOnlineUsers.indexOf(onlineUser));
                return;
            }
        }
    }

    @Override
    public void onUserDeleted(OnlineUser onlineUser) {
        mAdapter.notifyItemRemoved(mOnlineUsers.indexOf(onlineUser));
        mOnlineUsers.remove(onlineUser);
    }
}