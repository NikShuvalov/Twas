package shuvalov.nikita.twas.Activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
import shuvalov.nikita.twas.RecyclersAndHolders.ChatRoomsRecyclerAdapter;

public class ChatRoomListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private RecyclerView mRecyclerView;
    private ChatRoomsRecyclerAdapter mAdapter;
    private ChildEventListener mChatRoomsListener;
    private DatabaseReference mUserChatRoomRef;
    public GoogleApiClient mGoogleApiClient;
    public NearbyManager mNearbyManager;
    private FirebaseDatabase mFirebaseDatabase;
    private MessageListener mActiveListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);

        findViews();
        recyclerLogic();

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();

        mNearbyManager = NearbyManager.getInstance();

        mUserChatRoomRef = FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, SelfUserProfileUtils.getUserId(this));

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

        mActiveListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                super.onFound(message);
                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if (!soapBoxMessage.getContent().equals("")) {
                    ConnectionsSQLOpenHelper.getInstance(ChatRoomListActivity.this).addSoapBoxMessage(soapBoxMessage);
                }
                String mFoundId = soapBoxMessage.getUserId();


                //ToDo: Figure out what I can store as a value for the stranger Connections, maybe a counter?
                //Idea: If using a counter 0-10 encounters = Stranger, 11-25 Familiar, 26-50 Regular, 51-99 Acquaintance,100-499 Friendly, 500+ whatever
                FirebaseDatabaseUtils.getUserConnectionsRef(mFirebaseDatabase, SelfUserProfileUtils.getUserId(ChatRoomListActivity.this)).child(mFoundId).setValue(mFoundId); //Adds stranger's UID to user's connectionsList.


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
                FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,SelfUserProfileUtils.getUserId(ChatRoomListActivity.this))
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
                        ConnectionsSQLOpenHelper.getInstance(ChatRoomListActivity.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
                        ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile); //Adds Stranger's info to Singleton.
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

        //ToDo: Allow users to rename the chatRoom.

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
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserChatRoomRef.removeEventListener(mChatRoomsListener);

        if(mNearbyManager.isPublishing()){
            Nearby.Messages.unpublish(mGoogleApiClient,mNearbyManager.getActiveMessage());
            mNearbyManager.setPublishing(false);
        }
        if(mNearbyManager.isSubscribing()){
            Nearby.Messages.unsubscribe(mGoogleApiClient,mActiveListener);
            mNearbyManager.setSubscribing(false);
        }


        mGoogleApiClient.disconnect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        publish();
        subscribe();

    }

    public void publish(){
        if(mGoogleApiClient.isConnected()){
            Nearby.Messages.publish(mGoogleApiClient,mNearbyManager.getActiveMessage());
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
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
