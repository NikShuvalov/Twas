package shuvalov.nikita.twas.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class ProfileDetailActivity extends AppCompatActivity {

    TextView mNameText, mBioText, mDOBText;
    ImageView mImageView;

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

        mImageView = (ImageView)findViewById(R.id.profile_image_view);
    }

    public void bindDataToViews(Profile profile){
        mBioText.setText(profile.getBio());
        mNameText.setText(profile.getName());

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

        Picasso.with(this).load(profile.getUID()).into(mImageView);
    }
}
