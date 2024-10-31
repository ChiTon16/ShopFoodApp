package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shopfoodapp.R;
import com.example.shopfoodapp.databinding.ActivitySingupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SingupActivity extends BaseActivity {
    ActivitySingupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
    }

    private void setVariable() {
        binding.signupBtn.setOnClickListener(view -> {
            String email = binding.userEdt.getText().toString();
            String password = binding.passEdt.getText().toString();

            if (password.length() < 6){
                Toast.makeText(SingupActivity.this, "Your password must be 6 character", Toast.LENGTH_SHORT).show();
            }
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SingupActivity.this, task -> {
                if (task.isComplete()) {
                    Log.i(TAG, "onComplete: ");
                    startActivity(new Intent(SingupActivity.this, MainActivity.class));
                }
                else {
                    Log.i(TAG, "failure: ", task.getException());
                    Toast.makeText(SingupActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}