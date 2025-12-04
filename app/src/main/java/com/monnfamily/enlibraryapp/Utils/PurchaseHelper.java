package com.monnfamily.enlibraryapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClient.ProductType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.monnfamily.enlibraryapp.Constants.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PurchaseHelper implements PurchasesUpdatedListener {

    private static final String TAG = "PurchaseHelper";
    private BillingClient mBillingClient;
    private boolean mIsServiceConnected;
    private static boolean mIsAlreaySubscribed = false;
    private HashMap<String, ProductDetails> listProductDetails = new HashMap<>();
    private PurchaseListener mListener = null;
    private Purchase mPurchasedSKU = null;
    AppManager manager = AppManager.getInstance();
    private boolean isAppFree = false;

    public PurchaseHelper(Context context) {
        mBillingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        startConnection();
    }

    private void startConnection() {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
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
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingResponseCode.OK && purchases != null) {
            Log.w("PurchaseHelper", "onPurchasesUpdated" + purchases.get(0));
            if (purchases.size() > 0 && purchases.get(0).getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                mIsAlreaySubscribed = true;
                mPurchasedSKU = purchases.get(0);
                if (mListener != null)
                    mListener.updatePurchase();
            }
        }
    }

    public void setListener(PurchaseListener listener) {
        mListener = listener;
    }

    /**
     * PUBLIC FUNCTIONS
     */

    public ProductDetails getSubscribeDetailsForID(String pProductID) {
        return listProductDetails.get(pProductID);
    }

    public boolean isServiceConnected() {
        return mIsServiceConnected;
    }

    public boolean isAlreadySubscribed() {
        return mIsAlreaySubscribed;
    }

    /**
     * Initiate the billing flow for an in-app purchase or subscription.
     */
    public void launchBillingFlow(Activity currentActivity, String productId) {
        ProductDetails productDetails = getSubscribeDetailsForID(productId);
        Log.i("IsNull", productDetails + "");
        if (productDetails != null) {
            // Get the offer token for subscription
            List<ProductDetails.SubscriptionOfferDetails> offerDetails = productDetails.getSubscriptionOfferDetails();
            if (offerDetails == null || offerDetails.isEmpty()) {
                Log.e(TAG, "No subscription offers available");
                if (mListener != null) mListener.purchaseFailure();
                return;
            }
            
            String offerToken = offerDetails.get(0).getOfferToken();
            
            BillingFlowParams.ProductDetailsParams productDetailsParams = 
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build();

            List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
            productDetailsParamsList.add(productDetailsParams);

            BillingFlowParams.Builder billingFlowParamsBuilder = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList);

            // Handle subscription upgrade from monthly to yearly
            if (mIsAlreaySubscribed && productId.equals(Constant.yearly_purchased) && mPurchasedSKU != null) {
                Log.i(TAG, "Billing Yearly - Upgrade");
                BillingFlowParams.SubscriptionUpdateParams updateParams = 
                    BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                        .setOldPurchaseToken(mPurchasedSKU.getPurchaseToken())
                        .setReplaceProrationMode(BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_PRORATED_PRICE)
                        .build();
                billingFlowParamsBuilder.setSubscriptionUpdateParams(updateParams);
            } else {
                Log.i(TAG, "Billing monthly");
            }

            BillingResult responseCode = mBillingClient.launchBillingFlow(currentActivity, billingFlowParamsBuilder.build());
            Log.i(TAG, "Billing response code = " + responseCode.getResponseCode());

            if (responseCode.getResponseCode() == BillingResponseCode.OK) {
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
     * Redirects the user to the "Manage subscription" page for your app.
     */
    public void gotoManageSubscription(Context context) {
        String PACKAGE_NAME = context.getPackageName();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?package=" + PACKAGE_NAME));
        context.startActivity(browserIntent);
    }

    /**
     * Redirects the user to the "Manage subscription" page for your app.
     */
    public void cancelSubscribed(Context context) {
        if (mPurchasedSKU == null) return;
        String PACKAGE_NAME = context.getPackageName();
        String sku = getPurchasedSkuString();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?package=" + PACKAGE_NAME + "&sku=" + sku));
        context.startActivity(browserIntent);
    }

    public void appResumed() {
        getPurchasedItems();
    }

    public Purchase getPurchasedSKU() {
        return mPurchasedSKU;
    }

    public String getPurchasedSkuString() {
        if (mPurchasedSKU != null && mPurchasedSKU.getProducts() != null && !mPurchasedSKU.getProducts().isEmpty()) {
            return mPurchasedSKU.getProducts().get(0);
        }
        return "";
    }

    /**
     * To notify that setup is complete and the billing client is ready.
     */
    private void onServiceConnected(@BillingResponseCode int resultCode) {
        if (resultCode != BillingResponseCode.OK) return;

        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(Constant.monthly_purchased)
                .setProductType(ProductType.SUBS)
                .build());
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(Constant.yearly_purchased)
                .setProductType(ProductType.SUBS)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        mBillingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetailsList) {
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    for (ProductDetails productDetails : productDetailsList) {
                        listProductDetails.put(productDetails.getProductId(), productDetails);
                    }
                }
                getPurchasedItems();
            }
        });
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
        
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(ProductType.SUBS)
                .build();

        mBillingClient.queryPurchasesAsync(params, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchasesList) {
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    if (purchasesList.size() > 0 && purchasesList.get(0).getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        mIsAlreaySubscribed = true;
                        mPurchasedSKU = purchasesList.get(0);
                    }
                    // Setting is we have any data regarding the subscription
                    manager.setIsSubscribe(purchasesList.size() > 0);
                    Log.w("PurchaseHelper", "getPurchasedItems " + purchasesList);
                }
                if (mListener != null) {
                    Log.i(TAG, "Billing122");
                    mListener.updatePurchase();
                } else {
                    Log.i(TAG, "Billing123");
                }
            }
        });
    }

    /**
     * Purchase Listener Interface.
     */
    public interface PurchaseListener {
        void purchaseSuccess();

        void purchaseFailure();

        void updatePurchase();
    }
}
