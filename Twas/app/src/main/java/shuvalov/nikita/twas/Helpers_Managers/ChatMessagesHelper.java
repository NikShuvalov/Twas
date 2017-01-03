package shuvalov.nikita.twas.Helpers_Managers;

import java.util.ArrayList;

import shuvalov.nikita.twas.PoJos.ChatMessage;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class ChatMessagesHelper {
    private ArrayList<ChatMessage> mChatMessages;
    private ArrayList<String> mChatMakeShiftString;

    private static ChatMessagesHelper sChatMessagesHelper;

    public static ChatMessagesHelper getInstance() {
        if (sChatMessagesHelper == null){
            sChatMessagesHelper = new ChatMessagesHelper();
        }
        return sChatMessagesHelper;
    }

    private ChatMessagesHelper(){
        mChatMessages = new ArrayList<>();
        mChatMakeShiftString = new ArrayList<>();
    }

    public void addChatMessage(ChatMessage chatMessage){
        mChatMessages.add(chatMessage);
    }

    public ArrayList<ChatMessage> getChatLog(){
        return mChatMessages;
    }

    public void addMakeShiftMessage(String message){
        mChatMakeShiftString.add(message);
    }
    public ArrayList<String> getMakeShiftChatLog(){
        return mChatMakeShiftString;
    }

}
