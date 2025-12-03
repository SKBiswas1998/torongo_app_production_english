package com.monnfamily.enlibraryapp.Utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.contentful.vault.Vault;
import com.monnfamily.enlibraryapp.Contentful.Book;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

public final class App extends Application {
    /**
     * Here we are sync Vault to the CMS
     */
    private static App instance;
    private String mDirectoryPath;
    private PurchaseHelper mPurchaseHelper;
    private boolean mNeedsToCheckNetwork = false;
    public static final String TAG ="App";

//    private boolean isGettingDeviceCountry = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mDirectoryPath = String.valueOf(getApplicationContext().getDir("MonnFamily", Context.MODE_PRIVATE));
        mPurchaseHelper = new PurchaseHelper(this);
//        volleyGet();
    }

    public static App get() {
        return instance;
    }

    public Context getAppContext() {
        return getApplicationContext();
    }

    public String getMainDirectory() {
        return mDirectoryPath;
    }

    public String getDefaultIconDir() {
        return mDirectoryPath + "/DefaultIcon";
    }

    public String getCastIconDir() {
        return mDirectoryPath + "/DefaultIcon/Cast";
    }

    public String getCategoryIconDir() {
        return mDirectoryPath + "/DefaultIcon/Category";
    }

    public String getBookIconDir() {
        return mDirectoryPath + "/DefaultIcon/Book";
    }

    public String getBookDir() {
        return mDirectoryPath + "/DownloadedBooks";
    }

    public String getBookPath(String pBookId) {
        return this.getBookDir() + "/BookCompleted" + pBookId;
    }

    public PurchaseHelper getPurchaseHelper() {
        return mPurchaseHelper;
    }

    public void networkListnerCallback(boolean isConnected) {
        if (this.mNeedsToCheckNetwork && !isConnected) {
            //Show the popup.
        }
    }

    public void needsToLookNetworkChange(boolean needsToCheckNetwork) {
        this.mNeedsToCheckNetwork = needsToCheckNetwork;
    }

//    public void volleyGet() {
//        String url = "https://iplist.cc/api";
//        List<String> jsonResponses = new ArrayList<>();
//        Log.i(TAG, "volleyGet: Before Boolean");
//
//        if (isGettingDeviceCountry) return;
//        Log.i(TAG, "volleyGet: Fetching");
//        isGettingDeviceCountry = true;
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                Log.i(TAG, "onResponse: ");
//                try {
//                    isGettingDeviceCountry = false;
//                    String text = response != null ? response.getString("countrycode") : "null";
//                    AppManager.getInstance().setmDeveiceCountryCode(text);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                isGettingDeviceCountry = false;
//                error.printStackTrace();
//            }
//        });
//
//        requestQueue.add(jsonObjectRequest);
//
//    }
}
