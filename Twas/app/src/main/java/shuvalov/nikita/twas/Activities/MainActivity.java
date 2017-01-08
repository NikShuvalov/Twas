package shuvalov.nikita.twas.Activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.ChatRoomMessageService;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.RecyclersAndHolders.ProfileCollectionRecyclerAdapter;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String FOUND_ID_INTENT = "Found id";

    Toolbar mToolbar;
    String mFoundId;



    boolean mBackRecentlyPressed;
    RecyclerView mRecyclerView;

    NearbyManager mNearbyManager;
    ProfileCollectionRecyclerAdapter mProfileRecAdapter;

    public GoogleApiClient mGoogleApiClient;
    Message mFindMeMessage;
    MessageListener mActiveListener;


    Profile mProfile;
    DatabaseReference mSelfProfileRef;
    DatabaseReference mSelfConnectionsRef;
    DatabaseReference mSelfChatroomsRef;
    FirebaseDatabase mFirebaseDatabase;


    String mId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBackRecentlyPressed = false;

        mNearbyManager = NearbyManager.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();

        findViews();
        setUpRecyclerView();
//        retrieveStoredProfiles();
        getUsersFbdbInformation();


        if(mProfileRecAdapter.getItemCount()==0 && !SelfUserProfileUtils.getAskedForFriendship(this)){
            new AlertDialog.Builder(this).setTitle("You have no connections yet =(")
                    .setMessage("That's okay though; I'll be your friend!")
                    .setPositiveButton("Save me from my solitude!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mSelfConnectionsRef.child(AppConstants.MY_USER_ID).setValue(AppConstants.MY_USER_ID);
                            DatabaseReference strangerRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, AppConstants.MY_USER_ID);
                            SelfUserProfileUtils.setAskedForFriendship(MainActivity.this);

                            strangerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Profile strangerProfile = dataSnapshot.getValue(Profile.class);
                                    if (strangerProfile != null) {
                                        ConnectionsSQLOpenHelper.getInstance(MainActivity.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
                                        ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile); //Adds Stranger's info to Singleton.
                                        mProfileRecAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    })
                    .setNegativeButton("No thanks, I prefer an empty screen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this, "I'm sorry I annoyed you with my friendship", Toast.LENGTH_SHORT).show();
                            SelfUserProfileUtils.setAskedForFriendship(MainActivity.this);
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }

        mActiveListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                super.onFound(message);
                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if (!soapBoxMessage.getContent().equals("")) {
                    ConnectionsSQLOpenHelper.getInstance(MainActivity.this).addSoapBoxMessage(soapBoxMessage);
                }
                mFoundId = soapBoxMessage.getUserId();


                //ToDo: Figure out what I can store as a value for the stranger Connections, maybe a counter?
                //Idea: If using a counter 0-10 encounters = Stranger, 11-25 Familiar, 26-50 Regular, 51-99 Acquaintance,100-499 Friendly, 500+ whatever
                mSelfConnectionsRef.child(mFoundId).setValue(mFoundId); //Adds stranger's UID to user's connectionsList.

                DatabaseReference strangerRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, mFoundId);

                mSelfChatroomsRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        //ToDo: Maybe add a boolean here for first find.
                        Toast.makeText(MainActivity.this, "New ChatRoom", Toast.LENGTH_SHORT).show();
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
                        if(strangerProfile!=null){
                            ConnectionsSQLOpenHelper.getInstance(MainActivity.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
                            ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile); //Adds Stranger's info to Singleton.
                            mProfileRecAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Did not load correctly", Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "on heard signal, failed attempt to D/L profile ");
                    }
                });

                //ToDo: Do a count that adds found users, to keep track of active publishing users.
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);

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

    //ToDo: Move into splash screen activity. Should only be called a single time upon load.
