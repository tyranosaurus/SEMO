package com.tacademy.semo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tacademy.semo.R;
import com.tacademy.semo.adapter.RecentShareUserRecyclerViewAdapter;
import com.tacademy.semo.adapter.ShareUserRecyclerViewAdapter;
import com.tacademy.semo.item.User;
import com.tacademy.semo.network.NetworkDefineConstant;
import com.tacademy.semo.network.OkHttpInitSingtonManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShareActivity extends AppCompatActivity {

    // 유저 아이디
    int userId;
    boolean keyword;

    // 툴바
    Toolbar mToolbar;
    ActionBar mActionBar;

    ImageView imageViewBack;
    TextView textViewShare;
    ImageView imageViewRecyclerbin;
    RecyclerView recyclerViewShare;
    RecyclerView recyclerViewRecentShare;

    EditText editTextSearchEmail;
    ImageView imageViewPlus;
    TextView textViewComplete;

    // 어댑터
    ShareUserRecyclerViewAdapter shareUserAdapter;
    RecentShareUserRecyclerViewAdapter recentShareUserAdapter;

    // 키보드 내리기
    InputMethodManager inputMethodManager;

    // 공유 친구 목록
    ArrayList<User> selectedShareUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        userId = getIntent().getIntExtra("userId", 0);
        keyword = getIntent().getBooleanExtra("keyword", false);

        // 키보드
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 툴바 설정
        mToolbar = (Toolbar) findViewById(R.id.toolbarSetting);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        // 툴바에 사용할 커스텀 레이아웃 인플레이션
        View viewToolBar = getLayoutInflater().inflate(R.layout.toolbar_share, null);

        imageViewBack = (ImageView) viewToolBar.findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textViewShare = (TextView) viewToolBar.findViewById(R.id.textViewShare);

        imageViewRecyclerbin = (ImageView) viewToolBar.findViewById(R.id.imageViewRecyclerbin) ;
        imageViewRecyclerbin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 휴지통처리
            }
        });

        // 인플레이션한 커스텀 툴바 세팅
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        mActionBar.setCustomView(viewToolBar, layoutParams);

        // 이메일 입력
        editTextSearchEmail = (EditText) findViewById(R.id.editTextSearchEmail);
        // 플러스 버튼
        imageViewPlus = (ImageView) findViewById(R.id.imageViewPlus);
        imageViewPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 메모 비었을때 처리
                if ( editTextSearchEmail.getText().toString().trim().isEmpty() || editTextSearchEmail.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();

                    editTextSearchEmail.setText("");
                    inputMethodManager.hideSoftInputFromWindow(editTextSearchEmail.getWindowToken(), 0);

                    return;
                }

                new AsyncShareUserSearch().execute(editTextSearchEmail.getText().toString());

                editTextSearchEmail.setText("");
                inputMethodManager.hideSoftInputFromWindow(editTextSearchEmail.getWindowToken(), 0);
            }
        });

        // 완료
        textViewComplete = (TextView) findViewById(R.id.textViewComplete);
        textViewComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedShareUserList.clear();

                int memoId = getIntent().getIntExtra("memoId", 0);
                selectedShareUserList = shareUserAdapter.getSelectedShareUserList();

                if ( selectedShareUserList.size() < 1 ) {
                    Toast.makeText(getApplicationContext(), "공유할 친구가 없습니다.", Toast.LENGTH_SHORT).show();

                    return;
                }

                for ( int i = 0; i < selectedShareUserList.size(); i++ ) {
                    new AsyncShareUserAdd().execute(memoId, selectedShareUserList.get(i).userId);
            }
            }
        });

        // 공유할 친구
        recyclerViewShare = (RecyclerView) findViewById(R.id.recyclerViewShare);

        LinearLayoutManager layoutManagerShare = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        recyclerViewShare.setLayoutManager(layoutManagerShare);

        shareUserAdapter = new ShareUserRecyclerViewAdapter();
        recyclerViewShare.setAdapter(shareUserAdapter);

        // 최근 공유한 친구
        recyclerViewRecentShare = (RecyclerView) findViewById(R.id.recyclerViewRecentShare);

        LinearLayoutManager layoutManagerRecentShare = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        recyclerViewRecentShare.setLayoutManager(layoutManagerRecentShare);
        // 어댑터 만들어서 처리
        recentShareUserAdapter = new RecentShareUserRecyclerViewAdapter();
        recentShareUserAdapter.setSelectedShareUserList(shareUserAdapter.getSelectedShareUserList());
        recyclerViewRecentShare.setAdapter(recentShareUserAdapter);
        // 최근 공유한 유저 가져오기
        new AsyncRecentShareUsers().execute();
    }

    public class AsyncShareUserSearch extends AsyncTask<String, Integer, String> {
        String searchEmail;

        @Override
        protected String doInBackground(String... params) {
            searchEmail = params[0];
            // 서버 요청 결과(성공시 true, 실패시 false)
            boolean flag = false;
            // 서버 요청 성공시 응답받은 JSON 값
            String result = "";

            Response response = null;
            OkHttpClient toServer = null;

            try{
                toServer = OkHttpInitSingtonManager.getOkHttpClient();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_SEARCH_USER + searchEmail)
                        .get()
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                if( flag ){
                    returedJSON = response.body().string();

                    try {
                        JSONObject jsonFromServer = new JSONObject(returedJSON);
                        if ( jsonFromServer.isNull("data") ) {
                            String noUserResult = "noUser";
                            return noUserResult;
                        }

                        JSONObject jsonObjectShareUser = jsonFromServer.getJSONObject("data");

                        User shareUser = new User(jsonObjectShareUser.getInt("user_id"),
                                                  jsonObjectShareUser.getString("nickname"),
                                                  jsonObjectShareUser.getString("photo"),
                                                  jsonObjectShareUser.getString("email"));

                        if ( shareUserAdapter.checkOverLap(shareUser) || shareUserAdapter.checkOverLap(searchEmail) ) {
                            result = "contained";
                        } else {
                            shareUserAdapter.addShareUser(shareUser);
                            result = jsonFromServer.getString("msg");
                        }

                    } catch(JSONException jsone) {
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
                    response.close();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // 중복된 키워드 처리
            if ( result.equals("noUser") ) {
                Toast.makeText(getApplicationContext(), "유저가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();

                return;
            }

            // 중복된 키워드 처리
            if ( result.equals("contained") ) {
                Toast.makeText(getApplicationContext(), "이미 추가되었습니다.", Toast.LENGTH_SHORT).show();

                return;
            }

            if ( result != null ) {
                Log.e("onPostExecute", result);
            } else {
                Log.e("onPostExecute", "result is empty");
            }

            shareUserAdapter.notifyDataSetChanged();
        }
    }

    public class AsyncShareUserAdd extends AsyncTask<Integer, Integer, String> {
        int memoId;
        int coAuthor;

        @Override
        protected String doInBackground(Integer... params) {
            memoId = params[0];
            coAuthor = params[1];

            // 서버 요청 결과(성공시 true, 실패시 false)
            boolean flag = false;
            // 서버 요청 성공시 응답받은 JSON 값
            String result = "";

            Response response = null;
            OkHttpClient toServer = null;

            try{
                toServer = OkHttpInitSingtonManager.getOkHttpClient();

                //요청 Form세팅
                RequestBody postBody = new FormBody.Builder()
                        .add("owner_id", String.valueOf(userId))
                        .add("memo_id", String.valueOf(memoId))
                        .add("coAuthor", String.valueOf(coAuthor))
                        .build();

                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_INSERT_SHARE_USER)
                        .post(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                if( flag ){
                    returedJSON = response.body().string();

                    try {
                        JSONObject jsonFromServer = new JSONObject(returedJSON);

                        result = jsonFromServer.getString("msg");
                    } catch(JSONException jsone) {
                        Log.e("JSON ERROR", jsone.toString());
                    }
                } else {
                    //요청에러 발생시(http 에러)
                    Log.e("HTTP ERROR", "HTTP REQUEST ERROR OCCUR");
                    Log.e("유저 아이디", coAuthor+"");
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

                //shareUserAdapter.notifyDataSetChanged();

                if ( coAuthor == selectedShareUserList.get(selectedShareUserList.size() - 1).userId ) {

                    Intent intent = new Intent();
                    intent.putExtra("refresh", true);
                    intent.putExtra("selectedShareUserList", (Serializable) selectedShareUserList.clone());

                    if ( keyword ) {
                        intent.putExtra("keyword", true);
                    } else {
                        intent.putExtra("keyword", false);
                    }

                    setResult(Activity.RESULT_OK ,intent);

                    selectedShareUserList.clear();
                    shareUserAdapter.clearSelectedShareUserList();
                    shareUserAdapter.notifyDataSetChanged();
                    finish();
                }
            } else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncRecentShareUsers extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... params) {
            //int userId = params[0];
            // 서버 요청 결과(성공시 true, 실패시 false)
            boolean flag = false;
            // 서버 요청 성공시 응답받은 JSON 값
            String result = "";

            Response response = null;
            OkHttpClient toServer = null;

            try{
                toServer = OkHttpInitSingtonManager.getOkHttpClient();

                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_RECENT_SHARE_USER + userId)
                        .get()
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                if( flag ){
                    returedJSON = response.body().string();

                    try {
                        JSONObject jsonFromServer = new JSONObject(returedJSON);
                        JSONArray jsonArrayRecentShareUsers = jsonFromServer.getJSONArray("data");

                        for ( int i = 0; i < jsonArrayRecentShareUsers.length(); i++ ) {
                            JSONObject jsonObjectRecentShareUser = jsonArrayRecentShareUsers.getJSONObject(i);

                            recentShareUserAdapter.addRecentShareUser(new User(jsonObjectRecentShareUser.getInt("User.user_id"),
                                                                               jsonObjectRecentShareUser.getString("User.nickname"),
                                                                               jsonObjectRecentShareUser.getString("User.photo"),
                                                                               jsonObjectRecentShareUser.getString("User.email")));
                        }

                        result = jsonFromServer.getString("msg");
                    } catch(JSONException jsone) {
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

                recentShareUserAdapter.notifyDataSetChanged();
            } else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }
}
