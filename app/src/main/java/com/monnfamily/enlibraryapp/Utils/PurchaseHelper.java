package com.monnfamily.enlibraryapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchaseState;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.monnfamily.enlibraryapp.Constants.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static com.android.billingclient.api.BillingFlowParams.ProrationMode;

public class PurchaseHelper implements PurchasesUpdatedListener, SkuDetailsResponseListener {

    private static final String TAG = "PurchaseHelper";
    private BillingClient mBillingClient;
    private boolean mIsServiceConnected;
    private static boolean mIsAlreaySubscribed = false;
    private HashMap<String, SkuDetails> listSkuDetails = new HashMap<>();
    private PurchaseListener mListener = null;
    private Purchase mPurchasedSKU = null;
    AppManager manager = AppManager.getInstance();
    private boolean isAppFree = false;

    public PurchaseHelper(Context context) {
        mBillingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
        startConnection();
    }

    private void startConnection() {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    mIsServiceConnected = true;
                    onServiceConnected(billingResult.getResponseCode());
                } else {
                    mIsServiceConnected = false;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                mIsServiceConnected = false;
            }
        });
    }


    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingResponseCode.OK && purchases != null) {
            Log.w("PurchaseHelper", "onPurchasesUpdated" + purchases.get(0));
            if (purchases.size() > 0 && purchases.get(0).getPurchaseState() == PurchaseState.PURCHASED) {
                mIsAlreaySubscribed = true;
                mPurchasedSKU = purchases.get(0);
                if (mListener != null)
                    mListener.updatePurchase();
            }
        }
    }

    @Override
    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
        if (billingResult.getResponseCode() == BillingResponseCode.OK && skuDetailsList != null) {
            for (SkuDetails skuDetails : skuDetailsList) {
                listSkuDetails.put(skuDetails.getSku(), skuDetails);
            }
        }
        getPurchasedItems();
    }

    public void setListener(PurchaseListener listener) {
        mListener = listener;
    }

    /**
     * PUBLIC FUNCTIONS
     */


    public SkuDetails getSubscribeDetailsForID(String pProductID) {
        return listSkuDetails.get(pProductID);
    }

    public boolean isServiceConnected() {
        return mIsServiceConnected;
    }

    public boolean isAlreadySubscribed() {
        return mIsAlreaySubscribed;
    }

    /**
     * Initiate the billing flow for an in-app purchase or subscription.
     *
     * @param currentActivity current activity
     * @param productId       Specify the SKU that is being purchased to as published in the Google
     *                        Developer console.
     */
    public void launchBillingFlow(Activity currentActivity, String productId) {
        Log.i("IsNull", getSubscribeDetailsForID(productId) + "");
        if (getSubscribeDetailsForID(productId) != null) {
            BillingFlowParams mBillingFlowParams;
            if (mIsAlreaySubscribed && productId.equals(Constant.yearly_purchased)) {
                Log.i(TAG, "Billing Yearly");
                mBillingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(this.getSubscribeDetailsForID(productId))
                        .setOldSku(Constant.monthly_purchased)
                        .setReplaceSkusProrationMode(ProrationMode.IMMEDIATE_AND_CHARGE_PRORATED_PRICE)
                        .build();
            } else {
                Log.i(TAG, "Billing monthly");
                mBillingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(getSubscribeDetailsForID(productId))
                        .build();
            }

            BillingResult responseCode = mBillingClient.launchBillingFlow(currentActivity, mBillingFlowParams);
            Log.i(TAG, "Billing monthly->" + mBillingFlowParams.getAccountId() +
                    ", responseCode.getResponseCode() = " + responseCode.getResponseCode() +
                    ", mBillingFlowParams.getAccountId() = " + mBillingFlowParams.getAccountId());

            if (responseCode.getResponseCode() == BillingResponseCode.OK && mBillingFlowParams.getAccountId() != null) {
                mIsAlreaySubscribed = true;
                if (mListener != null)
                    mListener.purchaseSuccess();
            } else {
                if (mListener != null)
                    mListener.purchaseFailure();
            }
        }
    }

    /**
     * Call this method once you are done with this BillingClient reference.
     */
    public void endConnection() {
        if (mBillingClient != null && mBillingClient.isReady()) {
            mBillingClient.endConnection();
            mBillingClient = null;
        }
    }

    /**
     * Redirects the user to the “Manage subscription” page for your app.
     */
    public void gotoManageSubscription(Context context) {
        String PACKAGE_NAME = context.getPackageName();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?package=" + PACKAGE_NAME));
        context.startActivity(browserIntent);
    }

    /**
     * Redirects the user to the “Manage subscription” page for your app.
     */
    public void cancelSubscribed(Context context) {
        if (mPurchasedSKU == null) return;
        String PACKAGE_NAME = context.getPackageName();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?package=" + PACKAGE_NAME + "&sku=" + mPurchasedSKU.getSku()));
        context.startActivity(browserIntent);
    }


    public void appResumed() {
        getPurchasedItems();
    }

    public Purchase getPurchasedSKU() {
        return mPurchasedSKU;
    }

    /**
     * To notify that setup is complete and the billing client is ready.
     */
    private void onServiceConnected(@BillingResponseCode int resultCode) {
        if (resultCode != BillingResponseCode.OK) return;

        List<String> skuList = new ArrayList<>();
        skuList.add(Constant.monthly_purchased);
        skuList.add(Constant.yearly_purchased);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(SkuType.SUBS);
        mBillingClient.querySkuDetailsAsync(params.build(), this);
    }

    /**
     * Check if the subscription feature is supported by the Play Store.
     */
    private boolean isSubscriptionSupported() {
        BillingResult responseCode = mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
        if (responseCode.getResponseCode() != BillingResponseCode.OK)
            Log.w("PurchaseHelper", "isSubscriptionSupported() got an error response: " + responseCode);
        return responseCode.getResponseCode() == BillingResponseCode.OK;
    }

    /**
     * Get purchases details for all the items bought within your app.
     */

    private void getPurchasedItems() {
        mIsAlreaySubscribed = false;
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(SkuType.SUBS);
        if (purchasesResult.getResponseCode() == BillingResponseCode.OK) {
            if (purchasesResult.getPurchasesList().size() > 0 && purchasesResult.getPurchasesList().get(0).getPurchaseState() == PurchaseState.PURCHASED) {
                mIsAlreaySubscribed = true;
                mPurchasedSKU = purchasesResult.getPurchasesList().get(0);
            }
            // Setting is we have any data regarding the subscription
            manager.setIsSubscribe(purchasesResult.getPurchasesList().size() > 0);
            Log.w("PurchaseHelper", "getPurchasedItems " + purchasesResult.getPurchasesList());
        }
        if (mListener != null) {
            Log.i(TAG, "Billing122");
            mListener.updatePurchase();
        } else {
            Log.i(TAG, "Billing123");

        }
    }

    /**
     * Function that allow to get whether the app is free or not
     *
     * @return if "true" Don't show 3 popup after three while book reading and make Subscription
     * page reflect text "SUBSCRIPTION IS FREE FOR YOUR AREA. PLEASE ENJOY OUR APP! PLEASE TELL OTHERS ABOUT IT!!"
     */
//    public boolean getAppFreeStatus() {
//        //Check for Purchase
//        //Getting purchase data and checking the cost of the data
//        isAppFree = false;
//        try {
//            /**
//             * Code for free app as per Monthly Subscription amount
//             */
////            float month = (float) getSubscribeDetailsForID(Constant.monthly_purchased).getPriceAmountMicros() / 1000000;
////            isAppFree = month <= 0.0f;
//            /**
//             * Code for free app as per country code
//             * Note: Please remove temporary Country code INR and USA if not in requirement
//             */
//            // Add country code here (i.e. abc,def,geh) you want to free the app
//            String country = getSubscribeDetailsForID(Constant.monthly_purchased).getPrice();
//            SkuDetails details= getSubscribeDetailsForID(Constant.yearly_purchased);
//            String locale = App.get().getResources().getConfiguration().locale.getDisplayCountry();
//
//            isAppFree = manager.isAppFree().contains(manager.getmDeveiceCountryCode());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//       return isAppFree;
//    }

    /**
     * re
     * Purchase Listener Interface.
     */
    public interface PurchaseListener {
        void purchaseSuccess();

        void purchaseFailure();

        void updatePurchase();
    }


}
