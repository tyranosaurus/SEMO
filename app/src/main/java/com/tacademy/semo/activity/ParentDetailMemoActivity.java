package com.tacademy.semo.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.item.Comment;
import com.tacademy.semo.item.DtailParentMemo;
import com.tacademy.semo.item.IdeationChild;
import com.tacademy.semo.item.Memo;
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

import static android.util.Log.e;

public class ParentDetailMemoActivity extends AppCompatActivity {

    // 툴바
    Toolbar mToolbar;
    ActionBar mActionBar;

    // 화면
    ImageView imageViewBack;
    TextView textViewDetailMemo;
    ImageView imageViewOptionMenu;

    TextView textViewMemos;
    ImageView imageViewPin;
    TextView textViewWrittenDate;
    TextView textViewKeyword;
    EditText editTextComment;
    TextView textViewAddComment;
    TextView textViewCombinedTotal;
    TextView textViewCommentTotal;

    LinearLayout linearLayoutTargetMemo;
    LinearLayout linearLayoutCombineCancel;
    LinearLayout linearLayoutSave;
    LinearLayout linearLayoutParentMemoSpace;
    LinearLayout linearLayoutComment;

    // 메모 정보 받아올 Bundle 객체
    Bundle bundle;
    int fromUserId;
    int memo_id;
    Boolean pin;
    String body;
    String createdAt;
    String keyword;

    // 조합취소버튼
    ImageView imageViewCombineCancel;
    TextView textViewCombineCancel;

    // 저장버튼
    ImageView imageViewSave;
    TextView textViewSave;
    // 연관된 메모 더보기 버튼
    LinearLayout linearLayoutAddParent;
    // 댓글 더보기 버튼
    LinearLayout linearLayoutAddComment;

    // 내 메모의 차일드 메모들 저장하는 리스트
    ArrayList<IdeationChild> childrenMemoList = new ArrayList<>();
    ArrayList<View> dividedList = new ArrayList<>();
    ArrayList<Memo> modifiedMemoList = new ArrayList<>();
    ArrayList<TextView> modifiedViewList = new ArrayList<>();

    ArrayList<DtailParentMemo> parentMemoList = new ArrayList<>();
    ArrayList<Comment> commentList = new ArrayList<>();

    // 부모메모, 댓글 인덱스
    int parentMemoIndex = 0;
    int commentIndex = 0;

