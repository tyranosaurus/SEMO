package com.tacademy.semo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tacademy.semo.R;
import com.tacademy.semo.application.SemoApplication;

public class LoginActivity extends AppCompatActivity {

    // 쉐어드 프리퍼런스
    public SharedPreferences preferencesGuide;
    public SharedPreferences.Editor editor;

    EditText editTextEmail;
    EditText editTextPassword;
    TextView textViewLogin;
    CheckBox checkBoxAutoLogin;
    TextView textViewJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 쉐어드 프리퍼런스 설정
        preferencesGuide = getSharedPreferences("login", 0);
        editor = preferencesGuide.edit();

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textViewLogin = (TextView) findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 - 서버에서 일치하는 유저 있는지 이메일, 비번 체크해야함
                // 지금은 프리퍼런스로 이용
                String email = preferencesGuide.getString("email", "noEmail");
                String password = preferencesGuide.getString("password", "noPassword");

                if ( editTextEmail.getText().toString().trim().isEmpty() || editTextEmail.getText().toString().trim().equals("") ) {

                    Toast.makeText(getApplicationContext(), "이메일을 입력해 주세요", Toast.LENGTH_SHORT).show();

                    return;
                }

                if ( editTextPassword.getText().toString().trim().isEmpty() || editTextPassword.getText().toString().trim().equals("") ) {

                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해 주세요", Toast.LENGTH_SHORT).show();

                    return;
                }

                if ( email.equals(editTextEmail.getText().toString()) && password.equals(editTextPassword.getText().toString()) ) {

                    SemoApplication.setUserId(preferencesGuide.getInt("id", 6));

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);

                    finish();

                    return;
                } else {
                    Toast.makeText(getApplicationContext(), "이메일과 비빌번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();

                    return;
                }

            }
        });
        checkBoxAutoLogin = (CheckBox) findViewById(R.id.checkBoxAutoLogin);
        checkBoxAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 자동로그인
                if ( isChecked ) {
                    editor.putBoolean("autoLogin", true);
                    editor.commit();
                } else {
                    editor.putBoolean("autoLogin", false);
                    editor.commit();
                }
            }
        });
        textViewJoin = (TextView) findViewById(R.id.textViewJoin);
        textViewJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }
}
