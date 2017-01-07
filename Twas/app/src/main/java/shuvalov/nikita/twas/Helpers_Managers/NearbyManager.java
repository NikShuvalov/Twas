package shuvalov.nikita.twas.Helpers_Managers;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.Profile;

/**
 * Created by NikitaShuvalov on 12/16/16.
 */

public class NearbyManager {
    private boolean mGoogleApiConnected, mPublishing, mSubscribing;
    private GoogleApiClient mGoogleApiClient;
    private Message mActiveMessage;
//    private MessageListener mActiveListener;
//
//    private String mSelfId;
//    private Context mContext;
//
//    DatabaseReference mSelfProfileRef;
//    DatabaseReference mSelfConnectionsRef;
//    DatabaseReference mSelfChatroomsRef;
//    FirebaseDatabase mFirebaseDatabase;

    private static NearbyManager sMyNearbyManager;

    public static NearbyManager getInstance() {
        if(sMyNearbyManager ==null){
            sMyNearbyManager = new NearbyManager();
        }
        return sMyNearbyManager;
    }

    private NearbyManager() {
        mGoogleApiConnected = false;
        mPublishing = false;
        mSubscribing = false;

//        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    public boolean isPublishing() {
        return mPublishing;
    }

    public void setPublishing(boolean publishing) {
        mPublishing = publishing;
    }

    public boolean isSubscribing() {
        return mSubscribing;
    }

    public void setSubscribing(boolean subscribing) {
        mSubscribing = subscribing;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient){
        mGoogleApiClient = googleApiClient;
    }
    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }

    public void setActiveMessage(Message message){
        mActiveMessage = message;
    }

    public Message getActiveMessage(){
        return mActiveMessage;
    }


//    public NearbyManager setupForListening(String selfId, Context context){
//        mSelfId = selfId;
//        mContext = context;
//        return this;
//    }

//    public NearbyManager beginListening(GoogleApiClient googleApiClient){
//        if(mSelfId==null || mContext == null){
//            Log.e("Error", "UID and Context must be set to NearByManager before you can begin listening.");
//        }else {
//            mSubscribing=true;
//            mSelfProfileRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, mSelfId);
//            mSelfConnectionsRef = FirebaseDatabaseUtils.getUserConnectionsRef(mFirebaseDatabase, mSelfId);
//            mSelfChatroomsRef = FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, mSelfId);
//
//            mActiveListener = new MessageListener() {
//                @Override
//                public void onFound(Message message) {
//                    super.onFound(message);
//                    ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
//                    if (!soapBoxMessage.getContent().equals("")) {
//                        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//                        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(mContext).setAutoCancel(true);
//
//                        ConnectionsSQLOpenHelper.getInstance(mContext).addSoapBoxMessage(soapBoxMessage);
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
//                            ConnectionsSQLOpenHelper.getInstance(mContext).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
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
//        Nearby.Messages.subscribe(googleApiClient,mActiveListener);
//        return this;
//    }
//    public void stopListening(){
//        Nearby.Messages.unsubscribe(mGoogleApiClient,mActiveListener);
//        mSubscribing=false;
//    }
//
//    public void beginPublishing(byte[] messageToPublish){
//        Message message = new Message(messageToPublish);
//        Nearby.Messages.publish(mGoogleApiClient,message);
//    }
}
