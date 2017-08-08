package com.tacademy.semo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.tacademy.semo.item.Memo;
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

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.HomeViewHoler> {

    final int REQUEST_CODE_HOME_FRAGMENT_TO_DETAIL_ACTIVITY = 102;

    Context context;
    HomeActivity activity;
    ArrayList<Memo> memoList = new ArrayList<>();
    ArrayList<View> selectedViewList = new ArrayList<>();

    public ArrayList<Memo> getMemoList() {
        return memoList;
    }

    public HomeRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public void setViewUnselected() {

        for ( int i = 0; i < selectedViewList.size(); i++)  {
            activity.selectedMemoList.get(i).selected = false;
            selectedViewList.get(i).setBackgroundResource(R.drawable.memo_background);
        }
        selectedViewList.clear();
        activity.selectedMemoList.clear();

        notifyDataSetChanged();
    }

    public void setActivity(Activity activity) {
        this.activity = (HomeActivity) activity;
    }

    public void bringAllMemo(Memo memo) {
        memoList.add(0, memo);
    }

    public void addMemo(Memo memo) {

        if ( HomeFragment.pinMemoCount == 0 ) {
            memoList.add(0, memo);
        } else {
            memoList.add(HomeFragment.pinMemoCount, memo);
        }
    }

    public void removeMemo(Memo memo) {
        memoList.remove(memo);
    }

    @Override
    public HomeRecyclerViewAdapter.HomeViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memo, parent, false);

        return new HomeViewHoler(view);
    }

    @Override
    public void onBindViewHolder(final HomeRecyclerViewAdapter.HomeViewHoler holder, int position) {

        final Memo memo = memoList.get(position);
        // 핀
        if ( memo.pin ) {
            holder.imageViewPin.setImageResource(R.drawable.icon_pin);
        } else {
            holder.imageViewPin.setImageResource(R.drawable.icon_unpin);
        }
        holder.imageViewPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                memoList.remove(memo);

                if ( memo.pin ) {
                    memo.pin = false;
                    --HomeFragment.pinMemoCount;

                    for ( int i = HomeFragment.pinMemoCount; i < memoList.size(); i++ ) {
                        if ( memoList.get(i).memo_id < memo.memo_id ) {
                            memoList.add(i, memo);
                            break;
                        } else if ( i == memoList.size() - 1 ) {
                            memoList.add(i + 1, memo);
                            break;
                        }
                    }

                    Toast.makeText(activity.getApplicationContext(), "핀 해제!", Toast.LENGTH_SHORT).show();
                } else {
                    memo.pin = true;

                    for ( int i = 0; i < HomeFragment.pinMemoCount; i++ ) {
                        if ( memoList.get(i).memo_id < memo.memo_id ) {
                            memoList.add(i, memo);
                            break;
                        } else if ( i == HomeFragment.pinMemoCount - 1 ) {
                            memoList.add(i + 1, memo);
                            break;
                        }
                    }

                    if ( HomeFragment.pinMemoCount == 0 ) {
                        memoList.add(0, memo);
                    }

                    ++HomeFragment.pinMemoCount;
                    Toast.makeText(activity.getApplicationContext(), "핀 설정!", Toast.LENGTH_SHORT).show();
                }

                new AsyncPinSetting().execute(memo);
            }
        });

        // 아이데이션
        holder.imageViewIdeation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 번들객체에 메모 데이터 저장
                Bundle bundle = new Bundle();
                bundle.putInt("memo_id", memo.memo_id);
                bundle.putBoolean("pin", memo.pin);
                bundle.putString("body", memo.body);
                bundle.putString("createdAt", memo.createdAt);
                bundle.putString("keyword", memo.keyword);

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
                bundle.putInt("memo_id", memo.memo_id);
                bundle.putBoolean("pin", memo.pin);
                bundle.putString("body", memo.body);
                bundle.putString("createdAt", memo.createdAt);
                bundle.putString("keyword", memo.keyword);

                Intent intent = new Intent(activity, DetailMemoActivity.class);
                intent.putExtra("memoInfo", bundle);

                activity.startActivityForResult(intent, REQUEST_CODE_HOME_FRAGMENT_TO_DETAIL_ACTIVITY);
            }
        });
        // 메모내용
        holder.textViewMemoContent.setText(memo.body);
        if ( memo.selected ) {
            holder.linearLayoutMemo.setBackgroundResource(R.drawable.memo_selected_background);
        } else {
            holder.linearLayoutMemo.setBackgroundResource(R.drawable.memo_background);
        }
        // 메모 클릭 처리
        holder.textViewMemoContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 번들객체에 메모 데이터 저장
                Bundle bundle = new Bundle();
                bundle.putInt("memo_id", memo.memo_id);
                bundle.putBoolean("pin", memo.pin);
                bundle.putString("body", memo.body);
                bundle.putString("createdAt", memo.createdAt);
                bundle.putString("keyword", memo.keyword);

                Intent intent = new Intent(activity, DetailMemoActivity.class);
                intent.putExtra("memoInfo", bundle);

                activity.startActivityForResult(intent, REQUEST_CODE_HOME_FRAGMENT_TO_DETAIL_ACTIVITY);
            }
        });
        // 메모 롱클릭 이벤트 처리(휴지통 등)
        holder.textViewMemoContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if ( memo.selected ) {
                    // 선택된 메모 제거
                    activity.selectedMemoList.remove(memo);
                    selectedViewList.remove(holder.linearLayoutMemo);

                    memo.selected = false;
                } else {
                    // 선택된 메모 추가
                    activity.selectedMemoList.add(memo);
                    selectedViewList.add(holder.linearLayoutMemo);

                    memo.selected = true;
                }

                notifyDataSetChanged();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }

    public class HomeViewHoler extends RecyclerView.ViewHolder {

        // 핀 아이콘
        public ImageView imageViewPin;
        // 아이데이션 아이콘
        public ImageView imageViewIdeation;
        // 상세메모 아이콘
        public ImageView imageViewDetailMemo;
        // 메모 내용이 들어갈 텍스트뷰
        public TextView textViewMemoContent;
        // 뷰 자체
        public LinearLayout linearLayoutMemo;

        public HomeViewHoler(View itemView) {
            super(itemView);

            imageViewPin = (ImageView) itemView.findViewById(R.id.imageViewPin);
            imageViewIdeation = (ImageView) itemView.findViewById(R.id.imageViewIdeation);
            imageViewDetailMemo = (ImageView) itemView.findViewById(R.id.imageViewDetailMemo);
            textViewMemoContent = (TextView) itemView.findViewById(R.id.textViewMemoContent);
            linearLayoutMemo = (LinearLayout) itemView.findViewById(R.id.linearLayoutView);
        }
    }

    public class AsyncPinSetting extends AsyncTask<Memo, Integer, String> {
        @Override
        protected String doInBackground(Memo... params) {
            // 핀 상태를 바꿀 메모 객체
            Memo memo = params[0];
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
                        .add("pin", String.valueOf(memo.pin))  // 핀 상태 설정
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_UPDATE_MEMO_PIN + memo.memo_id)
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
                notifyDataSetChanged();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }
}
