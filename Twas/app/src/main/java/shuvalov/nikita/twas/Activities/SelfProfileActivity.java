package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.R;

public class SelfProfileActivity extends AppCompatActivity {
    private ImageView mProfileImage;
    private Button mAccessGallery, mTakeSelfie;
    private FloatingActionButton mSubmit;
    private EditText mName, mBio, mHobbies;

    private ArrayList<EditText> mPromptFields;

    public static final int PICK_IMAGE_REQUEST = 1; //ToDo: Move to appConstants and update where necessary.
    public static final int TAKE_IMAGE_REQUEST = 2; //ToDo: Same

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_profile);

        mPromptFields = new ArrayList<>();

        findViews();
        loadSelfImage();
        initButtons();
    }

    public void loadSelfImage(){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference bucketRef = firebaseStorage.getReferenceFromUrl(AppConstants.FIREBASE_IMAGE_BUCKET);

        String id = NearbyManager.getInstance().getSelfID();
        StorageReference storageRef = bucketRef.child(String.format(AppConstants.FIREBASE_USER_PROFILE_IMAGE, id));

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(SelfProfileActivity.this).load(uri).into(mProfileImage);//Loads, but takes a long time.
            }

        });
    }

    public void findViews(){
        mProfileImage = (ImageView)findViewById(R.id.profile_image_view);//Populate this if user already has a profile image.

        mAccessGallery = (Button)findViewById(R.id.upload_image_gallery);
        mTakeSelfie = (Button)findViewById(R.id.selfie_button);
        mSubmit = (FloatingActionButton) findViewById(R.id.submit_changes_button);

        mPromptFields.add(mName = (EditText)findViewById(R.id.name_entry));
        mPromptFields.add(mBio = (EditText)findViewById(R.id.about_me_entry));
        mPromptFields.add(mHobbies = (EditText)findViewById(R.id.hobbies_entry));
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
                        startActivityForResult(Intent.createChooser(galleryIntent,"Select Picture"),PICK_IMAGE_REQUEST);
                        break;
                    case R.id.selfie_button:
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent,TAKE_IMAGE_REQUEST);
                        break;
                }
            }
        };

        mAccessGallery.setOnClickListener(imageClickerListener);
        mTakeSelfie.setOnClickListener(imageClickerListener);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(EditText editText: mPromptFields){
                    if(!editText.getText().toString().isEmpty()){//Only updates changes where there are values to update.
                        switch (editText.getId()){
                            case R.id.name_entry:
                                //ToDo: Update Self Name
                                break;
                            case R.id.about_me_entry:
                                //ToDo: Update bio
                                break;
                            case R.id.hobbies_entry:
                                //ToDo: Update Hobbies
                                break;
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST||requestCode==TAKE_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            Uri uri = data.getData();
            try {
                Bitmap chosenProfileImage = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);

                mProfileImage.setImageBitmap(chosenProfileImage);

                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference bucketRef = firebaseStorage.getReferenceFromUrl(AppConstants.FIREBASE_IMAGE_BUCKET);

                String id = NearbyManager.getInstance().getSelfID();
                StorageReference storageRef = bucketRef.child(String.format(AppConstants.FIREBASE_USER_PROFILE_IMAGE, id));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                chosenProfileImage.compress(Bitmap.CompressFormat.JPEG,100, byteArrayOutputStream);

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
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        //ToDo: Store DownloadURL, in FBDB as Profile parameter and in local SQL database.
                    }
                });
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

}
