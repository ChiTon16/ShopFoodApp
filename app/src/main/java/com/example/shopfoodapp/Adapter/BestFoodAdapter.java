package com.example.shopfoodapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.shopfoodapp.Activity.DetailActivity;
import com.example.shopfoodapp.Domain.Foods;
import com.example.shopfoodapp.Helper.ManagmentCart;
import com.example.shopfoodapp.R;

import java.util.ArrayList;

public class BestFoodAdapter extends RecyclerView.Adapter<BestFoodAdapter.viewholder> {
    ArrayList<Foods> items;
    Context context;
    private Foods object;
    private int num = 1;
    private ManagmentCart managmentCart;

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
        holder.titleTxt.setText(items.get(position).getTitle());
        holder.priceTxt.setText("$"+items.get(position).getPrice());
        holder.timeTxt.setText(items.get(position).getTimeValue() + "min");
        holder.starTxt.setText("" + items.get(position).getStar());

        Glide.with(context)
                .load(items.get(position).getImagePath())
                .transform(new CenterCrop())
                .into(holder.pic);

        holder.cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Foods object = items.get(position); // Gán object từ danh sách items
                object.setNumberInCart(object.getNumberInCart() + num);

                if (managmentCart == null) {
                    managmentCart = new ManagmentCart(context); // Khởi tạo nếu chưa có
                }
                object.setNumberInCart(object.getNumberInCart() + num);
                managmentCart.insertFood(object);
            }
        });

        holder.pic.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", items.get(position));
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
