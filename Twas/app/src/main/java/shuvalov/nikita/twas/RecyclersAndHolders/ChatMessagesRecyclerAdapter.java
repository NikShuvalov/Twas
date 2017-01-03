package shuvalov.nikita.twas.RecyclersAndHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatMessagesRecyclerAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {
    ArrayList<ChatMessage> mChatMessages;
    String mSelfId;

    public ChatMessagesRecyclerAdapter(ArrayList<ChatMessage> chatMessages, String selfId) {
        mChatMessages = chatMessages;
        mSelfId = selfId;
    }


    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int resource;
        if(viewType==0){
            resource=R.layout.viewholder_chat_self;
        }else{
            resource = R.layout.viewholder_chat_message;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(resource,null);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        holder.bindDataToViews(mChatMessages.get(position));//True code
    }

    @Override
    public int getItemCount() {
        return mChatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = mChatMessages.get(position);

        //If self was the one who posted the message use return viewtype 0, else 1;
        if(chatMessage.getUserId().equals(mSelfId)){
            return 0;
        }
        return 1;
    }
}
