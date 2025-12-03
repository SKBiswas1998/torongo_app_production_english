package com.monnfamily.enlibraryapp.Contentful;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import com.monnfamily.enlibraryapp.Constants.Constant;

@ContentType(Constant.MODEL_BOOK_CAST)
public class Cast extends Resource {
    @Field  Integer castId;
    @Field  Integer version;
    @Field  String  castName;
    @Field  Asset   castImage;
    @Field  String  bangalaCastName;

    int mPositionIndex;

    public int getPositionIndex() {
        return mPositionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.mPositionIndex = positionIndex;
    }
    public Integer getCastId() {
        return castId;
    }

    public Integer getVersion() {
        return version;
    }

    public String getCastName() {
        return castName;
    }

    public String getCastImageURL() { return castImage.url(); }

    public String getBanglaCastName() {
        return bangalaCastName;
    }
}