//    public void retrieveStoredProfiles(){
//        Log.d("MainActivity", "Retrieving Stored Preferences ");
//        ArrayList<Profile> storedProfiles = ConnectionsSQLOpenHelper.getInstance(this).getAllConnections();
//        ConnectionsHelper.getInstance().addProfileConnectionsToCollection(storedProfiles);
//        mProfileRecAdapter.notifyDataSetChanged();
//    }

    //ToDo: Move method to another activity/class. So that we don't end up doing database check/syncs everytime we navigate back to here.
    public void getUsersFbdbInformation(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mId = SelfUserProfileUtils.getUserId(this);
        if(mId.equals(AppConstants.PREF_EMPTY)){
            Log.d("MainActivity", "Either Awesome ID or No ID found in sharedPref");
            Throwable throwable = new Throwable("Accessed requires a User ID");//FixMe: Pretty sure this isn't the right way to do this.
            try {
                throw throwable;
            } catch (Throwable throwable1) {
                throwable1.printStackTrace();
            }
        }

        mSelfProfileRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase,mId);
        mSelfConnectionsRef = FirebaseDatabaseUtils.getUserConnectionsRef(mFirebaseDatabase, mId);
        mSelfChatroomsRef = FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,mId);

        //Check for logged-in user's profile information, in case it was updated on another device.
        mSelfProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile myProfile = dataSnapshot.getValue(Profile.class);
                if(myProfile!=null){
                    SelfUserProfileUtils.assignProfileToSharedPreferences(MainActivity.this, myProfile);
                    Log.d("Profile test", "onDataChange: "+myProfile.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Checking FBDB", "onCancelled: "+ databaseError.getMessage());
            }
        });

        //ToDo: Check for logged-in user's connections list.
        mSelfConnectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Retrieve Connections", "Checking for connections");
                Iterable<DataSnapshot> connectionsSnapshot= dataSnapshot.getChildren();
                ArrayList<String> connectionsList = new ArrayList<>();
                while(connectionsSnapshot.iterator().hasNext()){
                    String profileUid = connectionsSnapshot.iterator().next().getKey();
                    Log.d("Connection Retrieval", "Connection UID: "+ profileUid);
                    connectionsList.add(profileUid);

                }
                syncUserProfiles(connectionsList);
//                //ToDo: Use the list of connections and compare with what we have in the database, then fill in what's missing.
//                ArrayList<String> missingIds = checkForMissingProfiles(connectionsList);
//                if(!missingIds.isEmpty()){
//                    Log.d("Retrieve Connections", "Some Missing Ids");
//                    retrieveMissingProfiles(missingIds);
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //ToDo: Do this during splash screen animation ideally.
    public void syncUserProfiles(ArrayList<String> userIds){
//        final ArrayList<Profile> syncedProfiles = new ArrayList<>();
        for(String userId: userIds){
            DatabaseReference userProfileReference = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase,userId);
            userProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Profile userProfile = dataSnapshot.getValue(Profile.class);
//                    syncedProfiles.add(userProfile);
                    ConnectionsSQLOpenHelper.getInstance(MainActivity.this).addNewConnection(userProfile);
                    ConnectionsHelper.getInstance().addProfileConnectionsToCollection(ConnectionsSQLOpenHelper.getInstance(MainActivity.this).getAllConnections());
                    mProfileRecAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
//        ConnectionsSQLOpenHelper.getInstance(this).addCollection(syncedProfiles);
//        ConnectionsHelper.getInstance().addProfileConnectionsToCollection(syncedProfiles);
//        mProfileRecAdapter.notifyDataSetChanged();
    }

    //ToDo: Move method to another activity/class
//    public void retrieveMissingProfiles(ArrayList<String> missingIdList){
//        for(String uid:missingIdList){
//            DatabaseReference profileRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase,uid);
//            profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Profile profile = dataSnapshot.getValue(Profile.class);
//                    if(profile!=null){
//                        ConnectionsSQLOpenHelper.getInstance(MainActivity.this).addNewConnection(profile);
//                        int profilesListSize = ConnectionsHelper.getInstance().addProfileToCollection(profile);
//                        mProfileRecAdapter.notifyItemInserted(profilesListSize-1);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
//    }

    //ToDo: Move method to another activity/class
