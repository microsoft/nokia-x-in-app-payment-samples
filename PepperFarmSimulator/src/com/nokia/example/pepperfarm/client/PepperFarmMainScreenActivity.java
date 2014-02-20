/**
 * Copyright (c) 2014 Nokia Corporation and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.nokia.example.pepperfarm.iap.Payment;
import com.nokia.example.pepperfarm.product.Config;
import com.nokia.example.pepperfarm.product.Content;
import com.nokia.example.pepperfarm.product.Content.Product;

import java.security.GeneralSecurityException;

/**
 * This is the first screen that loads in the Pepper Farm application.
 * It shows you a list of Peppers you have already purchased and includes a button allowing you to purchase more Peppers.
 */
public class PepperFarmMainScreenActivity extends FragmentActivity implements MainScreenPepperListFragment.Callbacks {

    /**
     * Used for logging so you know exactly where the logging messages are coming from.
     */
    private final static String LOG_TAG = PepperFarmMainScreenActivity.class.getCanonicalName();

    /**
     * A public string representing the full path name of this file. This is used in order to let the Enabler
     * know where to return to after a purchase request is completed.
     */
    public final static String PEPPER_FARM_MAIN_SCREEN_ACTIVITY =
            "com.nokia.example.pepperfarm.client.PepperFarmMainScreenActivity";


    public static Payment purchaseHandler;

    /**
     * Sets up the UI container for the Pepper Farm Main Screen
     * <p/>
     * As part of the set up, we use the Nokia In App Payment Library to request
     * the Enabler to return pricing data for each in app purchasable item we are selling.
     * For simplicity, this is done on the Main (UI) thread. Ideally you could spawn a new thread to set up
     * the NPayProductPricepoint objects and make the product data request to the library.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "Pepper Farm Main Screen Activity Created");
        Config.setVersiontitle(this);

        setContentView(R.layout.pepper_farm_main_view);

        Button buyPepperButton = (Button) findViewById(R.id.buy_button);
        buyPepperButton.setVisibility(View.INVISIBLE);

        View product_list = findViewById(R.id.product_list);
        product_list.refreshDrawableState();

        //Listener for the Purchase Requisition button
        buyPepperButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.PRODUCT_LIST");
                startActivity(intent);
            }
        });


        Button resetPurchases = (Button) findViewById(R.id.reset_purchases);
        resetPurchases.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Log.i("resetPurchases", "Consuming all items");
                for (Product product : Content.ITEMS) {
                    product.consumeItem();
                }
                if (MainScreenPepperListFragment.reference.adapter != null) {
                    MainScreenPepperListFragment.reference.adapter.clear();
                    MainScreenPepperListFragment.reference.adapter.notifyDataSetChanged();
                }
            }

        });

        Button fetchRestorables = (Button) findViewById(R.id.fetch_restorable_products);
        fetchRestorables.setOnClickListener
                (new Button.OnClickListener() {
                    public void onClick(View v) {
                        Log.i("fetchRestorables", "Fetching all restorable items");
                        purchaseHandler.getPurchases(true);
                    }
                });

    }

    /**
     * This method is used to allow a fragment to notify the controlling activity that a specific
     * item in the list has been selected. It could be used in layouts where you want to display the list
     * of items, and specific information about one item on the same screen, and need a way of displaying
     * to the user which item they have selected. Pepper Farm Simulator does not use this as each item
     * selection brings you to an entirely new activity.
     */
    @Override
    public void onItemSelected(String id) {

    }


    @Override
    public void onStart() {
        Log.i(LOG_TAG, "onStart");
        super.onStart();
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
    public void onStop() {
        Log.i(LOG_TAG, "onStop");
        super.onStop();
        purchaseHandler.cleanUp();
    }
}
