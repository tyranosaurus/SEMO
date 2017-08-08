package com.tacademy.semo.activity;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tacademy.semo.R;
import com.tacademy.semo.adapter.IdeationRecyclerViewAdapter;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.item.HiddenMemo;
import com.tacademy.semo.item.IdeationChild;
import com.tacademy.semo.item.NewCombineMemo;
import com.tacademy.semo.network.NetworkDefineConstant;
import com.tacademy.semo.network.OkHttpInitSingtonManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.util.Log.e;

public class IdeationActivity extends AppCompatActivity {

    // 유저아이디
    int userId = SemoApplication.getUserId();

    Toolbar mToolbar;
    ActionBar mActionBar;

    ImageView imageViewBack;
    TextView textViewIdeation;
    ImageView imageViewOptionMenu;
    ImageView imageViewPin;

    ScrollView scrollView;
    LinearLayout scrollLinearLayout;
    TextView textViewMemos;
    TextView textViewWrittenDate;
    TextView textViewKeyword;
    LinearLayout linearLayoutCombine;
    LinearLayout linearLayoutSave;
    RecyclerView recyclerView;

    // 메모 정보 받아올 Bundle 객체
    Bundle bundle;
    int memo_id;
    Boolean pin;
    String body;
    String createdAt;
    String keyword;

    // 어댑터
    IdeationRecyclerViewAdapter adapter;
    // 선택된 조합메모
    public ArrayList<NewCombineMemo> selectedNewCombineMemoList = new ArrayList<>();
    public ArrayList<LinearLayout> selectedViewList = new ArrayList<>();
    // 조합순서 및 마지막 히든메모
    int order = 0;
    EditText lastEditText = null;
    // 내 메모에 조합된 조합메모 및 히든메모 저장하는 리스트
    ArrayList<HashMap<Object, Object>> combineMemoList = new ArrayList<>();
    ArrayList<HashMap<Object, Object>> hiddenMemoList = new ArrayList<>();
    // 내 메모의 차일드 메모들 저장하는 리스트
    ArrayList<IdeationChild> childrenMemoList = new ArrayList<>();
    // 저장하기 버튼
    ImageView imageViewSave;
    TextView textViewSave;

