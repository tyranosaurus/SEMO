package com.tacademy.semo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.tacademy.semo.R;
import com.tacademy.semo.fragment.GuideFragment;

import java.util.ArrayList;

public class GuideActivity extends AppCompatActivity {

    // 쉐어드 프리퍼런스
    public SharedPreferences preferencesGuide;
    public SharedPreferences.Editor editor;

    ViewPager mViewPager;
    GuidePagerAdapter mAdapter;
    TabLayout mTabLayout;
    TextView mTextView;

    // 가이드 프래그먼트 리스트
    ArrayList<GuideFragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        // 쉐어드 프리퍼런스 설정
        preferencesGuide = getSharedPreferences("guide", 0);
        editor = preferencesGuide.edit();

        // ==========================================================================================================================================

        /*editor.putBoolean("offGuide", false);
                editor.commit();*/
        if ( preferencesGuide.getBoolean("offGuide", false) ) {
            Intent intent = new Intent(GuideActivity.this, LoginActivity.class);
            startActivity(intent);

            // 가이드 액티비티 종료
            finish();
        }

        // 스킵 설정
        mTextView = (TextView) findViewById(R.id.textView_skip);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("offGuide", true);
                editor.commit();

                Intent intent = new Intent(GuideActivity.this, LoginActivity.class);
                startActivity(intent);

                for ( int i = 0; i < fragmentList.size(); i++ ) {
                    recycleView(fragmentList.get(i).getImageView());
                }

                // 가이드 액티비티 종료
                finish();
            }
        });

        // 뷰페이저 설정
        mViewPager = (ViewPager) findViewById(R.id.guide_pager);
        mAdapter = new GuidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        // 탭 설정
        mTabLayout = (TabLayout) findViewById(R.id.tapLayout);
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());

        mViewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());

                recycleView(fragmentList.get(tab.getPosition()).getImageView());

                // 마지막 프래그먼트에서 tab 바, skip 텍스트 안보이게 설정
                if ( tab.getPosition() == mAdapter.getCount() - 1 ) {
                    mTextView.setVisibility(View.INVISIBLE);
                    mTabLayout.setVisibility(View.INVISIBLE);
                } else {
                    mTextView.setVisibility(View.VISIBLE);
                    mTabLayout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public class GuidePagerAdapter extends FragmentPagerAdapter {

        public GuidePagerAdapter(FragmentManager fm) {
            super(fm);

            fragmentList.add(GuideFragment.newInstance(0));
            fragmentList.add(GuideFragment.newInstance(1));
            fragmentList.add(GuideFragment.newInstance(2));
            fragmentList.add(GuideFragment.newInstance(3));
            fragmentList.add(GuideFragment.newInstance(4));
            fragmentList.add(GuideFragment.newInstance(5));

        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    @Override
    protected void onDestroy() {

        for ( int i = 0; i < fragmentList.size(); i++ ) {
            recycleView(fragmentList.get(i).getImageView());
        }

        super.onDestroy();
    }

    public void recycleView(View view) {

        if(view != null) {
            Drawable bg = view.getBackground();

            if(bg != null) {

                bg.setCallback(null);
                ((BitmapDrawable)bg).getBitmap().recycle();
                view.setBackgroundDrawable(null);
            }
        }
    }
}
