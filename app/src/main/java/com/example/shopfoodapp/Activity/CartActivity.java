package com.example.shopfoodapp.Activity;

import android.content.Intent;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class CartActivity extends BaseActivity {
    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private double tax;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");

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
        double delivery = managmentCart.getTotalFee()*0.1; // 10%
        delivery = Math.round(delivery * 100.0) / 100.0;

        tax = Math.round(managmentCart.getTotalFee()*percentTax* 100.0) / 100;

        double total = Math.round((managmentCart.getTotalFee() + tax + delivery)* 100)/100;
        double itemTotal = Math.round(managmentCart.getTotalFee() * 100) / 100;

        binding.totalFeeTxt.setText(decimalFormat.format( itemTotal  )+ " VND");
        binding.taxTxt.setText(decimalFormat.format( tax  )+ " VND");
        binding.deliveryTxt.setText(decimalFormat.format( delivery  )+ " VND");
        binding.totalTxt.setText(decimalFormat.format( total  )+ " VND");
    }

    private void placeOrder() {
        Intent intent = new Intent(this, SelectLocationActivity.class);
        startActivityForResult(intent, 1); // Request code 1
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);

            // Xử lý vị trí được chọn
            String locationMessage = "Selected location: (" + latitude + ", " + longitude + ")";
            Toast.makeText(this, locationMessage, Toast.LENGTH_SHORT).show();

            // Thêm vị trí vào đơn hàng
            uploadOrderWithLocation(latitude, longitude);
        }
    }

    private void uploadOrderWithLocation(double latitude, double longitude) {
        // Khởi tạo Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid(); // Lấy user ID

        // Tạo danh sách các mặt hàng trong giỏ hàng
        ArrayList<HashMap<String, Object>> items = new ArrayList<>();
        for (Foods food : managmentCart.getListCart()) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("name", food.getTitle());
            item.put("quantity", food.getNumberInCart());
            item.put("price", food.getPrice());
            item.put("imageUrl", food.getImagePath()); // Lấy URL ảnh từ đối tượng Foods
            items.add(item);
        }

        // Tạo dữ liệu đơn hàng
        HashMap<String, Object> order = new HashMap<>();
        order.put("items", items);                        // Danh sách sản phẩm
        order.put("subtotal", managmentCart.getTotalFee()); // Tổng tiền sản phẩm
        order.put("delivery", Math.round(managmentCart.getTotalFee() *0.1 * 100.0) / 100.0);                      // Phí giao hàng (cố định 10)
        order.put("tax", tax);                            // Thuế
        order.put("total", managmentCart.getTotalFee() + managmentCart.getTotalFee() * 0.1); // Tổng tiền
        order.put("status", "Pending");                   // Trạng thái đơn hàng
        order.put("timestamp", System.currentTimeMillis()); // Thời gian
        order.put("location", new HashMap<String, Double>() {{ // Thêm vị trí
            put("latitude", latitude);
            put("longitude", longitude);
        }});

        // Lưu đơn hàng vào subcollection "orders" của người dùng
        db.collection("users").document(userId).collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    // Làm sạch giỏ hàng sau khi đặt hàng thành công
                    managmentCart.clearCart();
                    initList(); // Cập nhật lại danh sách giỏ hàng để hiển thị trạng thái trống
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn hình trước
                })
                .addOnFailureListener(e -> {
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