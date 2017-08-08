package com.tacademy.semo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tacademy.semo.R;
import com.tacademy.semo.activity.KeywordActivity;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.item.RecommendKeyword;

import java.util.ArrayList;

public class KeywordFragment extends Fragment {

    int index;
    ArrayList<RecommendKeyword> recommendKeywordList = new ArrayList<>();
    LinearLayout linearLayoutRecommendKeyword;
    KeywordActivity activity;

    public KeywordFragment() {
    }

    public static KeywordFragment newInstance(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        KeywordFragment fragment = new KeywordFragment();
        fragment.setArguments(bundle);

        return fragment;
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
        activity = (KeywordActivity) getActivity();

        View rootView = inflater.inflate(R.layout.fragment_recommend_keyword, container, false);
        linearLayoutRecommendKeyword = (LinearLayout) rootView.findViewById(R.id.linearLayoutRecommendKeyword);

        if ( index == 0 ) {
            for ( int i = 0; i < 15; i++ ) {
                createRecommendKeyword(recommendKeywordList.get(i));
            }
        } else if ( index == 1 ) {
            for ( int i = 15; i < 30; i++ ) {
                createRecommendKeyword(recommendKeywordList.get(i));
            }
        } else if ( index == 2 ) {
            for ( int i = 30; i < 45; i++ ) {
                createRecommendKeyword(recommendKeywordList.get(i));
            }
        }

        return rootView;
    }

    public void setRecommendKeywords(ArrayList<RecommendKeyword> list){
        recommendKeywordList = list;
    }

    int columnCountRecommend = 0;
    int rowCountRecommend = 0;
    LinearLayout linearLayoutRecommend = null;
    public void createRecommendKeyword(final RecommendKeyword newKeyword) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Integer.parseInt("-10"), Integer.parseInt("-10"), Integer.parseInt("-10"), Integer.parseInt("-10"));

        final TextView textView = new TextView(getActivity());
        textView.setTextSize(10);
        textView.setTextColor(Color.parseColor("#4990e2"));
        textView.setLayoutParams(params);
        textView.setBackgroundResource(R.drawable.keyword_item);
        textView.setGravity(Gravity.CENTER);
        textView.setText(newKeyword.keyword);
        textView.setFocusable(false);

        // 내 키워드에 등록된 추천 키워드는 클릭 안됨
        if ( newKeyword.owned ) {
            textView.setTextColor(Color.parseColor("#bdbdbd"));
            textView.setEnabled(false);
        }

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 내 키워드가 눌려있다면 초기화
                for ( int i = 0; i < activity.selectedKeywordList.size(); i++ ) {
                    activity.selectedTextViewList.get(i).setBackgroundResource(R.drawable.keyword_item);
                    activity.selectedTextViewList.get(i).setFocusable(false);
                }
                activity.selectedKeywordList.clear();
                activity.selectedTextViewList.clear();

                if ( textView.isFocusable() ) {
                    activity.selectedRecommendKeywordList.remove(newKeyword);
                    activity.selectedRecommendTextViewList.remove(textView);

                    textView.setBackgroundResource(R.drawable.keyword_item);
                    textView.setFocusable(false);
                } else {
                    activity.selectedRecommendKeywordList.add(newKeyword);
                    activity.selectedRecommendTextViewList.add(textView);

                    textView.setBackgroundResource(R.drawable.keyword_item_selected);
                    textView.setFocusable(true);
                }
            }
        });

        activity.totalRecommendKeywordList.add(textView);

        if ( rowCountRecommend == 0 || columnCountRecommend >= 5 ) {
            // 새로운 row 생성
            linearLayoutRecommend = new LinearLayout(SemoApplication.getSemoContext());
            linearLayoutRecommend.setOrientation(LinearLayout.HORIZONTAL);
            linearLayoutRecommend.setWeightSum(0.0f);
            rowCountRecommend++;
            // row 추가
            linearLayoutRecommendKeyword.addView(linearLayoutRecommend);

            if ( columnCountRecommend >= 5 ) {
                columnCountRecommend = 0;
            }
        }

        if ( columnCountRecommend < 5 ) {
            linearLayoutRecommend.addView(textView);
            columnCountRecommend++;
        }
    }
}
