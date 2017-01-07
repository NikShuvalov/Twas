package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class SelfProfileActivity extends AppCompatActivity {
    private ImageView mProfileImage;
    private Button mAccessGallery, mTakeSelfie, mSoapBoxUpdateButt, mUpdateBirthday;
//    private FloatingActionButton mSubmit;
    private EditText mName, mBio, mSoapBoxMessage, mBirthdayEntry, mBirthYearEntry;
//    private EditText mDateEntry; //Placeholder, used for debugging.
    private boolean mUpdatedProfileImage = false;
    private Bitmap mChosenProfileImage, mProfileIconImage;
    private Spinner  mMonthSpinner, mGenderSpinner;
    private ArrayAdapter<CharSequence> mMonthAdapter, mGenderAdapter;
    private Toolbar mToolbar;
    private Profile mProfile;
    private int mBirthYear, mBirthMonth, mBirthDate;
    private int mBirthMonthSelected;
    private String mGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_profile);


        findViews();
        loadSelfImage();
        initButtons();
        loadCurrentValues();
        setSpinnerAdapters();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void loadCurrentValues(){
        mProfile = SelfUserProfileUtils.getUsersInfoAsProfile(this);
        mName.setText(mProfile.getName());
        mBio.setText(mProfile.getBio());
        mSoapBoxMessage.setText(SelfUserProfileUtils.getSoapBoxMessage(this));

        mBirthMonth=0;
        mBirthYear=-1;
        mBirthDate=-1;

        if(mProfile.getDOB()!=0){
            long birthdateMillis = mProfile.getDOB();
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTimeInMillis(birthdateMillis);
            mBirthYear = birthCal.get(Calendar.YEAR);
            mBirthMonth = birthCal.get(Calendar.MONTH);
            mBirthDate = birthCal.get(Calendar.DATE);

            mBirthYearEntry.setText(String.valueOf(mBirthYear));
            mBirthdayEntry.setText(String.valueOf(mBirthDate));
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
                Picasso.with(SelfProfileActivity.this).load(uri).into(mProfileImage);
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

        mGenderSpinner = (Spinner)findViewById(R.id.gender_select);
        mUpdateBirthday = (Button)findViewById(R.id.submit_birthday);
        mAccessGallery = (Button)findViewById(R.id.upload_image_gallery);
        mTakeSelfie = (Button)findViewById(R.id.selfie_button);
//        mSubmit = (FloatingActionButton) findViewById(R.id.submit_changes_button);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mName = (EditText)findViewById(R.id.name_entry);
        mBio = (EditText)findViewById(R.id.about_me_entry);

        mMonthSpinner = (Spinner)findViewById(R.id.month_spinner);
        mBirthdayEntry = (EditText)findViewById(R.id.birth_date_entry);
        mBirthYearEntry = (EditText)findViewById(R.id.birth_year_entry);

//        mDateEntry = (EditText)findViewById(R.id.date_entry); //Used for debugging for now

        mSoapBoxMessage = (EditText)findViewById(R.id.soapbox_status_entry);
        mSoapBoxUpdateButt = (Button)findViewById(R.id.update_soapbox_message);

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

        mSoapBoxUpdateButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String soapBoxString = mSoapBoxMessage.getText().toString();
                if(soapBoxString.isEmpty()){
                    Toast.makeText(SelfProfileActivity.this, "SoapBox Message was emptied", Toast.LENGTH_SHORT).show();
                }
                SelfUserProfileUtils.setNewSoapBoxMessage(SelfProfileActivity.this,soapBoxString);
                String selfId = SelfUserProfileUtils.getUserId(SelfProfileActivity.this);
                long timeStamp =SelfUserProfileUtils.getSoapBoxTimeStamp(SelfProfileActivity.this);
                ChatMessage soapBoxMessage = new ChatMessage(selfId,null,soapBoxString,timeStamp);
                ConnectionsSQLOpenHelper.getInstance(SelfProfileActivity.this).addSoapBoxMessage(soapBoxMessage);
            }
        });

        //FixMe: This only updates the changes locally, you still need to hit the submit button to actually sent out the new information to fbdb.
        mUpdateBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validDate = true;
                switch(mBirthMonthSelected){
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                    case 8:
                    case 10:
                    case 12:
                        if(Integer.parseInt(mBirthdayEntry.getText().toString())>31){
                            mBirthdayEntry.setError("Invalid Date");
                            validDate=false;
                        }
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        if(Integer.parseInt(mBirthdayEntry.getText().toString())>30){
                            mBirthdayEntry.setError("Invalid Date");
                            validDate=false;
                        }
                        break;
                    case 2:
                        int year = Integer.parseInt(mBirthYearEntry.getText().toString());
                        int date = Integer.parseInt(mBirthdayEntry.getText().toString());
                        if(date>=29){
                            if(!(date==29 && checkLeapYear(year))){
                                mBirthdayEntry.setError("Invalid Date");
                                validDate=false;
                            }
                        }
                        break;
                    default:
                        Toast.makeText(SelfProfileActivity.this, "No month selected", Toast.LENGTH_SHORT).show();
                        validDate=false;
                }
                if(validDate){
                    Calendar birthCalendar = Calendar.getInstance();
                    int year = Integer.parseInt(mBirthYearEntry.getText().toString());
                    int date = Integer.parseInt(mBirthdayEntry.getText().toString());
                    birthCalendar.set(year,mBirthMonth,date);
                    long dob = birthCalendar.getTimeInMillis();
                    mProfile.setDOB(dob);
                }
            }
        });
