package com.tacademy.semo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tacademy.semo.R;
import com.tacademy.semo.application.BackPressCloserHandler;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.fragment.KeywordFragment;
import com.tacademy.semo.item.Keyword;
import com.tacademy.semo.item.RecommendKeyword;
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

import static android.util.Log.e;
import static com.tacademy.semo.R.id.viewPager;

public class KeywordActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // 유저 ID
    int userId = SemoApplication.getUserId();
    String userNickname = SemoApplication.getUserNickname();
    String userProfile = SemoApplication.getUserProfile();
    String userEmail = SemoApplication.getUserEmail();

    InputMethodManager inputMethodManager;
    // 키워드 붙일 메모 아이디
    int memo_id = -1;

    Toolbar mToolbar;
    ActionBar mActionBar;
    ViewPager mViewPager;
    DrawerLayout drawer;
    NavigationView navigationView;

    // context_keyword 뷰
    EditText editTextWriteKeyword;
    ImageView imageViewPlusKeyword;
    ImageView imageViewDot01;
    ImageView imageViewDot02;
    ImageView imageViewDot03;

    // 키워드
    KeywordFragmentPagerAdapter adapter;
    String attachKeyword;

    int lastKeywordId;
    TextView textViewDefault;
    LinearLayout linearLayoutMyKeyword;

    // 종류에 맞는 키워드를 담을 리스트
    ArrayList<RecommendKeyword> recommendKeywordList = new ArrayList<>();
    ArrayList<Keyword> myKeywordList = new ArrayList<>();

    public ArrayList<Keyword> selectedKeywordList = new ArrayList<>();
    public ArrayList<TextView> selectedTextViewList = new ArrayList<>();

    public ArrayList<RecommendKeyword> selectedRecommendKeywordList = new ArrayList<>();
    public ArrayList<TextView> selectedRecommendTextViewList = new ArrayList<>();

    public ArrayList<TextView> totalRecommendKeywordList = new ArrayList<>();

    // Back키 두번 종료
    BackPressCloserHandler backPressCloseHandler;

    // 키워드 붙을 메모
    String keyword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyword);

        // Back키 두번 종료
        backPressCloseHandler = new BackPressCloserHandler(this);

        // 키워드붙일 메모
        memo_id = getIntent().getIntExtra("memo_id", -1);
        //=======================================================================================================
        if ( getIntent().getStringExtra("memo_keyword") != null ) {
            keyword = getIntent().getStringExtra("memo_keyword");
        }

        // 툴바 설정
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        // 툴바에 사용할 커스텀 레이아웃 인플레이션
        View viewToolBar = getLayoutInflater().inflate(R.layout.toolbar_keyword, null);

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

        TextView textViewKeyword = (TextView) viewToolBar.findViewById(R.id.textViewKeyword);
        textViewKeyword.setText("키워드");

        ImageView imageViewRecyclerbin = (ImageView) viewToolBar.findViewById(R.id.imageViewRecyclerbin);
        imageViewRecyclerbin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 키워드 삭제
                if ( selectedKeywordList.size() < 1 ) {
                    Toast.makeText(getApplicationContext(), "삭제할 키워드가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for ( int i = 0; i < selectedTextViewList.size(); i++ ) {
                    selectedTextViewList.get(i).setVisibility(View.GONE);
                }

                new AsyncKeywordDelete().execute(selectedKeywordList);

                // 키워드 삭제 후 화면 갱신
                myKeywordList.clear();
                recommendKeywordList.clear();
                new AsyncUserKeywords().execute();
            }
        });

        TextView textViewAttachKeyword = (TextView) viewToolBar.findViewById(R.id.textViewAttachKeyword);
        textViewAttachKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 키워드 등록 어싱크태스크 처리
                if ( selectedKeywordList.size() < 1 || selectedKeywordList.size() > 1 ) {
                    // 선택된 키워드가 여러개인 경우
                    Toast.makeText(getApplicationContext(), "메모에 하나의 키워드만 등록이 가능합니다.", Toast.LENGTH_SHORT).show();

                    return;
                } else if ( selectedKeywordList.size() == 1 ) {
                    // 오직 키워드 하나만 등록
                    new AsyncMemoKeywordAdd().execute(memo_id, selectedKeywordList.get(0).keyword_id);
                }
            }
        });

        // 인플레이션한 커스텀 툴바 세팅
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        mActionBar.setCustomView(viewToolBar, layoutParams);

        // 추천 키워드 뷰페이저 아래 점들 설정
        imageViewDot01 = (ImageView) findViewById(R.id.imageViewDot01);
        imageViewDot02 = (ImageView) findViewById(R.id.imageViewDot02);
        imageViewDot03 = (ImageView) findViewById(R.id.imageViewDot03);

        // 추천 키워드 뷰페이저 설정
        mViewPager = (ViewPager) findViewById(viewPager);
        adapter = new KeywordFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if ( position == 0 ) {
                    imageViewDot01.setImageResource(R.drawable.icon_selected_dot);
                    imageViewDot02.setImageResource(R.drawable.icon_unselected_dot);
                    imageViewDot03.setImageResource(R.drawable.icon_unselected_dot);
                } else if ( position == 1 ) {
                    imageViewDot01.setImageResource(R.drawable.icon_unselected_dot);
                    imageViewDot02.setImageResource(R.drawable.icon_selected_dot);
                    imageViewDot03.setImageResource(R.drawable.icon_unselected_dot);
                } else if ( position == 2 ) {
                    imageViewDot01.setImageResource(R.drawable.icon_unselected_dot);
                    imageViewDot02.setImageResource(R.drawable.icon_unselected_dot);
                    imageViewDot03.setImageResource(R.drawable.icon_selected_dot);
                }

                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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
        // 네비게이션바 나타날때 키워드화면에서는 키워드메뉴가 이미 눌러져있게 설정
        // 다른 네비게이션바 메뉴들도 해당 액티비티에가면 이미 눌러져 있게 설정
        navigationView.setCheckedItem(R.id.nav_keword);

        // context_recyclerbin 뷰 설정
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        editTextWriteKeyword = (EditText) findViewById(R.id.editTextWriteKeyword);
        editTextWriteKeyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if ( actionId == EditorInfo.IME_ACTION_DONE ) {
                    // 추천 키워드를 내 키워드로 등록
                    if ( selectedRecommendKeywordList.size() > 0 ) {
                        for ( int i = 0; i < selectedRecommendKeywordList.size(); i++ ) {
                            // 등록된 추천 키워드 글자색 변환 및 클릭 안됨
                            selectedRecommendTextViewList.get(i).setBackgroundResource(R.drawable.keyword_item);
                            selectedRecommendTextViewList.get(i).setTextColor(Color.parseColor("#bdbdbd"));
                            selectedRecommendTextViewList.get(i).setEnabled(false);

                            new AsyncNewKeywordInsert().execute(new Keyword(0, selectedRecommendKeywordList.get(i).keyword));
                        }

                        editTextWriteKeyword.setText("");
                        inputMethodManager.hideSoftInputFromWindow(editTextWriteKeyword.getWindowToken(), 0);

                        return true;
                    }

                    // 키워드 비었을때 처리
                    if ( editTextWriteKeyword.getText().toString().trim().isEmpty() || editTextWriteKeyword.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), "키워드가 비었습니다", Toast.LENGTH_SHORT).show();

                        editTextWriteKeyword.setText("");
                        inputMethodManager.hideSoftInputFromWindow(editTextWriteKeyword.getWindowToken(), 0);

                        return true;
                    } else if ( editTextWriteKeyword.getText().length() > 5) { // 키워드 글자가 5글자 초과시 처리
                        Toast.makeText(getApplicationContext(), "키워드는 5글자 이내로 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                        inputMethodManager.hideSoftInputFromWindow(editTextWriteKeyword.getWindowToken(), 0);

                        return true;
                    }

                    new AsyncNewKeywordInsert().execute(new Keyword(0, editTextWriteKeyword.getText().toString()));

                    // 메모삽입 후 입력칸 초기화 및 키보드 내리기
                    editTextWriteKeyword.setText("");
                    inputMethodManager.hideSoftInputFromWindow(editTextWriteKeyword.getWindowToken(), 0);

                    return true;
                }

                return false;
            }
        });
        imageViewPlusKeyword = (ImageView) findViewById(R.id.imageViewPlusKeword);
        imageViewPlusKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 추천 키워드를 내 키워드로 등록
                if ( selectedRecommendKeywordList.size() > 0 ) {
                    for ( int i = 0; i < selectedRecommendKeywordList.size(); i++ ) {
                        // 등록된 추천 키워드 글자색 변환 및 클릭 안됨
                        selectedRecommendTextViewList.get(i).setBackgroundResource(R.drawable.keyword_item);
                        selectedRecommendTextViewList.get(i).setTextColor(Color.parseColor("#bdbdbd"));
                        selectedRecommendTextViewList.get(i).setEnabled(false);

                        new AsyncNewKeywordInsert().execute(new Keyword(0, selectedRecommendKeywordList.get(i).keyword));
                    }

                    return;
                }

                // 키워드 비었을때 처리
                if ( editTextWriteKeyword.getText().toString().trim().isEmpty() || editTextWriteKeyword.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "키워드가 비었습니다", Toast.LENGTH_SHORT).show();

                    editTextWriteKeyword.setText("");
                    inputMethodManager.hideSoftInputFromWindow(editTextWriteKeyword.getWindowToken(), 0);

                    return;
                } else if ( editTextWriteKeyword.getText().length() > 5) { // 키워드 글자가 5글자 초과시 처리
                    Toast.makeText(getApplicationContext(), "키워드는 5글자 이내로 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                    inputMethodManager.hideSoftInputFromWindow(editTextWriteKeyword.getWindowToken(), 0);

                    return;
                }

                new AsyncNewKeywordInsert().execute(new Keyword(0, editTextWriteKeyword.getText().toString()));

                // 메모삽입 후 입력칸 초기화 및 키보드 내리기
                editTextWriteKeyword.setText("");
                inputMethodManager.hideSoftInputFromWindow(editTextWriteKeyword.getWindowToken(), 0);
            }
        });

        // 나의 키워드
        textViewDefault = (TextView) findViewById(R.id.textViewDefault);
        linearLayoutMyKeyword = (LinearLayout) findViewById(R.id.linearLayoutMyKeyword);

        if ( myKeywordList.size() < 1 ) {
            textViewDefault.setVisibility(View.VISIBLE);
            linearLayoutMyKeyword.setVisibility(View.GONE);
        } else {
            linearLayoutMyKeyword.setVisibility(View.VISIBLE);
            textViewDefault.setVisibility(View.GONE);
        }

        if ( memo_id != -1 ) {
            imageViewRecyclerbin.setVisibility(View.GONE);
            textViewAttachKeyword.setVisibility(View.VISIBLE);

            // EditText 힌트 설정
            editTextWriteKeyword.setHint("메모와 어울리는 키워드를 등록해보세요!");

            // 뷰페이저 안보임
            mViewPager.setVisibility(View.GONE);

            // 점 3개 안보임
            LinearLayout linearLayoutDots = (LinearLayout) findViewById(R.id.linearLayoutDots);
            linearLayoutDots.setVisibility(View.GONE);
        }

        new AsyncUserKeywords().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();

        navigationView.setCheckedItem(R.id.nav_keword);
    }

    @Override
    protected void onResume() {
        super.onResume();

        navigationView.setCheckedItem(R.id.nav_keword);
    }

    // 디바이스 Back 키 눌렸을때 처리
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if ( selectedKeywordList.size() > 0 ) {
            for ( int i = 0; i < selectedTextViewList.size(); i++)  {
                selectedTextViewList.get(i).setBackgroundResource(R.drawable.keyword_item);
                selectedTextViewList.get(i).setFocusable(false);
            }
            selectedTextViewList.clear();
            selectedKeywordList.clear();
        } else if ( selectedRecommendKeywordList.size() > 0 ) {
            for ( int i = 0; i < selectedRecommendTextViewList.size(); i++)  {
                selectedRecommendTextViewList.get(i).setBackgroundResource(R.drawable.keyword_item);
                selectedRecommendTextViewList.get(i).setFocusable(false);
            }
            selectedRecommendTextViewList.clear();
            selectedRecommendKeywordList.clear();
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
            Intent intent = new Intent(KeywordActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_recyclebin) {
            Intent intent = new Intent(KeywordActivity.this, RecyclerbinActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(KeywordActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    int columnCountMyKeyword = 0;
    int rowCountMyKeyword = 0;
    LinearLayout linearLayout = null;
    public void createNewKeyword(final Keyword newKeyword) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Integer.parseInt("-10"), Integer.parseInt("-10"), Integer.parseInt("-10"), Integer.parseInt("-10"));

        final TextView textView = new TextView(getApplicationContext());
        textView.setTextSize(10);
        textView.setTextColor(Color.parseColor("#4990e2"));
        textView.setLayoutParams(params);
        textView.setBackgroundResource(R.drawable.keyword_item);
        textView.setGravity(Gravity.CENTER);
        textView.setText(newKeyword.keyword);
        textView.setFocusable(false);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 추천 키워드가 눌려있다면 초기화
                for ( int i = 0; i < selectedRecommendKeywordList.size(); i++ ) {
                    selectedRecommendTextViewList.get(i).setBackgroundResource(R.drawable.keyword_item);
                    selectedRecommendTextViewList.get(i).setFocusable(false);
                }
                selectedRecommendKeywordList.clear();
                selectedRecommendTextViewList.clear();

                if ( textView.isFocusable() ) {
                    selectedKeywordList.remove(newKeyword);
                    selectedTextViewList.remove(textView);

                    textView.setBackgroundResource(R.drawable.keyword_item);
                    textView.setFocusable(false);
                } else {
                    selectedKeywordList.add(newKeyword);
                    selectedTextViewList.add(textView);

                    textView.setBackgroundResource(R.drawable.keyword_item_selected);
                    textView.setFocusable(true);
                }
            }
        });

        if ( rowCountMyKeyword == 0 || columnCountMyKeyword >= 5 ) {
            // 새로운 row 생성
            linearLayout = new LinearLayout(SemoApplication.getSemoContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setWeightSum(0.0f);
            rowCountMyKeyword++;
            // row 추가
            linearLayoutMyKeyword.addView(linearLayout);
            if ( columnCountMyKeyword >= 5 ) {
                columnCountMyKeyword = 0;
            }
        }

        if ( columnCountMyKeyword < 5 ) {
            linearLayout.addView(textView);
            columnCountMyKeyword++;
        }
    }

    public class KeywordFragmentPagerAdapter extends FragmentPagerAdapter {

        ArrayList<KeywordFragment> keywordFragmentList = new ArrayList<>();

        public KeywordFragmentPagerAdapter(FragmentManager fm) {
            super(fm);

            keywordFragmentList.add(KeywordFragment.newInstance(0));
            keywordFragmentList.add(KeywordFragment.newInstance(1));
            keywordFragmentList.add(KeywordFragment.newInstance(2));
        }

        @Override
        public Fragment getItem(int position) {
            keywordFragmentList.get(position).setRecommendKeywords(recommendKeywordList);

            return keywordFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return keywordFragmentList.size();
        }
    }

    public class AsyncNewKeywordInsert extends AsyncTask<Keyword, Integer, String> {

        Keyword newKeyword;

        @Override
        protected String doInBackground(Keyword... params) {
            newKeyword = params[0];

            for ( int i = 0; i < myKeywordList.size(); i++ ) {
                if ( myKeywordList.get(i).keyword.equals(newKeyword.keyword) ) {
                    String result = "contained";

                    return result;
                }
            }

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
                        .add("keyword", newKeyword.keyword)
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_INSERT_KEYWORD)
                        .post(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                // 응답결과에 따른 처리
                if( flag ){
                    returedJSON = response.body().string();
                    // 내 키워드 리스트에 추가
                    myKeywordList.add(newKeyword);
                    try {
                        JSONObject jsonObject = new JSONObject(returedJSON);
                        result = jsonObject.getString("msg");
                        // 삽입한 키워드 아이디값 가져오기
                        JSONObject jsonObjectNewKeyword = jsonObject.getJSONObject("data").getJSONObject("createdKeyword");
                        lastKeywordId = jsonObjectNewKeyword.getInt("keyword_id");
                        myKeywordList.get(myKeywordList.size() - 1).keyword_id = lastKeywordId;
                    } catch( JSONException jsone ) {
                        e("JSON ERROR", jsone.toString());
                    }
                } else {
                    //요청에러 발생시(http 에러)
                    e("HTTP ERROR", "HTTP REQUEST ERROR OCCUR");
                }

            } catch (UnknownHostException une) {
                // 호스트 못찾을떄
                e("NOT FOUND HOST URL", une.toString());
            } catch (UnsupportedEncodingException uee) {
                // 인코딩 오류가 났을때
                e("ENCODING ERROR", uee.toString());
            } catch (Exception e) {
                e("EXCEPTION ERROR", e.toString());
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

            // 중복된 키워드 처리
            if ( result.equals("contained") ) {
                Toast.makeText(getApplicationContext(), "이미 존재하는 키워드 입니다", Toast.LENGTH_SHORT).show();

                return;
            }

            if ( result != null ) {
                // 내 키워드 갱신
                createNewKeyword(myKeywordList.get(myKeywordList.size() - 1));
                Toast.makeText(getApplicationContext(), "새로운 키워드가 생성되었습니다.", Toast.LENGTH_SHORT).show();

                if ( textViewDefault.getVisibility() == View.VISIBLE ) {
                    textViewDefault.setVisibility(View.GONE);
                    linearLayoutMyKeyword.setVisibility(View.VISIBLE);
                }

                linearLayoutMyKeyword.invalidate();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }

            if ( selectedRecommendKeywordList.size() > 0 ) {
                selectedRecommendKeywordList.clear();
                selectedRecommendTextViewList.clear();
            }
        }
    }

    public class AsyncUserKeywords extends AsyncTask<Integer, Integer, String> {

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new Dialog(KeywordActivity.this, R.style.SemoDialog);
            progressDialog.setCancelable(true);

            ProgressBar progressbar = new ProgressBar(KeywordActivity.this);
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
                        .url(NetworkDefineConstant.SERVER_URL_SELECT_ALL_KEYWORD + userId)
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
                        JSONObject jsonObjectData = jsonFromServer.getJSONObject("data");

                        // 추천 키워드
                        JSONArray jsonArrayRecommendKeywords = jsonObjectData.getJSONArray("recommended");
                        for ( int i = 0; i < jsonArrayRecommendKeywords.length(); i++ ) {
                            recommendKeywordList.add(new RecommendKeyword(jsonArrayRecommendKeywords.getJSONObject(i).getString("keyword"),
                                                                          jsonArrayRecommendKeywords.getJSONObject(i).getBoolean("owned")));
                        }
                        // 내 키워드
                        JSONArray jsonArrayKeywords = jsonObjectData.getJSONArray("owned");
                        for ( int i = 0; i < jsonArrayKeywords.length(); i++ ) {
                            myKeywordList.add(new Keyword(jsonArrayKeywords.getJSONObject(i).getInt("keyword_id"),
                                                          jsonArrayKeywords.getJSONObject(i).getString("keyword")));
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

            columnCountMyKeyword = 0;
            rowCountMyKeyword = 0;
            linearLayout = null;
            if ( linearLayoutMyKeyword.getChildCount() > 0) {
                linearLayoutMyKeyword.removeAllViews();
            }


            if ( result != null ) {
                Log.e("onPostExecute", result);
            } else {
                Log.e("onPostExecute", "result is empty");
            }

            for ( int i = 0; i < myKeywordList.size(); i++ ) {
                createNewKeyword(myKeywordList.get(i));
            }

            if ( myKeywordList.size() < 1 ) {
                textViewDefault.setVisibility(View.VISIBLE);
                linearLayoutMyKeyword.setVisibility(View.GONE);
            } else {
                linearLayoutMyKeyword.setVisibility(View.VISIBLE);
                textViewDefault.setVisibility(View.GONE);
            }

            // 추천 키워드 갱신
            mViewPager.setAdapter(adapter);
        }
    }

    public class AsyncKeywordDelete extends AsyncTask<ArrayList<Keyword>, Integer, String> {
        ArrayList<Keyword> selectedKeywords;

        @Override
        protected String doInBackground(ArrayList<Keyword>... params) {
            // 메모 아이디
            selectedKeywords = params[0];

            // 서버 요청 결과(성공시 true, 실패시 false)
            boolean flag = false;
            // 서버 요청 성공시 응답받은 JSON 값
            String result = "";

            Response response = null;
            OkHttpClient toServer = null;

            try{
                toServer = OkHttpInitSingtonManager.getOkHttpClient();

                //요청 Form세팅
                String selectedKeywordsString = "";
                for ( int i = 0; i < selectedKeywords.size(); i++ ) {
                    selectedKeywordsString += selectedKeywords.get(i).keyword_id + ",";
                }
                selectedKeywordsString = selectedKeywordsString.substring(0, selectedKeywordsString.length() - 1);

                RequestBody postBody = new FormBody.Builder()
                        .add("keyword_id", selectedKeywordsString)
                        .build();

                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_DELETE_KEYWORD)
                        .delete(postBody)
                        .build();
                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();
                String returedJSON;

                if( flag ){
                    returedJSON = response.body().string();

                    for ( int i = 0; i < selectedKeywords.size(); i++ ) {
                        myKeywordList.remove(selectedKeywords.get(i));
                    }

                    try {
                        JSONObject jsonFromServer = new JSONObject(returedJSON);

                        result = jsonFromServer.getString("msg");
                        Log.e("result", result);
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
                Toast.makeText(getApplicationContext(), "키워드가 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                selectedTextViewList.clear();
                selectedKeywordList.clear();

                linearLayoutMyKeyword.invalidate();
            }  else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncMemoKeywordAdd extends AsyncTask<Integer, Integer, String> {
        // 키워드설정할 메모
        int memoId;
        int keywordId;

        @Override
        protected String doInBackground(Integer... params) {
            memoId = params[0];
            keywordId = params[1];
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
                        .add("memo_id", String.valueOf(memoId))  // 키워드가 붙을 메모 id
                        .add("keyword_id", String.valueOf(keywordId))  // 키워드 id
                        .add("connect", String.valueOf(true))  // 키워드 상태
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_ATTACH_KEYWORD)
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
                        JSONObject jsonObjectData = jsonObject.getJSONObject("data");
                        JSONObject jsonObjectMemo = jsonObjectData.getJSONObject("memo");
                        JSONArray jsonArrayKeywords = jsonObjectMemo.getJSONArray("Tags");

                        attachKeyword = jsonArrayKeywords.getJSONObject(0).getString("name");

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
                Log.e("onPostExecute", result);

                Toast.makeText(getApplicationContext(), "키워드가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                selectedKeywordList.clear();
                selectedTextViewList.clear();

                Intent intent = new Intent();
                intent.putExtra("isKeyword", true);
                intent.putExtra("attachKeyword", attachKeyword);
                setResult(Activity.RESULT_OK ,intent);

                // 액티비티 종료
                finish();

            }  else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }
}
