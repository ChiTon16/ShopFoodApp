package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private DatabaseReference database;
    private UserModel currentUserModel;
    private boolean isLoading = false;
    private String lastKey = null; // Lưu khóa cuối
    private ArrayList<Foods> foodList = new ArrayList<>();
    private FoodListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance().getReference();

        // Cài đặt RecyclerView và adapter
        binding.listFoodView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FoodListAdapter(foodList);
        binding.listFoodView.setAdapter(adapter);

        // Tải dữ liệu lần đầu tiên
        loadInitialData();

        // Thiết lập lắng nghe cuộn để tải thêm dữ liệu
        setupScrollListener();

        // Cập nhật thông tin người dùng
        FirebaseUtils.currentUserDetails().get().addOnCompleteListener(task -> {
            currentUserModel = task.getResult().toObject(UserModel.class);
            binding.txtUserName.setText(currentUserModel.getUsername());
        });

        // Hiển thị ảnh đại diện
        FirebaseUtils.getCurrentProfilePicStorageRef().getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtils.setProfilePic(getContext(), uri, binding.avtMain);
                    } else {
                        // Sử dụng ảnh mặc định nếu không có ảnh trong Firebase Storage
                        AndroidUtils.setProfilePic(getContext(), Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.drawable.avtpro), binding.avtMain);
                    }
                });

        initPrice();
        initBestFood();
        initCategory();

        setupListeners();

        return binding.getRoot();
    }

    private void loadInitialData() {
        isLoading = true; // Đặt trạng thái đang tải
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Foods");
        myRef.orderByKey().limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Foods food = data.getValue(Foods.class);
                        foodList.add(food);
                        lastKey = data.getKey(); // Lưu khóa cuối cùng
                    }
                    adapter.notifyDataSetChanged();
                }
                isLoading = false; // Hoàn tất tải
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LazyLoading", "Failed to load data: " + error.getMessage());
                isLoading = false;
            }
        });
    }

    private void loadMoreData() {
        if (isLoading || lastKey == null) return; // Kiểm tra trạng thái

        isLoading = true;
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Foods");
        myRef.orderByKey().startAfter(lastKey).limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Foods food = data.getValue(Foods.class);
                        foodList.add(food);
                        lastKey = data.getKey(); // Cập nhật khóa cuối cùng
                    }
                    adapter.notifyDataSetChanged();
                }
                isLoading = false; // Hoàn tất tải
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LazyLoading", "Failed to load data: " + error.getMessage());
                isLoading = false;
            }
        });
    }

    private void setupScrollListener() {
        binding.listFoodView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() == foodList.size() - 1 && !isLoading) {
                    loadMoreData(); // Gọi để tải thêm dữ liệu
                }
            }
        });
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

        binding.logoutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        });
    }

    private void initBestFood() {
        DatabaseReference myRef = database.child("Foods");
        binding.progressBarBestFood.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        // Lắng nghe khi người dùng chọn một mức giá từ Spinner
        binding.priceSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Price selectedPrice = (Price) parent.getItemAtPosition(position); // Lấy mức giá đã chọn
                list.clear();

                myRef.orderByChild("BestFood").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot issue : snapshot.getChildren()) {
                                Foods food = issue.getValue(Foods.class);
                                if (food != null && food.getPriceId() == selectedPrice.getId()) {
                                    list.add(food); // Thêm món ăn phù hợp với mức giá
                                }
                            }
                        }
                        updateBestFoodUI(list); // Cập nhật UI
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("HomeFragment", "Database error: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì nếu không có gì được chọn
            }
        });
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
                Log.e("HomeFragment", "Failed to load Category data: " + error.getMessage());
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
                Log.e("HomeFragment", "Failed to load Price data: " + error.getMessage());
            }
        });
    }
}
