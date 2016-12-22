package shuvalov.nikita.twas.RecyclersAndHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 12/22/16.
 */

public class ProfileCollectionViewHolder extends RecyclerView.ViewHolder {
    ImageView mPicView;
    TextView mNameView;


    public ProfileCollectionViewHolder(View itemView) {
        super(itemView);

        mPicView = (ImageView) itemView.findViewById(R.id.profile_image_view);
        mNameView = (TextView)itemView.findViewById(R.id.name_text);

    }

    public void bindProfileDataToViews(Profile profile){
        //ToDo: I really should separate the logic for pulling images from Firebase storage so that I can bind data like here.
        mPicView.setImageResource(R.drawable.shakespeare_modern_bard_post); //ToDo: Replace with actual image file.
        mNameView.setText(profile.getName());
    }
}
