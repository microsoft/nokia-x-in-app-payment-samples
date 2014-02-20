/**
 * Copyright (c) 2014 Nokia Corporation and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.client.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an in-app product's listing details.
 */
public class ProductDetails {
    String mProductId;
    String mType;
    String mPrice;
    String mCurrency;
    String mPriceFormatted;
    String mTitle;
    String mDescription;
    String mJson;
    String mPurchaseToken;
    boolean mRestorable;

    public ProductDetails(String jsonProductDetails) throws JSONException {
        mJson = jsonProductDetails;
        JSONObject o = new JSONObject(mJson);

        //optString returns empty string by default
        mProductId = o.optString("productId");
        mType = o.optString("type");
        mPrice = o.optString("priceValue");
        mCurrency = o.optString("currency");
        mPriceFormatted = o.optString("price");
        mTitle = o.optString("title");
        mDescription = o.optString("description");
        mPurchaseToken = o.optString("purchaseToken");
        mRestorable = false;
        if (o.optString("restorable").equalsIgnoreCase("true")) {
            mRestorable = true;
        }

    }

    public String getProductId() {
        return mProductId;
    }

    public String getType() {
        return mType;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public String getPriceFormatted() {
        return mPriceFormatted;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getPurchaseToken() {
        return mPurchaseToken;
    }

    public boolean getRestorable() {
        return mRestorable;
    }


    public void setRestore(boolean restore) {
        mRestorable = restore;
    }


    @Override
    public String toString() {
        return "Product details:" + mJson;
    }
}
