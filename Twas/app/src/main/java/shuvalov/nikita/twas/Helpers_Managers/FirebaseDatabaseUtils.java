package shuvalov.nikita.twas.Helpers_Managers;

import android.support.annotation.Nullable;
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

    /**
     *  Returns reference to Root->Users->User->Profile
     *
     * @param fbdb
     * @param uid
     * @return
     */
    public static DatabaseReference getUserProfileRef(FirebaseDatabase fbdb, String uid){
        return fbdb.getReference(AppConstants.FIREBASE_USERS).child(uid).child(AppConstants.FIREBASE_USER_CHILD_PROFILE);
    }

    /**
     *    Returns reference to Root->Users->User->Connections
     *
     * @param fbdb
     * @param uid
     * @return
     */
    public static DatabaseReference getUserConnectionsRef(FirebaseDatabase fbdb, String uid){
        return fbdb.getReference(AppConstants.FIREBASE_USERS).child(uid).child(AppConstants.FIREBASE_USER_CHILD_CONNECTIONS);
    }

    /**
     *     Returns reference to Root->Users->User->Chatrooms
     *
     * @param fbdb
     * @param uid
     * @return
     */
    public static DatabaseReference getUserChatroomsRef (FirebaseDatabase fbdb, String uid){
        return fbdb.getReference(AppConstants.FIREBASE_USERS).child(uid).child(AppConstants.FIREBASE_USER_CHILD_CHATROOMS);
    }

    /**
     * If chatroomId is known, then returns reference to Root->Chatrooms->Chatroom.
     * Otherwise pass Null as a param to get the parent reference of all of the chatrooms.
     *
     * @param fbdb
     * @param chatroomId
     * @return
     */
    public static DatabaseReference getChatroomRef(FirebaseDatabase fbdb, @Nullable String chatroomId){
        if(chatroomId== null){
            return fbdb.getReference(AppConstants.FIREBASE_CHATROOMS);
        }
        return fbdb.getReference(AppConstants.FIREBASE_CHATROOMS).child(chatroomId);
    }

    /**
     * Returns a reference to Root->Chatrooms->Chatroom->Messages
     *
     * @param fbdb
     * @param chatroomId
     * @return
     */
    public static DatabaseReference getChatroomMessagesRef (FirebaseDatabase fbdb, String chatroomId){
        //ToDo: Each message is going to need it's own directory within the Messages directory. This currently takes you to the parent "Messages" directory.
        return fbdb.getReference(AppConstants.FIREBASE_CHATROOMS).child(chatroomId).child(AppConstants.FIREBASE_CHATROOM_MESSSAGES);
    }

    public static DatabaseReference getChatroomChatroomNameRef(FirebaseDatabase fbdb, String roomId){
        return fbdb.getReference(AppConstants.FIREBASE_CHATROOMS).child(roomId).child(AppConstants.FIREBASE_USERS).child("roomName");

    }

}
