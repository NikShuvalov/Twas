package shuvalov.nikita.twas.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import shuvalov.nikita.twas.R;

public class ChatRoomActivity extends AppCompatActivity {
    private Button mSendButton;
    private EditText mMessageEntry;
    private RecyclerView mMessageRecycler;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        findViews();
        recyclerLogic();
        onClickLogic();
        /* This is going to display all of the chatmessages that belong to this chatRoom in a recyclerView.
        User can also send new messages from this activity.
        User can arrive to this activity either by clicking on a chatroom in their chatroom list or
        User can Arrive to this activity upon creation of a new chatroom.
        (Optional) User can invite additional users to the chatroom.
         */
    }

    public void findViews(){
        mSendButton = (Button)findViewById(R.id.send_butt);
        mMessageEntry = (EditText)findViewById(R.id.message_entry);
        mMessageRecycler = (RecyclerView)findViewById(R.id.message_recycler);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatRoom Name or Other UserName");
    }

    public void recyclerLogic(){

    }
    public void onClickLogic(){
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* 1. Check if message empty/get message in edittext.
                 2. clear edittext
                 3. Create new chatMessage.
                 3. Send chatMessage to chatroom fbdb.
                 4. Update locally.
                 5. Display new chatMessage to recycler.
                 6. Inform user somehow that message was sent, perhaps with one of the previous steps.
                 */
            }
        });
    }
}
