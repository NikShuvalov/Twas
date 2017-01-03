package shuvalov.nikita.twas.RecyclersAndHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatMessagesRecyclerAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {
    ArrayList<ChatMessage> mChatMessages;
//    ArrayList<String> mMakeShiftChat;

    //ToDo: The true code
    public ChatMessagesRecyclerAdapter(ArrayList<ChatMessage> chatMessages) {
        mChatMessages = chatMessages;
    }

    //ToDO: THe fake code
//    public ChatMessagesRecyclerAdapter(ArrayList<String> makeShiftChat) {
//        mMakeShiftChat = makeShiftChat;
//    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_chat_message,null);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
//        holder.mChatText.setText(mMakeShiftChat.get(position));//MakeShift code

        holder.bindDataToViews(mChatMessages.get(position));//True code
    }

//    ToDo: The real Code
    @Override
    public int getItemCount() {
        return mChatMessages.size();
    }

    //TODO: THe fake code
//
//    @Override
//    public int getItemCount() {
//        return mMakeShiftChat.size();
//    }

}
