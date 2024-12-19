package com.example.shopfoodapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopfoodapp.Adapter.FoodListAdapter;
import com.example.shopfoodapp.Domain.Foods;
import com.example.shopfoodapp.R;
import com.example.shopfoodapp.databinding.ActivityListFoodsBinding;
import com.example.shopfoodapp.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListFoodsActivity extends BaseActivity {
    ActivityListFoodsBinding binding;
    private RecyclerView.Adapter adapterListFood;
    private int categoryId;
    private String categoryName;
    private String searchText;
    private boolean isSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoodsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initList();
        setVariable();
    }

    private void setVariable() {

    }

    public static String normalizeString(String input) {
        if (input == null) return null;
        // Chuẩn hóa chuỗi để loại bỏ dấu
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", ""); // Loại bỏ dấu
        return normalized.toLowerCase(); // Chuyển về chữ thường
    }

    private void initList() {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Foods food = issue.getValue(Foods.class);

                        if (food != null) {
                            if (isSearch) {
                                // Chuẩn hóa cả tiêu đề và từ khóa tìm kiếm
                                String normalizedTitle = normalizeString(food.getTitle());
                                String normalizedSearchKey = normalizeString(searchText);

                                if (normalizedTitle.contains(normalizedSearchKey)) {
                                    list.add(food);
                                }
                            } else if(categoryId == -1) {
                                list.add(food);
                            }  else if (food.getCategoryId() == categoryId) {
                                // Nếu không phải tìm kiếm, lọc theo danh mục
                                list.add(food);
                            }
                        }
                    }
                }

                if (list.size() > 0) {
                    binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodsActivity.this, 2));
                    adapterListFood = new FoodListAdapter(list);
                    binding.foodListView.setAdapter(adapterListFood);
                }
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ListFoodsActivity", "Database error: " + error.getMessage());
            }
        });

//        Query query;
//        if (isSearch){
//            query = myRef.orderByChild("Title").startAt(searchText).endAt(searchText+ '\uf8ff');
//        } else if (categoryId == -1) {
//            // Lấy tất cả các món ăn nếu CategoryId = -1
//            query = myRef;
//        } else {
//            query = myRef.orderByChild("CategoryId").equalTo(categoryId);
//        }
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot issue : snapshot.getChildren()) {
//                        list.add(issue.getValue(Foods.class));
//                    }
//                }
//                if (list.size() > 0) {
//                    binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodsActivity.this, 2));
//                    adapterListFood = new FoodListAdapter(list);
//                    binding.foodListView.setAdapter(adapterListFood);
//                }
//                binding.progressBar.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("CategoryId", 0);
        categoryName = getIntent().getStringExtra("CategoryName");
        searchText = getIntent().getStringExtra("text");
        isSearch = getIntent().getBooleanExtra("isSearch", false);

        binding.titleTxt.setText(categoryName);
        binding.backBtn.setOnClickListener(view -> finish());
    }
}