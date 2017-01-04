package shuvalov.nikita.twas.RecyclersAndHolders;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import shuvalov.nikita.twas.Activities.ChatRoomActivity;
import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.PoJos.ChatRoom;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatRoomsRecyclerAdapter extends RecyclerView.Adapter<ChatRoomsViewHolder> {
    private ArrayList<ChatRoom> mChatRooms;

    public ChatRoomsRecyclerAdapter(ArrayList<ChatRoom> chatRooms) {
        mChatRooms = chatRooms;
    }

    @Override
    public ChatRoomsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_chatrooms,null);
        return new ChatRoomsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChatRoomsViewHolder holder, int position) {
        holder.bindDataToView(mChatRooms.get(position));
        holder.mCardContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatRoomActivity.class);
                intent.putExtra(AppConstants.ORIGIN_ACTIVITY, AppConstants.ORIGIN_CHATROOMS);
                intent.putExtra(AppConstants.PREF_CHATROOM,holder.getAdapterPosition());
                view.getContext().startActivity(intent);
            }
        });

        //ToDo; Figure out best way for user to change the name of the chatroom if they want to. Either in this activity or the chatRoom Activity via toolbar.
//        holder.mCardContainer.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                ChatRoom chatroom = mChatRooms.get(holder.getAdapterPosition());
//                AlertDialog alertDialog = new AlertDialog.Builder(view.getContext());
//                return false;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mChatRooms.size();
    }
}
