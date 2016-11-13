package com.example.android.fragments;

public class Category {

    int id;
    String category_name;

    // constructors
    public Category() {

    }

    public Category(String cat_name) {
        this.category_name = cat_name;
    }

    public Category(int id, String cat_name) {
        this.id = id;
        this.category_name = cat_name;
    }

    // setter
    public void setId(int id) {
        this.id = id;
    }

    public void setCatName(String cag_name) {
        this.category_name = cag_name;
    }

    // getter
    public int getId() {
        return this.id;
    }

    public String getCatName() {
        return this.category_name;
    }
}
