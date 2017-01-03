package shuvalov.nikita.twas.Activities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;

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

    TextView mNameText, mBioText, mDOBText;
    ImageView mImageView;
    Toolbar mToolbar;
    Button mChatInviteButton;
    FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        findViews();
        final Profile selectedProfile = ConnectionsHelper.getInstance().getProfileByPosition(getIntent().getIntExtra("Position in singleton",-1));
        bindDataToViews(selectedProfile);


        mChatInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToDo: Check if currentUser and selectedUser already have a chatroom together, to prevent making duplicates

                DatabaseReference chatroomsRef = FirebaseDatabaseUtils.getChatroomRef(mFirebaseDatabase, null);

                DatabaseReference createdChatroom = chatroomsRef.push();

                Profile selfUser = SelfUserProfileUtils.getUsersInfoAsProfile(ProfileDetailActivity.this);
//                HashMap<String, String> users = new HashMap<String, String>();
//
//                users.put(selectedProfile.getUID(),selectedProfile.getName());
//                users.put(selfUser.getUID(), selfUser.getName());

                ChatRoom chatroom = new ChatRoom();
                chatroom.setId(createdChatroom.getKey());
                chatroom.addUserToChatroom(selfUser.getUID());
                chatroom.addUserToChatroom(selectedProfile.getUID());
                createdChatroom.child(AppConstants.FIREBASE_USERS).setValue(chatroom);
                DatabaseReference messageReference = FirebaseDatabaseUtils.getChatroomMessagesRef(mFirebaseDatabase,chatroom.getId());
                long currentTime = Calendar.getInstance().getTimeInMillis();
                ChatMessage initialChatMessage = new ChatMessage(selfUser.getUID(),chatroom.getId(), String.format("%s has started this conversation",selfUser.getName()),currentTime);
                messageReference.push().setValue(initialChatMessage);

                //Adds a reference to the Chatroom to both members' profiles.
                FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, selfUser.getUID()).child(chatroom.getId()).setValue(chatroom);
                FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, selectedProfile.getUID()).child(chatroom.getId()).setValue(chatroom);

                //Adds the newly made chatroom and initial message to the local db.
                ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addChatRoom(chatroom);
                ConnectionsSQLOpenHelper.getInstance(ProfileDetailActivity.this).addMessage(initialChatMessage);
            }
        });
    }

    public void findViews(){
        mBioText = (TextView)findViewById(R.id.bio_text);
        mNameText = (TextView)findViewById(R.id.name_text);
        mDOBText = (TextView)findViewById(R.id.dob_text);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImageView = (ImageView)findViewById(R.id.profile_image_view);
        mChatInviteButton = (Button)findViewById(R.id.chat_invite_button);
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
            dateAsString = monthString+dateString+year;
        }else{
            dateAsString = String.valueOf(month)+date+year;
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
        mChatInviteButton.setText(String.format("Invite %s to chat", profile.getName()));
    }
}
