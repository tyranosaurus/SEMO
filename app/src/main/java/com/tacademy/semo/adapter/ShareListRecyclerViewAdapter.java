package com.tacademy.semo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tacademy.semo.R;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.item.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShareListRecyclerViewAdapter extends RecyclerView.Adapter<ShareListRecyclerViewAdapter.ShareListViewHoler> {

    ArrayList<User> shareUserList = new ArrayList<>();

    public void addShareList(User shareUser) {
        shareUserList.add(shareUser);
    }

    @Override
    public ShareListRecyclerViewAdapter.ShareListViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_share_list, parent, false);

        return new ShareListViewHoler(view);
    }

    @Override
    public void onBindViewHolder(ShareListRecyclerViewAdapter.ShareListViewHoler holder, int position) {
        final User shareUser = shareUserList.get(position);

        // 프로필 이미지 설정 : 글라이드 사용
        //Glide  설정
        Glide.with(SemoApplication.getSemoContext())
                .load(shareUser.profileImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.circleImageProfile);
        // 닉네임
        holder.textViewNickname.setText(shareUser.nickName);
    }

    @Override
    public int getItemCount() {
        return shareUserList.size();
    }

    public class ShareListViewHoler extends RecyclerView.ViewHolder {

        public CircleImageView circleImageProfile;
        public TextView textViewNickname;

        public ShareListViewHoler(View itemView) {
            super(itemView);

            circleImageProfile = (CircleImageView) itemView.findViewById(R.id.circleImageProfile);
            textViewNickname = (TextView) itemView.findViewById(R.id.textViewNickname);
        }
    }
}
