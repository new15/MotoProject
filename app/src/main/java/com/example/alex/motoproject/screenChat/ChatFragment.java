package com.example.alex.motoproject.screenChat;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.app.App;
import com.example.alex.motoproject.dagger.DaggerPresenterComponent;
import com.example.alex.motoproject.dagger.PresenterModule;
import com.example.alex.motoproject.dialog.ChatLocLimitDialogFragment;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.event.OnClickChatDialogFragmentEvent;
import com.example.alex.motoproject.retainFragment.FragmentWithRetainInstance;
import com.example.alex.motoproject.screenMain.MainActivity;
import com.example.alex.motoproject.util.KeyboardUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import static com.example.alex.motoproject.util.ArgKeys.MESSAGE_TEXT;

public class ChatFragment extends FragmentWithRetainInstance implements ChatMvp.PresenterToView {
    private static final int MESSAGE_MAX_CHARS = 200;

    @Inject
    ChatMvp.ViewToPresenter mPresenter;

    private RecyclerView mRecyclerView;
    private EditText mEditText;
    private ImageButton mSendButton;
    private ImageButton mShareLocationButton;
    private ChatAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public String getDataTag() {
        return ChatPresenter.class.getName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            DaggerPresenterComponent.builder()
                    .presenterModule(new PresenterModule(this))
                    .build()
                    .inject(this);
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setRetainData(mPresenter);
        outState.putString(MESSAGE_TEXT, mEditText.getText().toString());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mPresenter.onDestroyView();
        EventBus.getDefault().unregister(this);
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

        disableSendButton();

        EventBus.getDefault().register(this);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mPresenter = (ChatMvp.ViewToPresenter) getRetainData();
            mPresenter.onViewAttached(ChatFragment.this);
            mEditText.setText(savedInstanceState.getString(MESSAGE_TEXT));
        }

        mPresenter.onViewCreated();
    }

    @Override
    public void setupAll() {
        setupMessageSending();
        setupLocationSharing();
        setupTextFilter();
        setupRecyclerView();
        setupSwipeRefreshLayout();
    }

    @Override
    public void showToast(int stringId) {
        Toast.makeText(getContext(), getContext().getString(stringId), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean isLocationServiceOn() {
        return ((App) getContext().getApplicationContext()).isLocationListenerServiceOn();
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light,
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
        mShareLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onClickShareLocationButton();
            }
        });
    }

    private void setupMessageSending() {
        disableSendButton();
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPresenter.onEditTextChanged(charSequence);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPresenter.onOptionsItemSelected(item.getItemId());
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.appbar_chat, menu);
    }

    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mPresenter.onTouchRecyclerView();
                return false;
            }
        });
    }

    @Override
    public void hideKeyboard() {
        KeyboardUtil.hideKeyboard(getActivity());
    }

    @Override
    public int getLastCompletelyVisibleItemPosition() {
        return mLayoutManager.findLastCompletelyVisibleItemPosition();
    }

    @Override
    public void clearMessages() {
        mAdapter.clearMessages();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLocLimitDialog() {
        ((MainActivity) getActivity()).showDialogFragment(new ChatLocLimitDialogFragment(),
                ChatLocLimitDialogFragment.class.getSimpleName());
    }

    @Override
    public int getDistanceLimit() {
        return getActivity().getPreferences(Context.MODE_PRIVATE)
                .getInt(getString(R.string.chat_location_limit_preferences), 0);
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
    public void setAdapter(List<ChatMessage> messages) {
        mAdapter = new ChatAdapter(messages);
    }

    @Override
    public void updateMessage(int position) {
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void disableSendButton() {
        mSendButton.setEnabled(false);
        mSendButton.setColorFilter(ResourcesCompat
                .getColor(getResources(), R.color.grey500, null));
    }

    @Override
    public void enableSendButton() {
        mSendButton.setEnabled(true);
        mSendButton.setColorFilter(ResourcesCompat
                .getColor(getResources(), R.color.blue900, null));
    }

    @Override
    public void enableSwipeLayout(boolean enable) {
        mSwipeRefreshLayout.setEnabled(enable);
    }

    @Override
    public void hideShareLocationButton() {
        if (mShareLocationButton == null) {
            return; //fragment might not be created and not have a layout inflated
        }
        mShareLocationButton.setVisibility(View.GONE);
    }

    @Override
    public void showShareLocationButton() {
        mShareLocationButton.setVisibility(View.VISIBLE);
    }

    @Subscribe(sticky = true)
    public void onGpsStateChanged(GpsStatusChangedEvent event) {
        mPresenter.onGpsStateChanged(event);
    }

    @Subscribe
    public void OnClickChatDialogFragment(OnClickChatDialogFragmentEvent event) {
        mPresenter.onClickChatDialogFragment(event);
    }
}
