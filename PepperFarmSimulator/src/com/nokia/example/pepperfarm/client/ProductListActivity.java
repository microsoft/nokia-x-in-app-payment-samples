/**
 * Copyright (c) 2014 Microsoft Mobile and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import com.nokia.example.pepperfarm.client.util.Purchase;
import com.nokia.example.pepperfarm.iap.Payment;
import com.nokia.example.pepperfarm.product.Config;
import com.nokia.example.pepperfarm.product.Content;
import com.nokia.example.pepperfarm.product.Content.Product;

import java.security.GeneralSecurityException;

/**
 * An activity representing a list of purchasable IAP Products.
 * Much of the work of displaying the actual list is handled by ProductListFragment.java and especially ContentPurchaseAdapter,
 * which extends android.widget.ArrayAdapter
 * <p/>
 * This activity also implements the required
 * {@link ProductListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class ProductListActivity extends FragmentActivity implements
        ProductListFragment.Callbacks {

    public static Activity reference;
    public static Payment purchaseHandler;

    /**
     * Used for logging so you know exactly where the logging messages are coming from.
     */
    private final static String LOG_TAG = ProductListActivity.class.getCanonicalName();


    /**
     * Sets version title and enables the back button.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, " onCreate");

        Config.setVersiontitle(this);

        setContentView(R.layout.activity_product_list);
        reference = this;

        // Show the Up (Back) button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }


    /**
     * Callback method from {@link ProductListFragment.Callbacks}
     */
    @Override
    public void onItemSelected(String id) {

    }


    /**
     * This method is used to handle the Up (Back) button.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpTo(this, new Intent(this,
                        MainScreenPepperListFragment.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Handles the purchase response.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        /*
         * Verify that onActivityResult was successful,
         */
        if (resultCode == Activity.RESULT_OK) {
            //Verify that the purchase was successful, 
            int responseCode = data.getIntExtra("RESPONSE_CODE", Payment.RESULT_ERR);

            try {

                switch (responseCode) {

                    case Payment.RESULT_OK:
                        //Unlock content
                        if (data.getStringExtra("INAPP_PURCHASE_DATA") != null) {
                            Purchase purchaseInfo = new Purchase(data.getStringExtra("INAPP_PURCHASE_DATA"));

                            purchaseHandler.setPurchaseInProgress(Payment.KEY_NOT_IN_PROGRESS);

                            Product purchasedProduct = Content.ITEM_MAP.get(purchaseInfo.getProductId());
                            purchasedProduct.setPurchased(purchaseInfo);
                        }

                        if (ProductListFragment.purchaseListAdapter != null) {
                            ProductListFragment.purchaseListAdapter.notifyDataSetChanged();
                        }
                        break;

                    case Payment.RESULT_ITEM_ALREADY_OWNED:
                        Log.i(LOG_TAG, " User already bought the item");
                        //Item has already been bought, but we still made it available for purchasing. Need to sync all purchases.
                        purchaseHandler.getPurchases(true);
                        break;

                    case Payment.RESULT_USER_CANCELED:
                        //User pressed cancel in purchase dialog.
                        Log.i(LOG_TAG, "User canceled purchase dialog");
                        break;

                    default:
                        Log.i(LOG_TAG, "Purchase failed: " + responseCode);
                        break;
                }
            } catch (Exception e) {
                Log.i(LOG_TAG, "Exception while parsing payment response: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void connectToService() {
        try {
            if (purchaseHandler != null) {
                if (!purchaseHandler.npayAvailable) {
                    purchaseHandler.connectToService(this);
                }
            } else {
                purchaseHandler = new Payment(this);
                purchaseHandler.connectToService(this);
                Log.i(LOG_TAG, "Npay initialization SUCCESS, waiting for service connection");
            }
        } catch (GeneralSecurityException e) {
            Log.e(LOG_TAG, "Npay initialization FAILED: " + e.getMessage(), e);
        }
    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG, "onStart");
        super.onStart();
        connectToService();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");
        super.onStop();
        purchaseHandler.cleanUp();
    }


}
