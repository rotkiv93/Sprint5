package com.example.android.fragments;

public class Element {

    int id;
    String element_name;
    int foreign_id;

    // constructors
    public Element() {
    }

    public Element(String el_name) {
        this.element_name = el_name;
    }

    // setters
    public void setId(int id) {
        this.id = id;
    }


    public void setElement_name(String element_name) {
        this.element_name = element_name;
    }

    public void setForeign_id(int foreign_id) {
        this.foreign_id = foreign_id;
    }

    // getters
    public long getId() {
        return this.id;
    }

    public String getElement_name() {
        return element_name;
    }

    public int getForeign_id() {
        return foreign_id;
    }

}
