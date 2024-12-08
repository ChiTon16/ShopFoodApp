package com.example.shopfoodapp.Activity;

import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopfoodapp.Adapter.CartAdapter;
import com.example.shopfoodapp.Domain.Foods;
import com.example.shopfoodapp.Helper.ChangeNumberItemsListener;
import com.example.shopfoodapp.Helper.ManagmentCart;
import com.example.shopfoodapp.R;
import com.example.shopfoodapp.databinding.ActivityCartBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class CartActivity extends BaseActivity {
    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private double tax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);


        setVariable();
        caculateCart();
        initList();
    }

    private void initList() {
        if (managmentCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollViewCart.setVisibility(View.GONE);
        }
        else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollViewCart.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.cartView.setLayoutManager(linearLayoutManager);
        adapter = new CartAdapter(managmentCart.getListCart(), this, () -> caculateCart());
        binding.cartView.setAdapter(adapter);
    }

    private void caculateCart() {
        double percentTax = 0.02; //2% tax
        double delivery = 10; // 10 Dollar

        tax = Math.round(managmentCart.getTotalFee()*percentTax* 100.0) / 100;

        double total = Math.round((managmentCart.getTotalFee() + tax + delivery)* 100)/100;
        double itemTotal = Math.round(managmentCart.getTotalFee() * 100) / 100;

        binding.totalFeeTxt.setText("$" + itemTotal);
        binding.taxTxt.setText("$" + tax);
        binding.deliveryTxt.setText("$" + delivery);
        binding.totalTxt.setText("$" + total);
    }

    private void placeOrder() {
        // Khởi tạo Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Tạo danh sách các mặt hàng trong giỏ hàng
        ArrayList<HashMap<String, Object>> items = new ArrayList<>();
        for (Foods food : managmentCart.getListCart()) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("name", food.getTitle());
            item.put("quantity", food.getNumberInCart());
            item.put("price", food.getPrice());
            items.add(item);
        }

        // Tạo dữ liệu đơn hàng
        HashMap<String, Object> order = new HashMap<>();
        order.put("items", items);
        order.put("subtotal", managmentCart.getTotalFee());
        order.put("delivery", 10.0); // Delivery Fee
        order.put("tax", tax);       // Tax
        order.put("total", managmentCart.getTotalFee() + 10 + tax);
        order.put("status", "Pending");
        order.put("timestamp", System.currentTimeMillis());

        // Gửi đơn hàng lên Firebase
        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    Log.d("PlaceOrder", "Order placed successfully.");
                    // Hiển thị thông báo thành công
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    // Điều hướng hoặc reset giao diện
                    finish(); // Quay lại màn hình trước
                })
                .addOnFailureListener(e -> {
                    Log.e("PlaceOrder", "Failed to place order.", e);
                    // Hiển thị thông báo lỗi
                    Toast.makeText(this, "Failed to place order. Try again!", Toast.LENGTH_SHORT).show();
                });
    }


    private void setVariable() {
        binding.backBtn.setOnClickListener(view -> {
            finish();
        });
        binding.btnOrder.setOnClickListener(view -> {
            placeOrder();
        });
    }
}