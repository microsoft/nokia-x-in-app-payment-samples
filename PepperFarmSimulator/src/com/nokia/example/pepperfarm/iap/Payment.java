/**
 * Copyright (c) 2014 Microsoft Mobile and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.iap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import com.nokia.example.pepperfarm.client.MainScreenPepperListFragment;
import com.nokia.example.pepperfarm.client.ProductListFragment;
import com.nokia.example.pepperfarm.client.util.ProductDetails;
import com.nokia.example.pepperfarm.client.util.Purchase;
import com.nokia.example.pepperfarm.product.Content;
import com.nokia.example.pepperfarm.product.Content.Product;
import com.nokia.payment.iap.aidl.INokiaIAPService;
import org.json.JSONException;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Payment implements ServiceConnection {

    public static final int API_VERSION = 3;
    public Activity activity;
    public INokiaIAPService npay;
    public boolean npayAvailable = false;
    public static boolean purchases_asked = false;

    public static final String SHARED_PREFERENCE_KEY = "IAPDATA";
    public static final String PRODUCT_ID_KEY = "PRODUCTKEY";
    public static final String KEY_NOT_IN_PROGRESS = "NOTINPROGRESS";
    public static final String PREFLOAD_FAILED = "FAIL";


    public static final int RESULT_OK = 0;// - success
    public static final int RESULT_USER_CANCELED = 1;// - user pressed back or canceled a dialog
    public static final int RESULT_BILLING_UNAVAILABLE = 3;// - this billing API version is not supported for the type requested or billing is otherwise impossible (for example there is no SIM card inserted)
    public static final int RESULT_ITEM_UNAVAILABLE = 4;// - requested ProductID is not available for purchase
    public static final int RESULT_DEVELOPER_ERROR = 5;// - invalid arguments provided to the API
    public static final int RESULT_ERROR = 6;// - Fatal error during the API action
    public static final int RESULT_ITEM_ALREADY_OWNED = 7;// - Failure to purchase since item is already owned
    public static final int RESULT_ITEM_NOT_OWNED = 8;// - Failure to consume since item is not owned
    public static final int RESULT_NO_SIM_CARD = 9;

    public static final int RESULT_ERR = -100; //Used as a default value for Bundle.getInt, not part of the API	

    public static String PURCHASE_IN_PROGRESS_FOR_PRODUCT = "";

    private static String ITEM_TYPE_INAPP = "inapp";

    //This is the expected SHA1 finger-print in HEX format
    private static final String EXPECTED_SHA1_FINGERPRINT = "C476A7D71C4CB92641A699C1F1CAC93CA81E0396";

    private static final String ENABLER_PACKAGENAME = "com.nokia.payment.iapenabler";

    /**
     * Constructor of the Payment object. Needs to be initialized to access NIAP methods.
     *
     * @param context - The activity where Payment object is initialized
     */
    public Payment(Context context) {
    }


    /**
     * Binds to Nokia in-app payment service.
     *
     * @param ctx
     * @throws GeneralSecurityException If Nokia In-App payment enabler fingerprint is not valid
     */
    public void connectToService(Context ctx) throws GeneralSecurityException {

        activity = (Activity) ctx;

        //Verifies enabler fingerprint
        if (!verifyFingreprint()) {

            npayAvailable = false;
            errorAlert("Nokia In-App Payment Enabler is not available.");

            throw new GeneralSecurityException("Enabler fingerprint incorrect. Billing unavailable");

        } else {
            //Enabler fingerprint OK. Continue with binding. 
            Intent paymentEnabler = new Intent("com.nokia.payment.iapenabler.InAppBillingService.BIND");
            paymentEnabler.setPackage(ENABLER_PACKAGENAME);
            activity.bindService(paymentEnabler, this, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        npay = INokiaIAPService.Stub.asInterface(service);

        Log.i("onServiceConnected", "IAP service connected");
        npayAvailable = true;

        if (isBillingAvailable()) {
            getPurchases(false);
            fetchPrices();
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {


    }

    /**
     * Checks SHA1 fingerprint of the enabler
     *
     * @return true if signature matches, false if package is not found or signature does not match.
     */
    private boolean verifyFingreprint() {

        try {
            PackageInfo info = activity.getBaseContext().getPackageManager().getPackageInfo(ENABLER_PACKAGENAME, PackageManager.GET_SIGNATURES);

            if (info.signatures.length == 1) {

                byte[] cert = info.signatures[0].toByteArray();
                MessageDigest digest;
                digest = MessageDigest.getInstance("SHA1");
                byte[] ENABLER_FINGERPRINT = digest.digest(cert);
                byte[] EXPECTED_FINGERPRINT = hexStringToByteArray(EXPECTED_SHA1_FINGERPRINT);

                if (Arrays.equals(ENABLER_FINGERPRINT, EXPECTED_FINGERPRINT)) {
                    Log.i("isBillingAvailable", "NIAP signature verified");
                    return true;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public boolean isBillingAvailable() {

        boolean isAvailable = false;

        try {

            int responseCode = RESULT_ERR;

            if (npayAvailable)
                responseCode = npay.isBillingSupported(API_VERSION, activity.getPackageName(), ITEM_TYPE_INAPP);

            switch (responseCode) {
                case RESULT_OK:
                    isAvailable = true;
                    break;
                case RESULT_NO_SIM_CARD:
                    errorAlert("No SIM. Please Insert SIM card.");
                    break;
                case RESULT_ERR:
                    errorAlert("Nokia In-App Payment Enabler is not available.");
                    break;
                default:
                    errorAlert("Billing is not supported. " + responseCode);
                    break;
            }
        } catch (RemoteException e) {
            Log.e("isBillingAvailable", e.getMessage(), e);
            errorAlert("Billing is not supported. " + e.getMessage());
        }
        return isAvailable;
    }


    /**
     * Fetches the prices asynchronously
     */
    public void fetchPrices() {

        if (!isBillingAvailable())
            return;

        for (Product p : Content.ITEMS) {
            if (p.getPrice() != "") {
                Log.i("fetchPrices", "Prices already available. Not fetching.");
                return;
            }
        }


        AsyncTask<Void, String, Void> pricesTask = new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                ArrayList<String> productIdArray = new ArrayList<String>(Content.ITEM_MAP.keySet());
                Bundle productBundle = new Bundle();
                productBundle.putStringArrayList("ITEM_ID_LIST", productIdArray);

                try {
                    Bundle priceInfo = npay.getProductDetails(API_VERSION, activity.getPackageName(), ITEM_TYPE_INAPP, productBundle);

                    if (priceInfo.getInt("RESPONSE_CODE", RESULT_ERR) == RESULT_OK) {
                        ArrayList<String> productDetailsList = priceInfo.getStringArrayList("DETAILS_LIST");

                        for (String productDetails : productDetailsList) {
                            parseProductDetails(productDetails);
                        }

                    } else {
                        Log.e("fetchPrices", "PRICE - priceInfo was not ok: Result was: " + priceInfo.getInt("RESPONSE_CODE", -100));
                    }

                } catch (JSONException e) {
                    Log.e("fetchPrices", "PRODUCT DETAILS PARSING EXCEPTION: " + e.getMessage(), e);
                } catch (RemoteException e) {
                    Log.e("fetchPrices", "PRICE EXCEPTION: " + e.getMessage(), e);
                }
                return null;
            }


            private void parseProductDetails(String productDetails) throws JSONException {

                ProductDetails details = new ProductDetails(productDetails);

                Log.i("fetchPrices", productDetails);

                Product p = Content.ITEM_MAP.get(details.getProductId());

                if (p != null) {
                    p.setPrice(details.getPriceFormatted());
                    Log.i("fetchPrices", "PRICE RECEIVED - " + details.getPrice() + " " + details.getCurrency());
                } else {
                    Log.i("fetchPrices", "Unable to set price for product " + details.getProductId() + ". Product not found.");
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (ProductListFragment.purchaseListAdapter != null) {
                    ProductListFragment.purchaseListAdapter.notifyDataSetChanged();
                }
                if (MainScreenPepperListFragment.reference.adapter != null) {
                    MainScreenPepperListFragment.reference.adapter.notifyDataSetChanged();
                }
            }

        };
        pricesTask.execute();
    }

    /**
     * Check the products that we have already purchased
     */
    public void getPurchases(boolean ignoreLifeCycleCheck) {

        if (!isBillingAvailable())
            return;


        if (ignoreLifeCycleCheck == false) {
            if (purchases_asked) {
                Log.i("getPurchases", "Restorables already asked.");
                return;
            }
        }

        MainScreenPepperListFragment.getpeppers.setVisibility(View.INVISIBLE);
        MainScreenPepperListFragment.fetch_peppers_progressbar.setVisibility(View.VISIBLE);
        MainScreenPepperListFragment.fetching_peppers.setVisibility(View.VISIBLE);

        AsyncTask<Void, String, Void> restoreTask = new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<String> productIdArray = new ArrayList<String>(Content.ITEM_MAP.keySet());
                Bundle productBundle = new Bundle();
                productBundle.putStringArrayList("ITEM_ID_LIST", productIdArray);

                try {
                    Bundle purchases = npay.getPurchases(API_VERSION, activity.getPackageName(), ITEM_TYPE_INAPP, productBundle, null);
                    Log.i("getPurchases", "GET PURCHASES RESPONSE CODE: " + purchases.getInt("RESPONSE_CODE", RESULT_ERR));

                    if (purchases.getInt("RESPONSE_CODE", RESULT_ERR) == RESULT_OK) {

                        ArrayList<String> purchaseDataList = purchases.getStringArrayList("INAPP_PURCHASE_DATA_LIST");

                        for (String purchaseData : purchaseDataList) {
                            parsePurchaseData(purchaseData);
                        }
                        purchases_asked = true;
                    } else {
                        Log.e("getPurchases", "GET PURCHASES - response was not ok: Result was: " + purchases.getInt("RESPONSE_CODE", RESULT_ERR));
                    }

                } catch (JSONException e) {
                    Log.e("getPurchases", "PURCHASE DATA PARSING EXCEPTION: " + e.getMessage(), e);
                } catch (RemoteException e) {
                    Log.e("getPurchases", "EXCEPTION: " + e.getMessage(), e);
                }
                return null;
            }

            private void parsePurchaseData(String purchaseData) throws JSONException {

                Purchase purchase = new Purchase(purchaseData);

                Product p = Content.ITEM_MAP.get(purchase.getProductId());

                if (p != null) {
                    p.setPurchased(purchase);
                    Log.i("getPurchases", "Restoring product " + purchase.getProductId() + " Purchase token: " + purchase.getToken());
                } else {
                    Log.i("getPurchases", "Unable to restore product " + purchase.getProductId() + ". Product not found.");
                }

            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                MainScreenPepperListFragment.getpeppers.setVisibility(View.VISIBLE);
                MainScreenPepperListFragment.fetch_peppers_progressbar.setVisibility(View.GONE);
                MainScreenPepperListFragment.fetching_peppers.setVisibility(View.GONE);

                if (ProductListFragment.purchaseListAdapter != null) {
                    ProductListFragment.purchaseListAdapter.notifyDataSetChanged();
                }
                if (MainScreenPepperListFragment.reference.adapter != null) {
                    MainScreenPepperListFragment.reference.adapter.notifyDataSetChanged();
                }
            }

        };
        restoreTask.execute();
    }

    /**
     * Starts the payment process for the given product id. Application is suspended and the Npay service will complete the purchase.
     *
     * @param caller     - The response from the purchase is sent to the caller activity. Caller activity must override "protected void onActivityResult(int requestCode, int resultCode, Intent data)" method to get the response.
     * @param product_id - The product id that is being purchased.
     * @throws Exception
     */
    public void startPayment(Activity caller, String product_id) throws Exception {

        if (!isBillingAvailable())
            return;

        Bundle intentBundle = npay.getBuyIntent(API_VERSION, activity.getPackageName(), product_id, ITEM_TYPE_INAPP, "");
        PendingIntent purchaseIntent = intentBundle.getParcelable("BUY_INTENT");

        //Set purchase in progress
        setPurchaseInProgress(product_id);

        caller.startIntentSenderForResult(
                purchaseIntent.getIntentSender(),
                Integer.valueOf(0),
                new Intent(),
                Integer.valueOf(0),
                Integer.valueOf(0),
                Integer.valueOf(0));

    }

    public boolean consumeProduct(String productId, String token) {

        if (!isBillingAvailable())
            return false;

        boolean consumed = false;

        try {
            Log.i("consumeProduct", "Consuming product: " + productId + " PurchaseToken: " + token);
            int response = npay.consumePurchase(API_VERSION, activity.getPackageName(), productId, token);

            if (response == RESULT_OK)
                consumed = true;

        } catch (RemoteException e) {
            Log.e("isBillingAvailable", e.getMessage(), e);
        }
        return consumed;
    }


    public void setPurchaseInProgress(String productId) {
        if (productId.equals(KEY_NOT_IN_PROGRESS))
            Log.i("setPurchaseInProgress", "Resetting purcahse progress");
        else
            Log.i("setPurchaseInProgress", "Starting purchase for product " + productId);

        SharedPreferences prefs = activity.getSharedPreferences(SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        prefs.edit().putString(PRODUCT_ID_KEY, productId).commit();
    }

    public String getPurchaseInProgress() {
        SharedPreferences prefs = activity.getSharedPreferences(SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        String progress = prefs.getString(PRODUCT_ID_KEY, PREFLOAD_FAILED);
        Log.i("getPurchaseInProgress", "Purchase in progress is: " + progress);
        return progress;
    }

    /**
     * Shows an alert dialog when billing is not available.
     */
    public void errorAlert(String msg) {
        Log.i("errorAlert", "Displaying error alert");
        new AlertDialog.Builder(activity).setTitle("PepperFarm").setMessage(msg).setNeutralButton("Close", null).show();
    }

    public void cleanUp() {
        try {
            activity.unbindService(this);
            npayAvailable = false;
            Log.i("Payment", "Service disconnected");
        } catch (Exception ignored) {
            Log.i("cleanUp", "Service was cleared up already previously");
            //Unable to clean up. This means it is not registered or another error. Ignored exception
        }
    }

}
