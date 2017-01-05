package shuvalov.nikita.twas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.MessageListener;

import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.RecyclersAndHolders.ProfileCollectionRecyclerAdapter;


/**
 * Created by NikitaShuvalov on 1/4/17.
 */

public class BeaconMessageReceiver extends BroadcastReceiver {
    private Handler mHandler;
    private String mSelfId;
    MessageListener mActiveListener; //This will have to be fed through another class.
    ProfileCollectionRecyclerAdapter mProfileRecAdapter; //This will have to be fed through another class*
//    private PendingIntent mPendingIntent;

//    com.google.android.gms.nearby.messages.Message mActivemessage;

//    GoogleApiClient mGoogleApiClient;

    public BeaconMessageReceiver(){
//        mProfileRecAdapter = ConnectionsHelper.getInstance().getProfileCollectionRecyclerAdapter();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
//        mSelfId = SelfUserProfileUtils.getUserId(context);
        Nearby.Messages.handleIntent(intent, new MessageListener() {
            @Override
            public void onFound(com.google.android.gms.nearby.messages.Message message) {
                super.onFound(message);

                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if(!soapBoxMessage.getContent().equals("")){
                    ConnectionsSQLOpenHelper.getInstance(context).addSoapBoxMessage(soapBoxMessage);
                }

                //ToDo: Add the found ID to the database and have the profile be searched for when the app starts.

            }

            @Override
            public void onLost(com.google.android.gms.nearby.messages.Message message) {
                super.onLost(message);
            }

            @Override
            public void onDistanceChanged(com.google.android.gms.nearby.messages.Message message, Distance distance) {
                super.onDistanceChanged(message, distance);
            }

            @Override
            public void onBleSignalChanged(com.google.android.gms.nearby.messages.Message message, BleSignal bleSignal) {
                super.onBleSignalChanged(message, bleSignal);

                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if(!soapBoxMessage.getContent().equals("")){
                    ConnectionsSQLOpenHelper.getInstance(context).addSoapBoxMessage(soapBoxMessage);
                }
            }
        });
    }

//
//        Log.d("Service", "onCreate Called ");
//        HandlerThread handlerThread = new HandlerThread("MessagesHandler");
//        handlerThread.start();
//
//        mActiveListener = new MessageListener() {
//            @Override
//            public void onFound(com.google.android.gms.nearby.messages.Message message) {
//                super.onFound(message);
//                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
//                if(!soapBoxMessage.getContent().equals("")){
//                    ConnectionsSQLOpenHelper.getInstance(BeaconMessageReceiver.this).addSoapBoxMessage(soapBoxMessage);
//                }
//                String foundId = soapBoxMessage.getUserId();
//                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//
//                //ToDo: Figure out what I can store as a value for the stranger Connections, maybe a counter?
//                //Idea: If using a counter 0-10 encounters = Stranger, 11-25 Familiar, 26-50 Regular, 51-99 Acquaintance,100-499 Friendly, 500+ whatever
//                DatabaseReference selfConnectionRef = FirebaseDatabaseUtils.getUserProfileRef(firebaseDatabase,mSelfId);
//                selfConnectionRef.child(soapBoxMessage.getUserId()).setValue(soapBoxMessage.getUserId()); //Adds stranger's UID to user's connectionsList.
//
//                DatabaseReference strangerRef = FirebaseDatabaseUtils.getUserProfileRef(firebaseDatabase, foundId);
//
//                //Gets the stranger's profile information.
//                strangerRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Profile strangerProfile = dataSnapshot.getValue(Profile.class);
//                        ConnectionsSQLOpenHelper.getInstance(BeaconMessageReceiver.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
//                        ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile); //Adds Stranger's info to Singleton.
//                        mProfileRecAdapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.d("MainActivity", "on heard signal, failed attempt to D/L profile ");
//                    }
//                });
//
//                //ToDo: Do a count that adds found users, to keep track of active publishing users.
//            }

//            @Override
//            public void onLost(com.google.android.gms.nearby.messages.Message message) {
//                super.onLost(message);
//
//                //ToDo: Do a count that removes found users, to keep track of active publishing users.
//
//            }

//            @Override
//            public void onDistanceChanged(com.google.android.gms.nearby.messages.Message
//                                                  message, Distance distance) {
//                super.onDistanceChanged(message, distance);
//            }

//            @Override
//            public void onBleSignalChanged(com.google.android.gms.nearby.messages.Message
//                                                   message, BleSignal bleSignal) {
//                super.onBleSignalChanged(message, bleSignal);
//
//                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
//                if(!soapBoxMessage.getContent().equals("")){
//                    ConnectionsSQLOpenHelper.getInstance(BeaconMessageReceiver.this).addSoapBoxMessage(soapBoxMessage);
//                }
//
//                //ToDo: I think I need to put SoapBoxRecyclerAdapter here in order to update any changes.
//                //ToDo: On second thought, this service is created on MainActivity and that activity doesn't have a reference to the SoapbBox adapter.
//                // ToDo: Unless if I decide to create it in MainActivity and inject into both soapbox activity and this service.

//            }
//        };
//
//        Looper looper = handlerThread.getLooper();
//        mHandler = new Handler(looper){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                if(msg.obj.equals(AppConstants.START_PUBSUB)){
////                    publish();
//                    if(!mGoogleApiClient.isConnected()){
//                        Log.d("GoogleApi", "Never Connected");
//                    }
//
////                    SubscribeOptions subscribeOptions = new SubscribeOptions.Builder().setStrategy(Strategy.BLE_ONLY).build();
////                    Nearby.Messages.subscribe(mGoogleApiClient,getPendingIntent(), subscribeOptions);
//                }else if (msg.obj.equals(AppConstants.STOP_PUBSUB)){
//                    Nearby.Messages.unsubscribe(mGoogleApiClient,getPendingIntent());
//                }
//            }
//        };
//    }
//
//    public PendingIntent getPendingIntent(){
//        return PendingIntent.getBroadcast(this, 0, new Intent(this, BeaconMessageReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Message message = mHandler.obtainMessage();
//        switch(intent.getStringExtra(AppConstants.SERVICE_INTENT)){
//            case AppConstants.START_PUBSUB:
//                message.obj = AppConstants.START_PUBSUB;
//                break;
//            case AppConstants.STOP_PUBSUB:
//                message.obj = AppConstants.STOP_PUBSUB;
//                break;
//            case AppConstants.SHOUTOUT_PUBLISH:
//                message.obj = intent.getStringExtra(AppConstants.SHOUTOUT_MESSAGE);
//                break;
//        }
//        mHandler.sendMessage(message);
//        return START_STICKY;
//    }
//    public void publish(){
//        Log.d("Service", "publish: called");
//        Intent intent = new Intent (this, BeaconMessageReceiver.class);
//        PendingIntent stopSelf = PendingIntent.getService(this,0,intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        mActivemessage = new com.google.android.gms.nearby.messages.Message(ChatMessage.getBytesForSoapBox(new ChatMessage(mSelfId,"")));
//
//        //FixMe: This should allow for background publishing. But should go not here, whatever.
//        PublishOptions publishOptions = new PublishOptions.Builder().setStrategy(Strategy.BLE_ONLY).build();
//        Nearby.Messages.publish(mGoogleApiClient, mActivemessage,publishOptions);
//
//        NearbyManager.getInstance().setPublishing(true);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setContentTitle("Twas is active")
//                .setContentText("Click Here To Turn Off Discovery")
//                .setOngoing(true)
//                .setContentIntent(stopSelf)
//                .build();
//        }
//
//    @Override
//    public void onDestroy() {
//        mGoogleApiClient.disconnect();
//        super.onDestroy();
//    }

}
