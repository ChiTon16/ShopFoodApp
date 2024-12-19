package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.shopfoodapp.Adapter.BestFoodAdapter;
import com.example.shopfoodapp.Adapter.CategoryAdapter;
import com.example.shopfoodapp.Adapter.FoodListAdapter;
import com.example.shopfoodapp.Domain.Category;
import com.example.shopfoodapp.Domain.Foods;
import com.example.shopfoodapp.Domain.Price;
import com.example.shopfoodapp.Domain.Time;
import com.example.shopfoodapp.Domain.UserModel;
import com.example.shopfoodapp.R;
import com.example.shopfoodapp.Utils.AndroidUtils;
import com.example.shopfoodapp.Utils.FirebaseUtils;
import com.example.shopfoodapp.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private DatabaseReference database;
    UserModel currentUserModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance().getReference();

        FirebaseUtils.currentUserDetails().get().addOnCompleteListener(task -> {
            currentUserModel = task.getResult().toObject(UserModel.class);
            binding.txtUserName.setText(currentUserModel.getUsername());
        });

        FirebaseUtils.getCurrentProfilePicStorageRef().getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Uri uri  = task.getResult();
                        AndroidUtils.setProfilePic(getContext(),uri,binding.avtMain);
                    } else {
                        // Sử dụng ảnh mặc định nếu không có ảnh trong Firebase Storage
                        AndroidUtils.setProfilePic(getContext(), Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.drawable.avtpro), binding.avtMain);
                    }
                });

        initTime();
        initPrice();
        initBestFood();
        initCategory();
        initListFood();

        setupListeners();

        return binding.getRoot();
    }

    private void setupListeners() {
        binding.searchBtn.setOnClickListener(view -> {
            String text = binding.searchEdt.getText().toString();
            if (!text.isEmpty()) {
                Intent intent = new Intent(getActivity(), ListFoodsActivity.class);
                intent.putExtra("text", text);
                intent.putExtra("isSearch", true);
                startActivity(intent);
            }
        });

        binding.txtViewAll.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ListFoodsActivity.class);
            intent.putExtra("isSearch", false);
            intent.putExtra("CategoryId", -1);
            startActivity(intent);
        });

        binding.cartBtn.setOnClickListener(view -> startActivity(new Intent(getActivity(), CartActivity.class)));

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
            }
        });
    }

    private void initBestFood() {
        DatabaseReference myRef = database.child("Foods");
        binding.progressBarBestFood.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedPriceId = ((Price) binding.priceSp.getSelectedItem()).getId();
                int selectedTimeId = ((Time) binding.timeSp.getSelectedItem()).getId();

                myRef.orderByChild("BestFood").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot issue : snapshot.getChildren()) {
                                Foods food = issue.getValue(Foods.class);
                                if (food != null && food.getPriceId() == selectedPriceId && food.getTimeId() == selectedTimeId) {
                                    list.add(food);
                                }
                            }
                        }
                        updateBestFoodUI(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("HomeFragment", "Database error: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        binding.priceSp.setOnItemSelectedListener(filterListener);
        binding.timeSp.setOnItemSelectedListener(filterListener);
    }

    private void updateBestFoodUI(ArrayList<Foods> list) {
        if (list.size() > 0) {
            binding.noFoodTextView.setVisibility(View.GONE);
            binding.bestFoodView.setVisibility(View.VISIBLE);
            binding.bestFoodView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            BestFoodAdapter adapter = new BestFoodAdapter(list);
            binding.bestFoodView.setAdapter(adapter);
        } else {
            binding.noFoodTextView.setVisibility(View.VISIBLE);
            binding.bestFoodView.setVisibility(View.GONE);
        }
        binding.progressBarBestFood.setVisibility(View.GONE);
    }

    private void initCategory() {
        DatabaseReference myRef = database.child("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Category.class));
                    }
                    if (!list.isEmpty()) {
                        binding.categoryView.setLayoutManager(new GridLayoutManager(getContext(), 4));
                        CategoryAdapter adapter = new CategoryAdapter(list);
                        binding.categoryView.setAdapter(adapter);
                    }
                    binding.progressBarCategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initListFood() {
        DatabaseReference myRef = database.child("Foods");
        binding.progressListFood.setVisibility(View.VISIBLE);
        ArrayList<Foods> tempList = new ArrayList<>();
        ArrayList<Foods> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Foods food = data.getValue(Foods.class);
                    tempList.add(food);
                }

                Collections.shuffle(tempList);

                for (int i = 0; i < Math.min(30, tempList.size()); i++) {
                    list.add(tempList.get(i));
                }

                binding.progressListFood.setVisibility(View.GONE);
                binding.listFoodView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                FoodListAdapter adapter = new FoodListAdapter(list);
                binding.listFoodView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressListFood.setVisibility(View.GONE);
                Log.e("HomeFragment", error.getMessage());
            }
        });
    }

    private void initTime() {
        DatabaseReference myRef = database.child("Time");
        ArrayList<Time> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Time.class));
                    }
                    ArrayAdapter<Time> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.timeSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initPrice() {
        DatabaseReference myRef = database.child("Price");
        ArrayList<Price> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Price.class));
                    }
                    ArrayAdapter<Price> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.priceSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}