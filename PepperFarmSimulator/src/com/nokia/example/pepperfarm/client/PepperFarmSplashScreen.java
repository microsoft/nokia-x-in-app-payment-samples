/**
 * Copyright (c) 2014 Nokia Corporation and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ProgressBar;
import com.nokia.example.pepperfarm.product.Content;

/**
 * A splash screen used to give some time to complete the first get Product data call.
 * In a real game this may not be necessary as it is less likely (but still possible) that the user will get to any screens requiring
 * IAP Product data before the call returns.
 */
public class PepperFarmSplashScreen extends Activity {

    private final int SECONDS_DELAYED = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Show a little spinning wheel while we load.
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, null);
        progressDialog.setContentView(new ProgressBar(this));

        setContentView(R.layout.pepper_farm_splash_screen);

        if (Content.products_initialized == false) {
            Content.initializeProducts(getResources());
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(PepperFarmSplashScreen.this, PepperFarmMainScreenActivity.class));
                progressDialog.dismiss();
                finish();
            }
        }, SECONDS_DELAYED * 1000);
    }

}

