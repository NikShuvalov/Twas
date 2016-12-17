package shuvalov.nikita.twas;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by NikitaShuvalov on 12/17/16.
 */

public class SelfProfileRecyclerAdapter extends RecyclerView.Adapter{
    ArrayList<String> mTestArray;

    public SelfProfileRecyclerAdapter(ArrayList<String> testArray) {
        mTestArray = testArray;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_basic_info, null);
        return new BasicInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((TextView)holder.itemView.findViewById(R.id.test_view)).setText(mTestArray.get(0));

    }

    @Override
    public int getItemCount() {
        return mTestArray.size();
    }
}
