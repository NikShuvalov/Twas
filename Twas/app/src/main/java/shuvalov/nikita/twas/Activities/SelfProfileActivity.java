package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.SelfProfileRecyclerAdapter;

public class SelfProfileActivity extends AppCompatActivity {
    ImageView mProfileImage;
    Button mAccessGallery, mTakeSelfie;

    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int TAKE_IMAGE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_profile);

        findViews();
        initButtons();


    }
    public void findViews(){
        mProfileImage = (ImageView)findViewById(R.id.profile_image_view);
        mAccessGallery = (Button)findViewById(R.id.upload_image_gallery);
        mTakeSelfie = (Button)findViewById(R.id.selfie_button);
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
                    default:
                        Toast.makeText(SelfProfileActivity.this, "Gosh Darn't it", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mAccessGallery.setOnClickListener(imageClickerListener);
        mTakeSelfie.setOnClickListener(imageClickerListener);
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

                String id = NearbyManager.getInstance().getId();
                StorageReference storageRef = bucketRef.child(String.format(AppConstants.FIREBASE_USER_PROFILE_IMAGE, id));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                chosenProfileImage.compress(Bitmap.CompressFormat.JPEG,100, byteArrayOutputStream);
                byte[] imgStream = byteArrayOutputStream.toByteArray();

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
}
