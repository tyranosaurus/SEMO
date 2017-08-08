package com.tacademy.semo.activity;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tacademy.semo.R;
import com.tacademy.semo.adapter.HomeRecyclerViewAdapter;
import com.tacademy.semo.adapter.ShareMemoRecyclerViewAdapter;
import com.tacademy.semo.application.BackPressCloserHandler;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.fragment.HomeFragment;
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

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    final int REQUEST_CODE_HOME_ACTIVITY_TO_SHARE_ACTIVITY = 101;
    final int REQUEST_CODE_HOME_ACTIVITY_TO_DETAIL_ACTIVITY = 102;
    final int REQUEST_CODE_SHARE_FRAGMENT_TO_DETAIL_ACTIVITY = 103;
    final int REQUEST_CODE_SHARE_FRAGMENT_TO_KEYWORD_ACTIVITY = 104;

    int userId = SemoApplication.getUserId();
    String userNickname = SemoApplication.getUserNickname();
    String userProfile = SemoApplication.getUserProfile();
    String userEmail = SemoApplication.getUserEmail();

    int pagerIndex = 0;

    View viewToolBar;
    Toolbar mToolbar;
    ActionBar mActionBar;
    DrawerLayout drawer;
    NavigationView navigationView;

    TabLayout mTablayout;
    ViewPager mViewPager;

    // 검색버튼 눌렀는지 구분하는 boolean
    boolean searchOnOff;

    // 선택된 메모 리스트
    Memo toShareMemo;
    ArrayList<User> shareUserList = new ArrayList<>();

    public ArrayList<Memo> selectedMemoList = new ArrayList<>();
    public ArrayList<ShareMemo> selectedShareMemoList = new ArrayList<>();

    // 현재 프래그먼트
    HomeFragment currentFragment;
    ArrayList<HomeFragment> homeFragmentList = new ArrayList<>();

    // 어댑터
    HomeRecyclerViewAdapter homeRecyclerViewAdapter;
    ShareMemoRecyclerViewAdapter shareMemoRecyclerViewAdapter;

    // 검색
    EditText editTextSearch;

    // 키보드 내리기
    InputMethodManager inputMethodManager;

    // 메모 복사
    ClipboardManager clipboardManager;

    // Back키 두번 종료
    BackPressCloserHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Back키 두번 종료
        backPressCloseHandler = new BackPressCloserHandler(this);

        // 키보드
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // 클립보드 매니저
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        // 툴바 설정
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        // 툴바에 사용할 커스텀 레이아웃 인플레이션
        viewToolBar = getLayoutInflater().inflate(R.layout.semo_toolbar, null);

        setNavigationBar(viewToolBar);

        // 인플레이션한 커스텀 툴바 세팅
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        mActionBar.setCustomView(viewToolBar, layoutParams);

        // 탭 추가
        mTablayout = (TabLayout) findViewById(R.id.tabLayout);
        mTablayout.addTab(mTablayout.newTab().setText("내가 쓴 메모"));
        mTablayout.addTab(mTablayout.newTab().setText("공유 메모"));
        mTablayout.addTab(mTablayout.newTab().setText("베스트 메모"));
        mTablayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // 뷰 페이저 설정
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        HomeFragmentPagerAdapter adapter = new HomeFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTablayout));
        mTablayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                pagerIndex = tab.getPosition();
                mViewPager.setCurrentItem(pagerIndex);
                currentFragment = homeFragmentList.get(pagerIndex);

                homeRecyclerViewAdapter = homeFragmentList.get(0).getHomeRecyclerViewAdapter();
                shareMemoRecyclerViewAdapter = homeFragmentList.get(1).getShareMemoRecyclerViewAdapter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

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


        // 네비게이션바 나타날때 홈화면에서는 홈메뉴가 이미 눌러져있게 설정
        navigationView.setCheckedItem(R.id.nav_home);

        //FCM
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseInstanceId.getInstance().getToken();
    }

    @Override
    protected void onStart() {
        super.onStart();

        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();

        navigationView.setCheckedItem(R.id.nav_home);
    }

    // 디바이스 Back 키 눌렸을때 처리
    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if ( selectedMemoList.size() > 0 && currentFragment.getIndex() == 0 ) {
            currentFragment.getHomeRecyclerViewAdapter().setViewUnselected();

            return;
        }

        if ( selectedShareMemoList.size() > 0 && currentFragment.getIndex() == 1 ) {
            currentFragment.getShareMemoRecyclerViewAdapter().setViewUnselected();

            return;
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchOnOff) {
            viewToolBar = getLayoutInflater().inflate(R.layout.semo_toolbar, null);
            setNavigationBar(viewToolBar);
            // 인플레이션한 커스텀 툴바 세팅
            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            mActionBar.setCustomView(viewToolBar, layoutParams);

            searchOnOff = false;

            if ( currentFragment.getIndex() == 0 ) {
                currentFragment.callAsyncUserMemos();
            } else if ( currentFragment.getIndex() == 1 ) {
                currentFragment.callAsyncShareUserMemos();
            }

        }  else {
            backPressCloseHandler.onBackPressed();
            //super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_keword) {
            Intent intent = new Intent(HomeActivity.this, KeywordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_recyclebin) {
            Intent intent = new Intent(HomeActivity.this, RecyclerbinActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    // 툴바 오른쪽에 있는 옵션메뉴 눌렀을때 팝업메뉴 띄우는 함수
    public void showToolbarOptionMenu(View view){
        PopupMenu popupMenu = new PopupMenu(this, view);

        if ( pagerIndex == 0 ) {
            getMenuInflater().inflate(R.menu.menu_home_optionmenu, popupMenu.getMenu());
        } else if ( pagerIndex == 1 ) {
            getMenuInflater().inflate(R.menu.menu_share_optionmenu, popupMenu.getMenu());
        } else if ( pagerIndex == 2 ) {
            getMenuInflater().inflate(R.menu.menu_best_optionmenu, popupMenu.getMenu());
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.keywordSetting:
                        // 키워드 등록 처리
                        if ( currentFragment.getIndex() == 0 ) {
                            if ( selectedMemoList.size() < 1 || selectedMemoList.size() > 1) {
                                Toast.makeText(getApplicationContext(), "한번에 하나의 메모에 키워드 등록이 가능합니다.", Toast.LENGTH_SHORT).show();
                                break;
                            } else if ( selectedMemoList.size() == 1 ) {
                                // 번들객체에 메모 데이터 저장
                                Intent intent = new Intent(HomeActivity.this, KeywordActivity.class);
                                intent.putExtra("memo_id", selectedMemoList.get(0).memo_id);

                                // 키워드 액티비티 호출
                                startActivityForResult(intent, REQUEST_CODE_SHARE_FRAGMENT_TO_KEYWORD_ACTIVITY);
                            }
                        } else if ( currentFragment.getIndex() == 1 ) {
                            if ( selectedShareMemoList.size() < 1 || selectedShareMemoList.size() > 1) {
                                Toast.makeText(getApplicationContext(), "한번에 하나의 메모에 키워드 등록이 가능합니다.", Toast.LENGTH_SHORT).show();
                                break;
                            } else if ( selectedShareMemoList.size() == 1 ) {
                                // 번들객체에 메모 데이터 저장
                                Intent intent = new Intent(HomeActivity.this, KeywordActivity.class);
                                intent.putExtra("memo_id", selectedShareMemoList.get(0).memo.memo_id);
                                // 키워드 액티비티 호출
                                startActivityForResult(intent, REQUEST_CODE_SHARE_FRAGMENT_TO_KEYWORD_ACTIVITY);
                            }
                        }
                        break;
                    case R.id.copyMemo:
                        // 메모 내용 복사
                        String copyMemo = "";
                        if ( currentFragment.getIndex() == 0 ) {
                            for ( int i = 0; i < selectedMemoList.size(); i++ ) {
                                copyMemo += selectedMemoList.get(i).body + "\n\n";
                            }
                        } else if ( currentFragment.getIndex() == 1) {
                            for ( int i = 0; i < selectedShareMemoList.size(); i++ ) {
                                copyMemo += selectedShareMemoList.get(i).memo.body + "\n\n";
                            }
                        }

                        clipboardManager.setText(copyMemo);
                        Toast.makeText(getApplicationContext(), "메모가 복사되었습니다.", Toast.LENGTH_SHORT).show();

                        break;
                    case R.id.sharing:
                        // 내가 쓴 메모를 공유메모로 바꿈
                        if ( selectedMemoList.size() < 1 || selectedMemoList.size() > 1 ) {
                            Toast.makeText(getApplicationContext(), "메모 공유는 한 메모씩 가능합니다.", Toast.LENGTH_SHORT).show();
                            selectedMemoList.clear();
                            break;
                        } else {
                            homeRecyclerViewAdapter = homeFragmentList.get(0).getHomeRecyclerViewAdapter();

                            toShareMemo = selectedMemoList.get(0);

                            // 공유할 친구 선택하는 액티비티 띄움
                            Intent intent = new Intent(HomeActivity.this, ShareActivity.class);
                            intent.putExtra("userId", toShareMemo.userId);
                            intent.putExtra("memoId", toShareMemo.memo_id);
                            startActivityForResult(intent, REQUEST_CODE_HOME_ACTIVITY_TO_SHARE_ACTIVITY);
                        }
                        break;
                    case R.id.cancelSharing:
                        // 공유취소 처리
                        if ( selectedShareMemoList.size() < 1 ) {
                            Toast.makeText(getApplicationContext(), "공유를 취소할 메모가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        for ( int i = 0; i < selectedShareMemoList.size(); i++ ) {
                            new AsyncShareMemoCancel().execute(selectedShareMemoList.get(i));
                        }

                        selectedShareMemoList.clear();
                        break;
                    case R.id.recyclerbin:
                        // 메모삭제 처리
                        if ( selectedMemoList.size() < 1 ) {
                            Toast.makeText(getApplicationContext(), "삭제할 메모가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        for ( int i = 0; i < selectedMemoList.size(); i++ ) {
                            new AsyncMemoDelete().execute(selectedMemoList.get(i));
                        }
                        selectedMemoList.clear();
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

    public void setNavigationBar(View viewToolBar) {
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

                if ( currentFragment.getIndex() == 2 ) {
                    Toast.makeText(getApplicationContext(), "베스트메모에서는 검색할 수 없습니다.", Toast.LENGTH_SHORT).show();

                    return;
                }

                searchOnOff = true;

                View viewToolBar = getLayoutInflater().inflate(R.layout.toolbar_search, null);

                editTextSearch = (EditText) viewToolBar.findViewById(R.id.editTextSearch);
                editTextSearch.requestFocus();
                editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                        // 검색어가 비었을때 처리
                        if ( editTextSearch.getText().toString().trim().isEmpty() || editTextSearch.getText().toString().trim().equals("") ) {
                            Toast.makeText(getApplicationContext(), "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();

                            return true;
                        }


                        if( currentFragment.getIndex() == 0 && actionId == EditorInfo.IME_ACTION_SEARCH )
                        {
                            // 내 메모 검색 어싱크태스크
                            new AsyncMyMemoSearch().execute(editTextSearch.getText().toString());

                            editTextSearch.setText("");
                            inputMethodManager.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);

                            return true;
                        } else if ( currentFragment.getIndex() == 1 && actionId == EditorInfo.IME_ACTION_SEARCH ) {
                            // 공유메모 검색 어싱크태스크
                            new AsyncShareMemoSearch().execute(editTextSearch.getText().toString());

                            editTextSearch.setText("");
                            inputMethodManager.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);

                            return true;
                        }

                        return false;
                    }
                });

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
                if ( currentFragment.getIndex() == 2 ) {
                    Toast.makeText(getApplicationContext(), "베스트메모에서는 옵션메뉴가 없습니다.", Toast.LENGTH_SHORT).show();

                    return;
                }

                showToolbarOptionMenu(imageViewSettings);
            }
        });
    }

    public class HomeFragmentPagerAdapter extends FragmentPagerAdapter {

        public HomeFragmentPagerAdapter(FragmentManager fm) {
            super(fm);

            homeFragmentList.add(HomeFragment.newInstance(0));
            homeFragmentList.add(HomeFragment.newInstance(1));
            homeFragmentList.add(HomeFragment.newInstance(2));

            currentFragment = homeFragmentList.get(0);
        }

        @Override
        public Fragment getItem(int position) {
            return homeFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return homeFragmentList.size();
        }
    }

    public class AsyncMemoDelete extends AsyncTask<Memo, Integer, String> {
        Memo deleteMemo;

        @Override
        protected String doInBackground(Memo... params) {
            // 삭제할 메모
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
                        .add("delete", String.valueOf(true))
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

                    // 삭제된 메모 adapter 제거 처리
                    currentFragment.getHomeRecyclerViewAdapter().removeMemo(deleteMemo);
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
                Toast.makeText(getApplicationContext(), "메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                currentFragment.getHomeRecyclerViewAdapter().notifyDataSetChanged();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncShareMemoCancel extends AsyncTask<ShareMemo, Integer, String> {
        ShareMemo cancelShareMemo;

        @Override
        protected String doInBackground(ShareMemo... params) {
            cancelShareMemo = params[0];
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
                        .add("memo_id", String.valueOf(cancelShareMemo.memo.memo_id))
                        .add("user_id", String.valueOf(cancelShareMemo.memo.userId))
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_CANCEL_SHARE_MEMO)
                        .delete(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                if( flag ){
                    returedJSON = response.body().string();

                    try {
                        JSONObject jsonFromServer = new JSONObject(returedJSON);

                        // 공유 취소된 메모 adapter 제거 처리
                        currentFragment.getShareMemoRecyclerViewAdapter().removeShareMemo(cancelShareMemo);
                        // 공유 취소된 메모 내가 쓴 메모로 이동

                        homeRecyclerViewAdapter.addMemo(new Memo( jsonFromServer.getInt("newMemoId"),
                                                                  cancelShareMemo.pin,
                                                                  cancelShareMemo.memo.body,
                                                                  cancelShareMemo.createdAt,
                                                                  cancelShareMemo.memo.userId,
                                                                  null));

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
                Toast.makeText(getApplicationContext(), "메모의 공유가 취소되었습니다.", Toast.LENGTH_SHORT).show();

                currentFragment.getShareMemoRecyclerViewAdapter().notifyDataSetChanged();
                homeRecyclerViewAdapter.notifyDataSetChanged();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }
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
                        JSONObject jsonObjectShareMemo = jsonFromServer.getJSONObject("data");
                        JSONObject jsonObjectSharing = jsonObjectShareMemo.getJSONObject("sharing");
                        JSONObject jsonObjectSharedUser = jsonObjectShareMemo.getJSONObject("sharedUser");

                        ArrayList<User> userList = new ArrayList<>();
                        for ( int i = 0; i < shareUserList.size(); i++ ) {
                            userList.add(shareUserList.get(i));
                        }

                        shareMemoRecyclerViewAdapter.addShareMemo(new ShareMemo(jsonObjectSharing.getInt("sharing_id"),
                                jsonObjectSharing.getBoolean("pin"),
                                toShareMemo,
                                jsonObjectSharing.getString("createdAt"),
                                userList));

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

                Toast.makeText(getApplicationContext(), "새로운 공유메모가 생성되었습니다.", Toast.LENGTH_SHORT).show();

                homeRecyclerViewAdapter.removeMemo(toShareMemo);
                homeRecyclerViewAdapter.notifyDataSetChanged();
                shareMemoRecyclerViewAdapter.notifyDataSetChanged();
                selectedMemoList.clear();
            } else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncMyMemoSearch extends AsyncTask<String, Integer, String> {
        Dialog progressDialog;
        String searchBody;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new Dialog(HomeActivity.this, R.style.SemoDialog);
            progressDialog.setCancelable(true);

            ProgressBar progressbar = new ProgressBar(HomeActivity.this);
            progressbar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.semo_progress_dialog));

            progressDialog.addContentView(progressbar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            searchBody = params[0];
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
                        .url(NetworkDefineConstant.SERVER_URL_SELECT_USER_MEMO + userId + "&query=" + searchBody)
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

                        JSONObject jsonAllMemeos = jsonFromServer.getJSONObject("data");
                        JSONArray jsonArrayPinMemos = jsonAllMemeos.getJSONArray("pined");
                        JSONArray jsonArrayUnpinMemos = jsonAllMemeos.getJSONArray("unpined");

                        result = jsonFromServer.getString("msg");

                        String keyword = "";
                        JSONObject jsonObjectMemo = null;
                        homeRecyclerViewAdapter = homeFragmentList.get(0).getHomeRecyclerViewAdapter();
                        homeRecyclerViewAdapter.getMemoList().clear();//검색메모 가져오기위해 원래 메모 비움
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

                            homeRecyclerViewAdapter.bringAllMemo(new Memo(jsonObjectMemo.getInt("memo_id"),
                                                                          jsonObjectMemo.getBoolean("pin"),
                                                                          jsonObjectMemo.getString("body"),
                                                                          jsonObjectMemo.getString("createdAt"),
                                                                          jsonObjectMemo.getInt("user_id"),
                                                                          originAuthor,
                                                                          keyword));

                            keyword = ""; // 키워드 초기화
                        }

                        HomeFragment.pinMemoCount = jsonArrayPinMemos.length(); // 핀메모 개수

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

                homeRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncShareMemoSearch extends AsyncTask<String, Integer, String> {
        Dialog progressDialog;
        String searchBody;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new Dialog(HomeActivity.this, R.style.SemoDialog);
            progressDialog.setCancelable(true);

            ProgressBar progressbar = new ProgressBar(HomeActivity.this);
            progressbar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.semo_progress_dialog));

            progressDialog.addContentView(progressbar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            searchBody = params[0];
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
                        .url(NetworkDefineConstant.SERVER_URL_SELECT_USER_MEMO + userId + "&category=shared&query=" + searchBody)
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
                        shareMemoRecyclerViewAdapter = homeFragmentList.get(1).getShareMemoRecyclerViewAdapter();
                        shareMemoRecyclerViewAdapter.getShareMemoList().clear();//검색메모 가져오기위해 원래 메모 비움
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

                            shareMemoRecyclerViewAdapter.bringAllMemo(new ShareMemo(sharingId, pin, memo, createdAt, coAuthors));
                        }

                        HomeFragment.pinShareMemoCount = jsonArrayPinShareMemos.length(); // 핀메모 개수
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

                homeRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 공유친구 선택 액티비티에서 back키 눌렀을때 처리
        if ( data == null ) {
            return;
        }

        if ( requestCode == REQUEST_CODE_HOME_ACTIVITY_TO_SHARE_ACTIVITY) {

            boolean result = data.getBooleanExtra("refresh", false);
            shareMemoRecyclerViewAdapter = homeFragmentList.get(1).getShareMemoRecyclerViewAdapter();

            // 공유목록 친구들을 성공적으로 추가했다면 화면 갱신
            if ( result ) {
                ArrayList<User> selectedShareUserList = (ArrayList<User>) data.getSerializableExtra("selectedShareUserList");

                shareUserList.clear();
                shareUserList.add(new User(userId, userNickname, userProfile, userEmail));

                for ( int i = 0; i < selectedShareUserList.size(); i++ ) {
                    shareUserList.add(selectedShareUserList.get(i));
                }

                new AsyncShareUserAdd().execute(toShareMemo.memo_id, userId);
            }
        } else if ( requestCode == REQUEST_CODE_SHARE_FRAGMENT_TO_KEYWORD_ACTIVITY ) {
            if ( data.getBooleanExtra("isKeyword", false) ) {
                if ( currentFragment.getIndex() == 0 ) {
                    homeRecyclerViewAdapter = homeFragmentList.get(0).getHomeRecyclerViewAdapter();

                    selectedMemoList.get(0).keyword = data.getStringExtra("attachKeyword");
                    homeRecyclerViewAdapter.notifyDataSetChanged();

                    if ( selectedMemoList.size() > 0 && currentFragment.getIndex() == 0 ) {
                        currentFragment.getHomeRecyclerViewAdapter().setViewUnselected();
                    }
                } else if ( currentFragment.getIndex() == 1) {
                    shareMemoRecyclerViewAdapter = homeFragmentList.get(1).getShareMemoRecyclerViewAdapter();

                    selectedShareMemoList.get(0).memo.keyword = data.getStringExtra("attachKeyword");
                    shareMemoRecyclerViewAdapter.notifyDataSetChanged();

                    if ( selectedShareMemoList.size() > 0 && currentFragment.getIndex() == 1 ) {
                        currentFragment.getShareMemoRecyclerViewAdapter().setViewUnselected();
                    }
                }
            }
        } else if ( requestCode == REQUEST_CODE_HOME_ACTIVITY_TO_DETAIL_ACTIVITY ) {
            int modifiedMemoId = data.getIntExtra("modifiedMemoId", -1);
            String modifiedBody = data.getStringExtra("modifiedBody");
            homeRecyclerViewAdapter = homeFragmentList.get(0).getHomeRecyclerViewAdapter();

            for ( int i = 0; i < homeRecyclerViewAdapter.getMemoList().size(); i++ ) {
                if ( homeRecyclerViewAdapter.getMemoList().get(i).memo_id == modifiedMemoId ) {
                    homeRecyclerViewAdapter.getMemoList().get(i).body = modifiedBody;
                    homeRecyclerViewAdapter.notifyDataSetChanged();

                    break;
                }
            }
        } else if ( requestCode == REQUEST_CODE_SHARE_FRAGMENT_TO_DETAIL_ACTIVITY) {
            int modifiedMemoId = data.getIntExtra("modifiedMemoId", -1);
            String modifiedBody = data.getStringExtra("modifiedBody");
            shareMemoRecyclerViewAdapter = homeFragmentList.get(1).getShareMemoRecyclerViewAdapter();

            for ( int i = 0; i < shareMemoRecyclerViewAdapter.getShareMemoList().size(); i++ ) {
                if ( shareMemoRecyclerViewAdapter.getShareMemoList().get(i).memo.memo_id == modifiedMemoId ) {
                    shareMemoRecyclerViewAdapter.getShareMemoList().get(i).memo.body = modifiedBody;
                    shareMemoRecyclerViewAdapter.notifyDataSetChanged();

                    break;
                }
            }
        }
    }
}