    package com.example.shopfoodapp.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopfoodapp.Adapter.BestFoodAdapter;
import com.example.shopfoodapp.Adapter.CategoryAdapter;
import com.example.shopfoodapp.Adapter.FoodListAdapter;
import com.example.shopfoodapp.Domain.Category;
import com.example.shopfoodapp.Domain.Foods;
import com.example.shopfoodapp.Domain.Location;
import com.example.shopfoodapp.Domain.Price;
import com.example.shopfoodapp.Domain.Time;
import com.example.shopfoodapp.R;
import com.example.shopfoodapp.databinding.ActivityMainBinding;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collections;

    public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            // Lấy đối tượng SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
            // Kiểm tra chế độ đêm đã được lưu
            boolean nightMode = sharedPreferences.getBoolean("night", false);
            // Áp dụng chế độ giao diện tương ứng
            if (nightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(Color.parseColor("#0D1F29"));

        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    // Hiển thị HomeFragment
                    replaceFragment(homeFragment);
                    return true;

                case R.id.profile:
                    // Mở ProfileActivity
                    replaceFragment(profileFragment);
                    return true;
            }
            return false;
        });
            // Hiển thị mặc định HomeFragment khi mở ứng dụng
            if (savedInstanceState == null) {
                binding.bottomNavigation.setSelectedItemId(R.id.home);
            }

        }

        private void replaceFragment(Fragment fragment) {
            getSupportFragmentManager()
                    .popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment) // frame_container là ID của FrameLayout trong layout
                    .commit();
        }

    }