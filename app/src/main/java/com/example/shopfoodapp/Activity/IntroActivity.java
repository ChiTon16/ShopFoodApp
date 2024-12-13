package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.shopfoodapp.databinding.ActivityIntroBinding;

public class IntroActivity extends BaseActivity {
ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
        getWindow().setStatusBarColor(Color.parseColor("#0D1F29"));
    }

    private void setVariable() {
        binding.loginBtn.setOnClickListener(view -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
            }
            else {
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            }
        });

        binding.signupBtn.setOnClickListener(view -> startActivity(new Intent(IntroActivity.this, SignupActivity.class)));
    }
}