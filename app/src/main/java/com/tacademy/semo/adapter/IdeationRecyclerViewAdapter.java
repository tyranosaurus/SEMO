package com.tacademy.semo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tacademy.semo.R;
import com.tacademy.semo.activity.IdeationActivity;
import com.tacademy.semo.item.NewCombineMemo;

import java.util.ArrayList;

public class IdeationRecyclerViewAdapter extends RecyclerView.Adapter<IdeationRecyclerViewAdapter.IdeationViewHoler> {

    IdeationActivity activity;
    ArrayList<NewCombineMemo> newCombineMemoList = new ArrayList<>();

    public void setActivity(IdeationActivity ideationActivity) {
        this.activity = ideationActivity;
    }

    public void setUnselected() {

        for ( int i = 0; i < activity.selectedNewCombineMemoList.size(); i++ ) {
            activity.selectedNewCombineMemoList.get(i).selected = false;
            activity.selectedViewList.get(i).setBackgroundResource(R.drawable.memo_background);
        }

        activity.selectedNewCombineMemoList.clear();
        activity.selectedViewList.clear();

        notifyDataSetChanged();
    }

    public void addNewCombineMemo(NewCombineMemo newCombineMemo) {
        newCombineMemoList.add(newCombineMemo);
    }

    @Override
    public IdeationRecyclerViewAdapter.IdeationViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_combine_memo, parent, false);

        return new IdeationViewHoler(view);
    }

    @Override
    public void onBindViewHolder(final IdeationRecyclerViewAdapter.IdeationViewHoler holder, int position) {
        final NewCombineMemo newCombineMemo = newCombineMemoList.get(position);

        holder.textViewNickName.setText(newCombineMemo.nickname);
        holder.textViewMemoContent.setText(newCombineMemo.body);
        holder.textViewCombinedCount.setText(newCombineMemo.createdAt + "  조합 : " + newCombineMemo.connectionCount + "회");

        // 메모 선택여부 처리
        if ( newCombineMemo.selected ) {
            holder.linearLayoutParent.setBackgroundResource(R.drawable.memo_combined_background);
        } else {
            holder.linearLayoutParent.setBackgroundResource(R.drawable.memo_background);
        }

        holder.linearLayoutParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( newCombineMemo.selected ) {
                    activity.selectedNewCombineMemoList.remove(newCombineMemo);
                    activity.selectedViewList.remove(holder.linearLayoutParent);

                    newCombineMemo.selected = false;
                } else {
                    activity.selectedNewCombineMemoList.add(newCombineMemo);
                    activity.selectedViewList.add(holder.linearLayoutParent);

                    newCombineMemo.selected = true;
                }

                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return newCombineMemoList.size();
    }

    public class IdeationViewHoler extends RecyclerView.ViewHolder {
        TextView textViewNickName;
        TextView textViewMemoContent;
        TextView textViewCombinedCount;
        LinearLayout linearLayoutParent;

        public IdeationViewHoler(View itemView) {
            super(itemView);

            textViewNickName = (TextView) itemView.findViewById(R.id.textViewNickName);
            textViewMemoContent = (TextView) itemView.findViewById(R.id.textViewMemoContent);
            textViewCombinedCount = (TextView) itemView.findViewById(R.id.textViewCombinedCount);
            linearLayoutParent = (LinearLayout) itemView.findViewById(R.id.linearLayoutParent);
        }
    }
}
