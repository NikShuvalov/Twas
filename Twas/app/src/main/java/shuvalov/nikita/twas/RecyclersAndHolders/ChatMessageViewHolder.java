package shuvalov.nikita.twas.RecyclersAndHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    TextView mChatText;

    public ChatMessageViewHolder(View itemView) {
        super(itemView);
        mChatText = (TextView) itemView.findViewById(R.id.chat_message_text);
    }

    public void bindDataToViews(ChatMessage chatMessage){
        mChatText.setText(chatMessage.getContent());
    }
}
