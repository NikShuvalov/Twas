package shuvalov.nikita.twas.Helpers_Managers;

import java.util.ArrayList;

import shuvalov.nikita.twas.PoJos.ChatRoom;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatRoomsHelper {

    private ArrayList<ChatRoom> mChatRooms;

    private static ChatRoomsHelper sChatRoomsHelper;

    public static ChatRoomsHelper getInstance() {
        if(sChatRoomsHelper== null) {
            sChatRoomsHelper = new ChatRoomsHelper();
        }
        return sChatRoomsHelper;
    }

    private ChatRoomsHelper(){
        mChatRooms = new ArrayList<>();
    }

    public void addChatRoom(ChatRoom chatRoom){
        mChatRooms.add(chatRoom);
    }
    public ArrayList<ChatRoom> getChatRooms(){
        return mChatRooms;
    }
    public ChatRoom getChatRoomAtPosition(int position){
        return mChatRooms.get(position);
    }


    public int getNumberOfChatrooms(){
        return mChatRooms.size();
    }

}
