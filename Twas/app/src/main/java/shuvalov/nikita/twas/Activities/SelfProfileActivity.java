package shuvalov.nikita.twas.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class SelfProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {
    private ImageView mProfileImage;
    private Button mAccessGallery, mTakeSelfie, mSoapBoxUpdateButt, mUpdateBirthday;
    private EditText mName, mBio, mSoapBoxMessage, mBirthdayEntry, mBirthYearEntry;
    private boolean mUpdatedProfileImage = false;
    private Bitmap mChosenProfileImage, mProfileIconImage;
    private Spinner  mMonthSpinner, mGenderSpinner;
    private ArrayAdapter<CharSequence> mMonthAdapter, mGenderAdapter;
    private Toolbar mToolbar;
    private Profile mProfile;
    private int mBirthYear, mBirthMonth, mBirthDate;
    private int mBirthMonthSelected;
    private String mGender;
    private GoogleApiClient mGoogleApiClient;
    private NearbyManager mNearbyManager;
    private FirebaseDatabase mFirebaseDatabase;
    private MessageListener mActiveListener;
    private boolean mChangesMade, mGenderInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_profile);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mNearbyManager = NearbyManager.getInstance();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();



        findViews();
        loadSelfImage();
        initButtons();
        loadCurrentValues();
        setSpinnerAdapters();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mActiveListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                super.onFound(message);
                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if (!soapBoxMessage.getContent().equals("")) {
                    ConnectionsSQLOpenHelper.getInstance(SelfProfileActivity.this).addSoapBoxMessage(soapBoxMessage);
                }
                String mFoundId = soapBoxMessage.getUserId();


                //ToDo: Figure out what I can store as a value for the stranger Connections, maybe a counter?
                //Idea: If using a counter 0-10 encounters = Stranger, 11-25 Familiar, 26-50 Regular, 51-99 Acquaintance,100-499 Friendly, 500+ whatever
                FirebaseDatabaseUtils.getUserConnectionsRef(mFirebaseDatabase, SelfUserProfileUtils.getUserId(SelfProfileActivity.this)).child(mFoundId).setValue(mFoundId); //Adds stranger's UID to user's connectionsList.


                DatabaseReference strangerRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, mFoundId);

                //ToDo: Move this listener into a service. It should always be going.

                //Listens to ownChatrooms... I think.
                //ToDo: Double-check if still used, otherwise let's get rid of it.
