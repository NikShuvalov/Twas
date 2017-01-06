package shuvalov.nikita.twas.RecyclersAndHolders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    TextView mChatText, mTimeStampText;
    CardView mMessageHolder;

    public ChatMessageViewHolder(View itemView) {
        super(itemView);
        mChatText = (TextView) itemView.findViewById(R.id.chat_message_text);
        mTimeStampText = (TextView)itemView.findViewById(R.id.timestamp_text);
        mMessageHolder = (CardView)itemView.findViewById(R.id.message_card);
    }

    public void bindDataToViews(ChatMessage chatMessage){
        mChatText.setText(chatMessage.getContent());
        DateFormat sdf = new SimpleDateFormat("hh:mm a");
        String timeStamp = sdf.format(chatMessage.getTimeStamp());
        mTimeStampText.setText(timeStamp);
    }
}
