package shuvalov.nikita.twas.PoJos;

import android.support.annotation.Nullable;

/**
 * Created by NikitaShuvalov on 12/20/16.
 */

public class ChatRoom {
    String mId, mRoomName; //Room ID is a unique identifier for the room, room name is the nickname for the room.

    public static final String DEFAULT_NAME = "Generic Room"; //I want to use a random name generator. Just have to figure out the words to use.

    public ChatRoom(String id, @Nullable String roomName) {
        mId = id;
        if(roomName!=null){
            mRoomName = roomName;
        }else{
            mRoomName=DEFAULT_NAME;
        }
    }

    public String getId() {
        return mId;
    }

    public String getRoomName() {
        return mRoomName;
    }
}
