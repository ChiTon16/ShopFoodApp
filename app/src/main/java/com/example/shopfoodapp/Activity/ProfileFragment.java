package com.example.shopfoodapp.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopfoodapp.R;
import com.example.shopfoodapp.databinding.FragmentProfileBinding;
import com.google.firebase.database.DatabaseReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        sharedPreferences = requireContext().getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("night", false);

        // Set initial night mode state
        if (nightMode) {
            binding.swither.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        setupListeners();

        return binding.getRoot();
    }

    private void setupListeners() {
        // Night mode toggle listener
        binding.swither.setOnClickListener(view -> {
            if (nightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor = sharedPreferences.edit();
                editor.putBoolean("night", false);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor = sharedPreferences.edit();
                editor.putBoolean("night", true);
            }
            editor.apply();
        });
    }
}