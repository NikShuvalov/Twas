package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FireBaseStorageUtils;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.ChatRoom;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class ProfileDetailActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    TextView mNameText, mBioText, mDOBText, mGenderText;
    ImageView mImageView;
    Toolbar mToolbar;
    FirebaseDatabase mFirebaseDatabase;
    String mSelfUserId;
    Profile mSelectedProfile;
    private GoogleApiClient mGoogleApiClient;
    private NearbyManager mNearbyManager;
    private MessageListener mActiveListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();

        mNearbyManager = NearbyManager.getInstance();
        mSelfUserId = SelfUserProfileUtils.getUserId(ProfileDetailActivity.this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        findViews();
        if(getIntent().getStringExtra(AppConstants.ORIGIN_ACTIVITY).equals(AppConstants.ORIGIN_MAIN)){
            mSelectedProfile = ConnectionsHelper.getInstance().getProfileByPosition(getIntent().getIntExtra(AppConstants.PREF_HELPER_POSITION,-1));
        }else{
            mSelectedProfile = ConnectionsSQLOpenHelper.getInstance(this).getConnectionById(getIntent().getStringExtra(AppConstants.PREF_ID));
            if(mSelectedProfile==null){
                Toast.makeText(this, "Couldn't load profile of sender", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        bindDataToViews(mSelectedProfile);

        mActiveListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                super.onFound(message);
                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if (!soapBoxMessage.getContent().equals("")) {
                    ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addSoapBoxMessage(soapBoxMessage);
                }
                String mFoundId = soapBoxMessage.getUserId();


                //ToDo: Figure out what I can store as a value for the stranger Connections, maybe a counter?
                //Idea: If using a counter 0-10 encounters = Stranger, 11-25 Familiar, 26-50 Regular, 51-99 Acquaintance,100-499 Friendly, 500+ whatever
                FirebaseDatabaseUtils.getUserConnectionsRef(mFirebaseDatabase, SelfUserProfileUtils.getUserId(ProfileDetailActivity.this)).child(mFoundId).setValue(mFoundId); //Adds stranger's UID to user's connectionsList.


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
                FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,SelfUserProfileUtils.getUserId(ProfileDetailActivity.this))
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
                        ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
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
    }

    public void findViews(){
        mBioText = (TextView)findViewById(R.id.bio_text);
        mNameText = (TextView)findViewById(R.id.name_text);
        mDOBText = (TextView)findViewById(R.id.age_text);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        mGenderText = (TextView)findViewById(R.id.gender_text);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImageView = (ImageView)findViewById(R.id.profile_image_view);
    }

    public void bindDataToViews(Profile profile){
        mBioText.setText(profile.getBio());

        String userName = profile.getName();
        mNameText.setText(userName);
        getSupportActionBar().setTitle(userName);


        long birthdateMillis = profile.getDOB();
        Calendar birthCal = Calendar.getInstance();
        birthCal.setTimeInMillis(birthdateMillis);
        int year = birthCal.get(Calendar.YEAR);
        int month = birthCal.get(Calendar.MONTH);
        int date = birthCal.get(Calendar.DATE);
        String dateAsString;

        if(month<10||date<10){
            String monthString;
            String dateString;
            if(month<10){
                monthString = 0+String.valueOf(month);
            }else{
                monthString = String.valueOf(month);
            }
            if (date < 10) {
                dateString = 0+ String.valueOf(date);
            }else{
                dateString = String.valueOf(date);
            }
            dateAsString = monthString+"/"+dateString+"/"+year;
        }else{
            dateAsString = String.valueOf(month)+"/"+date+"/"+year;
        }
        mDOBText.setText(dateAsString);



        StorageReference imageStoreRef = FireBaseStorageUtils.getProfilePicStorageRef(profile.getUID());
        imageStoreRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(ProfileDetailActivity.this)
                        .load(uri)
                        .into(mImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mImageView.setImageResource(R.drawable.shakespeare_modern_bard_post);
            }
        });

        mGenderText.setText(profile.getGender());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_detail_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.message_opt:
                startChat();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void startChat(){
        DatabaseReference selfReference = FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,mSelfUserId);

        selfReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> chatRoomsData = dataSnapshot.getChildren();
                boolean done = false;
                while (chatRoomsData.iterator().hasNext() && !done) {
                    ChatRoom chatRoom = chatRoomsData.iterator().next().getValue(ChatRoom.class);
                    ArrayList<String> userIds = chatRoom.getUserIds();
                    Log.d("Testing", "onDataChange: " + userIds.get(0));
                    Log.d("Testing", "onDataChange: " + userIds.get(1));

                    //If both user IDs are found in one of the chatrooms that means users already have an existing chatroom together.
                    if ((userIds.get(0).equals(mSelfUserId) && userIds.get(1).equals(mSelectedProfile.getUID())) ||
                            (userIds.get(1).equals(mSelfUserId) && userIds.get(0).equals(mSelectedProfile.getUID()))) {
                        done = true;
                        Intent intent = new Intent(ProfileDetailActivity.this, ChatRoomActivity.class);
                        intent.putExtra(AppConstants.ORIGIN_ACTIVITY, AppConstants.ORIGIN_PROFILE_DETAIL);
                        intent.putExtra(AppConstants.PREF_CHATROOM, chatRoom.getId());
                        intent.putExtra(AppConstants.PREF_OTHER_UID, mSelectedProfile.getUID());
                        startActivity(intent);
                    }
                }
                if (!done) {
                    DatabaseReference chatroomsRef = FirebaseDatabaseUtils.getChatroomRef(mFirebaseDatabase, null);

                    DatabaseReference createdChatroom = chatroomsRef.push();

                    Profile selfUser = SelfUserProfileUtils.getUsersInfoAsProfile(ProfileDetailActivity.this);


                    ChatRoom chatroom = new ChatRoom();
                    chatroom.setId(createdChatroom.getKey());
                    chatroom.addUserToChatroom(selfUser.getUID());
                    chatroom.addUserToChatroom(mSelectedProfile.getUID());
                    createdChatroom.child(AppConstants.FIREBASE_USERS).setValue(chatroom);
                    DatabaseReference messageReference = FirebaseDatabaseUtils.getChatroomMessagesRef(mFirebaseDatabase, chatroom.getId());
                    long currentTime = Calendar.getInstance().getTimeInMillis();
                    ChatMessage initialChatMessage = new ChatMessage(selfUser.getUID(), chatroom.getId(), String.format("%s has started this conversation", selfUser.getName()), currentTime);
                    messageReference.push().setValue(initialChatMessage);

                    //Adds a reference to the Chatroom to both members' profiles.
                    FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, selfUser.getUID()).child(chatroom.getId()).setValue(chatroom);
                    chatroom.unreadMessages++;
                    FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, mSelectedProfile.getUID()).child(chatroom.getId()).setValue(chatroom);

                    //Adds the newly made chatroom and initial message to the local db.
                    ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addChatRoom(chatroom);
                    ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addMessage(initialChatMessage);

                    Intent intent = new Intent(ProfileDetailActivity.this, ChatRoomActivity.class);
                    intent.putExtra(AppConstants.ORIGIN_ACTIVITY, AppConstants.ORIGIN_PROFILE_DETAIL);
                    intent.putExtra(AppConstants.PREF_CHATROOM, chatroom.getId());
                    intent.putExtra(AppConstants.PREF_OTHER_UID, mSelectedProfile.getUID());
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        publish();
        subscribe();
    }

    public void publish(){
        //Actually does the sending
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

