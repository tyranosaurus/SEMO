package com.tacademy.semo.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tacademy.semo.R;
import com.tacademy.semo.activity.HomeActivity;
import com.tacademy.semo.activity.KeywordActivity;
import com.tacademy.semo.activity.ShareActivity;
import com.tacademy.semo.adapter.HomeBestRecyclerViewAdapter;
import com.tacademy.semo.adapter.HomeRecyclerViewAdapter;
import com.tacademy.semo.adapter.ShareMemoRecyclerViewAdapter;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.item.BestMemo;
import com.tacademy.semo.item.Memo;
import com.tacademy.semo.item.ShareMemo;
import com.tacademy.semo.item.User;
import com.tacademy.semo.network.NetworkDefineConstant;
import com.tacademy.semo.network.OkHttpInitSingtonManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.util.Log.e;

public class HomeFragment extends Fragment {

    final int REQUEST_CODE_HOME_FRAGMENT = 301;

    int index;

    EditText editTextWriteMemo;
    ImageView imageViewPlus;
    ImageView imageViewMemoMenu;
    ImageView imageViewMemoIcon;
    TextView textViewMymemo;
    RecyclerView recyclerView;
    LinearLayout default_mymemo;

    // 내가 쓴 메모
    HomeRecyclerViewAdapter myMemoAdapter;
    // 공유메모
    ShareMemoRecyclerViewAdapter adapterShare;
    // 베스트메모
    HomeBestRecyclerViewAdapter bestMemoAdapter;


    // 유저 ID
    int userId = SemoApplication.getUserId(); // 지금은 1로 고정했는데 나중에 로그인한거 받아오는 걸로 바꿔야함.
    // 새로 추가하는 메모
    int totalMemo;
    int lastShareMemoId;
    public static int pinMemoCount;
    public static int pinShareMemoCount;

    // 키보드 내리기
    InputMethodManager inputMethodManager;

    // 임시변수
    int tmpMemoId;

    public HomeFragment() {
    }

    public void callAsyncUserMemos() {
        new AsyncUserMemos().execute();
    }

    public void callAsyncShareUserMemos() {
        new AsyncShareMemos().execute();
    }

    public int getIndex() {
        return index;
    }

