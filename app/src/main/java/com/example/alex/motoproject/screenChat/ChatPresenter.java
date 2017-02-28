package com.example.alex.motoproject.screenChat;

import android.view.View;

import com.example.alex.motoproject.event.ConfirmShareLocationInChatEvent;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.event.ShareLocationInChatAllowedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class ChatPresenter implements ChatMVP.ViewToPresenter, ChatMVP.ModelToPresenter {

    private WeakReference<ChatMVP.PresenterToView> mView;
    private ChatMVP.PresenterToModel mModel = new ChatModel(this);

    @Inject
    public ChatPresenter(ChatMVP.PresenterToView view) {
        mView = new WeakReference<>(view);
    }

    private ChatMVP.PresenterToView getView() throws NullPointerException {
        if (mView != null) {
            return mView.get();
        } else {
            throw new NullPointerException("View is unavailable");
        }
    }

    @Override
    public void onEditTextTextChanged(CharSequence charSequence) {
        if (charSequence.length() > 0 && !charSequence.toString().matches("\\s+")) {
            getView().enableSendButton();
        } else {
            getView().disableSendButton();
        }
    }

    @Override
    public void onClickSendButton(String msg) {
        msg = msg.trim().replaceAll(" +", " ");
        msg = msg.replaceAll("\\n+", "\n");
        mModel.sendChatMessage(msg);
        getView().scrollToPosition(mModel.getMessagesSize());
        getView().cleanupEditText();
    }

    @Override
    public void onClickShareLocationButton() {
        EventBus.getDefault().post(new ConfirmShareLocationInChatEvent());
    }

    @Override
    public void onTouchRecyclerView(View view) {
        getView().hideKeyboard(view);
    }

    @Override
    public void registerChatMessagesListener() {
        mModel.registerChatMessagesListener();
    }

    @Override
    public void unregisterChatMessagesListener() {
        mModel.unregisterChatMessagesListener();
    }

    @Override
    public void registerAdapter() {
        getView().setListToAdapter(mModel.getMessages());
    }

    @Override
    public void onRefreshSwipeLayout() {
        mModel.fetchOlderChatMessages();
    }

    @Override
    public void showNewMessage() {
        int position = mModel.getMessagesSize();
        getView().notifyItemInserted(position);
        if (position > 1 &&
                getView().getLastCompletelyVisibleItemPosition() == position - 2) {
            getView().scrollToPosition(mModel.getMessagesSize());
        }
    }

    @Override
    public void showOlderMessages(int startPos, int lastPos) {
        getView().notifyItemRangeInserted(startPos, lastPos);
        getView().scrollToPosition(lastPos - 1);
    }

    @Override
    public void disableRefreshingSwipeLayout() {
        getView().disableRefreshingSwipeLayout();
    }

    @Override
    public void disableSwipeLayout() {
        getView().disableSwipeLayout();
    }

    @Override
    public void updateMessage(int position) {
        getView().updateMessage(position);
    }

    @Subscribe(sticky = true)
    public void onGpsStateChanged(GpsStatusChangedEvent event) {
        if (event.isGpsOn()) {
            getView().enableShareLocationButton();
        } else {
            getView().disableShareLocationButton();
        }
    }

    @Subscribe
    public void onShareLocationInChatAllowed(ShareLocationInChatAllowedEvent event) {
        mModel.fetchDataForLocationShare();
    }
}