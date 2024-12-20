package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shopfoodapp.R;
import com.example.shopfoodapp.databinding.ActivityCompleteSingupBinding;
import com.example.shopfoodapp.databinding.ActivitySingupBinding;

public class CompleteSingupActivity extends AppCompatActivity {
    ActivityCompleteSingupBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompleteSingupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnContinue.setOnClickListener(view -> startActivity(new Intent
                (CompleteSingupActivity.this, MainActivity.class)));
    }
}