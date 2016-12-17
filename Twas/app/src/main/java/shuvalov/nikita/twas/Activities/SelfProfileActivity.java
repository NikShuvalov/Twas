package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.SelfProfileRecyclerAdapter;

public class SelfProfileActivity extends AppCompatActivity {
    ImageView mProfileImage;
    Button mAccessGallery, mTakeSelfie;

    public static final int PICK_IMAGE_REQUEST = 1;

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
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
                        break;
                    case R.id.selfie_button:
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
        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            Uri uri = data.getData();
            try {
                mProfileImage.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(),uri));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
