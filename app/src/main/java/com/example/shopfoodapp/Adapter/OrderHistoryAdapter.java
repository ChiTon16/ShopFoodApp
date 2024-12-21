package com.example.shopfoodapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final ArrayList<HashMap<String, Object>> orderList;
    private final OnOrderDeletedListener onOrderDeletedListener;

    public OrderHistoryAdapter(ArrayList<HashMap<String, Object>> orderList, OnOrderDeletedListener onOrderDeletedListener) {
        this.orderList = orderList;
        this.onOrderDeletedListener = onOrderDeletedListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_order_history_adapter, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        HashMap<String, Object> order = orderList.get(position);

        holder.orderIdTextView.setText("Order ID: " + order.get("orderId"));
        holder.orderTotalTextView.setText("Total: $" + order.get("total"));
        holder.orderStatusTextView.setText("Status: " + order.get("status"));

        ArrayList<HashMap<String, Object>> items = (ArrayList<HashMap<String, Object>>) order.get("items");
        if (items != null && !items.isEmpty()) {
            String imageUrl = (String) items.get(0).get("imageUrl");
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.food_icon)
                    .error(R.drawable.img_error)
                    .into(holder.orderImageView);
        } else {
            holder.orderImageView.setImageResource(R.drawable.food_icon);
        }

        holder.deleteOrderButton.setOnClickListener(v -> {
            String orderId = (String) order.get("orderId");

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("orders")
                    .document(orderId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        orderList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, orderList.size());

                        if (onOrderDeletedListener != null) {
                            onOrderDeletedListener.onOrderDeleted(orderId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "Failed to delete order!", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, orderTotalTextView, orderStatusTextView;
        ImageView orderImageView;
        Button deleteOrderButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            orderTotalTextView = itemView.findViewById(R.id.orderTotalTextView);
            orderStatusTextView = itemView.findViewById(R.id.orderStatusTextView);
            orderImageView = itemView.findViewById(R.id.imgFood);
            deleteOrderButton = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface OnOrderDeletedListener {
        void onOrderDeleted(String orderId);
    }
}
