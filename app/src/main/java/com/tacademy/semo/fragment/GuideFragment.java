package com.tacademy.semo.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tacademy.semo.R;
import com.tacademy.semo.activity.GuideActivity;
import com.tacademy.semo.activity.HomeActivity;

public class GuideFragment extends Fragment {

    int index;
    ImageView imageView;
    ImageView imageViewIcon;
    TextView textViewMessage;
    Button button;

    public GuideFragment() {
    }

    public ImageView getImageView() {
        return imageView;
    }

    public static GuideFragment newInstance(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        GuideFragment fragment = new GuideFragment();
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
        final View rootView = inflater.inflate(R.layout.fragment_guide, container, false);

        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        // 이미지 아이콘
        imageViewIcon = (ImageView) rootView.findViewById(R.id.imageViewIcon);
        // 이미지 메세지
        textViewMessage = (TextView) rootView.findViewById(R.id.textViewMessage);
        // 완료 버튼 설정
        button = (Button) rootView.findViewById(R.id.button_complete);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GuideActivity) getActivity()).editor.putBoolean("offGuide", true);
                ((GuideActivity) getActivity()).editor.commit();

                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);

                getActivity().finish();
            }
        });

        // 프래그먼트에 따른 가이드 이미지 설정
        switch (index) {
            case 0:
                imageView.setImageDrawable((new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.guide01))));
                imageViewIcon.setImageResource(R.drawable.logo_semo);
                textViewMessage.setText(R.string.guide01);
                break;
            case 1:
                imageView.setImageDrawable((new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.guide02))));
                imageViewIcon.setImageResource(R.drawable.icon_guide02);
                textViewMessage.setText(R.string.guide02);
                break;
            case 2:
                imageView.setImageDrawable((new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.guide03))));
                imageViewIcon.setImageResource(R.drawable.icon_guide03);
                textViewMessage.setText(R.string.guide03);
                break;
            case 3:
                imageView.setImageDrawable((new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.guide04))));
                imageViewIcon.setImageResource(R.drawable.icon_guide04);
                textViewMessage.setText(R.string.guide04);
                break;
            case 4:
                imageView.setImageDrawable((new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.guide05))));
                imageViewIcon.setImageResource(R.drawable.icon_guide05);
                textViewMessage.setText(R.string.guide05);
                break;
            case 5:
                imageView.setImageDrawable((new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.guide06))));
                imageViewIcon.setImageResource(R.drawable.icon_guide06);
                textViewMessage.setText(R.string.guide06);

                button.setVisibility(View.VISIBLE);

                break;
            default:
                break;
        }

        return rootView;
    }
}