    // 키보드 내리기
    InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_detail_memo);

        // 키보드
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 타켓메모, 부모메모, 댓글 레이아웃
        linearLayoutTargetMemo = (LinearLayout) findViewById(R.id.linearLayoutTargetMemo);
        linearLayoutParentMemoSpace = (LinearLayout) findViewById(R.id.linearLayoutParentMemoSpace);
        linearLayoutComment = (LinearLayout) findViewById(R.id.linearLayoutComment);

        // 인텐트에 들어있는 메모정보 가져오기
        bundle = getIntent().getBundleExtra("memoInfo");
        fromUserId = bundle.getInt("userId");
        memo_id = bundle.getInt("memo_id");
        pin = bundle.getBoolean("pin");
        body = bundle.getString("body");
        createdAt = bundle.getString("createdAt");
        keyword = bundle.getString("keyword");

        // 툴바 설정
        mToolbar = (Toolbar) findViewById(R.id.toolbarSetting);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        // 툴바에 사용할 커스텀 레이아웃 인플레이션
        View viewToolBar = getLayoutInflater().inflate(R.layout.toolbar_detail_memo, null);

        imageViewBack = (ImageView) viewToolBar.findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textViewDetailMemo = (TextView) viewToolBar.findViewById(R.id.textViewDetailMemo);

        imageViewPin = (ImageView) viewToolBar.findViewById(R.id.imageViewPin);
        if ( pin ) {
            imageViewPin.setImageResource(R.drawable.icon_pin);
        } else {
            imageViewPin.setImageResource(R.drawable.icon_unpin);
        }

        imageViewOptionMenu = (ImageView) viewToolBar.findViewById(R.id.imageViewOptionMenu);
        imageViewOptionMenu.setImageResource(R.drawable.icon_settings_unselected);
        imageViewOptionMenu.setEnabled(false);

        // 인플레이션한 커스텀 툴바 세팅
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        mActionBar.setCustomView(viewToolBar, layoutParams);

        // 메모
        textViewMemos = (TextView) findViewById(R.id.textViewMemos);
        textViewMemos.setText(body);

        // 메모 작성 시간
        textViewWrittenDate = (TextView) findViewById(R.id.textViewWrittenDate);
        textViewWrittenDate.setText(createdAt);

        // 키워드
        textViewKeyword = (TextView) findViewById(R.id.textViewKeyword);
        textViewKeyword.setText(keyword);

        // 조합취소 레이아웃
        imageViewCombineCancel = (ImageView) findViewById(R.id.imageViewCombineCancel);
        imageViewCombineCancel.setImageResource(R.drawable.icon_combine_unselected);
        textViewCombineCancel = (TextView) findViewById(R.id.textViewCombineCancel);
        textViewCombineCancel.setTextColor(Color.parseColor("#656565"));

        linearLayoutCombineCancel = (LinearLayout) findViewById(R.id.linearLayoutCombineCancel);
        linearLayoutCombineCancel.setEnabled(false);

        // 저장버튼 레이아웃
        imageViewSave = (ImageView) findViewById(R.id.imageViewSave);
        imageViewSave.setImageResource(R.drawable.icon_unsave);
        textViewSave = (TextView) findViewById(R.id.textViewSave);
        textViewSave.setTextColor(Color.parseColor("#656565"));

        linearLayoutSave = (LinearLayout) findViewById(R.id.linearLayoutSave);
        linearLayoutSave.setEnabled(false);

        // 연관된 메모 더보기 버튼
        textViewCombinedTotal = (TextView) findViewById(R.id.textViewCombinedTotal);
        textViewCombinedTotal.setText("내 메모를 조합한 메모(" + parentMemoList.size() + ")");

        linearLayoutAddParent = (LinearLayout) findViewById(R.id.linearLayoutAddParent);
        linearLayoutAddParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 혹시 추가된 댓글이 있으면 갱신
                textViewCombinedTotal.setText("내 메모를 조합한 메모(" + parentMemoList.size() + ")");

                if ( parentMemoList.size() < 1 ) {
                    Toast.makeText(getApplicationContext(), "내 메모를 조합한 메모가 없습니다", Toast.LENGTH_SHORT).show();

                    return;
                }

                for ( int i = parentMemoIndex; i < parentMemoIndex + 3; i++ ) { // 최초 댓글 4개만 보이고 더보기 버튼을 눌러야 이후 4개가 나온다.
                    if ( i > parentMemoList.size() - 1) {
                        parentMemoIndex = parentMemoList.size() - 1; // 댓글리스트를 모두 다 불러왔다면 commentIndex는 리스트의 마지막을 가리킴

                        Toast.makeText(getApplicationContext(), "내 메모를 조합한 메모를 모두 불러왔습니다.", Toast.LENGTH_SHORT).show();

                        break;
                    }

                    linearLayoutParentMemoSpace.addView(addParentMemo(parentMemoList.get(i)));
                }
                parentMemoIndex += 3;
            }
        });

        // 댓글 더보기 버튼
        textViewCommentTotal = (TextView) findViewById(R.id.textViewCommentTotal);
        textViewCommentTotal.setText("댓글(" + commentList.size() + ")");

        linearLayoutAddComment = (LinearLayout) findViewById(R.id.linearLayoutAddComment);
        linearLayoutAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 혹시 추가된 댓글이 있으면 갱신
                textViewCommentTotal.setText("댓글(" + commentList.size() + ")");

                if ( commentList.size() < 1 ) {
                    Toast.makeText(getApplicationContext(), "댓글이 없습니다", Toast.LENGTH_SHORT).show();

                    return;
                }

                for ( int i = commentIndex; i < commentIndex + 4; i++ ) { // 최초 댓글 4개만 보이고 더보기 버튼을 눌러야 이후 4개가 나온다.
                    if ( i > commentList.size() - 1) {
                        commentIndex = commentList.size() - 1; // 댓글리스트를 모두 다 불러왔다면 commentIndex는 리스트의 마지막을 가리킴

                        Toast.makeText(getApplicationContext(), "댓글을 모두 불러왔습니다.", Toast.LENGTH_SHORT).show();

                        break;
                    }

                    linearLayoutComment.addView(addComment(commentList.get(i)));
                }
                commentIndex += 4;
            }
        });

        // 댓글 게시 버튼
        editTextComment = (EditText) findViewById(R.id.editTextComment);
        textViewAddComment = (TextView) findViewById(R.id.textViewAddComment);
        textViewAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 댓글 비었을때 처리(글자없고 엔터만 있는 경우도 처리
                if ( editTextComment.getText().toString().trim().isEmpty() || editTextComment.getText().toString().trim().equals("")) {

                    Toast.makeText(getApplicationContext(), "댓글이 비었습니다.", Toast.LENGTH_SHORT).show();

                    editTextComment.setText("");
                    inputMethodManager.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);

                    return;
                } else {
                    // 댓글 생성 어싱크태스크
                    new AsyncCommentAdd().execute(editTextComment.getText().toString());
                }

                editTextComment.setText("");
                inputMethodManager.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);

                // 새로 댓글 쓰고 갱신
                textViewCommentTotal.setText("댓글(" + commentList.size() + ")");
            }
        });

        // 상세보기할 메모 가져오기
        new AsyncDetailMemo().execute(memo_id);
    }

    public void addChildCombineMemo(String combineBody, int memo_id) {
        final Memo childCombineMemo = new Memo(memo_id, combineBody);

        // 구분선 및 조합메모가 들어갈 레이아웃
        LinearLayout.LayoutParams linearLayoutParmasAll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                     LinearLayout.LayoutParams.WRAP_CONTENT);

        final LinearLayout dividedCombinedLayout = new LinearLayout(getApplicationContext());
        dividedCombinedLayout.setOrientation(LinearLayout.VERTICAL);
        dividedCombinedLayout.setLayoutParams(linearLayoutParmasAll);

        // 조합메모가 들어갈 레이아웃
        final int layoutMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams linearLayoutParmas = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParmas.setMargins(layoutMargin, 0, layoutMargin, layoutMargin);

        final LinearLayout newCombineLayout = new LinearLayout(getApplicationContext());
        newCombineLayout.setOrientation(LinearLayout.HORIZONTAL);
        newCombineLayout.setLayoutParams(linearLayoutParmas);

        // 메모 추가
        final int textVIewMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.weight = 1;
        textViewParams.setMargins(textVIewMargin, 0, 0, 0);
        textViewParams.gravity = Gravity.CENTER;

        final TextView textViewCombineMemo = new TextView(getApplicationContext());
        textViewCombineMemo.setLayoutParams(textViewParams);
        textViewCombineMemo.setTag("false");

        // 구분선 추가
        final int heightDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        final int marginDividedLine = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams dividedLineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightDividedLine);
        dividedLineParams.setMargins(marginDividedLine, 0, marginDividedLine, marginDividedLine);

        View viewDividedLine = new View(getApplicationContext());
        viewDividedLine.setLayoutParams(dividedLineParams);
        viewDividedLine.setBackgroundColor(Color.parseColor("#e0e0e0"));
        viewDividedLine.setAlpha(0.6f);
        viewDividedLine.setTag(combineBody);

        // 조합메모 레이아웃에 들어갈 링크 이미지 추가
        final int widthImageView = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        final int heightImageView = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(widthImageView, heightImageView);
        imageViewParams.gravity = Gravity.CENTER;

        final ImageView imageViewLink = new ImageView(getApplicationContext());
        imageViewLink.setImageResource(R.drawable.icon_link);
        imageViewLink.setLayoutParams(imageViewParams);

        textViewCombineMemo.setTextColor(Color.parseColor("#de000000"));
        textViewCombineMemo.setTextSize(12.0f);
        textViewCombineMemo.setText(combineBody);

        newCombineLayout.addView(imageViewLink); // 링크 이미지 추가
        newCombineLayout.addView(textViewCombineMemo); // 조합 메모 추가

        dividedList.add(viewDividedLine); // 구분선 추가

        // 조합메모 레이아웃 붙임
        dividedCombinedLayout.addView(viewDividedLine);
        dividedCombinedLayout.addView(newCombineLayout);
        linearLayoutTargetMemo.addView(dividedCombinedLayout); // 조합 메모 추가
    }

    public TextView addChildHiddenMemo(String hiddenBody) {
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

        TextView textViewHiddenMemo = new TextView(getApplicationContext());
        textViewHiddenMemo.setLayoutParams(textViewParams);
        textViewHiddenMemo.setTextColor(Color.parseColor("#46d4b4"));
        textViewHiddenMemo.setTextSize(12.0f);
        textViewHiddenMemo.setText(hiddenBody);

        // 히든메모가 들어갈 레이아웃
        final int layoutMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams linearLayoutHiddenParmas = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                            LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutHiddenParmas.setMargins(layoutMargin, 0, layoutMargin, layoutMargin);

        LinearLayout newHiddenLayout = new LinearLayout(getApplicationContext());
        newHiddenLayout.setOrientation(LinearLayout.HORIZONTAL);
        newHiddenLayout.setLayoutParams(linearLayoutHiddenParmas);

        newHiddenLayout.addView(imageViewHidden); // 히든 이미지 추가
        newHiddenLayout.addView(textViewHiddenMemo); // 히든 메모 추가

        linearLayoutTargetMemo.addView(viewDividedLine); // 구분선 추가
        linearLayoutTargetMemo.addView(newHiddenLayout); // 히든메모 추가

        return textViewHiddenMemo;
    }

    public LinearLayout addParentMemo(final DtailParentMemo dtailParentMemo) {
        // 인플레이터
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final int textVIewMarginTB = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, getResources().getDisplayMetrics());
        final int textVIewMarginLR = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -9, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                     LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.setMargins(textVIewMarginLR, textVIewMarginTB, textVIewMarginLR, textVIewMarginTB);

        LinearLayout linearLayoutParentMemo = (LinearLayout) inflater.inflate(R.layout.item_parent_memo, null, true);
        linearLayoutParentMemo.setOrientation(LinearLayout.VERTICAL);
        linearLayoutParentMemo.setLayoutParams(linearLayoutParams);

        // 부모메모 작성자 닉네임
        TextView textViewNickname = (TextView) linearLayoutParentMemo.findViewById(R.id.textViewNickname);
        textViewNickname.setText(dtailParentMemo.userNickname);

        ImageView imageViewDetailMemo = (ImageView) linearLayoutParentMemo.findViewById(R.id.imageViewDetailMemo);
        imageViewDetailMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 번들객체에 메모 데이터 저장
                Bundle bundle = new Bundle();
                bundle.putInt("memo_id", dtailParentMemo.memo_id);
                bundle.putBoolean("pin", dtailParentMemo.pin);
                bundle.putString("body", dtailParentMemo.body);
                bundle.putString("createdAt", dtailParentMemo.createdAt);
                bundle.putInt("userId", fromUserId);
                if ( dtailParentMemo.keyword.equals("null") ) {
                    dtailParentMemo.keyword = "";
                }
                bundle.putString("keyword", dtailParentMemo.keyword);
                //bundle.putInt("userId", userId);

                Intent intent = new Intent(ParentDetailMemoActivity.this, ParentDetailMemoActivity.class);
                intent.putExtra("memoInfo", bundle);

                startActivity(intent);
            }
        });

        // 부보메모 내용
        TextView textViewBody = (TextView) linearLayoutParentMemo.findViewById(R.id.textViewBody);
        textViewBody.setText(dtailParentMemo.body);

        return linearLayoutParentMemo;
    }

    public LinearLayout addComment(Comment comment) {
        // 프로필 이미지
        final int widthImageView = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
        final int heightImageView = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
        final int circleImageViewMarginLR = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        final int circleImageViewMarginTB = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        final int circleBorder = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(widthImageView, heightImageView);
        imageViewParams.gravity = Gravity.CENTER;
        imageViewParams.setMargins(circleImageViewMarginLR, circleImageViewMarginTB, circleImageViewMarginLR, circleImageViewMarginTB);

        CircleImageView circleProfile = new CircleImageView(getApplicationContext());
        circleProfile.setLayoutParams(imageViewParams);
        circleProfile.setBorderColor(Color.parseColor("#bdbdbd"));
        circleProfile.setBorderWidth(circleBorder);
        // 프로필 Glide 설정
        Glide.with(SemoApplication.getSemoContext())
                .load(comment.user.profileImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(circleProfile);


        // 닉네임
        final int textVIewMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams textViewNicknameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                         LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewNicknameParams.setMargins(0, 0, 0, textVIewMargin);
        textViewNicknameParams.gravity = Gravity.CENTER_VERTICAL;

        TextView textViewNickname = new TextView(getApplicationContext());
        textViewNickname.setLayoutParams(textViewNicknameParams);
        textViewNickname.setTextColor(Color.parseColor("#4990e2"));
        textViewNickname.setTextSize(12.0f);
        textViewNickname.setText(comment.user.nickName);

        // 댓글내용
        LinearLayout.LayoutParams textViewCommentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                        LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView textViewComment = new TextView(getApplicationContext());
        textViewComment.setLayoutParams(textViewCommentParams);
        textViewComment.setTextColor(Color.parseColor("#de000000"));
        textViewComment.setTextSize(12.0f);
        textViewComment.setText(comment.body);

        // 닉네임, 댓글내용 담는 리니어레이아웃
        final int layoutMarginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        final int layoutMarginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams linearLayoutNickCommentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                           LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutNickCommentParams.setMargins(0, layoutMarginTop, layoutMarginRight, 0);

        LinearLayout linearLayoutNickComment = new LinearLayout(getApplicationContext());
        linearLayoutNickComment.setLayoutParams(linearLayoutNickCommentParams);

        linearLayoutNickComment.setOrientation(LinearLayout.VERTICAL);
        linearLayoutNickComment.addView(textViewNickname); // 닉네임 추가
        linearLayoutNickComment.addView(textViewComment); // 댓글내용 추가

        // 프로필, 닉네임, 댓글내용 담는 리니어레이아웃
        LinearLayout.LayoutParams linearLayoutPNCParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                        LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout linearLayoutPNC = new LinearLayout(getApplicationContext());
        linearLayoutPNC.setLayoutParams(linearLayoutPNCParams);

        linearLayoutPNC.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutPNC.addView(circleProfile);
        linearLayoutPNC.addView(linearLayoutNickComment);

        // 댓글 생성시간
        final int textVIewCommentMarginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        final int textVIewCommentMarginLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 76, getResources().getDisplayMetrics());
        final int textVIewCommentMarginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams textViewCreatedAtParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                          LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewCreatedAtParams.setMargins(textVIewCommentMarginLeft, textVIewCommentMarginTop, 0, textVIewCommentMarginBottom);

        TextView textViewCreatedAt = new TextView(getApplicationContext());
        textViewCreatedAt.setLayoutParams(textViewCreatedAtParams);
        textViewCreatedAt.setTextColor(Color.parseColor("#9e9e9e"));
        textViewCreatedAt.setTextSize(10.0f);
        textViewCreatedAt.setText(comment.createdAt);

        // 모두 들어가있는 레이아웃
        final int lastLayoutMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams linearLayoutAll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                  LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutAll.setMargins(0, 0, 0, lastLayoutMargin);

        LinearLayout linearLayoutLast = new LinearLayout(getApplicationContext());
        linearLayoutLast.setLayoutParams(linearLayoutAll);
        linearLayoutLast.setBackgroundColor(Color.parseColor("#ffffffff"));

        linearLayoutLast.setOrientation(LinearLayout.VERTICAL);
        linearLayoutLast.addView(linearLayoutPNC);
        linearLayoutLast.addView(textViewCreatedAt);

        return linearLayoutLast;
    }

    public class AsyncDetailMemo extends AsyncTask<Integer, Integer, String> {
        Dialog progressDialog;
        int detailMemoId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new Dialog(ParentDetailMemoActivity.this, R.style.SemoDialog);
            progressDialog.setCancelable(true);

            ProgressBar progressbar = new ProgressBar(ParentDetailMemoActivity.this);
            progressbar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.semo_progress_dialog));

            progressDialog.addContentView(progressbar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {
            detailMemoId = params[0];
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
                        .url(NetworkDefineConstant.SERVER_URL_SELECT_DETAIL_MEMO + detailMemoId)
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
                        JSONArray jsonArrayChildMemos = jsonFromServer.getJSONArray("children"); // 조합메모
                        JSONArray jsonArrayParentMemos = jsonFromServer.getJSONArray("parents"); // 내메모의 부모메모
                        JSONArray jsonArrayReplies = jsonFromServer.getJSONArray("replies"); // 댓글

                        // 타겟메모의 부모메모들
                        String keyword = "";

                        for ( int i = 0; i < jsonArrayParentMemos.length(); i++ ) {

                            if ( jsonArrayParentMemos.getJSONObject(i).getString("Tags.name") != null ) {
                                keyword = jsonArrayParentMemos.getJSONObject(i).getString("Tags.name");
                            }

                            parentMemoList.add(new DtailParentMemo( jsonArrayParentMemos.getJSONObject(i).getInt("memo_id"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getString("body"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getBoolean("shared"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getBoolean("hidden"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getBoolean("deleted"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getBoolean("pin"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getString("modifiedAt"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getString("createdAt"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getInt("connectionCount"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getInt("user_id"),
                                                                    jsonArrayParentMemos.getJSONObject(i).getString("origin_author.nickname"),
                                                                    0,
                                                                    keyword));
                            keyword = "";
                        }

                        // 댓글
                        for ( int i = 0; i < jsonArrayReplies.length(); i++ ) {
                            JSONObject jsonObjectUser = jsonArrayReplies.getJSONObject(i).getJSONObject("User");

                            commentList.add(0, new Comment( jsonArrayReplies.getJSONObject(i).getInt("reply_id"),
                                                            jsonArrayReplies.getJSONObject(i).getString("body"),
                                                            jsonArrayReplies.getJSONObject(i).getString("createdAt"),
                                                            jsonArrayReplies.getJSONObject(i).getString("modifiedAt"),
                                                            new User(jsonObjectUser.getInt("user_id"),
                                                                     jsonObjectUser.getString("nickname"),
                                                                     jsonObjectUser.getString("photo"),
                                                                     jsonObjectUser.getString("email"),
                                                                     jsonObjectUser.getString("token"))));
                        }

                        if ( jsonArrayChildMemos.length() < 1) {
                            String noChild = "noChild";
                            modifiedMemoList.add(new Memo(memo_id, body));
                            modifiedViewList.add(textViewMemos);

                            return noChild;
                        }
                        // 타겟메모의 자식메모들
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
                Log.e("onPostExecute", result);
                // 부모메모 뷰 생성
                textViewCombinedTotal.setText("내 메모를 조합한 메모(" + parentMemoList.size() + ")");
                if ( parentMemoList.size() < 3 ) {
                    for ( int i = 0; i < parentMemoList.size(); i++ ) {
                        linearLayoutParentMemoSpace.addView(addParentMemo(parentMemoList.get(i)));
                    }
                    parentMemoIndex += parentMemoList.size();
                } else {
                    for ( int i = 0; i < 3; i++ ) {
                        linearLayoutParentMemoSpace.addView(addParentMemo(parentMemoList.get(i)));
                    }
                    parentMemoIndex += 3;
                }
                // 댓글 뷰 생성
                textViewCommentTotal.setText("댓글(" + commentList.size() + ")");
                if ( commentList.size() < 4 ) {
                    for ( int i = 0; i < commentList.size(); i++ ) {
                        linearLayoutComment.addView(addComment(commentList.get(i)));
                    }
                    commentIndex += commentList.size();
                } else {
                    for ( int i = 0; i < 4; i++ ) { // 최초 댓글 4개만 보이고 더보기 버튼을 눌러야 이후 4개가 나온다.
                        linearLayoutComment.addView(addComment(commentList.get(i)));
                    }
                    commentIndex += 4;
                }
                // 내 메모와 수정될 히든메모만 따로 리스트에 넣는다.(내 메모는 먼저 넣어놓는다)
                modifiedMemoList.add(new Memo(memo_id, body));
                modifiedViewList.add(textViewMemos);
                if ( result.equals("noChild") ) {
                    Log.e("DETAIL", "No Child");

                    return;
                }

                // 여기서 차일드에 맞는 뷰 생성해줄 것
                for ( int i = 0; i < childrenMemoList.size(); i++ ) {
                    if ( childrenMemoList.get(i).hidden ) { // 차일드가 히든메모. 수정 못하게 막음
                        modifiedMemoList.add(new Memo(childrenMemoList.get(i).memo_id, null));
                        modifiedViewList.add(addChildHiddenMemo(childrenMemoList.get(i).body));
                    } else { // 차일드가 조합메모
                        addChildCombineMemo(childrenMemoList.get(i).body, childrenMemoList.get(i).memo_id);
                    }
                }
            } else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }

    public class AsyncCommentAdd extends AsyncTask <String, Integer, String> {
        String commentBody;

        @Override
        protected String doInBackground(String... params) {
            commentBody = params[0];
            boolean flag = false;
            // 서버 요청 성공시 응답받은 JSON 값
            String result = "";

            Response response = null;
            OkHttpClient toServer = null;

            try{
                Log.e("메모아이디", memo_id+"");
                Log.e("댓글달 유저아이디", fromUserId+"");
                Log.e("댓글내용", commentBody+"");
                toServer = OkHttpInitSingtonManager.getOkHttpClient();
                //요청 Form세팅(멀티파트 폼 데이터 형식)
                RequestBody postBody = new FormBody.Builder()
                        .add("memo_id", String.valueOf(memo_id)) // 댓글이 붙을 메모 아이디
                        .add("user_id", String.valueOf(fromUserId)) // 댓글을 다는 유저 아이디
                        .add("body", commentBody) // 댓글 내용
                        .build();
                //요청 세팅(form(Query String) 방식의 포스트)
                Request request = new Request.Builder()
                        .url(NetworkDefineConstant.SERVER_URL_INSERT_COMMENT)
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

                        JSONObject jsonObjectNewComment = jsonObject.getJSONObject("result");
                        JSONObject jsonObjectUser = jsonObjectNewComment.getJSONObject("User");

                        commentList.add(0, new Comment( jsonObjectNewComment.getInt("reply_id"),
                                jsonObjectNewComment.getString("body"),
                                jsonObjectNewComment.getString("createdAt"),
                                jsonObjectNewComment.getString("modifiedAt"),
                                new User(jsonObjectUser.getInt("user_id"),
                                        jsonObjectUser.getString("nickname"),
                                        jsonObjectUser.getString("photo"),
                                        jsonObjectUser.getString("email"),
                                        jsonObjectUser.getString("token"))));
                        commentIndex++;

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
                Log.e("onPostExecute", result);

                // 새로 단 댓글 뷰 생성
                linearLayoutComment.addView(addComment(commentList.get(0)), 0);

                // 새로 댓글 쓰고 갱신
                textViewCommentTotal.setText("댓글(" + commentList.size() + ")");

            }  else {
                Log.e("onPostExecute", "result is empty");
            }
        }
    }
}
