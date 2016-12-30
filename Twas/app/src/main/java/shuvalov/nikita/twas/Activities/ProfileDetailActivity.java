package shuvalov.nikita.twas.Activities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.FireBaseStorageUtils;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class ProfileDetailActivity extends AppCompatActivity {

    TextView mNameText, mBioText, mDOBText;
    ImageView mImageView;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        findViews();
        Profile selectedProfile = ConnectionsHelper.getInstance().getProfileByPosition(getIntent().getIntExtra("Position in singleton",-1));
        bindDataToViews(selectedProfile);
    }

    public void findViews(){
        mBioText = (TextView)findViewById(R.id.bio_text);
        mNameText = (TextView)findViewById(R.id.name_text);
        mDOBText = (TextView)findViewById(R.id.dob_text);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

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
    }
}
