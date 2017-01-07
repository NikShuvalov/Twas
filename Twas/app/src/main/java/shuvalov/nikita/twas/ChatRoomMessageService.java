package shuvalov.nikita.twas;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.FirebaseDatabase;

public class ChatRoomMessageService extends Service {
    private ChildEventListener mChatRoomListener;
    private FirebaseDatabase mFirebaseDatabase;

    public ChatRoomMessageService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
}
