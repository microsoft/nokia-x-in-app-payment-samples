/**
 * Copyright (c) 2014 Microsoft Mobile and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.client.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an in-app billing purchase.
 */
public class Purchase {
    String mProductId;
    String mDeveloperPayload;
    String mToken;
    String mOriginalJson;

    public Purchase(String jsonPurchaseInfo) throws JSONException {
        if (jsonPurchaseInfo != null) {
            mOriginalJson = jsonPurchaseInfo;
            JSONObject o = new JSONObject(mOriginalJson);
            mProductId = o.optString("productId");
            mDeveloperPayload = o.optString("developerPayload");
            mToken = o.optString("purchaseToken");
        } else {
            mOriginalJson = "";
            mProductId = "";
            mDeveloperPayload = "";
            mToken = "";
        }
    }

    public String getProductId() {
        return mProductId;
    }

    public String getDeveloperPayload() {
        return mDeveloperPayload;
    }

    public String getToken() {
        return mToken;
    }

    public String getOriginalJson() {
        return mOriginalJson;
    }

    public void setProductId(String input) {
        mProductId = input;
    }

    public void setDeveloperPayload(String input) {
        mDeveloperPayload = input;
    }

    public void setToken(String input) {
        mToken = input;
    }

    public void setOriginalJson(String input) {
        mOriginalJson = input;
    }

    @Override
    public String toString() {
        return "PurchaseInfo:" + mOriginalJson;
    }
}
