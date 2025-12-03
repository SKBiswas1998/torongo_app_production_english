package com.monnfamily.enlibraryapp.Contentful;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import com.monnfamily.enlibraryapp.Constants.Constant;

import java.util.Date;
import java.util.List;

@ContentType(Constant.MODEL_BOOK)
public class Book extends Resource{
    @Field  Integer         bookId;
    @Field  Integer         version;
    @Field  String          bookName;
    @Field  Asset           bookCover;
    @Field  Asset           bookMainImage;
    @Field  Asset           bookSound;
    @Field  List<Page>      bookDetail;
    @Field  List<Cast>      bookCast;
    @Field  List<Category>  bookCategory;
    @Field  Boolean         isBookForFree;


    public Integer getBookId() {
        return bookId;
    }

    public Integer getVersion() {
        return version;
    }

    public String getBookName() {
        return bookName;
    }

    public String getBookCoverURL() { return bookCover.url(); }

    public String getBookMainImageURL() {
        return bookMainImage.url();
    }

    public String getBookSoundURL() {
        if(bookSound == null) return null;
        return bookSound.url();
    }

    public Boolean getBookForFree() {
        return isBookForFree;
    }
    public List<Page> getAllPage() {
        return bookDetail;
    }

    public List<Cast> getBookCast() {
        return bookCast;
    }

    public List<Category> getBookCategory() {
        return bookCategory;
    }

    public boolean containsCategory(Integer categoryID){
        for (Category category: bookCategory) {
            if(category.getCategoryId().equals(categoryID)) return true;
        }
        return  false;
    }

    public boolean containsCast(Integer castID){
        for (Cast cast: bookCast) {
            if(cast.getCastId().equals(castID)) return true;
        }
        return  false;
    }

    public Page getPageDetailsForNumber(Integer pPageNumber){
        for (Page singlePage: bookDetail) {
            if (singlePage.getPageNumber() == pPageNumber)
                return singlePage;
        }
        return null;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public void setBookDetail(List<Page> bookDetail) {
        this.bookDetail = bookDetail;
    }

    public void setBookForFree(Boolean bookForFree) {
        isBookForFree = bookForFree;
    }
}


