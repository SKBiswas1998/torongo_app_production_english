package com.monnfamily.enlibraryapp.Model;

import android.graphics.drawable.Drawable;

public class CategoryModel {
    String name;
    Drawable image;

    public CategoryModel(String name, Drawable image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public Drawable getImage() {
        return image;
    }
}
