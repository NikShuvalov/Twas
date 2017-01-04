package shuvalov.nikita.twas.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import shuvalov.nikita.twas.Helpers_Managers.ChatRoomsHelper;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatRoom;
import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.RecyclersAndHolders.ChatRoomsRecyclerAdapter;

public class ChatRoomListActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ChatRoomsRecyclerAdapter mAdapter;
    private ChildEventListener mChatRoomsListener;
    private DatabaseReference mUserChatRoomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);

        findViews();
        recyclerLogic();

        mUserChatRoomRef = FirebaseDatabaseUtils.getUserChatroomsRef(FirebaseDatabase.getInstance(), SelfUserProfileUtils.getUserId(this));

        //ToDo: Dropping the singleton values might be faster than not adding duplicates when numbers start getting higher. Ask About it.
        mChatRoomsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                if(!ChatRoomsHelper.getInstance().addChatRoom(chatRoom)){
                    mAdapter.notifyItemInserted(ChatRoomsHelper.getInstance().getNumberOfChatrooms()-1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mUserChatRoomRef.addChildEventListener(mChatRoomsListener);
//
//        usersChatroomRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Iterable<DataSnapshot> chatRooms = dataSnapshot.getChildren();
//                while(chatRooms.iterator().hasNext()){
//                    ChatRoom chatRoom = chatRooms.iterator().next().getValue(ChatRoom.class);
//                    ChatRoomsHelper.getInstance().addChatRoom(chatRoom);
//                }
//                mAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });



        //ToDo: Allow users to rename the chatRoom.
        /*
        This will display all of the chatrooms that the user belongs to.
        User can select a chatroom to view all messages in the chatroom.
        (Optional) User can create a new chatroom
         */
    }

    public void recyclerLogic(){
        mAdapter = new ChatRoomsRecyclerAdapter(ChatRoomsHelper.getInstance().getChatRooms());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    public void findViews(){
        mRecyclerView = (RecyclerView)findViewById(R.id.chatroom_list_recycler);
        Toolbar toolbar= (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Chatrooms");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserChatRoomRef.removeEventListener(mChatRoomsListener);
    }
}
