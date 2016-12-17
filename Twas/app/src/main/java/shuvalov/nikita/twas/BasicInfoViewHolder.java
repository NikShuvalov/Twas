package shuvalov.nikita.twas;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by NikitaShuvalov on 12/17/16.
 */

public class BasicInfoViewHolder extends RecyclerView.ViewHolder {
    TextView mTestView;


    public BasicInfoViewHolder(View itemView) {
        super(itemView);
        mTestView = (TextView)itemView.findViewById(R.id.test_view);
    }
}
