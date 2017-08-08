package com.tacademy.semo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tacademy.semo.R;
import com.tacademy.semo.activity.DetailMemoActivity;
import com.tacademy.semo.activity.HomeActivity;
import com.tacademy.semo.activity.IdeationActivity;
import com.tacademy.semo.fragment.HomeFragment;
import com.tacademy.semo.item.ShareMemo;
import com.tacademy.semo.item.User;
import com.tacademy.semo.network.NetworkDefineConstant;
import com.tacademy.semo.network.OkHttpInitSingtonManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShareMemoRecyclerViewAdapter extends RecyclerView.Adapter<ShareMemoRecyclerViewAdapter.ShareMemoViewHoler> {

    final int REQUEST_CODE_SHARE_FRAGMENT_TO_DETAIL_ACTIVITY = 103;

    Context context;
    HomeActivity activity;
    ArrayList<ShareMemo> shareMemoList = new ArrayList<>();
    ArrayList<View> selectedShareViewList = new ArrayList<>();

    ShareListRecyclerViewAdapter adapter;

    public ShareMemoRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public ArrayList<ShareMemo> getShareMemoList() {
        return shareMemoList;
    }

    public void setViewUnselected() {

        for ( int i = 0; i < selectedShareViewList.size(); i++)  {
            activity.selectedShareMemoList.get(i).selected = false;
            selectedShareViewList.get(i).setBackgroundResource(R.drawable.memo_background);
        }

        selectedShareViewList.clear();
        activity.selectedShareMemoList.clear();

        notifyDataSetChanged();
    }

    public void setActivity(Activity activity) {
        this.activity = (HomeActivity) activity;
    }

    public void setNewShareMemoCoAuthorList(ArrayList<User> newCoAuthorList) {
        shareMemoList.get(HomeFragment.pinShareMemoCount).coAuthorList.addAll(newCoAuthorList);
        adapter.notifyDataSetChanged();
    }

    public void bringAllMemo(ShareMemo newShareMemo) {
        shareMemoList.add(0, newShareMemo);
    }

    public void removeShareMemo(ShareMemo removeShareMemo) {
        shareMemoList.remove(removeShareMemo);
    }

    public void addShareMemo(ShareMemo newShareMemo) {
        if ( HomeFragment.pinShareMemoCount == 0 ) {
            shareMemoList.add(0, newShareMemo);
        } else {
            shareMemoList.add(HomeFragment.pinShareMemoCount, newShareMemo);
        }
    }

    @Override
    public ShareMemoRecyclerViewAdapter.ShareMemoViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_share_memo, parent, false);

        return new ShareMemoViewHoler(view);
    }

    @Override
    public void onBindViewHolder(final ShareMemoRecyclerViewAdapter.ShareMemoViewHoler holder, int position) {
        final ShareMemo shareMemo = shareMemoList.get(position);

        // 핀
        if ( shareMemo.pin ) {
            holder.imageViewPin.setImageResource(R.drawable.icon_pin);
        } else {
            holder.imageViewPin.setImageResource(R.drawable.icon_unpin);
        }
        holder.imageViewPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareMemoList.remove(shareMemo);

                if ( shareMemo.pin ) {
                    shareMemo.pin = false;
                    --HomeFragment.pinShareMemoCount;

                    for ( int i = HomeFragment.pinShareMemoCount; i < shareMemoList.size(); i++ ) {
                        if ( shareMemoList.get(i).shareId < shareMemo.shareId ) {
                            shareMemoList.add(i, shareMemo);
                            break;
                        } else if ( i == shareMemoList.size() - 1 ) {
                            shareMemoList.add(i + 1, shareMemo);
                            break;
                        }
                    }

                    Toast.makeText(activity.getApplicationContext(), "핀 해제!", Toast.LENGTH_SHORT).show();
                } else {
                    shareMemo.pin = true;

                    for ( int i = 0; i < HomeFragment.pinShareMemoCount; i++ ) {
                        if ( shareMemoList.get(i).shareId < shareMemo.shareId ) {
                            shareMemoList.add(i, shareMemo);
                            break;
                        } else if ( i == HomeFragment.pinShareMemoCount - 1 ) {
                            shareMemoList.add(i + 1, shareMemo);
                            break;
                        }
                    }

                    if ( HomeFragment.pinShareMemoCount == 0 ) {
                        shareMemoList.add(0, shareMemo);
                    }

                    ++HomeFragment.pinShareMemoCount;
                    Toast.makeText(activity.getApplicationContext(), "핀 설정!", Toast.LENGTH_SHORT).show();
                }

                new AsyncSharePinSetting().execute(shareMemo);
            }
        });
        // 아이데이션
        holder.imageViewIdeation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 번들객체에 메모 데이터 저장
                Bundle bundle = new Bundle();
                bundle.putInt("memo_id", shareMemo.memo.memo_id);
                bundle.putBoolean("pin", shareMemo.pin);
                bundle.putString("body", shareMemo.memo.body);
                bundle.putString("createdAt", shareMemo.memo.createdAt);
                bundle.putString("keyword", shareMemo.memo.keyword);

                Intent intent = new Intent(activity, IdeationActivity.class);
                intent.putExtra("memoInfo", bundle);

                activity.startActivity(intent);
            }
        });
        // 상세보기
        holder.imageViewDetailMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 번들객체에 메모 데이터 저장
                Bundle bundle = new Bundle();
                bundle.putInt("memo_id", shareMemo.memo.memo_id);
                bundle.putBoolean("pin", shareMemo.pin);
                bundle.putString("body", shareMemo.memo.body);
                bundle.putString("createdAt", shareMemo.memo.createdAt);
                bundle.putString("keyword", shareMemo.memo.keyword);

                Intent intent = new Intent(activity, DetailMemoActivity.class);
                intent.putExtra("memoInfo", bundle);

                activity.startActivityForResult(intent, REQUEST_CODE_SHARE_FRAGMENT_TO_DETAIL_ACTIVITY);
            }
        });
        // 메모내용
        holder.textViewMemoContent.setText(shareMemo.memo.body);
        if ( shareMemo.selected ) {
            holder.linearLayoutMemo.setBackgroundResource(R.drawable.memo_selected_background);
        } else {
            holder.linearLayoutMemo.setBackgroundResource(R.drawable.memo_background);
        }
        // 메모 클릭 처리
        holder.textViewMemoContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 메모 수정 기능
                // 번들객체에 메모 데이터 저장
                Bundle bundle = new Bundle();
                bundle.putInt("memo_id", shareMemo.memo.memo_id);
                bundle.putBoolean("pin", shareMemo.pin);
                bundle.putString("body", shareMemo.memo.body);
                bundle.putString("createdAt", shareMemo.memo.createdAt);
                bundle.putString("keyword", shareMemo.memo.keyword);

                Intent intent = new Intent(activity, DetailMemoActivity.class);
                intent.putExtra("memoInfo", bundle);

                activity.startActivityForResult(intent, REQUEST_CODE_SHARE_FRAGMENT_TO_DETAIL_ACTIVITY);
            }
        });
        // 메모 롱클릭 이벤트 처리(휴지통 등)
        holder.textViewMemoContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if ( shareMemo.selected ) {
                    // 선택된 메모 제거
                    activity.selectedShareMemoList.remove(shareMemo);
                    selectedShareViewList.remove(holder.linearLayoutMemo);

                    shareMemo.selected = false;
                } else {
                    // 선택된 메모 추가
                    activity.selectedShareMemoList.add(shareMemo);
                    selectedShareViewList.add(holder.linearLayoutMemo);

                    shareMemo.selected = true;
                }

                notifyDataSetChanged();

                return true;
            }
        });

        // 공유친구  리사이클러뷰 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity.getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        holder.recyclerViewShare.setLayoutManager(layoutManager);

        adapter = new ShareListRecyclerViewAdapter();
        for ( int i = 0; i < shareMemo.coAuthorList.size(); i++ ) {
            adapter.addShareList(shareMemo.coAuthorList.get(i));
        }
        holder.recyclerViewShare.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return shareMemoList.size();
    }

    public class ShareMemoViewHoler extends RecyclerView.ViewHolder {

        public ImageView imageViewPin;
        public ImageView imageViewIdeation;
        public ImageView imageViewDetailMemo;
        public TextView textViewMemoContent;
        public RecyclerView recyclerViewShare;
        public LinearLayout linearLayoutMemo;

        public ShareMemoViewHoler(View itemView) {
            super(itemView);

            imageViewPin = (ImageView) itemView.findViewById(R.id.imageViewPin);
            imageViewIdeation = (ImageView) itemView.findViewById(R.id.imageViewIdeation);
            imageViewDetailMemo = (ImageView) itemView.findViewById(R.id.imageViewDetailMemo);
            textViewMemoContent = (TextView) itemView.findViewById(R.id.textViewMemoContent);
            recyclerViewShare = (RecyclerView) itemView.findViewById(R.id.recyclerViewShare);
            linearLayoutMemo = (LinearLayout) itemView.findViewById(R.id.linearLayoutMemo);
        }
    }

    public class AsyncSharePinSetting extends AsyncTask<ShareMemo, Integer, String> {
        @Override
        protected String doInBackground(ShareMemo... params) {
            // 핀 상태를 바꿀 메모 객체
            ShareMemo shareMemo = params[0];

            // 서버 요청 결과(성공시 true, 실패시 false)
            boolean flag = false;
            // 서버 요청 성공시 응답받은 JSON 값
            String result = "";

            Response response = null;
            OkHttpClient toServer = null;

            try{
                toServer = OkHttpInitSingtonManager.getOkHttpClient();
                //요청 Form세팅(멀티파트 폼 데이터 형식)
                RequestBody postBody = new FormBody.Builder()
                        .add("user_id", String.valueOf(shareMemo.memo.userId))
                        .add("memo_id", String.valueOf(shareMemo.memo.memo_id))
                        .add("pin", String.valueOf(shareMemo.pin))  // 핀 상태 설정
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_UPDATE_SHARE_MEMO_PIN)
                        .post(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                // 응답결과에 따른 처리
                if( flag ) {
                    returedJSON = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(returedJSON);
                        result = jsonObject.getString("msg");

                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                    } catch( JSONException jsone ) {
                        Log.e("JSON ERROR", jsone.toString());
                    }
                } else {
                    //요청에러 발생시(http 에러)
                    Log.e("HTTP ERROR", "HTTP REQUEST ERROR OCCUR");
                }

            } catch (UnknownHostException une) {
                // 호스트 못찾을떄
                Log.e("NOT FOUND HOST URL", une.toString());
            } catch (UnsupportedEncodingException uee) {
                // 인코딩 오류가 났을때
                Log.e("ENCODING ERROR", uee.toString());
            } catch (Exception e) {
                Log.e("EXCEPTION ERROR", e.toString());
            } finally {
                if(response != null) {
                    // 연결 해제
                    response.close();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if ( result != null ) {
                Log.e("onPostExecute", result);
                notifyDataSetChanged();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }
}
