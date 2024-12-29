package com.example.shopfoodapp.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.shopfoodapp.Domain.Foods;
import com.example.shopfoodapp.Helper.ManagmentCart;
import com.example.shopfoodapp.R;
import com.example.shopfoodapp.databinding.ActivityDetailBinding;

import java.text.DecimalFormat;

public class DetailActivity extends BaseActivity {
    ActivityDetailBinding binding;
    private Foods object;
    private int num = 1;
    private ManagmentCart managmentCart;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));

        getIntentExtra();
        setVariable();
    }

    private void setVariable() {
        managmentCart = new ManagmentCart(this);

        binding.backBtn.setOnClickListener(view -> finish());

        Glide.with(DetailActivity.this)
                .load(object.getImagePath())
                .into(binding.pic);

        binding.priceTxt.setText(decimalFormat.format( object.getPrice()  )+ " VND");
        binding.titleTxt.setText(object.getTitle());
        binding.descriptionTxt.setText(object.getDescription());
        binding.rateTxt.setText(object.getStar() + "rating");
        binding.ratingBar.setRating((float) object.getStar());
        binding.totalTxt.setText(decimalFormat.format( num * object.getPrice()  )+ " VND");

        binding.plusBtn.setOnClickListener(view -> {
            num = num + 1;
            binding.numTxt.setText(num + " ");
            binding.totalTxt.setText(decimalFormat.format( num * object.getPrice()  )+ " VND");
        });

        binding.minusBtn.setOnClickListener(view -> {
            if (num > 1) {
                num = num - 1;
                binding.numTxt.setText(num + " ");
                binding.totalTxt.setText(decimalFormat.format( num * object.getPrice()  )+ " VND");
            }
        });

        binding.addBtn.setOnClickListener(view -> {
            object.setNumberInCart(num);
            managmentCart.insertFood(object);
        });
    }

    private void getIntentExtra() {
        object = (Foods) getIntent().getSerializableExtra("object");
    }
}