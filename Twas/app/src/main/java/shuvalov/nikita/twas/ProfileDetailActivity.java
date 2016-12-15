package shuvalov.nikita.twas;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileDetailActivity extends AppCompatActivity {

    TextView mNameText, mBioText, mDOBTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        findViews();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String mId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        DatabaseReference myRef;
        if (mId.equals(MainActivity.HTC_PHONE_ANDROID_ID)){
            myRef = firebaseDatabase.getReference(MainActivity.SAMSUNG_PHONE_ANDROID_ID);
        }else{
            myRef = firebaseDatabase.getReference(MainActivity.HTC_PHONE_ANDROID_ID);

        }
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String bio = dataSnapshot.child("bio").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String dob = dataSnapshot.child("dob").getValue().toString();
                Profile stalkedProfile = new Profile(name, bio, dob);
                Log.d("ProfileDetailActivity", "onDataChange: "+bio);
                mBioText.setText(stalkedProfile.getBio());
                mNameText.setText(stalkedProfile.getName());
                mDOBTest.setText(stalkedProfile.getDOB());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void findViews(){
        mBioText = (TextView)findViewById(R.id.bio_text);
        mNameText = (TextView)findViewById(R.id.name_text);
        mDOBTest = (TextView)findViewById(R.id.dob_text);
    }
}
