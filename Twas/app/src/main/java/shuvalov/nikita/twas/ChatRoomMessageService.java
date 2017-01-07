package shuvalov.nikita.twas;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import shuvalov.nikita.twas.Activities.ChatRoomActivity;
import shuvalov.nikita.twas.Activities.ChatRoomListActivity;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;

public class ChatRoomMessageService extends Service {
    private ChildEventListener mChatRoomListener;
    private FirebaseDatabase mFirebaseDatabase;
    private String mSelfUid;

    public ChatRoomMessageService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        Intent intent = new Intent(this, ChatRoomListActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        mSelfUid = SelfUserProfileUtils.getUserId(this);
        mChatRoomListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String numOfUnread = String.valueOf(dataSnapshot.child(AppConstants.FIREBASE_UNREAD_MESSAGES).getValue());

                NotificationCompat.Builder builder = new NotificationCompat.Builder(ChatRoomMessageService.this);

                String notificationText = "You have new message(s)";
//                if(numOfUnread.equals("1")){
//                    notificationText = "You have an unread message";
//                }else{
//                    notificationText = String.format("You have %s unread messages",numOfUnread);
//                }
                //ToDo: Add a sound?
                builder.setAutoCancel(true)
                        .setContentTitle("TWAS")
                        .setContentText(notificationText)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert);//ToDo: Change to mipMap

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1492, builder.build());
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
        FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,mSelfUid).addChildEventListener(mChatRoomListener);
    }
}
