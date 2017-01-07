package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FireBaseStorageUtils;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.ChatRoom;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class ProfileDetailActivity extends AppCompatActivity {

    TextView mNameText, mBioText, mDOBText, mGenderText;
    ImageView mImageView;
    Toolbar mToolbar;
    FirebaseDatabase mFirebaseDatabase;
    String mSelfUserId;
    Profile mSelectedProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        mSelfUserId = SelfUserProfileUtils.getUserId(ProfileDetailActivity.this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        findViews();
        if(getIntent().getStringExtra(AppConstants.ORIGIN_ACTIVITY).equals(AppConstants.ORIGIN_MAIN)){
            mSelectedProfile = ConnectionsHelper.getInstance().getProfileByPosition(getIntent().getIntExtra(AppConstants.PREF_HELPER_POSITION,-1));
        }else{
            mSelectedProfile = ConnectionsSQLOpenHelper.getInstance(this).getConnectionById(getIntent().getStringExtra(AppConstants.PREF_ID));
            if(mSelectedProfile==null){
                Toast.makeText(this, "Couldn't load profile of sender", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        bindDataToViews(mSelectedProfile);


//        mChatInviteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Check if currentUser and selectedUser already have a chatroom together, to prevent making duplicates
//                DatabaseReference selfReference = FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,mSelfUserId);
//                selfReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Iterable<DataSnapshot> chatRoomsData = dataSnapshot.getChildren();
//                        boolean done = false;
//                        while(chatRoomsData.iterator().hasNext() && !done){
//                            ChatRoom chatRoom = chatRoomsData.iterator().next().getValue(ChatRoom.class);
//                            ArrayList<String> userIds = chatRoom.getUserIds();
//                            Log.d("Testing", "onDataChange: "+ userIds.get(0));
//                            Log.d("Testing", "onDataChange: "+ userIds.get(1));
//
//                            //If both user IDs are found in one of the chatrooms that means users already have an existing chatroom together.
//                            if((userIds.get(0).equals(mSelfUserId) && userIds.get(1).equals(mSelectedProfile.getUID()))||
//                                    (userIds.get(1).equals(mSelfUserId) && userIds.get(0).equals(mSelectedProfile.getUID()))) {
//                                done = true;
//                                Intent intent = new Intent(ProfileDetailActivity.this, ChatRoomActivity.class);
//                                intent.putExtra(AppConstants.ORIGIN_ACTIVITY, AppConstants.ORIGIN_PROFILE_DETAIL);
//                                intent.putExtra(AppConstants.PREF_CHATROOM, chatRoom.getId());
//                                startActivity(intent);
//                            }
//                        }
//                        if(!done) {
//                            DatabaseReference chatroomsRef = FirebaseDatabaseUtils.getChatroomRef(mFirebaseDatabase, null);
//
//                            DatabaseReference createdChatroom = chatroomsRef.push();
//
//                            Profile selfUser = SelfUserProfileUtils.getUsersInfoAsProfile(ProfileDetailActivity.this);
//
//
//                            ChatRoom chatroom = new ChatRoom();
//                            chatroom.setId(createdChatroom.getKey());
//                            chatroom.addUserToChatroom(selfUser.getUID());
//                            chatroom.addUserToChatroom(mSelectedProfile.getUID());
//                            createdChatroom.child(AppConstants.FIREBASE_USERS).setValue(chatroom);
//                            DatabaseReference messageReference = FirebaseDatabaseUtils.getChatroomMessagesRef(mFirebaseDatabase, chatroom.getId());
//                            long currentTime = Calendar.getInstance().getTimeInMillis();
//                            ChatMessage initialChatMessage = new ChatMessage(selfUser.getUID(), chatroom.getId(), String.format("%s has started this conversation", selfUser.getName()), currentTime);
//                            messageReference.push().setValue(initialChatMessage);
//
//                            //Adds a reference to the Chatroom to both members' profiles.
//                            FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, selfUser.getUID()).child(chatroom.getId()).setValue(chatroom);
//                            FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, mSelectedProfile.getUID()).child(chatroom.getId()).setValue(chatroom);
//
//                            //Adds the newly made chatroom and initial message to the local db.
//                            ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addChatRoom(chatroom);
//                            ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addMessage(initialChatMessage);
//
//                            Intent intent = new Intent(ProfileDetailActivity.this, ChatRoomActivity.class);
//                            intent.putExtra(AppConstants.ORIGIN_ACTIVITY, AppConstants.ORIGIN_PROFILE_DETAIL);
//                            intent.putExtra(AppConstants.PREF_CHATROOM, chatroom.getId());
//                            startActivity(intent);
//                        }
//
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//
//
//            }
//        });
    }

    public void findViews(){
        mBioText = (TextView)findViewById(R.id.bio_text);
        mNameText = (TextView)findViewById(R.id.name_text);
        mDOBText = (TextView)findViewById(R.id.age_text);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        mGenderText = (TextView)findViewById(R.id.gender_text);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImageView = (ImageView)findViewById(R.id.profile_image_view);
    }

    public void bindDataToViews(Profile profile){
        mBioText.setText(profile.getBio());

        String userName = profile.getName();
        mNameText.setText(userName);
        getSupportActionBar().setTitle(userName);


        long birthdateMillis = profile.getDOB();
        Calendar birthCal = Calendar.getInstance();
        birthCal.setTimeInMillis(birthdateMillis);
        int year = birthCal.get(Calendar.YEAR);
        int month = birthCal.get(Calendar.MONTH);
        int date = birthCal.get(Calendar.DATE);
        String dateAsString;

        if(month<10||date<10){
            String monthString;
            String dateString;
            if(month<10){
                monthString = 0+String.valueOf(month);
            }else{
                monthString = String.valueOf(month);
            }
            if (date < 10) {
                dateString = 0+ String.valueOf(date);
            }else{
                dateString = String.valueOf(date);
            }
            dateAsString = monthString+"/"+dateString+"/"+year;
        }else{
            dateAsString = String.valueOf(month)+"/"+date+"/"+year;
        }
        mDOBText.setText(dateAsString);



        StorageReference imageStoreRef = FireBaseStorageUtils.getProfilePicStorageRef(profile.getUID());
        imageStoreRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(ProfileDetailActivity.this)
                        .load(uri)
                        .into(mImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mImageView.setImageResource(R.drawable.shakespeare_modern_bard_post);
            }
        });

        mGenderText.setText(profile.getGender());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_detail_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.message_opt:
                startChat();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void startChat(){
        DatabaseReference selfReference = FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,mSelfUserId);

        selfReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> chatRoomsData = dataSnapshot.getChildren();
                boolean done = false;
                while (chatRoomsData.iterator().hasNext() && !done) {
                    ChatRoom chatRoom = chatRoomsData.iterator().next().getValue(ChatRoom.class);
                    ArrayList<String> userIds = chatRoom.getUserIds();
                    Log.d("Testing", "onDataChange: " + userIds.get(0));
                    Log.d("Testing", "onDataChange: " + userIds.get(1));

                    //If both user IDs are found in one of the chatrooms that means users already have an existing chatroom together.
                    if ((userIds.get(0).equals(mSelfUserId) && userIds.get(1).equals(mSelectedProfile.getUID())) ||
                            (userIds.get(1).equals(mSelfUserId) && userIds.get(0).equals(mSelectedProfile.getUID()))) {
                        done = true;
                        Intent intent = new Intent(ProfileDetailActivity.this, ChatRoomActivity.class);
                        intent.putExtra(AppConstants.ORIGIN_ACTIVITY, AppConstants.ORIGIN_PROFILE_DETAIL);
                        intent.putExtra(AppConstants.PREF_CHATROOM, chatRoom.getId());
                        startActivity(intent);
                    }
                }
                if (!done) {
                    DatabaseReference chatroomsRef = FirebaseDatabaseUtils.getChatroomRef(mFirebaseDatabase, null);

                    DatabaseReference createdChatroom = chatroomsRef.push();

                    Profile selfUser = SelfUserProfileUtils.getUsersInfoAsProfile(ProfileDetailActivity.this);


                    ChatRoom chatroom = new ChatRoom();
                    chatroom.setId(createdChatroom.getKey());
                    chatroom.addUserToChatroom(selfUser.getUID());
                    chatroom.addUserToChatroom(mSelectedProfile.getUID());
                    createdChatroom.child(AppConstants.FIREBASE_USERS).setValue(chatroom);
                    DatabaseReference messageReference = FirebaseDatabaseUtils.getChatroomMessagesRef(mFirebaseDatabase, chatroom.getId());
                    long currentTime = Calendar.getInstance().getTimeInMillis();
                    ChatMessage initialChatMessage = new ChatMessage(selfUser.getUID(), chatroom.getId(), String.format("%s has started this conversation", selfUser.getName()), currentTime);
                    messageReference.push().setValue(initialChatMessage);

                    //Adds a reference to the Chatroom to both members' profiles.
                    FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, selfUser.getUID()).child(chatroom.getId()).setValue(chatroom);
                    FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, mSelectedProfile.getUID()).child(chatroom.getId()).setValue(chatroom);

                    //Adds the newly made chatroom and initial message to the local db.
                    ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addChatRoom(chatroom);
                    ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addMessage(initialChatMessage);

                    Intent intent = new Intent(ProfileDetailActivity.this, ChatRoomActivity.class);
                    intent.putExtra(AppConstants.ORIGIN_ACTIVITY, AppConstants.ORIGIN_PROFILE_DETAIL);
                    intent.putExtra(AppConstants.PREF_CHATROOM, chatroom.getId());
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

