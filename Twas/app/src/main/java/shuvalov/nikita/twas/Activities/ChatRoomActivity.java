package shuvalov.nikita.twas.Activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.ChatRoom;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.RecyclersAndHolders.ChatMessagesRecyclerAdapter;

public class ChatRoomActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private Button mSendButton;
    private EditText mMessageEntry;
    private RecyclerView mMessageRecycler;
    private ChatMessagesRecyclerAdapter mAdapter;
    private String mChatRoomId;
    private DatabaseReference mChatRoomRef;
    private ChildEventListener mChatRoomListener;
    private FirebaseDatabase mFirebaseDatabase;
    public GoogleApiClient mGoogleApiClient;
    private NearbyManager mNearbyManager;
    private MessageListener mActiveListener;
    private String mOtherUserId, mSelfUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        mSelfUserId = SelfUserProfileUtils.getUserId(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mNearbyManager = NearbyManager.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();

        findViews();
        getChatLog();
        recyclerLogic();
        onClickLogic();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        /* This is going to display all of the chatMessages that belong to this chatRoom in a recyclerView.
        ToDo: (Optional) User can invite additional users to the chatroom.
         */

        mActiveListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                super.onFound(message);
                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if (!soapBoxMessage.getContent().equals("")) {
                    ConnectionsSQLOpenHelper.getInstance(ChatRoomActivity.this).addSoapBoxMessage(soapBoxMessage);
                }
                String mFoundId = soapBoxMessage.getUserId();


                //ToDo: Figure out what I can store as a value for the stranger Connections, maybe a counter?
                //Idea: If using a counter 0-10 encounters = Stranger, 11-25 Familiar, 26-50 Regular, 51-99 Acquaintance,100-499 Friendly, 500+ whatever
                FirebaseDatabaseUtils.getUserConnectionsRef(mFirebaseDatabase, SelfUserProfileUtils.getUserId(ChatRoomActivity.this)).child(mFoundId).setValue(mFoundId); //Adds stranger's UID to user's connectionsList.


                DatabaseReference strangerRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, mFoundId);

//                DatabaseReference strangerRef = FirebaseDatabaseUtils.getChildReference(mFirebaseDatabase, mFoundId, AppConstants.theoneforprofiles);

//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainActivity.this);
//
////                ConnectionsSQLOpenHelper.getInstance().addSoapBoxMessage(soapBoxMessage);
//                notificationBuilder.setContentText(soapBoxMessage.getContent()).setContentTitle("New SoapBoxMessage").setSmallIcon(android.R.drawable.ic_dialog_alert);
//                notificationManager.notify(0,notificationBuilder.build());

                //ToDo: Move this listener into a service. It should always be going.

                //Listens to ownChatrooms... I think.
                    FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,SelfUserProfileUtils.getUserId(ChatRoomActivity.this))
                        .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        //This method should give a notification if a new chatroom and/or message is created.
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
                //Gets the stranger's profile information.
                strangerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Profile strangerProfile = dataSnapshot.getValue(Profile.class);
                        if(strangerProfile!=null) {
                            ConnectionsSQLOpenHelper.getInstance(ChatRoomActivity.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
                            ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile); //Adds Stranger's info to Singleton.
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("MainActivity", "on heard signal, failed attempt to D/L profile ");
                    }
                });

                //ToDo: Do a count that adds found users, to keep track of active publishing users.
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
//                Toast.makeText(MainActivity.this, "Lost the signal", Toast.LENGTH_SHORT).show();

                //ToDo: Do a count that removes found users, to keep track of active publishing users.

            }

            @Override
            public void onDistanceChanged(Message message, Distance distance) {
                super.onDistanceChanged(message, distance);
            }

            @Override
            public void onBleSignalChanged(Message message, BleSignal bleSignal) {
                super.onBleSignalChanged(message, bleSignal);
                Log.d("Testing Shots fired", "Please clap");
            }
        };
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
            getOtherUsersId(chatRoom);
        } else if (getIntent().getStringExtra(AppConstants.ORIGIN_ACTIVITY).equals(AppConstants.ORIGIN_PROFILE_DETAIL)) {
            mChatRoomId = getIntent().getStringExtra(AppConstants.PREF_CHATROOM);
            mOtherUserId = getIntent().getStringExtra(AppConstants.PREF_OTHER_UID);
        }

        FirebaseDatabaseUtils.getUsersUnreadMessagesRef(mFirebaseDatabase,mSelfUserId,mChatRoomId).setValue(0);

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

        if(mNearbyManager.isPublishing()){
            Nearby.Messages.unpublish(mGoogleApiClient,mNearbyManager.getActiveMessage());
            mNearbyManager.setPublishing(false);
        }
        if(mNearbyManager.isSubscribing()){
            Nearby.Messages.unsubscribe(mGoogleApiClient,mActiveListener);
            mNearbyManager.setSubscribing(false);
        }
        mChatRoomRef.removeEventListener(mChatRoomListener);

        mGoogleApiClient.disconnect();
    }

    public void recyclerLogic(){
        mAdapter = new ChatMessagesRecyclerAdapter(ChatMessagesHelper.getInstance().getChatLog(),mSelfUserId);//True Code

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
                    long timeStamp = Calendar.getInstance().getTimeInMillis();

                    ChatMessage chatMessage = new ChatMessage(mSelfUserId, mChatRoomId,chatContent,timeStamp);
                    mChatRoomRef.push().setValue(chatMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //If pushing value is successful then let's increment up the other users unread message count.
                            final DatabaseReference unreadMessagesRef = FirebaseDatabaseUtils.getUsersUnreadMessagesRef(mFirebaseDatabase,mOtherUserId,mChatRoomId);
                            unreadMessagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long numOfUnread = (long)dataSnapshot.getValue();
                                    numOfUnread++;
                                    unreadMessagesRef.setValue(numOfUnread);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        publish();
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    public void publish(){
        if(mGoogleApiClient.isConnected()){
            Nearby.Messages.publish(mGoogleApiClient,mNearbyManager.getActiveMessage());
            Log.d("NearBy", "publishing ID");
            mNearbyManager.setPublishing(true);
        }else{
            Toast.makeText(this, "Not connected to Google Cloud", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "publish: failed");
        }
    }
    public void subscribe(){
        Nearby.Messages.subscribe(mGoogleApiClient, mActiveListener);
        mNearbyManager.setSubscribing(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
        }
    }
    public void getOtherUsersId(ChatRoom chatRoom){
        ArrayList<String> userIds = chatRoom.getUserIds();
        if(userIds.get(0).equals(mSelfUserId)){
            mOtherUserId = userIds.get(1);
        }else{
            mOtherUserId=userIds.get(0);
        }
    }
}
