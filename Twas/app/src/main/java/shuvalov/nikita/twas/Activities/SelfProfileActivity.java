package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class SelfProfileActivity extends AppCompatActivity {
    private ImageView mProfileImage;
    private Button mAccessGallery, mTakeSelfie;
    private FloatingActionButton mSubmit;
    private EditText mName, mBio;
    private EditText mDateEntry; //Placeholder, used for debugging.
    private boolean mUpdatedProfileImage = false;
    private Bitmap mChosenProfileImage;
    private Spinner mGenders, mDate, mMonth, mYear;
    private ArrayAdapter<CharSequence> mGenderAdapter, mDateAdapter, mMonthAdapter, mYearAdapter;

    private Profile mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_profile);

        findViews();
        loadSelfImage();
        initButtons();
        setSpinnerAdapters();
        loadCurrentValues();
    }

    public void loadCurrentValues(){
        mProfile = SelfUserProfileUtils.getUsersInfoAsProfile(this);
        mName.setText(mProfile.getName());
        mBio.setText(mProfile.getBio());

        if(mProfile.getDOB()!=0){
            long birthdateMillis = mProfile.getDOB();
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTimeInMillis(birthdateMillis);
            int year = birthCal.get(Calendar.YEAR);
            int month = birthCal.get(Calendar.MONTH);
            int date = birthCal.get(Calendar.DATE);
            String dateAsString;

            //ToDo: Remove this later probs. For now this snippet keeps the format consistent for birthdate.
            if(month<10){
                dateAsString = 0+String.valueOf(month)+date+year;
            }else{
                dateAsString = String.valueOf(month)+date+year;
            }
            mDateEntry.setText(dateAsString);
        }
    }

    public void loadSelfImage(){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference bucketRef = firebaseStorage.getReferenceFromUrl(AppConstants.FIREBASE_IMAGE_BUCKET);

        String id = getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE, MODE_PRIVATE).getString(AppConstants.PREF_ID, AppConstants.PREF_EMPTY);
        StorageReference storageRef = bucketRef.child(String.format(AppConstants.FIREBASE_USER_PROFILE_IMAGE, id));

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(SelfProfileActivity.this).load(uri).into(mProfileImage);//Loads, but takes a long time.
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProfileImage.setImageResource(R.drawable.shakespeare_modern_bard_post);//Default image if no image is found.
            }
        });
    }

    public void findViews(){
        mProfileImage = (ImageView)findViewById(R.id.profile_image_view);//Populate this if user already has a profile image.

        mAccessGallery = (Button)findViewById(R.id.upload_image_gallery);
        mTakeSelfie = (Button)findViewById(R.id.selfie_button);
        mSubmit = (FloatingActionButton) findViewById(R.id.submit_changes_button);

        mName = (EditText)findViewById(R.id.name_entry);
        mBio = (EditText)findViewById(R.id.about_me_entry);
        mGenders = (Spinner)findViewById(R.id.gender_select);
        mDate = (Spinner)findViewById(R.id.date_spinner);
        mMonth = (Spinner)findViewById(R.id.month_spinner);
        mYear = (Spinner)findViewById(R.id.year_spinner);

        mDateEntry = (EditText)findViewById(R.id.date_entry); //Used for debugging for now

    }

    public void initButtons(){
        View.OnClickListener imageClickerListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.upload_image_gallery:
                        Intent galleryIntent = new Intent();
                        galleryIntent.setType("image/*");
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(galleryIntent,"Select Picture"),AppConstants.PICK_IMAGE_REQUEST);
                        break;
                    case R.id.selfie_button:
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent,AppConstants.TAKE_IMAGE_REQUEST);
                        break;
                }
            }
        };

        mAccessGallery.setOnClickListener(imageClickerListener);
        mTakeSelfie.setOnClickListener(imageClickerListener);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mName.getText().toString();
                String bio = mBio.getText().toString();
                String gender = "Male";
                String dateOfBirth = mDateEntry.getText().toString();
                int month = Integer.parseInt(dateOfBirth.substring(0,2));
                int date = Integer.parseInt(dateOfBirth.substring(2,4));
                int year = Integer.parseInt(dateOfBirth.substring(4));

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, date);

                long birthInMillis = calendar.getTimeInMillis();

                mProfile.setName(name);
                mProfile.setBio(bio);
                mProfile.setGender(gender);
                mProfile.setDOB(birthInMillis);

                SelfUserProfileUtils.assignProfileToSharedPreferences(SelfProfileActivity.this, mProfile);
                if(mUpdatedProfileImage){
                    uploadProfileImage();
                }

                //ToDo: I might want to move my dataBase uploads and downloads to a helper class.
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dbRef = db.getReference(mProfile.getUID()).child(AppConstants.FIREBASE_USER_CHILD_PROFILE);
                dbRef.setValue(mProfile);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==AppConstants.PICK_IMAGE_REQUEST||requestCode==AppConstants.TAKE_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            Uri uri = data.getData();
            try {
                mChosenProfileImage = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                mProfileImage.setImageBitmap(mChosenProfileImage);
                mUpdatedProfileImage=true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Code retrieved from:
    // http://android-coding.blogspot.com/2011/06/reduce-bitmap-size-using.html
    // With minor changes
//    public Bitmap shrinkBitmap(byte[] imageData, double scaleDownRatio){
//        BitmapFactory.Options bitmapFactOP = new BitmapFactory.Options();
//        bitmapFactOP.inJustDecodeBounds= true;
//
//        bitmapFactOP.inSampleSize = (int)(100/scaleDownRatio);
//
//        bitmapFactOP.inJustDecodeBounds= false;
//
//        return BitmapFactory.decodeByteArray(imageData,0,imageData.length, bitmapFactOP);
//    }

    public void uploadProfileImage(){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference bucketRef = firebaseStorage.getReferenceFromUrl(AppConstants.FIREBASE_IMAGE_BUCKET);

        final String userId = SelfUserProfileUtils.getUserId(SelfProfileActivity.this);
        StorageReference storageRef = bucketRef.child(String.format(AppConstants.FIREBASE_USER_PROFILE_IMAGE, userId));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        mChosenProfileImage.compress(Bitmap.CompressFormat.JPEG,100, byteArrayOutputStream);

        //Checks to see if the imageSize is larger than the firebase limit. If so, it scales the image down to an allowed amount.
        long imageSize = byteArrayOutputStream.toByteArray().length;
        byte[] imgStream = byteArrayOutputStream.toByteArray();
        if(imageSize> AppConstants.FIREBASE_MAX_PHOTO_SIZE){
            Toast.makeText(this, "Image file too large", Toast.LENGTH_SHORT).show();

//                    double scaledDownRatio = (double)AppConstants.FIREBASE_MAX_PHOTO_SIZE/(double)imageSize;
//                    int scaledQuality = (int)(scaledDownRatio*100)-1;
//                    chosenProfileImage = shrinkBitmap(imgStream, scaledDownRatio);

//                    chosenProfileImage.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
//                    imgStream = byteArrayOutputStream.toByteArray();
        }else{
            imgStream = byteArrayOutputStream.toByteArray();
        }

        Log.d("boas compressed image", "Size of photo "+byteArrayOutputStream.toByteArray().length);

        UploadTask uploadTask = storageRef.putBytes(imgStream);
        uploadTask.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SelfProfileActivity.this, "Failed to upload picture", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(SelfProfileActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void setSpinnerAdapters(){
        mGenderAdapter = ArrayAdapter.createFromResource(this,R.array.genders, android.R.layout.simple_spinner_item);
        mGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenders.setAdapter(mGenderAdapter);
        mGenders.setSelection(0); //ToDo: Selection should be based off of sharedPreferences values.

        mDateAdapter = ArrayAdapter.createFromResource(this,R.array.genders, android.R.layout.simple_spinner_item);
        mDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDate.setAdapter(mDateAdapter);
        mDate.setSelection(0);//ToDo: Current Selection should be based off of sharedPreferences value, ALSO a selector needed depending on which month it is?

        mMonthAdapter = ArrayAdapter.createFromResource(this,R.array.months, android.R.layout.simple_spinner_item);
        mMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMonth.setAdapter(mMonthAdapter);
        mMonth.setSelection(0); //ToDo: Current Selection should be based of sharedpref

        mYearAdapter = ArrayAdapter.createFromResource(this,R.array.genders, android.R.layout.simple_spinner_item);
        mYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYear.setAdapter(mYearAdapter);
        mYear.setSelection(0); //ToDo: Current selection should be based of sharedPref

    }
}
