package com.example.shopfoodapp.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopfoodapp.Domain.UserModel;
import com.example.shopfoodapp.R;
import com.example.shopfoodapp.Utils.AndroidUtils;
import com.example.shopfoodapp.Utils.FirebaseUtils;
import com.example.shopfoodapp.databinding.FragmentProfileBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.database.DatabaseReference;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    UserModel currentUserModel;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;

    public ProfileFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data!=null && data.getData()!=null){
                            selectedImageUri = data.getData();
                            AndroidUtils.setProfilePic(getContext(),selectedImageUri,binding.profileAvt);
                        }
                    }
                }
        );
    }

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

        getUserData();
        setupListeners();

        binding.profleUpdateBtn.setOnClickListener((v -> {
            updateBtnClick();
        }));

        binding.profileAvt.setOnClickListener((v)->{
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });

        return binding.getRoot();
    }

    void updateBtnClick() {
        String newUsername = binding.profileUsername.getText().toString();
        if(newUsername.isEmpty() || newUsername.length()<3){
            binding.profileUsername.setError("Username length should be at least 3 chars");
            return;
        }
        currentUserModel.setUsername(newUsername);
        setInProgress(true);
        updateToFirestore();

        if(selectedImageUri!=null){
            FirebaseUtils.getCurrentProfilePicStorageRef().putFile(selectedImageUri)
                    .addOnCompleteListener(task -> {
                        updateToFirestore();
                    });
        }else{
            updateToFirestore();
        }

    }

    void updateToFirestore() {
        FirebaseUtils.currentUserDetails().set(currentUserModel)
                .addOnCompleteListener(task -> {
                    if (!isAdded()) return; // Fragment đã bị detach
                    setInProgress(false);
                    if (task.isSuccessful()) {
                        AndroidUtils.showToast(requireContext(), "Updated successfully");
                    } else {
                        AndroidUtils.showToast(requireContext(), "Updated failed");
                    }
                });
    }

    void getUserData(){
        setInProgress(true);

        FirebaseUtils.getCurrentProfilePicStorageRef().getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Uri uri  = task.getResult();
                        AndroidUtils.setProfilePic(getContext(),uri,binding.profileAvt);
                    } else {
                        // Sử dụng ảnh mặc định nếu không có ảnh trong Firebase Storage
                        AndroidUtils.setProfilePic(getContext(), Uri.parse("android.resource://"
                                + getContext().getPackageName() + "/" + R.drawable.avtpro), binding.profileAvt);
                    }
                });

        FirebaseUtils.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            currentUserModel = task.getResult().toObject(UserModel.class);
            binding.profileUsername.setText(currentUserModel.getUsername());
            binding.profilePhone.setText(currentUserModel.getPhone());
        });
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

        binding.logoutBtn.setOnClickListener(view -> {
            FirebaseUtils.logout();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    void setInProgress(boolean inProgress){
        if(inProgress){
            binding.profileProgressBar.setVisibility(View.VISIBLE);
            binding.profleUpdateBtn.setVisibility(View.GONE);
        }else{
            binding.profileProgressBar.setVisibility(View.GONE);
            binding.profleUpdateBtn.setVisibility(View.VISIBLE);
        }
    }
}