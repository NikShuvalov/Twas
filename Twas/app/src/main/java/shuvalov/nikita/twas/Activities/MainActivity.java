package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import shuvalov.nikita.twas.AppConstants;
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
    Button mSendButt, mSignOutButton, mProfileButton;
    TextView mDisplayText;
    String mFoundId;


    Button mDebugButton; //FixMe: Remove

    boolean mBackRecentlyPressed;
    RecyclerView mRecyclerView;

    NearbyManager mNearbyManager;
    ProfileCollectionRecyclerAdapter mProfileRecAdapter;

    GoogleApiClient mGoogleApiClient;
    Message mFindMeMessage;
//    Message mActiveMessage;
    MessageListener mActiveListener;


    Profile mProfile;
    DatabaseReference mSelfProfileRef;
    DatabaseReference mSelfConnectionsRef;
    DatabaseReference mSelfChatroomsRef;
    FirebaseDatabase mFirebaseDatabase;


    String mId;
//    String[] navOptions= new String[]{"Profile","Home", "Settings","SoapBox Feed","Invite Friends", "Donate", "About"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBackRecentlyPressed=false;

        mNearbyManager = NearbyManager.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();

        findViews();
        setUpRecyclerView();
        retrieveStoredProfiles();
        getUsersFbdbInformation();

        soapBoxDebug();


        mActiveListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                super.onFound(message);
                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if(!soapBoxMessage.getContent().equals("")) {
                    ConnectionsSQLOpenHelper.getInstance(MainActivity.this).addSoapBoxMessage(soapBoxMessage);
                }
                mFoundId = soapBoxMessage.getUserId();
//                mFoundId = new String(message.getContent()); //Gets message from other phone, which holds just that phone's UID for now.


                //ToDo: Figure out what I can store as a value for the stranger Connections, maybe a counter?
                //Idea: If using a counter 0-10 encounters = Stranger, 11-25 Familiar, 26-50 Regular, 51-99 Acquaintance,100-499 Friendly, 500+ whatever
                mSelfConnectionsRef.child(mFoundId).setValue(mFoundId); //Adds stranger's UID to user's connectionsList.

                DatabaseReference strangerRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, mFoundId);
//                DatabaseReference strangerRef = FirebaseDatabaseUtils.getChildReference(mFirebaseDatabase, mFoundId, AppConstants.theoneforprofiles);

                //Gets the stranger's profile information.
                strangerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Profile strangerProfile = dataSnapshot.getValue(Profile.class);
                        ConnectionsSQLOpenHelper.getInstance(MainActivity.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
                        ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile); //Adds Stranger's info to Singleton.
                        mProfileRecAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Did not load correctly", Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "on heard signal, failed attempt to D/L profile ");
                    }
                });
                mDisplayText.setText(mFoundId);
                Toast.makeText(MainActivity.this, mFoundId, Toast.LENGTH_SHORT).show();

                //ToDo: Do a count that adds found users, to keep track of active publishing users.
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
                Toast.makeText(MainActivity.this, "Lost the signal", Toast.LENGTH_SHORT).show();

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
                mDisplayText.setText(ChatMessage.getSoapBoxMessageFromBytes(message.getContent()).getContent());
            }
        };

        //ToDo:Remove this, simply using for debugging to navigate to chatroomListActivity
        mSendButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatroomIntent = new Intent(MainActivity.this, ChatRoomListActivity.class);
                startActivity(chatroomIntent);
            }
        });

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                //ToDo: Clear users information in SharedPref and Database, unless if I keep database info tied to specific Users.
                Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                ConnectionsSQLOpenHelper.getInstance(MainActivity.this).clearDatabase();
                Intent intent = new Intent(MainActivity.this, FirebaseLogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(MainActivity.this, SelfProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    public void soapBoxDebug(){
        mDebugButton = (Button)findViewById(R.id.soapbox_debug);
        mDebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(MainActivity.this, SoapBoxFeedActivity.class);
                startActivity(intent);
            }
        });
    }

    //ToDo: Move into splash screen activity. Should only be called a single time upon load.
    public void retrieveStoredProfiles(){
        ArrayList<Profile> storedProfiles = ConnectionsSQLOpenHelper.getInstance(this).getAllConnections();
        ConnectionsHelper.getInstance().addProfileConnectionsToCollection(storedProfiles);
        mProfileRecAdapter.notifyDataSetChanged();
    }

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

        //ToDo: Check for logged-in user's chatroom associations.
        /*  This is going to have to be a constant listener in case a new chatroom/message appears over the course of the session.
         */

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
                //ToDo: Use the list of connections and compare with what we have in the database, then fill in what's missing.
                ArrayList<String> missingIds = checkForMissingProfiles(connectionsList);
                if(!missingIds.isEmpty()){
                    Log.d("Retrieve Connections", "Some Missing Ids");
                    retrieveMissingProfiles(missingIds);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //ToDo: Move method to another activity/class
    public void retrieveMissingProfiles(ArrayList<String> missingIdList){
        for(String uid:missingIdList){
            DatabaseReference profileRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase,uid);
            profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    ConnectionsSQLOpenHelper.getInstance(MainActivity.this).addNewConnection(profile);
                    int profilesListSize = ConnectionsHelper.getInstance().addProfileToCollection(profile);
                    mProfileRecAdapter.notifyItemInserted(profilesListSize-1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    //ToDo: Move method to another activity/class
    public ArrayList<String> checkForMissingProfiles(ArrayList<String> userIdList){
        ArrayList<Profile> currentStoredProfiles = ConnectionsSQLOpenHelper.getInstance(MainActivity.this).getAllConnections();
        ArrayList<String> currentStoredIds = new ArrayList<>();
        ArrayList<String> idsMissingProfiles = new ArrayList<>();
        for(Profile profile: currentStoredProfiles){
            currentStoredIds.add(profile.getUID());
        }

        for(String uid: userIdList){
            if(!currentStoredIds.contains(uid)){
                idsMissingProfiles.add(uid);
            }
        }

        return idsMissingProfiles;
    }


    public void findViews(){
        mSendButt = (Button)findViewById(R.id.send_butt);
        mDisplayText = (TextView)findViewById(R.id.display_text);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        mSignOutButton = (Button)findViewById(R.id.sign_out_button);
        mProfileButton = (Button)findViewById(R.id.self_profile_button);
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
        NearbyManager.getInstance().setGoogleApiConnected(true);
        publish();
        subscribe();
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
        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
        }
    }

    public void publish(){

        mFindMeMessage = new Message(ChatMessage.getBytesForSoapBox(new ChatMessage(mId,"")));
        Log.d("NearBy", "publishing ID: "+ mId);
        if(mNearbyManager.isGoogleApiConnected()){
            Nearby.Messages.publish(mGoogleApiClient, mFindMeMessage);
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
    protected void onDestroy() {
        //ToDo: Find out if this is even necessary
//        if(mNearbyManager.isPublishing()){
//            Nearby.Messages.unpublish(mGoogleApiClient,mFindMeMessage);
//        }
//        if(mNearbyManager.isSubscribing()){
//            Nearby.Messages.unsubscribe(mGoogleApiClient,mActiveListener);
//        }
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackRecentlyPressed=false;
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
}
