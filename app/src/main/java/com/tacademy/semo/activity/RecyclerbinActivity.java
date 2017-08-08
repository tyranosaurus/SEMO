package com.tacademy.semo.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tacademy.semo.R;
import com.tacademy.semo.adapter.RecyclerbinRecyclerViewAdapter;
import com.tacademy.semo.application.BackPressCloserHandler;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.item.Memo;
import com.tacademy.semo.network.NetworkDefineConstant;
import com.tacademy.semo.network.OkHttpInitSingtonManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecyclerbinActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // 유저 ID
    int userId = SemoApplication.getUserId(); // 지금은 1로 고정했는데 나중에 로그인한거 받아오는 걸로 바꿔야함.
    String userNickname = SemoApplication.getUserNickname();
    String userProfile = SemoApplication.getUserProfile();
    String userEmail = SemoApplication.getUserEmail();

    View viewToolBar;
    Toolbar mToolbar;
    ActionBar mActionBar;
    DrawerLayout drawer;
    NavigationView navigationView;

    // context_recyclerbin 뷰
    LinearLayout linearLayout;
    RecyclerView recyclerView;
    RecyclerbinRecyclerViewAdapter adapter;

    // 검색버튼 눌렀는지 구분하는 boolean
    boolean searchOnOff;

    // 선택된 메모
    public ArrayList<Memo> selectedMemoList = new ArrayList<>();

    // Back키 두번 종료
    BackPressCloserHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerbin);

        // Back키 두번 종료
        backPressCloseHandler = new BackPressCloserHandler(this);

        // 툴바 설정
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        // 툴바에 사용할 커스텀 레이아웃 인플레이션
        viewToolBar = getLayoutInflater().inflate(R.layout.semo_toolbar, null);

        setToolBar(viewToolBar);

        // 인플레이션한 커스텀 툴바 세팅
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        mActionBar.setCustomView(viewToolBar, layoutParams);

        // 검색 아이콘 비활성화
        ImageView imageViewSearch = (ImageView) viewToolBar.findViewById(R.id.imageViewSearch);
        imageViewSearch.setEnabled(false);
        imageViewSearch.setVisibility(View.INVISIBLE);

        // DrawerLayout  설정
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // 네비게이션바 메뉴 아이템 선택 설정
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setAlpha(0.9f);
        navigationView.setNavigationItemSelectedListener(this); // NavigationView.OnNavigationItemSelectedListener implements 후
                                                                // onNavigationItemSelected() 오버라이드함
        // 네비게이션바 헤더부분 설정
        View navHeaderMain = navigationView.getHeaderView(0);

        // 유저 프로필 사진 - Glide설정
        CircleImageView circleImageProfle = (CircleImageView) navHeaderMain.findViewById(R.id.circleImageProfile);
        Glide.with(SemoApplication.getSemoContext())
                .load(userProfile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(circleImageProfle);
        // 유저 닉네임 + 인사말
        TextView textViewHello = (TextView) navHeaderMain.findViewById(R.id.textViewHello);
        textViewHello.setText(userNickname + "님\n반갑습니다!");
        // 이메일
        TextView textViewEmail = (TextView) navHeaderMain.findViewById(R.id.textViewEmail);
        textViewEmail.setText(userEmail);
        // 네비게이션바 나타날때 휴지통화면에서는 휴지통메뉴가 이미 눌러져있게 설정
        // 다른 네비게이션바 메뉴들도 해당 액티비티에가면 이미 눌러져 있게 설정
        navigationView.setCheckedItem(R.id.nav_recyclebin);

        // context_recyclerbin 뷰 설정
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerbinRecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);

        new AsyncUserDeleteMemos().execute(userId);

        if ( adapter.getItemCount() < 1 ) {
            linearLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            linearLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        navigationView.setCheckedItem(R.id.nav_recyclebin);
    }

    @Override
    protected void onResume() {
        super.onResume();

        navigationView.setCheckedItem(R.id.nav_recyclebin);
    }

    // 디바이스 Back 키 눌렸을때 처리
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchOnOff) {
            viewToolBar = getLayoutInflater().inflate(R.layout.semo_toolbar, null);
            setToolBar(viewToolBar);
            // 인플레이션한 커스텀 툴바 세팅
            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            mActionBar.setCustomView(viewToolBar, layoutParams);

            searchOnOff = false;
        } else if ( selectedMemoList.size() > 0 ) {
            adapter.setViewUnselected();
            selectedMemoList.clear();
        } else {
            backPressCloseHandler.onBackPressed();
            //super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(RecyclerbinActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_keword) {
            Intent intent = new Intent(RecyclerbinActivity.this, KeywordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(RecyclerbinActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    // 툴바 오른쪽에 있는 옵션메뉴 눌렀을때 팝업메뉴 띄우는 함수
    public void showToolbarOptionMenu(View view){
        PopupMenu popupMenu = new PopupMenu(this, view);
        getMenuInflater().inflate(R.menu.menu_recyclerbin_optionmenu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.restore:
                        // 메모 복원처리
                        if ( selectedMemoList.size() < 1 ) {
                            Toast.makeText(getApplicationContext(), "복원할 메모가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        for ( int i = 0; i < selectedMemoList.size(); i++ ) {
                            new AsyncMemoRestore().execute(selectedMemoList.get(i));
                        }
                        selectedMemoList.clear();
                        break;
                    case R.id.selectedDelete:
                        if ( selectedMemoList.size() < 1 ) {
                            Toast.makeText(getApplicationContext(), "삭제할 메모가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        for ( int i = 0; i < selectedMemoList.size(); i++ ) {
                            new AsyncDeleteSelectedMemo().execute(selectedMemoList.get(i));
                        }
                        selectedMemoList.clear();
                        break;
                    case R.id.allDelete:
                        new AsyncDeleteAllMemos().execute();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "올바르지 않은 메뉴입니다.", Toast.LENGTH_SHORT).show();
                        break;
                }

                return true;
            }
        });

        popupMenu.show();
    }

    public void setToolBar(View viewToolBar) {
        this.viewToolBar = viewToolBar;

        ImageView imageViewMenu = (ImageView) viewToolBar.findViewById(R.id.imageViewMenu);
        imageViewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        TextView textViewTitle = (TextView) viewToolBar.findViewById(R.id.textViewTitle);
        textViewTitle.setText("홈");

        ImageView imageViewSearch = (ImageView) viewToolBar.findViewById(R.id.imageViewSearch);
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchOnOff = true;

                View viewToolBar = getLayoutInflater().inflate(R.layout.toolbar_search, null);

                // 인플레이션한 커스텀 툴바 세팅
                ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
                mActionBar.setCustomView(viewToolBar, layoutParams);
            }
        });

        // 여기 파이널로 쓰인거 주의할 것!
        final ImageView imageViewSettings = (ImageView) viewToolBar.findViewById(R.id.imageViewSettings);
        imageViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToolbarOptionMenu(imageViewSettings);
            }
        });
    }

    public class AsyncUserDeleteMemos extends AsyncTask<Integer, Integer, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new Dialog(RecyclerbinActivity.this, R.style.SemoDialog);
            progressDialog.setCancelable(true);

            ProgressBar progressbar = new ProgressBar(RecyclerbinActivity.this);
            progressbar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.semo_progress_dialog));

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
                        .url(NetworkDefineConstant.SERVER_URL_SELECT_DELETE_MEMO + userId)
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
                        JSONArray jsonArrayDeleteMemeos = jsonFromServer.getJSONArray("data");

                        JSONObject jsonObjectDeleteMemo = null;
                        for ( int i = 0; i < jsonArrayDeleteMemeos.length(); i++ ) {
                            jsonObjectDeleteMemo = jsonArrayDeleteMemeos.getJSONObject(i);
                            adapter.addMemo(new Memo(jsonObjectDeleteMemo.getInt("memo_id"),
                                    jsonObjectDeleteMemo.getBoolean("pin"),
                                    jsonObjectDeleteMemo.getString("body")));
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
            } else {
                Log.e("onPostExecute", "result is empty");
            }

            if ( adapter.getItemCount() < 1 ) {
                // 메모 없을때 리사이클러뷰 안보임
                linearLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
            }
        }
    }

    public class AsyncMemoRestore extends AsyncTask <Memo, Integer, String> {
        Memo restoreMemo;

        @Override
        protected String doInBackground(Memo... params) {
            // 복원할 메모
            restoreMemo = params[0];
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
                        .add("memo_id", String.valueOf(restoreMemo.memo_id))
                        .add("delete", "false")
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_DELETE_MEMO)
                        .post(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                if( flag ){
                    returedJSON = response.body().string();

                    // 복원된 메모 adapter 제거 처리
                    adapter.removeMemo(restoreMemo);
                    try {
                        JSONObject jsonFromServer = new JSONObject(returedJSON);

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
                // 메모 복원 후 뷰 갱신
                Toast.makeText(getApplicationContext(), "메모가 복원되었습니다.", Toast.LENGTH_SHORT).show();

                adapter.notifyDataSetChanged();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }

            if ( adapter.getItemCount() < 1 ) {
                // 메모 없을때 리사이클러뷰 안보임
                linearLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
            }
        }
    }

    public class AsyncDeleteSelectedMemo extends AsyncTask<Memo, Integer, String> {
        Memo deleteMemo;

        @Override
        protected String doInBackground(Memo... params) {
            deleteMemo = params[0];
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
                        .add("memo_id", String.valueOf(deleteMemo.memo_id))
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_DELETE_SELECTED_MEMO)
                        .delete(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                if( flag ){
                    returedJSON = response.body().string();

                    // 복원된 메모 adapter 제거 처리
                    adapter.removeMemo(deleteMemo);
                    try {
                        JSONObject jsonFromServer = new JSONObject(returedJSON);

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
                // 메모 삭제 후 뷰 갱신
                Toast.makeText(getApplicationContext(), "선택 메모를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }

            if ( adapter.getItemCount() < 1 ) {
                // 메모 없을때 리사이클러뷰 안보임
                linearLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
            }
        }
    }

    public class AsyncDeleteAllMemos extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... params) {
            // userId = params[0];
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
                        .add("user_id", String.valueOf(userId))
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_DELETE_ALL_MEMO)
                        .delete(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                if( flag ){
                    returedJSON = response.body().string();

                    adapter.removeAllMemo();
                    try {
                        JSONObject jsonFromServer = new JSONObject(returedJSON);

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
                // 메모 삭제 후 뷰 갱신
                Toast.makeText(getApplicationContext(), "전체 메모를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }

            if ( adapter.getItemCount() < 1 ) {
                linearLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                linearLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
