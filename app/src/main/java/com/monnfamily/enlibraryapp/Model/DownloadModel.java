package com.monnfamily.enlibraryapp.Model;

public class DownloadModel {

    private String mFileLocation;
    private String  mAssetUrl;

    public DownloadModel(String mFileLocation, String mAssetUrl) {
        this.mFileLocation = mFileLocation;
        this.mAssetUrl = mAssetUrl;
    }

    public String getmFileLocation() {
        return mFileLocation;
    }

    public String getmAssetUrl() {
        return mAssetUrl;
    }
}