    // 클립보드 매니저
    ClipboardManager clipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ideation);

        // 클립보드 매니저
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        // 인텐트에 들어있는 메모정보 가져오기
        bundle = getIntent().getBundleExtra("memoInfo");
        memo_id = bundle.getInt("memo_id");
        pin = bundle.getBoolean("pin");
        body = bundle.getString("body");
        createdAt = bundle.getString("createdAt");
        keyword = bundle.getString("keyword");

        // 저장하기 이미지, 버튼
        imageViewSave = (ImageView) findViewById(R.id.imageViewSave);
        textViewSave = (TextView) findViewById(R.id.textViewSave);

        // 스크롤뷰
        scrollView = (ScrollView) findViewById(R.id.scrollViewParent);
        scrollLinearLayout = (LinearLayout) findViewById(R.id.scrollLinearLayout);

        // 툴바 설정
        mToolbar = (Toolbar) findViewById(R.id.toolbarSetting);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        // 툴바에 사용할 커스텀 레이아웃 인플레이션
        View viewToolBar = getLayoutInflater().inflate(R.layout.toolbar_ideation, null);

        imageViewBack = (ImageView) viewToolBar.findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textViewIdeation = (TextView) viewToolBar.findViewById(R.id.textViewIdeation);

        imageViewPin = (ImageView) viewToolBar.findViewById(R.id.imageViewPin);
        if ( pin ) {
            imageViewPin.setImageResource(R.drawable.icon_pin);
        } else {
            imageViewPin.setImageResource(R.drawable.icon_unpin);
        }

        imageViewOptionMenu = (ImageView) viewToolBar.findViewById(R.id.imageViewOptionMenu);
        imageViewOptionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToolbarOptionMenu(imageViewOptionMenu);
            }
        });

        // 인플레이션한 커스텀 툴바 세팅
        final ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        mActionBar.setCustomView(viewToolBar, layoutParams);

        // 조합을 할 내 메모
        textViewMemos = (TextView) findViewById(R.id.textViewMemos);
        textViewMemos.setText(body);

        // 생성시간 설정
        textViewWrittenDate = (TextView) findViewById(R.id.textViewWrittenDate);
        textViewWrittenDate.setText(createdAt);

        // 키워드 설정
        textViewKeyword = (TextView) findViewById(R.id.textViewKeyword);
        textViewKeyword.setText(keyword);

        // 조합하기 버튼
        linearLayoutCombine = (LinearLayout) findViewById(R.id.linearLayoutCombine);
        linearLayoutCombine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( lastEditText != null && lastEditText.length() < 1 ) {
                    scrollLinearLayout.removeViewAt(scrollLinearLayout.getChildCount() - 2); // 구분선 제거
                    scrollLinearLayout.removeViewAt(scrollLinearLayout.getChildCount() - 1); // 마지막 히든메모가 들어있는 레이아웃 제거
                    --order;
                } else if ( lastEditText != null && lastEditText.length() > 0 ) {
                    // 히든메모 데이터 추가
                    HashMap<Object, Object> hiddenHashMap = new HashMap<>();
                    hiddenHashMap.put("order", order); // 아래에서 증가시켰으므로 바로 order 저장
                    hiddenHashMap.put("body", lastEditText.getText().toString());
                    hiddenHashMap.put("user_id", userId);
                    hiddenMemoList.add(hiddenHashMap);
                    // 한 번조합한 히든메모는 수정못하게 막음
                    // 수정은 모두 상세보기에서 가능7
                    lastEditText.setEnabled(false);
                }
                // 선택한 조합메모 추가
                for ( int i = 0; i < selectedNewCombineMemoList.size(); i++ ) {
                    addCombineMemo(selectedNewCombineMemoList.get(i));

                    // 조합메모 데이터 추가
                    HashMap<Object, Object> combineHashMap = new HashMap<>();
                    combineHashMap.put("order", ++order);
                    combineHashMap.put("memo_id", selectedNewCombineMemoList.get(i).memoId);
                    combineMemoList.add(combineHashMap);
                }
                // 히든메모 입력칸 추가
                lastEditText = addHiddenMemo();
                // 항상 마지막 히든메모에 포커스 주기
                lastEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        lastEditText.requestFocus();
                        ++order;
                    }
                });

                // 조합버튼 누르고 선택된메모들 모두 초기화
                for ( int i = 0; i < selectedNewCombineMemoList.size(); i++ ) {
                    selectedNewCombineMemoList.get(i).selected = false;
                }
                selectedNewCombineMemoList.clear();
                selectedViewList.clear();
                adapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), "조합완료", Toast.LENGTH_SHORT).show();

                // 조합하기버튼 누르면 order가 무조건 1보다 크니까 저장버튼 활성화
                if ( order > 0 ) {
                    imageViewSave.setImageResource(R.drawable.icon_save);
                    textViewSave.setTextColor(Color.parseColor("#4990e2"));
                }
            }
       });
        // 저장하기 버튼
        linearLayoutSave = (LinearLayout) findViewById(R.id.linearLayoutSave);
        linearLayoutSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( lastEditText == null || order < 1) {
                    Toast.makeText(getApplicationContext(), "조합할 메모를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if ( lastEditText.length() < 1 && order == 1) {
                    Toast.makeText(getApplicationContext(), "조합할 메모를 작성해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if ( lastEditText != null && lastEditText.length() < 1 ) { // 히든메모가 비었을때
                    // 조합메모 추가 어싱크태스크
                    for ( int i = 0; i < combineMemoList.size(); i++ ) {
                        new AsyncCombineMemoAdd().execute(memo_id,
                                                          (Integer) combineMemoList.get(i).get("memo_id"),
                                                          (Integer) combineMemoList.get(i).get("order"));
                    }
                    // 히든메모 추가 어싱크태스크
                    for ( int i = 0; i < hiddenMemoList.size(); i++ ) {
                        HiddenMemo hiddenMemo = new HiddenMemo(memo_id,
                                                               (Integer) hiddenMemoList.get(i).get("user_id"),
                                                               (String) hiddenMemoList.get(i).get("body"),
                                                               (Integer) hiddenMemoList.get(i).get("order"));
                        new AsyncHiddenMemoAdd().execute(hiddenMemo);
                    }
                } else if ( lastEditText != null && lastEditText.length() > 0 ){ // 히든메모가 안 비었을때
                    // 히든메모 데이터 추가
                    HashMap<Object, Object> hiddenHashMap = new HashMap<>();
                    hiddenHashMap.put("order", order); // 아래에서 증가시켰으므로 바로 order 저장
                    hiddenHashMap.put("body", lastEditText.getText().toString());
                    hiddenHashMap.put("user_id", userId);
                    hiddenMemoList.add(hiddenHashMap);

                    // 조합메모 추가 어싱크태스크
                    for ( int i = 0; i < combineMemoList.size(); i++ ) {
                        new AsyncCombineMemoAdd().execute(memo_id,
                                (Integer) combineMemoList.get(i).get("memo_id"),
                                (Integer) combineMemoList.get(i).get("order"));
                    }
                    // 히든메모 추가 어싱크태스크
                    for ( int i = 0; i < hiddenMemoList.size(); i++ ) {
                        HiddenMemo hiddenMemo = new HiddenMemo(memo_id,
                                (Integer) hiddenMemoList.get(i).get("user_id"),
                                (String) hiddenMemoList.get(i).get("body"),
                                (Integer) hiddenMemoList.get(i).get("order"));
                        new AsyncHiddenMemoAdd().execute(hiddenMemo);
                    }
                    // lastEditText 수정 못하게 막고 초기화
                    lastEditText.setEnabled(false);
                    lastEditText = null;
                }

                // 저장버튼 누르고 선택된메모들 모두 초기화(조합메모 선택하고 바로 저장 누르는 경우)
                for ( int i = 0; i < selectedNewCombineMemoList.size(); i++ ) {
                    selectedNewCombineMemoList.get(i).selected = false;
                }
                selectedNewCombineMemoList.clear();
                selectedViewList.clear();
                adapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), "저장완료", Toast.LENGTH_SHORT).show();

                // 저장하기버튼 누르면 다시 비활성화
                if ( order > 0 ) {
                    imageViewSave.setImageResource(R.drawable.icon_unsave);
                    textViewSave.setTextColor(Color.parseColor("#656565"));
                }
            }
        });

        // 리사이클러뷰 세팅
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManagerComment = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManagerComment);

        adapter = new IdeationRecyclerViewAdapter();
        adapter.setActivity(IdeationActivity.this);
        recyclerView.setAdapter(adapter);

        // 아이데이션할 메모 정보 가져오는 어싱크태스크
        new AsyncIdeationMemo().execute(memo_id); // 현재 테스트용으로 50번 메모로 고정
        // 조합할 메모 가져오는 어싱크태스크
        new AsyncCombineMemos().execute(memo_id);
    }

    @Override
    public void onBackPressed() {

        if ( selectedNewCombineMemoList.size() > 0 || selectedViewList.size() > 0 ) {
            adapter.setUnselected();

            return;
        }

        super.onBackPressed();
    }

    // 툴바 오른쪽에 있는 옵션메뉴 눌렀을때 팝업메뉴 띄우는 함수
    public void showToolbarOptionMenu(View view){
        PopupMenu popupMenu = new PopupMenu(this, view);
        getMenuInflater().inflate(R.menu.menu_my_ideation_optionmenu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.copyMemo:
                        clipboardManager.setText(body);
                        Toast.makeText(getApplicationContext(), "메모가 복사되었습니다.", Toast.LENGTH_SHORT).show();

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

    public void addCombineMemo(NewCombineMemo newCombineMemo) {

        // 구분선 추가
        final int heightDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        final int marginDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams dividedLineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightDividedLine);
        dividedLineParams.setMargins(marginDividedLine, 0, marginDividedLine, marginDividedLine);

        View viewDividedLine = new View(getApplicationContext());
        viewDividedLine.setLayoutParams(dividedLineParams);
        viewDividedLine.setBackgroundColor(Color.parseColor("#e0e0e0"));
        viewDividedLine.setAlpha(0.6f);

        // 조합메모 레이아웃에 들어갈 링크 이미지 추가
        final int widthImageView = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        final int heightImageView = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(widthImageView, heightImageView);
        imageViewParams.gravity = Gravity.CENTER;

        ImageView imageViewLink = new ImageView(getApplicationContext());
        imageViewLink.setImageResource(R.drawable.icon_link);
        imageViewLink.setLayoutParams(imageViewParams);

        // 메모 추가
        final int textVIewMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                 LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.weight = 1;
        textViewParams.setMargins(textVIewMargin, 0, 0, 0);
        textViewParams.gravity = Gravity.CENTER;

        TextView textViewCombineMemo = new TextView(getApplicationContext());
        textViewCombineMemo.setLayoutParams(textViewParams);
        textViewCombineMemo.setTextColor(Color.parseColor("#de000000"));
        textViewCombineMemo.setTextSize(12.0f);
        textViewCombineMemo.setText(newCombineMemo.body);

        // 조합메모가 들어갈 레이아웃
        final int layoutMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams linearLayoutParmas = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                     LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParmas.setMargins(layoutMargin, 0, layoutMargin, layoutMargin);

        LinearLayout newCombineLayout = new LinearLayout(getApplicationContext());
        newCombineLayout.setOrientation(LinearLayout.HORIZONTAL);
        newCombineLayout.setLayoutParams(linearLayoutParmas);

        newCombineLayout.addView(imageViewLink); // 링크 이미지 추가
        newCombineLayout.addView(textViewCombineMemo); // 조합 메모 추가

        // 조합메모 레이아웃 붙임
        scrollLinearLayout.addView(viewDividedLine); // 구분선 추가
        scrollLinearLayout.addView(newCombineLayout); // 조합 메모 추가
    }

    public EditText addHiddenMemo() {
        // 구분선
        final int heightDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        final int marginDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams dividedLineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightDividedLine);
        dividedLineParams.setMargins(marginDividedLine, 0, marginDividedLine, marginDividedLine);

        View viewDividedLine = new View(getApplicationContext());
        viewDividedLine.setLayoutParams(dividedLineParams);
        viewDividedLine.setBackgroundColor(Color.parseColor("#e0e0e0"));
        viewDividedLine.setAlpha(0.6f);

        // 히든메모 이미지 추가
        final int widthImageView = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        final int heightImageView = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(widthImageView, heightImageView);
        imageViewParams.gravity = Gravity.CENTER;

        ImageView imageViewHidden = new ImageView(getApplicationContext());
        imageViewHidden.setImageResource(R.drawable.icon_hidden);
        imageViewHidden.setLayoutParams(imageViewParams);

        // 히든메모 추가
        final int textVIewMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.weight = 1;
        textViewParams.gravity = Gravity.CENTER;
        textViewParams.setMargins(textVIewMargin, 0, 0, 0);

        EditText editTextHiddenMemo = new EditText(getApplicationContext());
        editTextHiddenMemo.setLayoutParams(textViewParams);
        editTextHiddenMemo.setTextColor(Color.parseColor("#46d4b4"));
        editTextHiddenMemo.setTextSize(12.0f);
        editTextHiddenMemo.setHint("이곳에 새로운 아이디어를 적어보세요!");
        editTextHiddenMemo.setHintTextColor(Color.parseColor("#bdbdbd"));

        // 히든메모가 들어갈 레이아웃
        final int layoutMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams linearLayoutHiddenParmas = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                 LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutHiddenParmas.setMargins(layoutMargin, 0, layoutMargin, layoutMargin);

        LinearLayout newHiddenLayout = new LinearLayout(getApplicationContext());
        newHiddenLayout.setOrientation(LinearLayout.HORIZONTAL);
        newHiddenLayout.setLayoutParams(linearLayoutHiddenParmas);

        newHiddenLayout.addView(imageViewHidden); // 히든 이미지 추가
        newHiddenLayout.addView(editTextHiddenMemo); // 히든 메모 추가

        scrollLinearLayout.addView(viewDividedLine); // 구분선 추가
        scrollLinearLayout.addView(newHiddenLayout); // 히든메모 추가

        // 스크롤 끝까지 처리
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        return editTextHiddenMemo;
    }

    public void addChildCombineMemo(String combineBody) {

        // 구분선 추가
        final int heightDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        final int marginDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams dividedLineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightDividedLine);
        dividedLineParams.setMargins(marginDividedLine, 0, marginDividedLine, marginDividedLine);

        View viewDividedLine = new View(getApplicationContext());
        viewDividedLine.setLayoutParams(dividedLineParams);
        viewDividedLine.setBackgroundColor(Color.parseColor("#e0e0e0"));
        viewDividedLine.setAlpha(0.6f);

        // 조합메모 레이아웃에 들어갈 링크 이미지 추가
        final int widthImageView = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        final int heightImageView = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(widthImageView, heightImageView);
        imageViewParams.gravity = Gravity.CENTER;

        ImageView imageViewLink = new ImageView(getApplicationContext());
        imageViewLink.setImageResource(R.drawable.icon_link);
        imageViewLink.setLayoutParams(imageViewParams);

        // 메모 추가
        final int textVIewMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.weight = 1;
        textViewParams.setMargins(textVIewMargin, 0, 0, 0);
        textViewParams.gravity = Gravity.CENTER;

        TextView textViewCombineMemo = new TextView(getApplicationContext());
        textViewCombineMemo.setLayoutParams(textViewParams);
        textViewCombineMemo.setTextColor(Color.parseColor("#de000000"));
        textViewCombineMemo.setTextSize(12.0f);
        textViewCombineMemo.setText(combineBody);

        // 조합메모가 들어갈 레이아웃
        final int layoutMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams linearLayoutParmas = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                    LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParmas.setMargins(layoutMargin, 0, layoutMargin, layoutMargin);

        LinearLayout newCombineLayout = new LinearLayout(getApplicationContext());
        newCombineLayout.setOrientation(LinearLayout.HORIZONTAL);
        newCombineLayout.setLayoutParams(linearLayoutParmas);

        newCombineLayout.addView(imageViewLink); // 링크 이미지 추가
        newCombineLayout.addView(textViewCombineMemo); // 조합 메모 추가

        // 조합메모 레이아웃 붙임
        scrollLinearLayout.addView(viewDividedLine); // 구분선 추가
        scrollLinearLayout.addView(newCombineLayout); // 조합 메모 추가
    }

    public EditText addChildHiddenMemo(String hiddenBody) {
        // 구분선
        final int heightDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        final int marginDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams dividedLineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightDividedLine);
        dividedLineParams.setMargins(marginDividedLine, 0, marginDividedLine, marginDividedLine);

        View viewDividedLine = new View(getApplicationContext());
        viewDividedLine.setLayoutParams(dividedLineParams);
        viewDividedLine.setBackgroundColor(Color.parseColor("#e0e0e0"));
        viewDividedLine.setAlpha(0.6f);

        // 히든메모 이미지 추가
        final int widthImageView = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        final int heightImageView = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(widthImageView, heightImageView);
        imageViewParams.gravity = Gravity.CENTER;

        ImageView imageViewHidden = new ImageView(getApplicationContext());
        imageViewHidden.setImageResource(R.drawable.icon_hidden);
        imageViewHidden.setLayoutParams(imageViewParams);

        // 히든메모 추가
        final int textVIewMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.weight = 1;
        textViewParams.gravity = Gravity.CENTER;
        textViewParams.setMargins(textVIewMargin, 0, 0, 0);

        EditText editTextHiddenMemo = new EditText(getApplicationContext());
        editTextHiddenMemo.setLayoutParams(textViewParams);
        editTextHiddenMemo.setTextColor(Color.parseColor("#46d4b4"));
        editTextHiddenMemo.setTextSize(12.0f);
        editTextHiddenMemo.setText(hiddenBody);
        editTextHiddenMemo.setSelection(editTextHiddenMemo.length());

        // 히든메모가 들어갈 레이아웃
        final int layoutMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams linearLayoutHiddenParmas = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                            LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutHiddenParmas.setMargins(layoutMargin, 0, layoutMargin, layoutMargin);

        LinearLayout newHiddenLayout = new LinearLayout(getApplicationContext());
        newHiddenLayout.setOrientation(LinearLayout.HORIZONTAL);
        newHiddenLayout.setLayoutParams(linearLayoutHiddenParmas);

        newHiddenLayout.addView(imageViewHidden); // 히든 이미지 추가
        newHiddenLayout.addView(editTextHiddenMemo); // 히든 메모 추가

        scrollLinearLayout.addView(viewDividedLine); // 구분선 추가
        scrollLinearLayout.addView(newHiddenLayout); // 히든메모 추가

        // 스크롤 끝까지 처리
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        return editTextHiddenMemo;
    }

    public class AsyncIdeationMemo extends AsyncTask<Integer, Integer, String> {
        int parentMemoId;

        @Override
        protected String doInBackground(Integer... params) {
            parentMemoId = params[0];
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
                        .url(NetworkDefineConstant.SERVER_URL_SELECT_IDEATION_MEMO + parentMemoId)
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
                        JSONArray jsonArrayChildMemos = jsonFromServer.getJSONArray("children");

                        if ( jsonArrayChildMemos.length() < 1) {
                            String noChild = "noChild";

                            return noChild;
                        }

                        for ( int i = 0; i < jsonArrayChildMemos.length(); i++ ) {
                            childrenMemoList.add(new IdeationChild(jsonArrayChildMemos.getJSONObject(i).getInt("memo_id"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getString("body"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getBoolean("shared"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getBoolean("hidden"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getBoolean("deleted"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getBoolean("pin"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getString("modifiedAt"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getString("createdAt"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getInt("connectionCount"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getInt("user_id"),
                                                                   jsonArrayChildMemos.getJSONObject(i).getInt("Combination.ordernum")));
                        }

                        order = childrenMemoList.get(childrenMemoList.size() - 1).order; // order 최신화

                        result = jsonFromServer.getString("msg");

                    } catch(JSONException jsone) {
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
                    response.close();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if ( result.equals("noChild") ) {
                e("IDEATION", "No Child");

                // 부모메보 불려오고 모든 리스트 초기화(여기서부터 다시 조합 시작)
                childrenMemoList.clear();
                selectedNewCombineMemoList.clear();
                selectedViewList.clear();
                combineMemoList.clear();
                hiddenMemoList.clear();

                return;
            }

            if ( result != null ) {
                e("onPostExecute", result);

                // 여기서 차일드에 맞는 뷰 생성해줄 것. 마지막에 차일드리스트 clear();
                for ( int i = 0; i < childrenMemoList.size(); i++ ) {
                    if ( childrenMemoList.get(i).hidden ) { // 차일드가 히든메모. 수정 못하게 막음
                        addChildHiddenMemo(childrenMemoList.get(i).body).setEnabled(false);
                    } else { // 차일드가 조합메모
                        addChildCombineMemo(childrenMemoList.get(i).body);
                    }
                }
                // 부모메보 불려오고 모든 리스트 초기화(여기서부터 다시 조합 시작)
                childrenMemoList.clear();
                selectedNewCombineMemoList.clear();
                selectedViewList.clear();
                combineMemoList.clear();
                hiddenMemoList.clear();
            } else {
                e("onPostExecute", "result is empty");
            }

            // 스크롤 끝까지 처리
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    public class AsyncCombineMemos extends AsyncTask<Integer, Integer, String> {
        Dialog progressDialog;
        int memoId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new Dialog(IdeationActivity.this, R.style.SemoDialog);
            progressDialog.setCancelable(true);

            ProgressBar progressbar = new ProgressBar(IdeationActivity.this);
            progressbar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.semo_progress_dialog));

            progressDialog.addContentView(progressbar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {
            memoId = params[0];
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
                        .url(NetworkDefineConstant.SERVER_URL_SELECT_COMBINE_MEMO + memoId)
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
                        JSONArray jsonObjectData = jsonFromServer.getJSONArray("recommendedMemos");

                        for ( int i = 0; i < jsonObjectData.length(); i++ ) {
                            int memoId = jsonObjectData.getJSONObject(i).getInt("memo_id");
                            String body = jsonObjectData.getJSONObject(i).getString("body");
                            String nickname = jsonObjectData.getJSONObject(i).getString("origin_author.nickname");
                            int userId = jsonObjectData.getJSONObject(i).getInt("origin_author.user_id");
                            String createdAt = jsonObjectData.getJSONObject(i).getString("createdAt");
                            int connectionCount = jsonObjectData.getJSONObject(i).getInt("connectionCount");

                            adapter.addNewCombineMemo(new NewCombineMemo(memoId, body, nickname, userId, createdAt, connectionCount));
                        }

                        result = jsonFromServer.getString("msg");

                    } catch(JSONException jsone) {
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
                e("onPostExecute", result);
                adapter.notifyDataSetChanged();
            } else {
                e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncCombineMemoAdd extends AsyncTask<Integer, Integer, String> {
        int parent_memo_id;
        int child_memo_id;
        int ordernum;

        @Override
        protected String doInBackground(Integer... params) {
            parent_memo_id = params[0];
            child_memo_id = params[1];
            ordernum = params[2];
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
                        .add("parent_memo_id", String.valueOf(parent_memo_id))
                        .add("child_memo_id", String.valueOf(child_memo_id))
                        .add("ordernum", String.valueOf(ordernum))
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_INSERT_COMBINE_MEMO)
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

            if ( result != null ) {
                e("onPostExecute", result);
                // 저장후 조합메모리스트 초기화
                combineMemoList.clear();
            } else {
                e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncHiddenMemoAdd extends AsyncTask<HiddenMemo, Integer, String> {
        int parent_memo_id;
        int user_id;
        String body;
        int ordernum;

        @Override
        protected String doInBackground(HiddenMemo... params) {
            parent_memo_id = params[0].parent_memo_id;
            user_id = params[0].user_id;
            body = params[0].body;
            ordernum = params[0].ordernum;
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
                        .add("parent_memo_id", String.valueOf(parent_memo_id))
                        .add("user_id", String.valueOf(user_id))
                        .add("body", body)
                        .add("ordernum", String.valueOf(ordernum))
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_INSERT_HIDDEN_MEMO)
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

            if ( result != null ) {
                e("onPostExecute", result);
                // 저장후 히든메모리스트 초기화
                hiddenMemoList.clear();
            } else {
                e("onPostExecute", "result is empty");
            }
        }
    }
}
