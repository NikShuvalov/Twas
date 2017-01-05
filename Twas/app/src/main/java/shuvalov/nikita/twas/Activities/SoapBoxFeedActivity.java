package shuvalov.nikita.twas.Activities;

import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.RecyclersAndHolders.ChatMessagesRecyclerAdapter;

public class SoapBoxFeedActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
//    private Button mSend;
//    private EditText mMessageEntry;
    private RecyclerView mRecyclerView;
    private ChatMessagesRecyclerAdapter mMessagesRecyclerAdapter;
    private MessageListener mActiveListener;
    String mSelfId;

    Message mActiveMessage;

    NearbyManager mNearbyManager;
    GoogleApiClient mGoogleApiClient;

//    DatabaseReference mSelfProfileRef;
//    DatabaseReference mSelfConnectionsRef;
//    DatabaseReference mSelfChatroomsRef;
//    FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap_box_feed);

//        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mSelfId = SelfUserProfileUtils.getUserId(this);


        findViews();
        recyclerLogic();
        mNearbyManager = NearbyManager.getInstance();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

//        mSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(mMessageEntry.getText().toString().equals("")){
//                    mMessageEntry.setError("Can't send an empty message");
//                }else{
//                    String shoutOut = mMessageEntry.getText().toString();
//
//                    ChatMessage soapBoxMessage = new ChatMessage(mSelfId, shoutOut);
//                    byte[] soapBoxBytes = ChatMessage.getBytesForSoapBox(soapBoxMessage);
//
//                    if(mGoogleApiClient.isConnected()){
//                        PublishOptions blastOptions = new PublishOptions.Builder().setStrategy(
//                                new Strategy.Builder().setTtlSeconds(60).build()).build();
//                        if(mGoogleApiClient.isConnected()){
//                            Nearby.Messages.unpublish(mGoogleApiClient,mActiveMessage);
//                            mActiveMessage = new Message(soapBoxBytes);
//
//                            Nearby.Messages.publish(mGoogleApiClient,mActiveMessage, blastOptions).setResultCallback(new ResultCallback<Status>() {
//                                @Override
//                                public void onResult(@NonNull Status status) {
//                                    Log.d("SoapBox Status", "Sending "+status.isSuccess());
//                                }
//                            });
//                            mMessageEntry.setText("");
//                        }else{
//                            Toast.makeText(SoapBoxFeedActivity.this, "Message failed to send, try again", Toast.LENGTH_SHORT).show();
//                        }
//                        //Turn this off after some time. Or immediately?
//                    }
//                    //ToDo: Publish this! Blast it out for everyone near by to hear.
//
//                }
//            }
//        });
//        createNearbyListener();
    }

//    public void createNearbyListener(){
//        mSelfProfileRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, mSelfId);
//            mSelfConnectionsRef = FirebaseDatabaseUtils.getUserConnectionsRef(mFirebaseDatabase, mSelfId);
//            mSelfChatroomsRef = FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, mSelfId);
//
//            mActiveListener = new MessageListener() {
//                @Override
//                public void onFound(Message message) {
//                    ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
//                    Log.d("SoapBoxActivity", "Found Message: "+soapBoxMessage.getContent()+" "+soapBoxMessage.getUserId());
//                    Log.d("SoapBoxActivityListener", "...these active?");
//
//                    if (!soapBoxMessage.getContent().equals("")) {
//                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(SoapBoxFeedActivity.this);
//
//                        ConnectionsSQLOpenHelper.getInstance(SoapBoxFeedActivity.this).addSoapBoxMessage(soapBoxMessage);
//                        notificationBuilder.setContentText(soapBoxMessage.getContent()).setContentTitle("New SoapBoxMessage");
//                        notificationManager.notify(0,notificationBuilder.build());;
//                    }
//                    String foundId = soapBoxMessage.getUserId();
////                mFoundId = new String(message.getContent()); //Gets message from other phone, which holds just that phone's UID for now.
//
//
//                    //ToDo: Figure out what I can store as a value for the stranger Connections, maybe a counter?
//                    //Idea: If using a counter 0-10 encounters = Stranger, 11-25 Familiar, 26-50 Regular, 51-99 Acquaintance,100-499 Friendly, 500+ whatever
//                    mSelfConnectionsRef.child(foundId).setValue(foundId); //Adds stranger's UID to user's connectionsList.
//
//                    DatabaseReference strangerRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, foundId);
////                DatabaseReference strangerRef = FirebaseDatabaseUtils.getChildReference(mFirebaseDatabase, mFoundId, AppConstants.theoneforprofiles);
//
//                    //Gets the stranger's profile information.
//                    strangerRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            Profile strangerProfile = dataSnapshot.getValue(Profile.class);
//                            ConnectionsSQLOpenHelper.getInstance(SoapBoxFeedActivity.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
//                            ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile); //Adds Stranger's info to Singleton.
//
////                            mProfileRecAdapter.notifyDataSetChanged();
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            Log.d("MainActivity", "on heard signal, failed attempt to D/L profile ");
//                        }
//                    });
//
//                    //ToDo: Do a count that adds found users, to keep track of active publishing users.
//                }
//
//                @Override
//                public void onLost(Message message) {
//                    super.onLost(message);
//
//                    //ToDo: Do a count that removes found users, to keep track of active publishing users.
//
//                }
//
//                @Override
//                public void onDistanceChanged(Message message, Distance distance) {
//                    super.onDistanceChanged(message, distance);
//                }
//
//                @Override
//                public void onBleSignalChanged(Message message, BleSignal bleSignal) {
//                    super.onBleSignalChanged(message, bleSignal);
//                    Log.d("Testing Shots fired", "Please clap");
//                }
//            };
//        }



    public void findViews(){
//        mSend = (Button)findViewById(R.id.send_butt);
//        mMessageEntry = (EditText)findViewById(R.id.message_entry);
        mRecyclerView = (RecyclerView)findViewById(R.id.soapbox_feed_recycler);
    }

    public void recyclerLogic(){
        mMessagesRecyclerAdapter = new ChatMessagesRecyclerAdapter(ConnectionsSQLOpenHelper.getInstance(this).getSoapBoxMessages(),mSelfId);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setAdapter(mMessagesRecyclerAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        int size = mMessagesRecyclerAdapter.getItemCount();
        if(size==0){
            mRecyclerView.smoothScrollToPosition(0);
        }else{
            mRecyclerView.smoothScrollToPosition(size-1);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        subscribe();
        publish();

//        mNearbyManager.setupForListening(mSelfId,this).beginListening(mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {
        NearbyManager.getInstance().setGoogleApiConnected(false);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();

        }
        super.onDestroy();
    }

    public void publish(){
        String id = SelfUserProfileUtils.getUserId(this);
        String soapBoxMessageString = SelfUserProfileUtils.getUserId(this);
        mActiveMessage = new Message(ChatMessage.getBytesForSoapBox(new ChatMessage(id,soapBoxMessageString)));
        Nearby.Messages.publish(mGoogleApiClient, mActiveMessage);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void subscribe(){
        Nearby.Messages.subscribe(mGoogleApiClient, mActiveListener);
        mNearbyManager.setSubscribing(true);
    }
}
