package shuvalov.nikita.twas.RecyclersAndHolders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    TextView mChatText, mTimeStampText, mSenderNameText;
    CardView mMessageHolder;

    public ChatMessageViewHolder(View itemView, int viewType) {
        super(itemView);
        mChatText = (TextView) itemView.findViewById(R.id.chat_message_text);
        mTimeStampText = (TextView)itemView.findViewById(R.id.timestamp_text);
        mMessageHolder = (CardView)itemView.findViewById(R.id.message_card);
        if(viewType==1){
            mSenderNameText = (TextView)itemView.findViewById(R.id.sender_name_text);
        }
    }

    public void bindDataToViews(ChatMessage chatMessage, int viewType){
        mChatText.setText(chatMessage.getContent());
        DateFormat sdf = new SimpleDateFormat("hh:mm a MM/dd/yy");
        String timeStamp = sdf.format(chatMessage.getTimeStamp());
        mTimeStampText.setText(timeStamp);
        if(viewType==1){
            if(!SelfUserProfileUtils.getUserId(mChatText.getContext()).equals(AppConstants.MY_USER_ID)){ //Checks to see if it's my uid. As in my uid. Nikita's Id, it's it's Nikita's uid.
                String name = ConnectionsSQLOpenHelper.getInstance(mSenderNameText.getContext()).getConnectionById(chatMessage.getUserId()).getName();
                String sentByText = "Sent by "+name;
                mSenderNameText.setText(sentByText);
            }else{//My account, Nikita's account, crashes since I have anonymous users (users who I've never had a connection with) messaging me.
                mSenderNameText.setText("Sent by a fan");
            }
        }
    }
}
