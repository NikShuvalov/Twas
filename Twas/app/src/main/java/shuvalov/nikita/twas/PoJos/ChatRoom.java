package shuvalov.nikita.twas.PoJos;

import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by NikitaShuvalov on 12/20/16.
 */

public class ChatRoom {
    String mId, mRoomName; //Room ID is a unique identifier for the room, room name is the nickname for the room.
    ArrayList<String> mUserIds;

    public static final String DEFAULT_NAME = "Generic Room"; //I want to use a random name generator. Just have to figure out the words to use.

    public ChatRoom(String id, @Nullable String roomName) {
        mId = id;
        mUserIds = new ArrayList<>();
        if(roomName!=null){
            mRoomName = roomName;
        }else{
            mRoomName=DEFAULT_NAME;
        }
    }
    public ChatRoom(){
        mUserIds = new ArrayList<>();
        mRoomName = DEFAULT_NAME;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setRoomName(String roomName) {
        mRoomName = roomName;
    }

    public String getId() {
        return mId;
    }

    public String getRoomName() {
        return mRoomName;
    }

    public void setUserIds(ArrayList<String> userIds) {
        mUserIds = userIds;
    }

    public void addUserToChatroom(String uid){
        mUserIds.add(uid);
    }

    public ArrayList<String> getUserIds() {
        return mUserIds;
    }
}
