package com.example.shopfoodapp.Activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shopfoodapp.Adapter.OrderHistoryAdapter;
import com.example.shopfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private OrderHistoryAdapter adapter;
    private ArrayList<HashMap<String, Object>> orderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        // Ánh xạ RecyclerView và ProgressBar
        recyclerView = view.findViewById(R.id.orderHistoryRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderHistoryAdapter(orderList, orderId -> {
            // Xử lý callback khi xóa đơn hàng
            Toast.makeText(getContext(), "Order " + orderId + " deleted!", Toast.LENGTH_SHORT).show();
            loadOrderHistory(); // Tải lại danh sách sau khi xóa
        });
        recyclerView.setAdapter(adapter);

        // Tải dữ liệu đơn hàng
        loadOrderHistory();

        return view;
    }

    private void loadOrderHistory() {
        progressBar.setVisibility(View.VISIBLE); // Hiển thị ProgressBar

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Xóa danh sách trước khi tải mới
        orderList.clear();
        adapter.notifyDataSetChanged();

        db.collection("users").document(userId).collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            HashMap<String, Object> order = (HashMap<String, Object>) doc.getData();
                            if (order != null) {
                                order.put("orderId", doc.getId());
                                orderList.add(order);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "No orders found!", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged(); // Cập nhật Adapter
                    progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderHistory", "Error loading orders: " + e.getMessage());
                    progressBar.setVisibility(View.GONE); // Ẩn ProgressBar nếu có lỗi
                });
    }
}