//        mSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                submitChanges();
//            }
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode==AppConstants.PICK_IMAGE_REQUEST||requestCode==AppConstants.TAKE_IMAGE_REQUEST) && (resultCode==RESULT_OK && data!=null && data.getData()!=null)){
            Uri uri = data.getData();
            try {
                mProfileIconImage = mChosenProfileImage = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);

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
        byte[] imgStream;
        Log.d("Image", "Resized image size "+imageSize);

        if(imageSize> AppConstants.FIREBASE_MAX_PHOTO_SIZE){
            Toast.makeText(this, "Image shrunk due to large size", Toast.LENGTH_LONG).show();
            //ToDo: This will make it smaller.

            //ToDo: try to keep actual aspect ratio.
            double aspectRatio = mChosenProfileImage.getHeight()/mChosenProfileImage.getWidth();

            Log.d("Image", "Height: "+mChosenProfileImage.getHeight()+ "Width: "+mChosenProfileImage.getWidth());
            int height  = 768;
            int width= 1024;

            if(aspectRatio>1.0){
                height = 1024;
                width = 768;
            }

            mChosenProfileImage = Bitmap.createScaledBitmap(mChosenProfileImage, width,
                    height, true);

            byteArrayOutputStream = new ByteArrayOutputStream();

            mChosenProfileImage.compress(Bitmap.CompressFormat.JPEG,100, byteArrayOutputStream);

            //Checks to see if the imageSize is larger than the firebase limit. If so, it scales the image down to an allowed amount.
            imageSize = byteArrayOutputStream.toByteArray().length;
            imgStream = byteArrayOutputStream.toByteArray();
            Log.d("Image", "Resized image size "+imageSize);

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
                updateImageIconFile();
            }
        });
    }
    public void setSpinnerAdapters(){
        mGenderAdapter = ArrayAdapter.createFromResource(this,R.array.genders, android.R.layout.simple_spinner_item);
        mGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenderSpinner.setAdapter(mGenderAdapter);
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mGender=(String)adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mGender = SelfUserProfileUtils.getUsersInfoAsProfile(this).getGender();
        int selection=0;
        String[] genders = getResources().getStringArray(R.array.genders);
        for (int i = 0; i<genders.length;i++){
            if(genders[i].equals(mGender)){
                selection=i;
            }
        }

        mGenderSpinner.setSelection(selection); //ToDo: Selection should be based off of sharedPreferences values.

        mMonthAdapter = ArrayAdapter.createFromResource(this,R.array.months, android.R.layout.simple_spinner_item);
        mMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMonthSpinner.setAdapter(mMonthAdapter);
        mMonthSpinner.setSelection(mBirthMonth);
        mMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mBirthMonthSelected = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuinflater = getMenuInflater();
        menuinflater.inflate(R.menu.selfprofile_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.submit_changes:
                submitChanges();
        }
        return super.onOptionsItemSelected(item);
    }
    public void submitChanges(){
        String name = mName.getText().toString();
        String bio = mBio.getText().toString();

//                if(!mDateEntry.getText().toString().isEmpty()) {
//                    String dateOfBirth = mDateEntry.getText().toString();
//                    int month = Integer.parseInt(dateOfBirth.substring(0, 2));
//                    int date = Integer.parseInt(dateOfBirth.substring(2, 4));
//                    int year = Integer.parseInt(dateOfBirth.substring(4));
//
//                    Calendar calendar = Calendar.getInstance();
//                    calendar.set(year, month, date);
//
//                    long birthInMillis = calendar.getTimeInMillis();
//                    mProfile.setDOB(birthInMillis);
//                }

        mProfile.setName(name);
        mProfile.setBio(bio);
        mProfile.setGender(mGender);

        SelfUserProfileUtils.assignProfileToSharedPreferences(SelfProfileActivity.this, mProfile);
        if(mUpdatedProfileImage){
            uploadProfileImage();
        }

        //ToDo: I might want to move my dataBase uploads and downloads to a helper class.
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = FirebaseDatabaseUtils.getUserProfileRef(db, mProfile.getUID());
//                DatabaseReference dbRef = db.getReference(mProfile.getUID()).child(AppConstants.FIREBASE_USER_CHILD_PROFILE);
        dbRef.setValue(mProfile);
    }


    public boolean checkLeapYear(int year){
        if(year%4!=0){
            return false;
        }else if (year%100==0 && year%400!=0){
            return false;
        }
        return true;
    }

    //http://stackoverflow.com/questions/8471226/how-to-resize-image-bitmap-to-a-given-size
//    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
//                                   boolean filter) {
//        float ratio = Math.min(
//                (float) maxImageSize / realImage.getWidth(),
//                (float) maxImageSize / realImage.getHeight());
//        int width = Math.round((float) ratio * realImage.getWidth());
//        int height = Math.round((float) ratio * realImage.getHeight());
//
//        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
//                height, filter);
//        return newBitmap;
//    }
    public void updateImageIconFile(){

        ByteArrayOutputStream boas = new ByteArrayOutputStream();

        //FixMe: Dimensions might need to be adjusted for the profileIconImage.
        mProfileIconImage = Bitmap.createScaledBitmap(mProfileIconImage, 350,
                350, true);
        mProfileIconImage.compress(Bitmap.CompressFormat.JPEG,2, boas);
        byte[] iconBytes = boas.toByteArray();
        Log.d("IconImage", "IconFileSize: "+iconBytes.length);
        SelfUserProfileUtils.setProfileIconImageFile(this,iconBytes);
    }
}