//    public ArrayList<String> checkForMissingProfiles(ArrayList<String> userIdList){
//        ArrayList<Profile> currentStoredProfiles = ConnectionsSQLOpenHelper.getInstance(MainActivity.this).getAllConnections();
//        ArrayList<String> currentStoredIds = new ArrayList<>();
//        ArrayList<String> idsMissingProfiles = new ArrayList<>();
//        for(Profile profile: currentStoredProfiles){
//            currentStoredIds.add(profile.getUID());
//        }
//        for(String uid: userIdList){
//            if(!currentStoredIds.contains(uid)){
//                idsMissingProfiles.add(uid);
//            }
//        }
//
//        return idsMissingProfiles;
//    }


    public void findViews(){
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
    }

    public void setUpRecyclerView(){
        mRecyclerView = (RecyclerView)findViewById(R.id.profiles_recyclerview);

        mProfileRecAdapter = new ProfileCollectionRecyclerAdapter(ConnectionsHelper.getInstance().getConnections());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mProfileRecAdapter);
    }


    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        NearbyManager.getInstance().setGoogleApiClient(mGoogleApiClient);
        publish();
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
        }
        Intent intent = new Intent(MainActivity.this, ChatRoomMessageService.class);
        startService(intent);

    }

    //ToDo: This will have to be a helper Method possibly.
    public void setUpActiveMessage(){
        String soapBoxMessageString = SelfUserProfileUtils.getSoapBoxMessage(this);
        long timeStamp = SelfUserProfileUtils.getSoapBoxTimeStamp(this);
        mFindMeMessage = new Message(ChatMessage.getBytesForSoapBox(new ChatMessage(mId,null,soapBoxMessageString, timeStamp)));
        mNearbyManager.setActiveMessage(mFindMeMessage);
    }

    public void publish(){
        //Initially sets up the ActiveMessage to be sent out.
        if(mNearbyManager.getActiveMessage()==null){
            setUpActiveMessage();
        }

        //Actually does the sending
        if(mGoogleApiClient.isConnected()){
            Nearby.Messages.publish(mGoogleApiClient,mNearbyManager.getActiveMessage());
            Log.d("NearBy", "publishing ID: "+ mId);
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
    protected void onPause() {
        if(mNearbyManager.isPublishing()){
            Nearby.Messages.unpublish(mGoogleApiClient, mNearbyManager.getActiveMessage());
            mNearbyManager.setPublishing(false);
        }
        if(mNearbyManager.isSubscribing()){
            Nearby.Messages.unsubscribe(mGoogleApiClient, mActiveListener);
            mNearbyManager.setSubscribing(false);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBackRecentlyPressed=false;
        if(mGoogleApiClient.isConnected()){
            if(!mNearbyManager.isPublishing()){
                publish();
            }
            if(!mNearbyManager.isSubscribing()){
                subscribe();
            }

        }else{
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onBackPressed() {
        if(!mBackRecentlyPressed){
            mBackRecentlyPressed=true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        else{
            mBackRecentlyPressed=false;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.mainactivity_menu,menu);

        //Sets user's Profile Icon Image in toolbar.
        String iconImageDataAsString = SelfUserProfileUtils.getProfileIconImageFile(this);
        if(!iconImageDataAsString.equals("")){
            byte[] iconImageData = Base64.decode(iconImageDataAsString,Base64.DEFAULT);
            Drawable iconImage = new BitmapDrawable(BitmapFactory.decodeByteArray(iconImageData,0,iconImageData.length));
            menu.findItem(R.id.self_profile_option).setIcon(iconImage);
        }else{
            //ToDo: Set a default image Icon here.
            menu.findItem(R.id.self_profile_option).setIcon(R.drawable.shakespeare_modern_bard_post);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.chatrooms_option:
                intent = new Intent(MainActivity.this, ChatRoomListActivity.class);
                startActivity(intent);
                break;
            case R.id.soapbox_option:
                intent  = new Intent(MainActivity.this, SoapBoxFeedActivity.class);
                startActivity(intent);
                break;
            case R.id.signout_option:
                signSelfOut();
                break;
            case R.id.self_profile_option:
                intent =new Intent(MainActivity.this, SelfProfileActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signSelfOut(){
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();

        ConnectionsSQLOpenHelper.getInstance(this).clearDatabase();
        Intent intent = new Intent(MainActivity.this, FirebaseLogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
