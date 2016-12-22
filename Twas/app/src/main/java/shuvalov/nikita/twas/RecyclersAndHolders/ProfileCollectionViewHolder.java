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
        mPicView.setImageResource(R.drawable.shakespeare_modern_bard_post); //Sets default image until actual image is loaded.
        mNameView.setText(profile.getName());
    }
}
