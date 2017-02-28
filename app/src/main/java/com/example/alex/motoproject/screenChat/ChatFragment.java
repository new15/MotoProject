package com.example.alex.motoproject.screenChat;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.alex.motoproject.DaggerPresenterComponent;
import com.example.alex.motoproject.PresenterModule;
import com.example.alex.motoproject.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

public class ChatFragment extends Fragment implements ChatMVP.PresenterToView {
    private static final int MESSAGE_MAX_CHARS = 200;
    @Inject
    ChatPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private EditText mEditText;
    private ImageButton mSendButton;
    private ImageButton mShareLocationButton;
    private ChatAdapter mAdapter;
    private Parcelable savedInstanceStateRecycler;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DaggerPresenterComponent.builder()
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
        mPresenter.registerChatMessagesListener();
        mPresenter.registerAdapter();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onStart() {
        if (savedInstanceStateRecycler != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceStateRecycler);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        savedInstanceStateRecycler = mRecyclerView.getLayoutManager().onSaveInstanceState();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mPresenter.unregisterChatMessagesListener();
        EventBus.getDefault().unregister(mPresenter);
        mPresenter = null;
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditText = (EditText) view.findViewById(R.id.edittext_message_chat);
        mSendButton = (ImageButton) view.findViewById(R.id.button_send_chat);
        mShareLocationButton = (ImageButton) view.findViewById(R.id.button_sharelocation_chat);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_chat);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.container_chat_swipe);
        setupMessageSending();
        setupLocationSharing();
        setupTextFilter();
        setupRecyclerView();
        setupSwipeRefreshLayout();
        EventBus.getDefault().register(mPresenter);
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.onRefreshSwipeLayout();
            }
        });
    }

    private void setupLocationSharing() {
        mShareLocationButton.setEnabled(false);
        mShareLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onClickShareLocationButton();
            }
        });
    }

    private void setupMessageSending() {
        mSendButton.setEnabled(false);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPresenter.onEditTextTextChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onClickSendButton(mEditText.getText().toString());
            }
        });
    }

    @Override
    public void scrollToPosition(int position) {
        mRecyclerView.smoothScrollToPosition(position);

    }

    @Override
    public void cleanupEditText() {
        mEditText.setText("");
    }

    private void setupTextFilter() {
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MESSAGE_MAX_CHARS)});
    }

    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mPresenter.onTouchRecyclerView(view);
                return false;
            }
        });
    }

    @Override
    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public int getLastCompletelyVisibleItemPosition() {
        return mLayoutManager.findLastCompletelyVisibleItemPosition();
    }

    @Override
    public void notifyItemInserted(int position) {
        mAdapter.notifyItemInserted(position - 1);
    }

    @Override
    public void notifyItemRangeInserted(int startPos, int lastPos) {
        mAdapter.notifyItemRangeInserted(startPos, lastPos);
    }

    @Override
    public void disableRefreshingSwipeLayout() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void setListToAdapter(List<ChatMessage> messages) {
        mAdapter = new ChatAdapter(messages);
    }

    @Override
    public void updateMessage(int position) {
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void disableSendButton() {
        mSendButton.setEnabled(false);
    }

    @Override
    public void enableSendButton() {
        mSendButton.setEnabled(true);
    }

    @Override
    public void disableSwipeLayout() {
        mSwipeRefreshLayout.setEnabled(false);
    }

    @Override
    public void disableShareLocationButton() {
        mShareLocationButton.setEnabled(false);
    }

    @Override
    public void enableShareLocationButton() {
        mShareLocationButton.setEnabled(true);
    }
}