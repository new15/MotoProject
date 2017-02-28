package com.example.alex.motoproject.screenChat;

import android.view.View;

import java.util.List;

public interface ChatMVP {

    interface ViewToPresenter {
        void onEditTextTextChanged(CharSequence charSequence);

        void onClickSendButton(String msg);

        void onClickShareLocationButton();

        void onTouchRecyclerView(View view);

        void registerChatMessagesListener();

        void unregisterChatMessagesListener();

        void registerAdapter();

        void onRefreshSwipeLayout();
    }

    interface PresenterToView {
        void disableSendButton();

        void enableSendButton();

        void scrollToPosition(int position);

        void cleanupEditText();

        void hideKeyboard(View view);

        void updateMessage(int position);

        void notifyItemInserted(int position);

        void notifyItemRangeInserted(int mitPos, int lastPos);

        void setListToAdapter(List<ChatMessage> messages);

        void disableRefreshingSwipeLayout();

        void disableSwipeLayout();

        void disableShareLocationButton();

        void enableShareLocationButton();

        int getLastCompletelyVisibleItemPosition();
    }

    interface PresenterToModel {
        void sendChatMessage(String msg);

        void registerChatMessagesListener();

        void unregisterChatMessagesListener();

        int getMessagesSize();

        List<ChatMessage> getMessages();

        void fetchOlderChatMessages();

        void fetchDataForLocationShare();
    }

    interface ModelToPresenter {
        void showNewMessage();

        void showOlderMessages(int startPos, int lastPos);

        void disableRefreshingSwipeLayout();

        void disableSwipeLayout();

        void updateMessage(int position);
    }
}