package com.tacademy.semo.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tacademy.semo.R;
import com.tacademy.semo.activity.DetailMemoActivity;
import com.tacademy.semo.activity.RecyclerbinActivity;
import com.tacademy.semo.item.Memo;

import java.util.ArrayList;

public class RecyclerbinRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerbinRecyclerViewAdapter.RecyclerbinViewHolder> {

    RecyclerbinActivity activity;
    ArrayList<Memo> memoList = new ArrayList<>();
    ArrayList<View> selectedViewList = new ArrayList<>();

    public RecyclerbinRecyclerViewAdapter(Activity activity) {
        this.activity = (RecyclerbinActivity)activity;
    }

    public void setViewUnselected() {
        for ( int i = 0; i < selectedViewList.size(); i++)  {
            selectedViewList.get(i).setBackgroundResource(R.drawable.memo_background);
            selectedViewList.get(i).setFocusable(false);
        }
        selectedViewList.clear();
    }

    public void addMemo(Memo memo) {
        memoList.add(memo);
    }

    public void removeMemo(Memo memo) {
        memoList.remove(memo);
    }

    public void removeAllMemo() {
        memoList.clear();
    }

    @Override
    public RecyclerbinRecyclerViewAdapter.RecyclerbinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memo, parent, false);

        return new RecyclerbinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerbinRecyclerViewAdapter.RecyclerbinViewHolder holder, int position) {
        final Memo memo = memoList.get(position);

        // 상세보기
        holder.imageViewDetailMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 번들객체에 메모 데이터 저장
                Bundle bundle = new Bundle();
                bundle.putInt("memo_id", memo.memo_id);
                bundle.putBoolean("pin", memo.pin);
                bundle.putString("body", memo.body);

                Intent intent = new Intent(activity, DetailMemoActivity.class);
                intent.putExtra("memoInfo", bundle);

                activity.startActivity(intent);
            }
        });
        // 메모내용
        holder.textViewMemoContent.setText(memo.body);
        // 메모 롱클릭 이벤트 처리(휴지통 등)
        holder.view.setFocusable(false);
        holder.view.setBackgroundResource(R.drawable.memo_background);
        holder.textViewMemoContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if ( holder.view.isFocusable() ) {
                    // 선택된 메모 제거
                    activity.selectedMemoList.remove(memo);
                    selectedViewList.remove(holder.view);

                    holder.view.setBackgroundResource(R.drawable.memo_background);
                    holder.view.setFocusable(false);
                } else {
                    // 선택된 메모 추가
                    activity.selectedMemoList.add(memo);
                    selectedViewList.add(holder.view);

                    holder.view.setBackgroundResource(R.drawable.memo_selected_background);
                    holder.view.setFocusable(true);
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }

    public class RecyclerbinViewHolder extends RecyclerView.ViewHolder {

        // 핀 아이콘
        public ImageView imageViewPin;
        // 아이데이션 아이콘
        public ImageView imageViewIdeation;
        // 상세메모 아이콘
        public ImageView imageViewDetailMemo;
        // 메모 내용이 들어갈 텍스트뷰
        public TextView textViewMemoContent;
        // 메모 자체 뷰
        public View view;

        public RecyclerbinViewHolder(View itemView) {
            super(itemView);

            imageViewPin = (ImageView) itemView.findViewById(R.id.imageViewPin);
            imageViewPin.setVisibility(View.INVISIBLE);
            imageViewIdeation = (ImageView) itemView.findViewById(R.id.imageViewIdeation);
            imageViewIdeation.setVisibility(View.INVISIBLE);

            imageViewDetailMemo = (ImageView) itemView.findViewById(R.id.imageViewDetailMemo);
            textViewMemoContent = (TextView) itemView.findViewById(R.id.textViewMemoContent);
            view = itemView;
        }
    }
}
