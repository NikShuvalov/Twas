package shuvalov.nikita.twas.RecyclersAndHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import shuvalov.nikita.twas.PoJos.ChatRoom;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatRoomsViewHolder extends RecyclerView.ViewHolder {
    TextView mChatRoomIdText;

    public ChatRoomsViewHolder(View itemView) {
        super(itemView);
        mChatRoomIdText = (TextView)itemView.findViewById(R.id.chatroom_id_text);
    }
    public void bindDataToView(String chatRoomId){
        //ToDo: Chang to taking in a chatRoomObject maybe to keep track of name and shit.
        mChatRoomIdText.setText(chatRoomId);
    }
}
