package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.shopfoodapp.databinding.ActivitySingupBinding;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends BaseActivity {
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
            String username = binding.userNameEdt.getText().toString();

            if (password.length() < 6){
                Toast.makeText(SignupActivity.this, "Your password must be 6 character", Toast.LENGTH_SHORT).show();
            }
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, task -> {
                if (task.isComplete()) {
                    Log.i(TAG, "onComplete: ");
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Lấy UID của người dùng và lưu username vào Firestore
                        String userId = user.getUid();
                        saveUsernameToFirestore(userId, username);
                    }
                }
                else {
                    Log.i(TAG, "failure: ", task.getException());
                    Toast.makeText(SignupActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            });

        });
    }

    private void saveUsernameToFirestore(String userId, String username) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Lưu thành công
                    Toast.makeText(this, "SignUp successful", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi lưu username vào Firestore
                    Toast.makeText(this, "Error saving username", Toast.LENGTH_SHORT).show();
                });
    }
}