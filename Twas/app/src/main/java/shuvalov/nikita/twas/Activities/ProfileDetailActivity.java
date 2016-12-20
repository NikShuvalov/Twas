package shuvalov.nikita.twas.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class ProfileDetailActivity extends AppCompatActivity {

    TextView mNameText, mBioText, mDOBTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        findViews();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String foundProfile = getIntent().getStringExtra(MainActivity.FOUND_ID_INTENT); //Get the id that was passed in the intent to find it in the firebase database.
        DatabaseReference myRef =  firebaseDatabase.getReference(foundProfile);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String bio = dataSnapshot.child("bio").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String dob = dataSnapshot.child("dob").getValue().toString();
//                Profile stalkedProfile = new Profile(name, bio, dob, null, null);
                Log.d("ProfileDetailActivity", "onDataChange: "+bio);
//                bindDataToViews(stalkedProfile);
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

    public void bindDataToViews(Profile profile){
        mBioText.setText(profile.getBio());
        mNameText.setText(profile.getName());
//        mDOBTest.setText(profile.getDOB());
    }
}
