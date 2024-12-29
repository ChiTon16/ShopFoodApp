package com.example.shopfoodapp.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.shopfoodapp.Domain.Foods;

import java.util.ArrayList;


public class ManagmentCart {
    private Context context;
    private TinyDB tinyDB;

    public ManagmentCart(Context context) {
        this.context = context;
        this.tinyDB=new TinyDB(context);
    }

//    public void insertFood(Foods item) {
//        ArrayList<Foods> listpop = getListCart();
//        boolean existAlready = false;
//        int n = 0;
//        for (int i = 0; i < listpop.size(); i++) {
//            if (listpop.get(i).getTitle().equals(item.getTitle())) {
//                existAlready = true;
//                n = i;
//                break;
//            }
//        }
//        if(existAlready){
//            listpop.get(n).setNumberInCart(item.getNumberInCart());
//        }else{
//            listpop.add(item);
//        }
//        tinyDB.putListObject("CartList",listpop);
//        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show();
//    }

    public void insertFood(Foods item) {
        ArrayList<Foods> listpop = getListCart();
        boolean existAlready = false;
        int n = 0;

        for (int i = 0; i < listpop.size(); i++) {
            if (listpop.get(i).getId() == item.getId()) { // So sánh bằng ID
                existAlready = true;
                n = i;
                break;
            }
        }

        if (existAlready) {
            // Cộng thêm số lượng nếu món đã tồn tại
            int currentNumber = listpop.get(n).getNumberInCart();
            listpop.get(n).setNumberInCart(currentNumber + item.getNumberInCart());
        } else {
            // Thêm món mới
            listpop.add(item);
        }

        saveCartList(listpop); // Lưu lại giỏ hàng
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show();
    }



    public ArrayList<Foods> getListCart() {
        return tinyDB.getListObject("CartList");
    }

    public Double getTotalFee(){
        ArrayList<Foods> listItem=getListCart();
        double fee=0;
        for (int i = 0; i < listItem.size(); i++) {
            fee=fee+(listItem.get(i).getPrice()*listItem.get(i).getNumberInCart());
        }
        return fee;
    }
    public void minusNumberItem(ArrayList<Foods> listItem,int position,ChangeNumberItemsListener changeNumberItemsListener){
        if(listItem.get(position).getNumberInCart()==1){
            listItem.remove(position);
        }else{
            listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart()-1);
        }
        tinyDB.putListObject("CartList",listItem);
        changeNumberItemsListener.change();
    }
    public  void plusNumberItem(ArrayList<Foods> listItem,int position,ChangeNumberItemsListener changeNumberItemsListener){
        listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart()+1);
        tinyDB.putListObject("CartList",listItem);
        changeNumberItemsListener.change();
    }

    // Xóa toàn bộ giỏ hàng
    public void clearCart() {
        tinyDB.remove("CartList");

    }

    public void saveCartList(ArrayList<Foods> cartList) {
        tinyDB.putListObject("CartList", cartList); // Lưu danh sách giỏ hàng
    }

}
