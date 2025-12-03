package com.monnfamily.enlibraryapp.Model;

import android.graphics.drawable.Drawable;

public class DummyCategoryModel {
    private int mCastID;
    private Drawable mCastImage;

    public DummyCategoryModel(int mCastID, Drawable mCastImage) {
        this.mCastID = mCastID;
        this.mCastImage = mCastImage;
    }

    public int getmCastID() {
        return mCastID;
    }

    public Drawable getmCastImage() {
        return mCastImage;
    }
}