//                FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase,SelfUserProfileUtils.getUserId(SelfProfileActivity.this))
//                        .addChildEventListener(new ChildEventListener() {
//                            @Override
//                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                                //This method should give a notification if a new chatroom and/or message is created.
//                            }
//
//                            @Override
//                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                            }
//
//                            @Override
//                            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                            }
//
//                            @Override
//                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
                //Gets the stranger's profile information.
                strangerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Profile strangerProfile = dataSnapshot.getValue(Profile.class);
                        if(strangerProfile!=null) {
                            ConnectionsSQLOpenHelper.getInstance(SelfProfileActivity.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
                            ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile); //Adds Stranger's info to Singleton.
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("MainActivity", "on heard signal, failed attempt to D/L profile ");
                    }
                });

                //ToDo: Do a count that adds found users, to keep track of active publishing users.
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
//                Toast.makeText(MainActivity.this, "Lost the signal", Toast.LENGTH_SHORT).show();

                //ToDo: Do a count that removes found users, to keep track of active publishing users.

            }

            @Override
            public void onDistanceChanged(Message message, Distance distance) {
                super.onDistanceChanged(message, distance);
            }

            @Override
            public void onBleSignalChanged(Message message, BleSignal bleSignal) {
                super.onBleSignalChanged(message, bleSignal);
                Log.d("Testing Shots fired", "Please clap");
            }
        };
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
        mChangesMade=false;
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
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextWatcher textWatcher = new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                mChangesMade=true;
            }
        };

        mName = (EditText)findViewById(R.id.name_entry);
        mName.addTextChangedListener(textWatcher);


        mBio = (EditText)findViewById(R.id.about_me_entry);
        mBio.addTextChangedListener(textWatcher);

        mMonthSpinner = (Spinner)findViewById(R.id.month_spinner);
        mBirthdayEntry = (EditText)findViewById(R.id.birth_date_entry);
        mBirthYearEntry = (EditText)findViewById(R.id.birth_year_entry);

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

        //FixMe: This only updates the changes locally, you still need to hit the submit button to actually send out the new information to fbdb.
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
                    mChangesMade=true;
                    Calendar birthCalendar = Calendar.getInstance();
                    int year = Integer.parseInt(mBirthYearEntry.getText().toString());
                    int date = Integer.parseInt(mBirthdayEntry.getText().toString());
                    birthCalendar.set(year,mBirthMonthSelected,date);
                    long dob = birthCalendar.getTimeInMillis();
                    mProfile.setDOB(dob);
                }
            }
        });
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
                finish();
            }
        });
    }

    public void setSpinnerAdapters(){
        mGenderAdapter = ArrayAdapter.createFromResource(this,R.array.genders, android.R.layout.simple_spinner_item);
        mGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenderSpinner.setAdapter(mGenderAdapter);
        mGender = SelfUserProfileUtils.getUsersInfoAsProfile(this).getGender();
        int selection=0;
        String[] genders = getResources().getStringArray(R.array.genders);
        for (int i = 0; i<genders.length;i++){
            if(genders[i].equals(mGender)){
                selection=i;
            }
        }

        mGenderSpinner.setSelection(selection);
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mGender=(String)adapterView.getItemAtPosition(i);
                if(!mGenderInit){
                    mGenderInit=true;
                }else{
                    mChangesMade=true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mMonthAdapter = ArrayAdapter.createFromResource(this,R.array.months, android.R.layout.simple_spinner_item);
        mMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMonthSpinner.setAdapter(mMonthAdapter);
        mMonthSpinner.setSelection(mBirthMonth-1);
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
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo!=null && networkInfo.isConnected()){
                    submitChanges();
                }else{
                    Toast.makeText(this, "No connection, can't save changes", Toast.LENGTH_SHORT).show();
                }
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void submitChanges(){
        String name = mName.getText().toString();
        String bio = mBio.getText().toString();

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
        dbRef.setValue(mProfile);
        mChangesMade=false;
    }

    public boolean checkLeapYear(int year){
        if(year%4!=0){
            return false;
        }else if (year%100==0 && year%400!=0){
            return false;
        }
        return true;
    }

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

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        publish();
        subscribe();
    }

    public void publish(){
        if(mGoogleApiClient.isConnected()){
            Nearby.Messages.publish(mGoogleApiClient, mNearbyManager.getActiveMessage());
            mNearbyManager.setPublishing(true);
        }else{
            Toast.makeText(this, "Not connected to Google Cloud", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "publish: failed");
        }
    }

    public void subscribe(){
        Nearby.Messages.subscribe(mGoogleApiClient, mActiveListener);
        mNearbyManager.setSubscribing(true);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onPause() {
        if(mNearbyManager.isPublishing()){
            Nearby.Messages.unpublish(mGoogleApiClient,mNearbyManager.getActiveMessage());
            mNearbyManager.setPublishing(false);
        }
        if(mNearbyManager.isSubscribing()){
            Nearby.Messages.unsubscribe(mGoogleApiClient,mActiveListener);
            mNearbyManager.setSubscribing(false);
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if((mChangesMade || mUpdatedProfileImage) && networkInfo!=null && networkInfo.isConnected()){
            new AlertDialog.Builder(this).setTitle("Hold on")
                    .setMessage("You made changes, but haven't saved them.")
                    .setPositiveButton("Submit Changes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            submitChanges();
                            if(!mUpdatedProfileImage){
                                finish();
                            }
                        }
                    })
                    .setNegativeButton("Dismiss changes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    }).create().show();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        mGenderInit=mChangesMade=false;
        super.onResume();
    }
}
