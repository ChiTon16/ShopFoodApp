package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopfoodapp.databinding.ActivitySingupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    ActivitySingupBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    Handler handler = new Handler(Looper.getMainLooper());
    boolean isCheckingVerification = false;
    boolean canResendVerificationEmail = true; // Kiểm soát trạng thái gửi lại email
    CountDownTimer countDownTimer; // Bộ đếm thời gian

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Xử lý sự kiện đăng ký
        binding.btnSingup.setOnClickListener(view -> {
            String email = binding.edtEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            String username = binding.etUsername.getText().toString();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo tài khoản mới
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUsernameToFirestore(user.getUid(), username);
                                sendVerificationEmail(user);
                                startVerificationCheck(user);
                            }
                        } else {
                            Toast.makeText(this, "Failed to create account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Xử lý sự kiện gửi lại email xác thực
        binding.btnResend.setOnClickListener(view -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && canResendVerificationEmail) {
                sendVerificationEmail(user);
                canResendVerificationEmail = false;
                // Đặt trạng thái có thể gửi lại sau 30 giây
                new Handler().postDelayed(() -> canResendVerificationEmail = true, 30000);
            } else {
                Toast.makeText(this, "Please wait before resending email.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnLogin.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void saveUsernameToFirestore(String userId, String username) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("emailVerified", false); // Thêm cờ kiểm tra trạng thái email

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("check username", "complete save username");
                })
                .addOnFailureListener(e -> {
                    Log.d("check username", "complete save username");
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startVerificationCheck(FirebaseUser user) {
        isCheckingVerification = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                user.reload(); // Cập nhật trạng thái người dùng từ Firebase
                if (user.isEmailVerified()) {
                    isCheckingVerification = false;
                    updateEmailVerifiedInFirestore(user.getUid());
                    Toast.makeText(SignupActivity.this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                    // Chuyển đến màn hình tiếp theo
                    Intent intent = new Intent(SignupActivity.this, CompleteSingupActivity.class);
                    startActivity(intent);
                    finish();
                } else if (isCheckingVerification) {
                    handler.postDelayed(this, 3000); // Lặp lại kiểm tra sau 3 giây
                }
            }
        }, 3000); // Kiểm tra lần đầu sau 3 giây
    }

    private void startResendCountdown() {
        // Vô hiệu hóa nút resend
        binding.btnResend.setEnabled(false);

        // Bộ đếm 15 giây
        countDownTimer = new CountDownTimer(15000, 1000) { // 15 giây, cập nhật mỗi 1 giây
            @Override
            public void onTick(long millisUntilFinished) {
                // Hiển thị thời gian còn lại trên nút resend
                binding.btnResend.setText("Resend in " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                // Kích hoạt lại nút resend sau khi đếm xong
                binding.btnResend.setEnabled(true);
                binding.btnResend.setText("Resend Verification Email");
            }
        };

        countDownTimer.start();
    }


    private void updateEmailVerifiedInFirestore(String userId) {
        db.collection("users").document(userId)
                .update("emailVerified", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User email verified status updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update email verified status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
