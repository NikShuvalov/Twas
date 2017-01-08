package shuvalov.nikita.twas.RecyclersAndHolders;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import shuvalov.nikita.twas.Activities.ProfileDetailActivity;
import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 1/5/17.
 */

public class SoapBoxMessagesRecyclerAdapter extends RecyclerView.Adapter<ChatMessageViewHolder>{
    private ArrayList<ChatMessage> mChatMessages;
    private String mSelfId;

    public SoapBoxMessagesRecyclerAdapter(ArrayList<ChatMessage> chatMessages, String selfId) {
        mChatMessages = chatMessages;
        mSelfId = selfId;
    }


    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int resource;
        if(viewType==0){
            resource= R.layout.viewholder_chat_self;
        }else{
            resource = R.layout.viewholder_chat_message;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(resource,null);
        return new ChatMessageViewHolder(view,-1);
    }

    @Override
    public void onBindViewHolder(final ChatMessageViewHolder holder, int position) {
        holder.bindDataToViews(mChatMessages.get(position),-1);
        final String uid = mChatMessages.get(position).getUserId();
        if (holder.getItemViewType() == 1) {
            holder.mMessageHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ProfileDetailActivity.class);
                    intent.putExtra(AppConstants.ORIGIN_ACTIVITY, AppConstants.ORIGIN_SOAPBOX_FEED);
                    intent.putExtra(AppConstants.PREF_ID, uid);
                    view.getContext().startActivity(intent);
                }
            });
        }
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
