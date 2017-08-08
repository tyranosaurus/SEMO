package com.tacademy.semo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tacademy.semo.R;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.item.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShareUserRecyclerViewAdapter extends RecyclerView.Adapter<ShareUserRecyclerViewAdapter.ShareViewHolder> {

    ArrayList<User> shareUserList = new ArrayList<>();
    ArrayList<User> selectedShareUserList = new ArrayList<>();

    public void clearSelectedShareUserList() {
        selectedShareUserList.clear();
    }

    public ArrayList<User> getSelectedShareUserList() {
        return selectedShareUserList;
    }

    public boolean checkOverLap(String email) {

        for ( int i = 0; i < selectedShareUserList.size(); i++ ) {
            if ( selectedShareUserList.get(i).email.equals(email) ) {
                return true;
            }
        }

        return false;
    }

    public boolean checkOverLap(User checkUser) {

        for ( int i = 0; i < shareUserList.size(); i++ ) {
            if ( shareUserList.get(i).email.equals(checkUser.email) ) {
                return true;
            }
        }

        return false;
    }

    public void addShareUser(User shareUser) {
        shareUserList.add(shareUser);
    }

    @Override
    public ShareUserRecyclerViewAdapter.ShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_share, parent, false);

        return new ShareViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ShareUserRecyclerViewAdapter.ShareViewHolder holder, int position) {
        final User shareUser = shareUserList.get(position);

        // 뷰 자체
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( holder.view.isFocusable() ) {
                    selectedShareUserList.remove(shareUser);

                    holder.imageViewRadio.setImageResource(R.drawable.icon_radio_unselected);
                    holder.view.setFocusable(false);
                } else {
                    selectedShareUserList.add(shareUser);

                    holder.imageViewRadio.setImageResource(R.drawable.icon_radio_selected);
                    holder.view.setFocusable(true);
                }
            }
        });
        // 프로필 : Glide 설정
        //Glide  설정
        Glide.with(SemoApplication.getSemoContext())
                .load(shareUser.profileImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.circleImageProfile);
        // 닉네임
        holder.textViewNickName.setText(shareUser.nickName);
        // 이메일
        holder.textViewEmail.setText(shareUser.email);
    }

    @Override
    public int getItemCount() {
        return shareUserList.size();
    }

    public class ShareViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewRadio;
        CircleImageView circleImageProfile;
        TextView textViewNickName;
        TextView textViewEmail;
        View view;

        public ShareViewHolder(View itemView) {
            super(itemView);

            imageViewRadio = (ImageView) itemView.findViewById(R.id.imageViewRadio);
            circleImageProfile = (CircleImageView) itemView.findViewById(R.id.circleImageProfile);
            textViewNickName = (TextView) itemView.findViewById(R.id.textViewNickName);
            textViewEmail = (TextView) itemView.findViewById(R.id.textViewEmail);
            view = itemView;
            view.setFocusable(false);
        }
    }
}
