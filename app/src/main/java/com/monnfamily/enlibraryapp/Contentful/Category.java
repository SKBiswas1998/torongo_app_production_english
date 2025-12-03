package com.monnfamily.enlibraryapp.Contentful;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import com.monnfamily.enlibraryapp.Constants.Constant;

@ContentType(Constant.MODEL_BOOK_CATEGORY)
public class Category extends Resource {
    @Field  Integer categoryId;
    @Field  Integer version;
    @Field  String  categoryName;
    @Field  Asset   categoryImage;
    @Field  Asset   banglaCategoryImage;
    @Field  String  banglaCategoryText;
    int mPositionIndex;

    public int getPositionIndex() {
        return mPositionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.mPositionIndex = positionIndex;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public Integer getVersion() {
        return version;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryImageURL() {
        return categoryImage.url();
    }

    public String getBanglaCategoryName() {
        return banglaCategoryText;
    }

    public String getBanglaCategoryImageURL() {
        return banglaCategoryImage.url();
    }

}