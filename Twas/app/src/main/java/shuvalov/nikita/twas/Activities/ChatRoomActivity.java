package shuvalov.nikita.twas.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ChatMessagesHelper;
import shuvalov.nikita.twas.Helpers_Managers.ChatRoomsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.ChatRoom;
import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.RecyclersAndHolders.ChatMessagesRecyclerAdapter;

public class ChatRoomActivity extends AppCompatActivity {
    private Button mSendButton;
    private EditText mMessageEntry;
    private RecyclerView mMessageRecycler;
    private ChatMessagesRecyclerAdapter mAdapter;
    private String mChatRoomId;
    private DatabaseReference mChatRoomRef;
    private ChildEventListener mChatRoomListener;
    private FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);


        mFirebaseDatabase = FirebaseDatabase.getInstance();

        findViews();
        getChatLog();
        recyclerLogic();
        onClickLogic();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        /* This is going to display all of the chatMessages that belong to this chatRoom in a recyclerView.
        ToDo: (Optional) User can invite additional users to the chatroom.
         */
    }

    public void findViews(){
        mSendButton = (Button)findViewById(R.id.send_butt);
        mMessageEntry = (EditText)findViewById(R.id.message_entry);
        mMessageRecycler = (RecyclerView)findViewById(R.id.message_recycler);
        Toolbar toolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.chatroom_menu,menu);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()){
//            case (R.id.rename):
//                AlertDialog renameDialog = new AlertDialog.Builder(this)
//                        .setView(R.layout.rename_dialog)
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.dismiss();
//                            }
//                        }).create();
//                renameDialog.show();
//                final EditText nameEdit = (EditText)renameDialog.findViewById(R.id.new_name_entry);
//                Button renameButton = (Button) renameDialog.findViewById(R.id.submit_name);
//                renameButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String newName = nameEdit.getText().toString();
//                        if (newName.isEmpty()){
//                            nameEdit.setError("Can't be empty");
//                        }else{
//                            //ToDo:Currently this code only updates the Chatroom name for the shared chatRoom, and user's own reference to the chatroom, but not to the other user.
//                            //FixMe: It updates the name in the fbdb, for self and main but the changes don't show up when reloading the activity.
//                            FirebaseDatabaseUtils.getChatroomChatroomNameRef(mFirebaseDatabase,mChatRoomId).setValue(newName); //Updates the RoomName as seen from the Chatroom Directory
//                            FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,SelfUserProfileUtils.getUserId(ChatRoomActivity.this)).child(mChatRoomId).child("roomName").setValue(newName);//Updates the RoomName as seen from selfUser file.
//                            getSupportActionBar().setTitle(newName);
//                        }
//                    }
//                });
//
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    public void getChatLog() {
        ChatRoom chatRoom;
        if (getIntent().getStringExtra(AppConstants.ORIGIN_ACTIVITY).equals(AppConstants.ORIGIN_CHATROOMS)) {
            int chatRoomPos = getIntent().getIntExtra(AppConstants.PREF_CHATROOM, -1);
            if (chatRoomPos == -1) {
                Toast.makeText(this, "ChatRoom couldn't be found", Toast.LENGTH_SHORT).show();
                finish();
            }

            chatRoom = ChatRoomsHelper.getInstance().getChatRoomAtPosition(chatRoomPos);
            getSupportActionBar().setTitle(chatRoom.getRoomName());
            mChatRoomId = chatRoom.getId();
        } else if (getIntent().getStringExtra(AppConstants.ORIGIN_ACTIVITY).equals(AppConstants.ORIGIN_PROFILE_DETAIL)) {
            mChatRoomId = getIntent().getStringExtra(AppConstants.PREF_CHATROOM);
        }

        ChatMessagesHelper.getInstance().cleanChatLog(mChatRoomId);
        mChatRoomRef = FirebaseDatabaseUtils.getChatroomMessagesRef(FirebaseDatabase.getInstance(), mChatRoomId);
        mChatRoomListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                ChatMessagesHelper.getInstance().addChatMessage(newMessage);
                ConnectionsSQLOpenHelper.getInstance(ChatRoomActivity.this).addMessage(newMessage);
                mAdapter.notifyItemInserted(mAdapter.getItemCount()-1);
                mMessageRecycler.smoothScrollToPosition(mAdapter.getItemCount()-1);
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
        mChatRoomRef.addChildEventListener(mChatRoomListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mChatRoomRef.removeEventListener(mChatRoomListener);
    }

    public void recyclerLogic(){
        mAdapter = new ChatMessagesRecyclerAdapter(ChatMessagesHelper.getInstance().getChatLog(),SelfUserProfileUtils.getUserId(this));//True Code

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mMessageRecycler.setAdapter(mAdapter);
        mMessageRecycler.setLayoutManager(linearLayoutManager);
//        int size = mAdapter.getItemCount();
//        if(size==0){
//            mMessageRecycler.smoothScrollToPosition(0);
//        }else{
//            mMessageRecycler.smoothScrollToPosition(size-1);
//        }
    }


    public void onClickLogic(){
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMessageEntry.getText().toString().equals("")){
                    mMessageEntry.setError("Message can't be empty");
                }else{
                    String chatContent = mMessageEntry.getText().toString();
                    mMessageEntry.setText("");
                    String userId = SelfUserProfileUtils.getUserId(ChatRoomActivity.this);
                    long timeStamp = Calendar.getInstance().getTimeInMillis();

                    ChatMessage chatMessage = new ChatMessage(userId,mChatRoomId,chatContent,timeStamp);
                    mChatRoomRef.push().setValue(chatMessage);
                }
            }
        });
    }
}
