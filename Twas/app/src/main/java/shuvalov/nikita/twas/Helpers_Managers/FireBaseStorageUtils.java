package shuvalov.nikita.twas.Helpers_Managers;

import android.net.Uri;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import shuvalov.nikita.twas.AppConstants;

/**
 * Created by NikitaShuvalov on 12/22/16.
 */

public class FireBaseStorageUtils {

    public static StorageReference getProfilePicStorageRef(String uid) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference bucketRef = firebaseStorage.getReferenceFromUrl(AppConstants.FIREBASE_IMAGE_BUCKET);
        return bucketRef.child(String.format(AppConstants.FIREBASE_USER_PROFILE_IMAGE, uid));
    }
}

