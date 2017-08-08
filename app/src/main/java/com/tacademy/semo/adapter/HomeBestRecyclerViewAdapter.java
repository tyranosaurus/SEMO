package com.tacademy.semo.adapter;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tacademy.semo.R;
import com.tacademy.semo.activity.HomeActivity;
import com.tacademy.semo.activity.ParentDetailMemoActivity;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.item.BestMemo;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;

public class HomeBestRecyclerViewAdapter extends RecyclerView.Adapter<HomeBestRecyclerViewAdapter.HomeBestMemoViewHolder> {

    // 댓글다는 유저 아이디
    int userId = SemoApplication.getUserId();
    Context context;
    ArrayList<BestMemo> bestMemoList = new ArrayList<>();
    HomeActivity activity;

    // 클립보드 복사
    ClipboardManager clipboardManager;

    public HomeBestRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public void setActivity(HomeActivity activity) {
        this.activity = activity;

        clipboardManager = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
    }

    public void addBestMemo(BestMemo bestMemo) {
        bestMemoList.add(bestMemo);
    }

    @Override
    public HomeBestRecyclerViewAdapter.HomeBestMemoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_best_memo, parent, false);

        return new HomeBestMemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeBestRecyclerViewAdapter.HomeBestMemoViewHolder holder, int position) {
        final BestMemo bestMemo = bestMemoList.get(position);

        holder.textViewRanking.setText(String.valueOf(bestMemo.rank));
        holder.textViewNickname.setText(bestMemo.user.nickName);
        holder.textViewCombineCount.setText("누적 조합 " + String.valueOf(bestMemo.connectionCount) + " 회");
        holder.textViewMemoContents.setText(bestMemo.body);
        // 베스트메모 클릭시 상세보기
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 번들객체에 메모 데이터 저장
                Bundle bundle = new Bundle();
                bundle.putInt("memo_id", bestMemo.memoId);
                bundle.putBoolean("pin", bestMemo.pin);
                bundle.putString("body", bestMemo.body);
                bundle.putString("createdAt", bestMemo.createdAt);
                bundle.putInt("userId", userId);

                if ( bestMemo.keyword.equals("null") ) {
                    bestMemo.keyword = "";
                }
                bundle.putString("keyword", bestMemo.keyword);

                Intent intent = new Intent(activity, ParentDetailMemoActivity.class);
                intent.putExtra("memoInfo", bundle);

                activity.startActivity(intent);
            }
        });
        // 베스트메모 롱클릭시 선택리스트에 들어감
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 메모 내용 복사
                clipboardManager.setText(bestMemo.body);

                Toast.makeText(activity.getApplicationContext(), "메모가 복사되었습니다.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return bestMemoList.size();
    }

    public class HomeBestMemoViewHolder extends RecyclerView.ViewHolder {

        TextView textViewRanking;
        TextView textViewNickname;
        TextView textViewCombineCount;
        TextView textViewMemoContents;
        View view;

        public HomeBestMemoViewHolder(View itemView) {
            super(itemView);

            textViewRanking = (TextView) itemView.findViewById(R.id.textViewRanking);
            textViewNickname = (TextView) itemView.findViewById(R.id.textViewNickname);
            textViewCombineCount = (TextView) itemView.findViewById(R.id.textViewCombineCount);
            textViewMemoContents = (TextView) itemView.findViewById(R.id.textViewMemoContents);
            view = itemView;
        }
    }
}
