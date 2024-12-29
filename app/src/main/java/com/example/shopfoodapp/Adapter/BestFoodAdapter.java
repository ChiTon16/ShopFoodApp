package com.example.shopfoodapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.shopfoodapp.Activity.DetailActivity;
import com.example.shopfoodapp.Domain.Foods;
import com.example.shopfoodapp.Helper.ManagmentCart;
import com.example.shopfoodapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class BestFoodAdapter extends RecyclerView.Adapter<BestFoodAdapter.viewholder> {
    ArrayList<Foods> items;
    Context context;
    private Foods object;
    private int num = 1;
    private ManagmentCart managmentCart;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");


    public BestFoodAdapter(ArrayList<Foods> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public BestFoodAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_bestdeal,parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull BestFoodAdapter.viewholder holder, int position) {
        Foods currentFood = items.get(position);

        // Thiết lập dữ liệu món ăn
        holder.titleTxt.setText(currentFood.getTitle());
        holder.priceTxt.setText(decimalFormat.format(currentFood.getPrice()) + " VND");
        holder.timeTxt.setText(currentFood.getTimeValue() + "min");
        holder.starTxt.setText("" + currentFood.getStar());

        Glide.with(context)
                .load(currentFood.getImagePath())
                .transform(new CenterCrop())
                .into(holder.pic);

        // Sự kiện khi bấm nút thêm vào giỏ hàng
        holder.cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (managmentCart == null) {
                    managmentCart = new ManagmentCart(context); // Khởi tạo nếu chưa có
                }

                // Kiểm tra nếu món ăn đã tồn tại trong giỏ hàng
                ArrayList<Foods> cartList = managmentCart.getListCart();
                boolean exists = false;

                for (Foods food : cartList) {
                    if (food.getId() == currentFood.getId()) { // So sánh bằng ID
                        // Nếu món đã tồn tại, tăng số lượng
                        food.setNumberInCart(food.getNumberInCart() + 1);
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    // Nếu món chưa tồn tại, thêm mới
                    currentFood.setNumberInCart(1);
                    cartList.add(currentFood);
                }

                // Cập nhật lại giỏ hàng
                managmentCart.saveCartList(cartList);

                // Thông báo cho người dùng
                Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện khi bấm vào hình ảnh để xem chi tiết món ăn
        holder.pic.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", currentFood);
            context.startActivity(intent);
        });
    }




    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, starTxt, timeTxt, cartBtn;
        ImageView pic;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleText);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            starTxt = itemView.findViewById(R.id.starTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            cartBtn = itemView.findViewById(R.id.add_cart_Btn);
            pic = itemView.findViewById(R.id.pic);
        }
    }

}
