package shuvalov.nikita.twas.Helpers_Managers;

import android.support.annotation.RestrictTo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import shuvalov.nikita.twas.AppConstants;

/**
 * Created by NikitaShuvalov on 12/28/16.
 */

public class FirebaseDatabaseUtils {

    public static DatabaseReference getChildReference(FirebaseDatabase fbdb, String uid, String childPath){
        return fbdb.getReference(uid).child(childPath);

    }

    public static DatabaseReference getUserProfileRef(FirebaseDatabase fbdb, String uid){
        return fbdb.getReference(uid).child(AppConstants.FIREBASE_USER_CHILD_PROFILE);
    }

    public static DatabaseReference getUserConnectionsRef(FirebaseDatabase fbdb, String uid){
        return fbdb.getReference(uid).child(AppConstants.FIREBASE_USER_CHILD_CONNECTIONS);
    }

    public static DatabaseReference getUserChatroomsRef (FirebaseDatabase fbdb, String uid){
        return fbdb.getReference(uid).child(AppConstants.FIREBASE_USER_CHILD_CHATROOMS);
    }
}
