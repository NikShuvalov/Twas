package shuvalov.nikita.twas.RecyclersAndHolders;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FireBaseStorageUtils;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatRoom;
import shuvalov.nikita.twas.R;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatRoomsViewHolder extends RecyclerView.ViewHolder {
    TextView mChatRoomIdText, mChatMemberNameText;
    ImageView mImageView;
    CardView mCardContainer;

    public ChatRoomsViewHolder(View itemView) {
        super(itemView);
        mChatRoomIdText = (TextView)itemView.findViewById(R.id.chatroom_name_text);
        mImageView = (ImageView)itemView.findViewById(R.id.image_view);
        mCardContainer = (CardView)itemView.findViewById(R.id.chatroom_card);
        mChatMemberNameText = (TextView)itemView.findViewById(R.id.chat_member_text);
    }
    public void bindDataToView(ChatRoom chatRoom){
        mChatRoomIdText.setText(chatRoom.getRoomName());
        String id=  chatRoom.getUserIds().get(0); //Get the first user's id.

        if(SelfUserProfileUtils.getUserId(mChatRoomIdText.getContext()).equals(id)){ //If This is the same as the user's id, then let's set it to the other user's id.
            id = chatRoom.getUserIds().get(1);
            String name = ConnectionsSQLOpenHelper.getInstance(mChatRoomIdText.getContext()).getConnectionById(id).getName();
            String details;
            if(name.isEmpty()){
                details = "Chatting with an unnamed one\n(Because they have not set up their name yet)";
            }else{
                details = "Chatting with "+ name;
            }
            mChatMemberNameText.setText(details);

        }
        StorageReference imageRef = FireBaseStorageUtils.getProfilePicStorageRef(id);

        ConnectivityManager connectivityManager = (ConnectivityManager) mCardContainer.getContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()){
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(mImageView.getContext()).load(uri).into(mImageView);
                }
            });
        }else{
            mImageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }

    }
}
