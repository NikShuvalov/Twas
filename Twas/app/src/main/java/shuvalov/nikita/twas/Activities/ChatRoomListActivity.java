package shuvalov.nikita.twas.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import shuvalov.nikita.twas.R;

public class ChatRoomListActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);

        findViews();

        /*
        This will display all of the chatrooms that the user belongs to.
        User can select a chatroom to view all messages in the chatroom.
        (Optional) User can create a new chatroom
         */
    }

    public void findViews(){
        mRecyclerView = (RecyclerView)findViewById(R.id.chatroom_list_recycler);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Chatrooms");
    }
}
