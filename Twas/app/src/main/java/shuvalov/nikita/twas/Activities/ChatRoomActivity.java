package shuvalov.nikita.twas.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private Toolbar mToolbar;
    private ChatMessagesRecyclerAdapter mAdapter;
    private String mChatRoomId;
    private DatabaseReference mChatRoomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);


        findViews();
        getChatLog();
        recyclerLogic();
        onClickLogic();

        /* This is going to display all of the chatmessages that belong to this chatRoom in a recyclerView.
        User can arrive to this activity either by clicking on a chatroom in their chatroom list or
        User can Arrive to this activity upon creation of a new chatroom.
        (Optional) User can invite additional users to the chatroom.
         */
    }

    public void findViews(){
        mSendButton = (Button)findViewById(R.id.send_butt);
        mMessageEntry = (EditText)findViewById(R.id.message_entry);
        mMessageRecycler = (RecyclerView)findViewById(R.id.message_recycler);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatRoom Name or Other UserName");
    }

    public void getChatLog(){
        ChatRoom chatRoom;
        if(getIntent().getStringExtra(AppConstants.ORIGIN_ACTIVITY).equals(AppConstants.ORIGIN_CHATROOMS)){
            int chatRoomPos = getIntent().getIntExtra(AppConstants.PREF_CHATROOM,-1);
            if(chatRoomPos == -1){
                Toast.makeText(this, "ChatRoom couldn't be found", Toast.LENGTH_SHORT).show();
                finish();
            }

            chatRoom = ChatRoomsHelper.getInstance().getChatRoomAtPosition(chatRoomPos);
            mChatRoomId = chatRoom.getId();
        }else if (getIntent().getStringExtra(AppConstants.ORIGIN_ACTIVITY).equals(AppConstants.ORIGIN_PROFILE_DETAIL)){
            mChatRoomId = getIntent().getStringExtra(AppConstants.PREF_CHATROOM);
        }

        ChatMessagesHelper.getInstance().cleanChatLog(mChatRoomId);
        mChatRoomRef = FirebaseDatabaseUtils.getChatroomMessagesRef(FirebaseDatabase.getInstance(), mChatRoomId);

        mChatRoomRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                ChatMessagesHelper.getInstance().addChatMessage(newMessage);
                ConnectionsSQLOpenHelper.getInstance(ChatRoomActivity.this).addMessage(newMessage);
                mAdapter.notifyItemInserted(ChatMessagesHelper.getInstance().getChatLog().size()-1);
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
        });

    }

    public void recyclerLogic(){
        mAdapter = new ChatMessagesRecyclerAdapter(ChatMessagesHelper.getInstance().getChatLog());//True Code

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mMessageRecycler.setAdapter(mAdapter);
        mMessageRecycler.setLayoutManager(linearLayoutManager);
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
