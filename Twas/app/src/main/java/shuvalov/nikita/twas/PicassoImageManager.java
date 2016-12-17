package shuvalov.nikita.twas;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

/**
 * Created by NikitaShuvalov on 12/2/16.
 */

public class PicassoImageManager {
    private Context mContext;
    private ImageView mImageView;
    private RequestCreator mRequestCreator;

    /**
     * The Manager takes in an imageview and its context to create a picassoImageManager object.
     * Then use the setImageFromUrl method to load an image from that url and updated the imageview with that image.
     *
     * Create a new picassoImageManager for each image.
     * //FixMe: Try to make the manager change parameters instead of having to make new manager for each call.
     * FixMe:... Only issue I can see with that is that if imageView and context are changed before image is loaded it would load the image into the wrong imageView
     *
     * @param context
     * @param imageView
     */


    public PicassoImageManager(Context context, ImageView imageView){
        mContext = context;
        mImageView = imageView;
    }

    /**
     * Takes in the url of image to be loaded.
     * @param imageUrl
     */
    public boolean setImageFromUrl(final String imageUrl) {
        if (!imageUrl.isEmpty()) {
            Picasso.with(mContext).load(imageUrl).into(mImageView);
            return true;
        }
        return false;
    }

}