    public static HomeFragment newInstance(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public HomeRecyclerViewAdapter getHomeRecyclerViewAdapter() {
        return myMemoAdapter;
    }

    public ShareMemoRecyclerViewAdapter getShareMemoRecyclerViewAdapter() {
        return adapterShare;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( getArguments() != null ) {
            index = getArguments().getInt("index");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = null;
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        if ( index  < 2 ) {
            rootView = inflater.inflate(R.layout.fragment_home01, container, false);

            imageViewMemoIcon = (ImageView) rootView.findViewById(R.id.imageViewMemoIcon);
            textViewMymemo = (TextView) rootView.findViewById(R.id.textViewMymemo);
            default_mymemo = (LinearLayout) rootView.findViewById(R.id.default_mymemo);

            editTextWriteMemo = (EditText) rootView.findViewById(R.id.editTextWriteMemo);
            imageViewPlus = (ImageView) rootView.findViewById(R.id.imageViewPlus);
            imageViewPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 메모 비었을때 처리
                    if ( editTextWriteMemo.getText().toString().trim().isEmpty() || editTextWriteMemo.getText().toString().trim().equals("") ) {
                        Toast.makeText(getContext(), "메모가 비었습니다", Toast.LENGTH_SHORT).show();

                        editTextWriteMemo.setText("");
                        inputMethodManager.hideSoftInputFromWindow(editTextWriteMemo.getWindowToken(), 0);

                        return;
                    }

                    if ( index == 0 ) { // 내가 쓴 메모
                        // 새 메모 추가 및 화면 갱신
                        new AsyncNewMemoInsert().execute(editTextWriteMemo.getText().toString());
                    } else if ( index == 1 ) {
                        // 새 공유메모 추가 및 화면 갱신
                        new AsyncNewShareMemoInsert().execute(editTextWriteMemo.getText().toString());
                    }

                    // 메모삽입 후 입력칸 초기화 및 키보드 내리기
                    editTextWriteMemo.setText("");
                    inputMethodManager.hideSoftInputFromWindow(editTextWriteMemo.getWindowToken(), 0);
                }
            });

            imageViewMemoMenu = (ImageView) rootView.findViewById(R.id.imageViewMemoMenu);
            imageViewMemoMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMemoPopupOptionMenu(imageViewMemoMenu);
                }
            });

        } else if ( index == 2 ) {
            rootView = inflater.inflate(R.layout.fragment_home_best, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        } else {
            Log.e("HomeFragment_ERROR","index value is not under 2");
        }

        // 메모 가져오기 (내가쓴 메모, 공유메모, 베스트메모)
        switch ( index ) {
            case 0:

                recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
                // 메모작성 메뉴 반투명 설정
                imageViewMemoMenu.setAlpha(120);

                StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);

                myMemoAdapter = new HomeRecyclerViewAdapter(getContext());
                myMemoAdapter.setActivity(getActivity());
                recyclerView.setAdapter(myMemoAdapter);

                myMemoAdapter.notifyDataSetChanged();
                new AsyncUserMemos().execute();

                break;
            case 1:

                recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
                // 공유메모 아이콘으로 바꿈
                imageViewMemoIcon.setImageResource(R.drawable.icon_sharememo);
                // 메모작성 메뉴 반투명 설정
                imageViewMemoMenu.setAlpha(120);

                StaggeredGridLayoutManager layoutManagerShareMemo = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManagerShareMemo);

                adapterShare = new ShareMemoRecyclerViewAdapter(getContext());
                adapterShare.setActivity(getActivity());

                recyclerView.setAdapter(adapterShare);

                new AsyncShareMemos().execute();

                break;
            case 2:
                LinearLayoutManager bestMemoLayourManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(bestMemoLayourManager);

                bestMemoAdapter = new HomeBestRecyclerViewAdapter(getContext());
                bestMemoAdapter.setActivity((HomeActivity) getActivity());
                recyclerView.setAdapter(bestMemoAdapter);

                new AsyncBestMemos().execute();

                break;
            default:
                break;
        }

        return rootView;
    }

    public void showMemoPopupOptionMenu(View view){
        PopupMenu popupMenu = new PopupMenu(getContext(), view);

        getActivity().getMenuInflater().inflate(R.menu.menu_memo_optionmenu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.keywordSetting:

                        // 메모 비었을때 처리
                        if ( editTextWriteMemo.getText().toString().trim().isEmpty() || editTextWriteMemo.getText().toString().trim().equals("") ) {
                            Toast.makeText(getContext(), "메모가 비었습니다", Toast.LENGTH_SHORT).show();

                            editTextWriteMemo.setText("");
                            inputMethodManager.hideSoftInputFromWindow(editTextWriteMemo.getWindowToken(), 0);

                            break;
                        }

                        if ( index == 0 ) {
                            new AsyncNewMemoInsert().execute(editTextWriteMemo.getText().toString(), "keyword");
                        } else if ( index == 1 ) {
                            new AsyncNewShareMemoInsert().execute(editTextWriteMemo.getText().toString(), "keyword");
                        }

                        editTextWriteMemo.setText("");

                        break;
                    case R.id.sharing:
                        Toast.makeText(getActivity().getApplicationContext(), "준비 중입니다..", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getActivity().getApplicationContext(), "올바르지 않은 메뉴입니다.", Toast.LENGTH_SHORT).show();
                        break;
                }

                return true;
            }
        });

        popupMenu.show();
    }

    public class AsyncNewMemoInsert extends AsyncTask<String, Integer, String> {
        // 새로운 메모
        String newMemo;
        String attachKeyword = null;

        @Override
        protected String doInBackground(String... params) {

            newMemo = params[0];

            if ( params.length > 1 ) {
                attachKeyword = params[1];
            }

            // 서버 요청 결과(성공시 true, 실패시 false)
            boolean flag = false;
            // 서버 요청 성공시 응답받은 JSON 값
            String result = "";

            Response response = null;
            OkHttpClient toServer = null;

            try{
                toServer = OkHttpInitSingtonManager.getOkHttpClient();
                //요청 Form세팅(멀티파트 폼 데이터 형식)
                RequestBody postBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("user_id", String.valueOf(userId))  // 유저의 primary key
                        .addFormDataPart("body", newMemo) // 새로 만든 메모
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_INSERT_MEMO)
                        .post(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                // 응답결과에 따른 처리
                if( flag ){
                    returedJSON = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(returedJSON);
                        JSONObject jsonObjectNewMemo = jsonObject.getJSONObject("newMemo");

                        myMemoAdapter.addMemo(new Memo(jsonObjectNewMemo.getInt("memo_id"),
                                                       jsonObjectNewMemo.getBoolean("pin"),
                                                       jsonObjectNewMemo.getString("body"),
                                                       jsonObjectNewMemo.getString("createdAt"),
                                                       jsonObjectNewMemo.getInt("user_id"),
                                                       null));
                        result = jsonObject.getString("msg");
                    } catch( JSONException jsone ) {
                        e("JSON ERROR", jsone.toString());
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
                Toast.makeText(getContext(), "새로운 메모가 생성되었습니다.", Toast.LENGTH_SHORT).show();

                myMemoAdapter.notifyDataSetChanged();

                if ( attachKeyword != null && attachKeyword.equals("keyword") ) {
                    // 번들객체에 메모 데이터 저장
                    Intent intent = new Intent(getActivity(), KeywordActivity.class);
                    intent.putExtra("memo_id", myMemoAdapter.getMemoList().get(0).memo_id);
                    // 키워드 액티비티 호출
                    startActivity(intent);
                }
            }  else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncUserMemos extends AsyncTask<Integer, Integer, String> {

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new Dialog(getContext(), R.style.SemoDialog);
            progressDialog.setCancelable(true);

            ProgressBar progressbar = new ProgressBar(getContext());
            progressbar.setIndeterminateDrawable(getContext().getResources().getDrawable(R.drawable.semo_progress_dialog));

            progressDialog.addContentView(progressbar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {

            // 유저 ID
            //userId = params[0];
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
                        .url(NetworkDefineConstant.SERVER_URL_SELECT_USER_MEMO + userId)
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
                        totalMemo = jsonFromServer.getInt("total");
                        JSONObject jsonAllMemeos = jsonFromServer.getJSONObject("data");
                        JSONArray jsonArrayPinMemos = jsonAllMemeos.getJSONArray("pined");
                        JSONArray jsonArrayUnpinMemos = jsonAllMemeos.getJSONArray("unpined");

                        result = jsonFromServer.getString("msg");

                        String keyword = "";
                        JSONObject jsonObjectMemo = null;
                        myMemoAdapter.getMemoList().clear();
                        for ( int i = 0; i < jsonArrayPinMemos.length() + jsonArrayUnpinMemos.length(); i++ ) {
                            if ( i < jsonArrayUnpinMemos.length() ) {
                                jsonObjectMemo = jsonArrayUnpinMemos.getJSONObject(i);
                            } else {
                                jsonObjectMemo = jsonArrayPinMemos.getJSONObject(i - jsonArrayUnpinMemos.length());
                            }
                            // 메모의 유저 정보
                            JSONObject jsonObjectOriginAuthor = jsonObjectMemo.getJSONObject("origin_author");
                            User originAuthor = new User(jsonObjectOriginAuthor.getInt("user_id"),
                                                         jsonObjectOriginAuthor.getString("nickname"),
                                                         jsonObjectOriginAuthor.getString("photo"));
                            // 키워드 정보
                            JSONArray jsonArrayKeyword = jsonObjectMemo.getJSONArray("Tags");

                            if ( jsonArrayKeyword.length() > 0 ) {
                                keyword = jsonArrayKeyword.getJSONObject(0).getString("name");
                            }

                            myMemoAdapter.bringAllMemo(new Memo(jsonObjectMemo.getInt("memo_id"),
                                                           jsonObjectMemo.getBoolean("pin"),
                                                           jsonObjectMemo.getString("body"),
                                                           jsonObjectMemo.getString("createdAt"),
                                                           jsonObjectMemo.getInt("user_id"),
                                                           originAuthor,
                                                           keyword));

                            keyword = ""; // 키워드 초기화
                        }

                        pinMemoCount = jsonArrayPinMemos.length(); // 핀메모 개수

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
                    // 리스폰스 개체에 실질적인 연결이 되어있으므로
                    // 3.* 버전 이상에서는 반드시 리스폰스를 닫는다.
                    response.close();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if ( result != null ) {
                Log.e("onPostExecute", result);

                myMemoAdapter.notifyDataSetChanged();
            } else {
                Log.e("onPostExecute", "result is empty");
            }

            if ( myMemoAdapter.getItemCount() < 1 ) {
                // 메모 없을때 리사이클러뷰 안보임
                default_mymemo.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                default_mymemo.setVisibility(View.GONE);
            }
        }
    }

    public class AsyncShareMemos extends AsyncTask<Integer, Intent, String> {

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new Dialog(getContext(), R.style.SemoDialog);
            progressDialog.setCancelable(true);

            ProgressBar progressbar = new ProgressBar(getContext());
            progressbar.setIndeterminateDrawable(getContext().getResources().getDrawable(R.drawable.semo_progress_dialog));

            progressDialog.addContentView(progressbar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {
            //userId = params[0];
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
                        .url(NetworkDefineConstant.SERVER_URL_SELECT_SHARE_MEMO + userId + "&category=shared")
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
                        JSONObject jsonAllShareMemeos = jsonFromServer.getJSONObject("data");
                        JSONArray jsonArrayPinShareMemos = jsonAllShareMemeos.getJSONArray("pined");
                        JSONArray jsonArrayUnpinShareMemos = jsonAllShareMemeos.getJSONArray("unpined");

                        result = jsonFromServer.getString("msg");

                        String keyword = "";
                        JSONObject jsonObjectShareMemo = null;
                        adapterShare.getShareMemoList().clear();
                        for ( int i = 0; i < jsonArrayPinShareMemos.length() + jsonArrayUnpinShareMemos.length(); i++ ) {
                            if ( i < jsonArrayUnpinShareMemos.length() ) {
                                jsonObjectShareMemo = jsonArrayUnpinShareMemos.getJSONObject(i);
                            } else {
                                jsonObjectShareMemo = jsonArrayPinShareMemos.getJSONObject(i - jsonArrayUnpinShareMemos.length());
                            }

                            // 공유메모 아이디
                            int sharingId = jsonObjectShareMemo.getInt("sharing_id");
                            // 공유메모 핀
                            boolean pin = jsonObjectShareMemo.getBoolean("pin");
                            // 공유메모
                            JSONObject jsonObjectMemo = jsonObjectShareMemo.getJSONObject("Memo");
                            JSONArray jsonArrayKeyword = jsonObjectMemo.getJSONArray("Tags");

                            if ( jsonArrayKeyword.length() > 0 ) {
                                keyword = jsonArrayKeyword.getJSONObject(0).getString("name");
                            }

                            Memo memo = new Memo(jsonObjectMemo.getInt("memo_id"),
                                                 jsonObjectMemo.getString("body"),
                                                 jsonObjectMemo.getBoolean("shared"),
                                                 jsonObjectMemo.getString("createdAt"),
                                                 jsonObjectMemo.getInt("user_id"),
                                                 keyword);
                            keyword = "";

                            // 작성 시간
                            String createdAt = jsonObjectShareMemo.getString("createdAt");
                            // 공유 친구들
                            JSONArray jsonArrayCoAuthors = jsonObjectMemo.getJSONArray("coAuthors");
                            ArrayList<User> coAuthors = new ArrayList<>();
                            for ( int j = 0; j < jsonArrayCoAuthors.length(); j++ ) {
                                JSONObject jsonObjectShareUser = jsonArrayCoAuthors.getJSONObject(j);

                                User shareUser = new User(jsonObjectShareUser.getInt("user_id"),
                                                          jsonObjectShareUser.getString("nickname"),
                                                          jsonObjectShareUser.getString("photo"));

                                coAuthors.add(shareUser);
                            }

                            adapterShare.bringAllMemo(new ShareMemo(sharingId, pin, memo, createdAt, coAuthors));
                        }

                        pinShareMemoCount = jsonArrayPinShareMemos.length(); // 핀메모 개수
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
                    // 리스폰스 개체에 실질적인 연결이 되어있으므로
                    // 3.* 버전 이상에서는 반드시 리스폰스를 닫는다.
                    response.close();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if ( result != null ) {
                Log.e("onPostExecute", result);

                adapterShare.notifyDataSetChanged();
            } else {
                Log.e("onPostExecute", "result is empty");
            }

            // 메모 없을때 리사이클러뷰 안보임
            if ( adapterShare.getItemCount() < 1 ) {
                // 메모 없을때 리사이클러뷰 안보임
                recyclerView.setVisibility(View.GONE);
                default_mymemo.setVisibility(View.INVISIBLE);
            } else {
                default_mymemo.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    public class AsyncNewShareMemoInsert extends AsyncTask<String, Integer, String> {
        // 새로운 공유 메모생성
        String newShareMemo;
        String attachKeyword = null;

        @Override
        protected String doInBackground(String... params) {
            newShareMemo = params[0];

            if ( params.length > 1 ) {
                attachKeyword = params[1];
            }

            // 서버 요청 결과(성공시 true, 실패시 false)
            boolean flag = false;
            // 서버 요청 성공시 응답받은 JSON 값
            String result = "";

            Response response = null;
            OkHttpClient toServer = null;

            try{
                toServer = OkHttpInitSingtonManager.getOkHttpClient();
                //요청 Form세팅(멀티파트 폼 데이터 형식)
                RequestBody postBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("user_id", String.valueOf(userId))  // 유저의 primary key
                        .addFormDataPart("body", newShareMemo) // 새로 만든 메모
                        .addFormDataPart("shared", String.valueOf(true))
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_INSERT_MEMO)
                        .post(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                // 응답결과에 따른 처리
                if( flag ){
                    returedJSON = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(returedJSON);
                        result = jsonObject.getString("msg");

                        JSONObject jsonObjectShareMemo = jsonObject.getJSONObject("newMemo");
                        // 공유메모 아이디
                        int sharingId = jsonObject.getInt("sharing_id");
                        // 삽입한 공유메모 아이디값
                        lastShareMemoId = jsonObjectShareMemo.getInt("memo_id");
                        // 핀
                        boolean shared = jsonObjectShareMemo.getBoolean("shared");
                        // 메모 내용
                        String body = jsonObjectShareMemo.getString("body");
                        // 핀
                        boolean pin = jsonObjectShareMemo.getBoolean("pin");
                        // 작성시간
                        String createdAt = jsonObjectShareMemo.getString("createdAt");
                        // 유저 아이디(오리지널 작성자)
                        int originalUserId = jsonObjectShareMemo.getInt("user_id");
                        // 공유친구 목록
                        JSONArray jsonArrayCoAuthors = jsonObjectShareMemo.getJSONArray("coAuthors");
                        ArrayList<User> coAuthors = new ArrayList<>();
                        for ( int j = 0; j < jsonArrayCoAuthors.length(); j++ ) {
                            JSONObject jsonObjectShareUser = jsonArrayCoAuthors.getJSONObject(j);

                            User shareUser = new User(jsonObjectShareUser.getInt("user_id"),
                                    jsonObjectShareUser.getString("nickname"),
                                    jsonObjectShareUser.getString("photo"));

                            coAuthors.add(shareUser);
                        }

                        // 공유메모에 들어갈 메모객체 생성
                        tmpMemoId = lastShareMemoId;
                        Memo memo = new Memo(lastShareMemoId, body, shared, createdAt, originalUserId);

                        adapterShare.addShareMemo(new ShareMemo(sharingId, pin, memo, createdAt, coAuthors));
                    } catch( JSONException jsone ) {
                        e("JSON ERROR", jsone.toString());
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
                Toast.makeText(getContext(), "새로운 공유메모가 생성되었습니다.", Toast.LENGTH_SHORT).show();

                // 공유할 친구 선택하는 액티비티 띄움
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("memoId", tmpMemoId);

                if ( attachKeyword != null && attachKeyword.equals("keyword") ) {
                    intent.putExtra("keyword", true);
                } else {
                    intent.putExtra("keyword", false);
                }

                startActivityForResult(intent, REQUEST_CODE_HOME_FRAGMENT);

                adapterShare.notifyDataSetChanged();

            }  else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncBestMemos extends AsyncTask<Integer, Integer, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new Dialog(getContext(), R.style.SemoDialog);
            progressDialog.setCancelable(true);

            ProgressBar progressbar = new ProgressBar(getContext());
            progressbar.setIndeterminateDrawable(getContext().getResources().getDrawable(R.drawable.semo_progress_dialog));

            progressDialog.addContentView(progressbar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {
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
                        .url(NetworkDefineConstant.SERVER_URL_BEST_MEMO)
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

                        JSONArray jsonArrayBestMemos = jsonFromServer.getJSONArray("data");
                        String keyword = "";
                        for ( int i = 0; i < jsonArrayBestMemos.length(); i++ ) {
                            JSONObject jsonObjectUser = jsonArrayBestMemos.getJSONObject(i).getJSONObject("origin_author");
                            User user = new User(jsonObjectUser.getInt("user_id"),
                                                 jsonObjectUser.getString("nickname"),
                                                 jsonObjectUser.getString("photo"));

                            // 키워드 정보
                            JSONArray jsonArrayKeyword = jsonArrayBestMemos.getJSONObject(i).getJSONArray("Tags");

                            if ( jsonArrayKeyword.length() > 0 ) {
                                keyword = jsonArrayKeyword.getJSONObject(0).getString("name");
                            }

                            bestMemoAdapter.addBestMemo(new BestMemo(i + 1,
                                                                     jsonArrayBestMemos.getJSONObject(i).getInt("memo_id"),
                                                                     jsonArrayBestMemos.getJSONObject(i).getString("body"),
                                                                     jsonArrayBestMemos.getJSONObject(i).getBoolean("shared"),
                                                                     jsonArrayBestMemos.getJSONObject(i).getBoolean("hidden"),
                                                                     jsonArrayBestMemos.getJSONObject(i).getBoolean("deleted"),
                                                                     jsonArrayBestMemos.getJSONObject(i).getBoolean("pin"),
                                                                     jsonArrayBestMemos.getJSONObject(i).getString("createdAt"),
                                                                     jsonArrayBestMemos.getJSONObject(i).getInt("connectionCount"),
                                                                     jsonArrayBestMemos.getJSONObject(i).getInt("user_id"),
                                                                     user,
                                                                     keyword));
                            keyword = "";
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
                    // 리스폰스 개체에 실질적인 연결이 되어있으므로
                    // 3.* 버전 이상에서는 반드시 리스폰스를 닫는다.
                    response.close();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            if ( result != null ) {
                Log.e("onPostExecute", result);

                bestMemoAdapter.notifyDataSetChanged();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 공유친구 선택 액티비티에서 back키 눌렀을때 처리
        if ( data == null ) {
            return;
        }

        if ( requestCode == REQUEST_CODE_HOME_FRAGMENT ) {

            boolean result = data.getBooleanExtra("refresh", false);

            // 공유목록 친구들을 성공적으로 추가했다면 화면 갱신
            if ( result ) {

                ArrayList<User> selectedShareUserList = (ArrayList<User>) data.getSerializableExtra("selectedShareUserList");
                adapterShare.setNewShareMemoCoAuthorList(selectedShareUserList);
                adapterShare.notifyDataSetChanged();

                if ( data.getBooleanExtra("keyword", false) ) {
                    // 번들객체에 메모 데이터 저장
                    Intent intent = new Intent(getActivity(), KeywordActivity.class);
                    intent.putExtra("memo_id", adapterShare.getShareMemoList().get(0).memo.memo_id);
                    // 키워드 액티비티 호출
                    startActivity(intent);
                }
            }
        }
    }
}
