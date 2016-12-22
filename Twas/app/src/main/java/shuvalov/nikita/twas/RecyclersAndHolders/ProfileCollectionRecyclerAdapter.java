package shuvalov.nikita.twas.RecyclersAndHolders;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

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
    public void onBindViewHolder(ProfileCollectionViewHolder holder, int position) {
        holder.bindProfileDataToViews(mProfileList.get(position));
    }

    @Override
    public int getItemCount() {
        return mProfileList.size();
    }
}
