package com.example.shopfoodapp.Domain;

public class Price {
    private int Id;
    private String value;

    public Price() {

    }

    @Override
    public String toString() {
        return value;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
