package shuvalov.nikita.twas.RecyclersAndHolders;


import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import shuvalov.nikita.twas.Helpers_Managers.FireBaseStorageUtils;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

/**
 * Created by NikitaShuvalov on 12/22/16.
 */

public class ProfileCollectionRecyclerAdapter extends RecyclerView.Adapter<ProfileCollectionViewHolder> {
    private List<Profile> mProfileList;

    public ProfileCollectionRecyclerAdapter(List<Profile> profileList) {
        mProfileList = profileList;
    }

    @Override
    public ProfileCollectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_profile,null);
        return new ProfileCollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ProfileCollectionViewHolder holder, int position) {
        Profile profile = mProfileList.get(position);
        holder.bindProfileDataToViews(profile);
        StorageReference imageStoreRef = FireBaseStorageUtils.getProfilePicStorageRef(profile.getUID());
        imageStoreRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(holder.mPicView.getContext()).load(uri).into(holder.mPicView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProfileList.size();
    }
}
