package shuvalov.nikita.twas.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.RecyclersAndHolders.ChatMessagesRecyclerAdapter;

public class SoapBoxFeedActivity extends AppCompatActivity {
    private Button mSend;
    private EditText mMessageEntry;
    private RecyclerView mRecyclerView;
    private ChatMessagesRecyclerAdapter mMessagesRecyclerAdapter;
    String mSelfId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap_box_feed);

        mSelfId = SelfUserProfileUtils.getUserId(this);
        findViews();
        recyclerLogic();

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMessageEntry.getText().toString().equals("")){
                    mMessageEntry.setError("Can't send an empty message");
                }else{
                    String shoutOut = mMessageEntry.getText().toString();
                    mMessageEntry.setText("");

                    ChatMessage soapBoxMessage = new ChatMessage(mSelfId, shoutOut);
                    byte[] soapBoxBytes = ChatMessage.getBytesForSoapBox(soapBoxMessage); //ToDo: Publish this! Blast it out for everyone near by to hear.

                }
            }
        });
    }

    public void findViews(){
        mSend = (Button)findViewById(R.id.send_butt);
        mMessageEntry = (EditText)findViewById(R.id.message_entry);
        mRecyclerView = (RecyclerView)findViewById(R.id.soapbox_feed_recycler);
    }

    public void recyclerLogic(){
        mMessagesRecyclerAdapter = new ChatMessagesRecyclerAdapter(ConnectionsSQLOpenHelper.getInstance(this).getSoapBoxMessages(),mSelfId);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);

        mRecyclerView.setAdapter(mMessagesRecyclerAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }
}
