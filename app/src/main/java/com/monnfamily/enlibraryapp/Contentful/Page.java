package com.monnfamily.enlibraryapp.Contentful;


import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import com.monnfamily.enlibraryapp.Constants.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@ContentType(Constant.MODEL_BOOK_PAGE)
public class Page extends Resource {
    @Field
    Integer pageNumber;
    @Field
    Asset pageText;
    @Field
    Asset pageImage;
    @Field
    Asset pageAudio;
    @Field
    String pageAudioDuration;
    @Field
    String pageTitle;
    @Field
    Integer pageVersion;
    private List<String> mDisplayStringArray = null;

    public Integer getPageNumber() {
        return pageNumber;
    }

    public String getPageText() {
        return pageText.url();
    }

    public String getPageImage() {
        return pageImage.url();
    }

    public String getPageAudio() {
        return pageAudio.url();
    }

    public String getPageAudioDuration() {
        return pageAudioDuration;
    }

    public Integer getPageVersion() {
        return pageVersion;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    /*
     * Public Function.
     */
    public JSONArray getPageAudioDurationJSON() {

        JSONArray jsonArr = null;
        try {

            if (getPageAudioDuration() != null) {

                String jsonString = getPageAudioDuration();
                if (jsonString.contains("values")) {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    jsonArr = jsonObject.getJSONArray("values");
                } else {
                    jsonArr = new JSONArray(jsonString);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonArr;

    }

    public List<String> getDisplayStringArray() {
        return mDisplayStringArray;
    }

    public void setDisplayStringArray(List<String> pDisplayStringArray) {
        this.mDisplayStringArray = pDisplayStringArray;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPageAudioDuration(String pageAudioDuration) {
        this.pageAudioDuration = pageAudioDuration;
    }


}
