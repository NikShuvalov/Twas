package shuvalov.nikita.twas.RecyclersAndHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
}
