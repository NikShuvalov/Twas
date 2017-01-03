package shuvalov.nikita.twas.Helpers_Managers;

import java.util.ArrayList;

import shuvalov.nikita.twas.PoJos.ChatMessage;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatMessagesHelper {
    private ArrayList<ChatMessage> mChatMessages;

    private static ChatMessagesHelper sChatMessagesHelper;

    public static ChatMessagesHelper getInstance() {
        if (sChatMessagesHelper == null){
            sChatMessagesHelper = new ChatMessagesHelper();
        }
        return sChatMessagesHelper;
    }

    private ChatMessagesHelper(){
        mChatMessages = new ArrayList<>();
    }

    public void addChatMessage(ChatMessage chatMessage){
        mChatMessages.add(chatMessage);
    }

    public ArrayList<ChatMessage> getChatLog(){
        return mChatMessages;
    }

    public void cleanChatLog(String chatroomId) {
        //If the id of the chatroom we are entering is not the same as the id of the messages that are already inside then let's clear the list. Otherwise keep it.
        if (!mChatMessages.isEmpty()) {
            String id = mChatMessages.get(0).getRoomID();
            if (!chatroomId.equals(id)) {
                mChatMessages = new ArrayList<>();
            }
        }

    }
}
