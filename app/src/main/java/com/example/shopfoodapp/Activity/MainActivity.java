    package com.example.shopfoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopfoodapp.Adapter.BestFoodAdapter;
import com.example.shopfoodapp.Adapter.CategoryAdapter;
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

    public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);
        setContentView(binding.getRoot());

        initLocation();
        initTime();
        initPrice();
        initBestFood();
        initCategory();
        setVariable();
    }

        private void setVariable() {
            FirebaseUser user = mAuth.getCurrentUser();
            String userId = user.getUid();  // Lấy userId từ FirebaseAuth

            // Lấy dữ liệu username từ Firestore
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            binding.txtUserName.setText(username);  // Hiển thị username lên TextView
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi nếu lấy dữ liệu thất bại
                        binding.txtUserName.setText("Error");
                    });


            binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });

            binding.searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = binding.searchEdt.getText().toString();
                    if (!text.isEmpty()) {
                        Intent intent = new Intent(MainActivity.this, ListFoodsActivity.class);
                        intent.putExtra("text", text);
                        intent.putExtra("isSearch", true);
                        startActivity(intent);
                    }
                }
            });

            binding.cartBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CartActivity.class)));
        }

        private void initBestFood() {
            DatabaseReference myRef = database.getReference("Foods");
            binding.progressBarBestFood.setVisibility(View.VISIBLE);
            ArrayList<Foods> list = new ArrayList<>();

            binding.priceSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                    // Lấy `priceId` từ Spinner
                    Price selectedPrice = (Price) parentView.getItemAtPosition(position);
                    int selectedPriceId = selectedPrice.getId(); // `getId()` là phương thức trong lớp Price để lấy `priceId`

                    // Truy vấn Firebase dựa trên `BestFood` và `priceId`
                    Query query = myRef.orderByChild("BestFood").equalTo(true); // Lọc các món ăn là BestFood
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            list.clear(); // Xóa dữ liệu cũ trước khi thêm món ăn mới
                            if (snapshot.exists()) {
                                for (DataSnapshot issue : snapshot.getChildren()) {
                                    Foods food = issue.getValue(Foods.class);

                                    // Lọc món ăn theo `priceId`
                                    if (food != null && food.getPriceId() == selectedPriceId) {
                                        list.add(food); // Thêm món ăn vào danh sách nếu `priceId` khớp
                                    }
                                }

                                // Cập nhật RecyclerView với danh sách món ăn đã lọc
                                if (list.size() > 0) {
                                    // Ẩn TextView thông báo "Không có món ăn"
                                    binding.noFoodTextView.setVisibility(View.GONE);
                                    binding.bestFoodView.setVisibility(View.VISIBLE);
                                    binding.bestFoodView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                    BestFoodAdapter adapter = new BestFoodAdapter(list);
                                    binding.bestFoodView.setAdapter(adapter);
                                } else {
                                    binding.noFoodTextView.setVisibility(View.VISIBLE);
                                    binding.bestFoodView.setVisibility(View.GONE);
                                }
                            }
                            binding.progressBarBestFood.setVisibility(View.GONE); // Ẩn progress bar khi đã tải xong
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("MainActivity", "Database error: " + error.getMessage());
                        }
                    });

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

//            Query query = myRef.orderByChild("BestFood").equalTo(true);
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        for (DataSnapshot issue: snapshot.getChildren()) {
//                            list.add(issue.getValue(Foods.class));
//                        }
//                        if (list.size()>0){
//                            binding.bestFoodView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
//                            RecyclerView.Adapter adapter = new BestFoodAdapter(list);
//                            binding.bestFoodView.setAdapter(adapter);
//                        }
//                        binding.progressBarBestFood.setVisibility(View.GONE);
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
        }

        private void initCategory() {
            DatabaseReference myRef = database.getReference("Category");
            binding.progressBarCategory.setVisibility(View.VISIBLE);
            ArrayList<Category> list = new ArrayList<>();

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot issue: snapshot.getChildren()) {
                            list.add(issue.getValue(Category.class));
                        }
                        if (list.size()>0){
                            binding.categoryView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4));
                            RecyclerView.Adapter adapter = new CategoryAdapter(list);
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

        private void initLocation() {
            DatabaseReference myRef = database.getReference("Location");
            ArrayList<Location> list = new ArrayList<>();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot issue: snapshot.getChildren()) {
                            list.add(issue.getValue(Location.class));
                        }
                        ArrayAdapter<Location> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.locationSp.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void initTime() {
            DatabaseReference myRef = database.getReference("Time");
            ArrayList<Time> list = new ArrayList<>();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot issue: snapshot.getChildren()) {
                            list.add(issue.getValue(Time.class));
                        }
                        ArrayAdapter<Time> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
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
            DatabaseReference myRef = database.getReference("Price");
            ArrayList<Price> list = new ArrayList<>();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot issue: snapshot.getChildren()) {
                            list.add(issue.getValue(Price.class));
                        }
                        ArrayAdapter<Price> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
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