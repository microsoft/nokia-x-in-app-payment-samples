/**
 * Copyright (c) 2014 Nokia Corporation and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.paymentoneapk;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.vending.billing.IInAppBillingService;
import com.nokia.payment.iap.aidl.INokiaIAPService;

import java.util.ArrayList;

public class PaymentOneAPKActivity extends Activity {

	private static final String TAG = PaymentOneAPKActivity.class.getCanonicalName();

	private final PaymentOneAPKService mService = new PaymentOneAPKService();

	private final ArrayList<String> productSkus = new ArrayList<String>(10);
	private Button buyButton;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Log.d(TAG, "com.nokia.IABinAll.MyActivity.onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		buyButton = (Button) findViewById(R.id.buy);
		buyButton.setEnabled(false);

		buyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				launchPurchase("android.test.purchased", "inapp", 1234, "");
			}
		});

		final Intent intent = mService.getServiceIntent(this);

		bindService(intent, serviceConn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d(TAG, "com.nokia.example.paymentoneapk.PaymentOneActivity.onActivityResult");

		Log.d(TAG, "requestCode = " + requestCode);
		Log.d(TAG, "resultCode = " + resultCode);

		Toast.makeText(this, "Item purchased", 1500).show();

		consumeTestProduct();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void mapProductsSkus() {

		productSkus.add("android.test.purchased");

		Bundle productMappings = new Bundle();
		productMappings.putString("1023608", "android.test.purchased");

		try {
			mService.mapProducts(3, getPackageName(), productMappings);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void launchPurchase(final String sku, final String itemType, final int requestCode,
		final String extraData) {

		Log.d(TAG, "com.nokia.example.paymentoneapk.PaymentOneActivity.launchPurchase");

		try {
			final Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), sku, itemType, extraData);

			final int response = buyIntentBundle.getInt("RESPONSE_CODE");

			if (response != 0) {
				Log.e(TAG, "error while buying. response=" + response);
				toastMessage("Got an error while buying: " + response);
				return;
			}

			final PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

			Log.d(TAG, "pendingIntent = " + pendingIntent);

			startIntentSenderForResult(
				pendingIntent.getIntentSender(), requestCode, new Intent(), 0, 0, 0
			);

		} catch (final RemoteException e) {
			e.printStackTrace();
			toastMessage("Got an exception while guying");
		} catch (final IntentSender.SendIntentException e) {
			e.printStackTrace();
			toastMessage("Got an exception while guying");
		}
	}

	public void consumeTestProduct() {
		Log.d(TAG, "com.nokia.example.paymentoneapk.PaymentOneActivity.consumeProduct");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mService.consumePurchase(
						3, getPackageName(), "inapp:" + getPackageName() + ":android.test.purchased"
					);

				} catch (RemoteException e) {
					Log.e(TAG, "error while consuming", e);
					toastMessage("Got an exception while consuming");
					return;
				}

				toastMessage("Item consumed");

			}
		}).start();
	}

	private void checkIfBillingIsSupported() {
		Log.d(TAG, "com.nokia.example.paymentoneapk.PaymentOneActivity.checkIfBillingIsSupported");

		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					int result = mService.isBillingSupported(3, getPackageName(), "inapp");

					if (result != 0) {
						toastMessage("Billing is not supported.");

						Log.d(TAG, "result = " + result);

						return;
					}
					buyButton.setEnabled(true);

					queryProductDetails();

				} catch (RemoteException e) {
					Log.e(TAG, "error while isBillingSupported", e);
					toastMessage("Got an exception while consuming");
				}
			}
		}).start();
	}

	private void queryProductDetails() {
		Log.d(TAG, "com.nokia.example.paymentoneapk.PaymentOneActivity.queryProductDetails");

		final Bundle querySkus = new Bundle();

		querySkus.putStringArrayList("ITEM_ID_LIST", productSkus);

		new Thread(new Runnable() {
			@Override
			public void run() {

				final Bundle skuDetails;
				try {

					skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);

				} catch (final RemoteException e) {
					Log.e(TAG, "query error", e);
					return;
				}

				final int response = skuDetails.getInt("RESPONSE_CODE");
				Log.d(TAG, "response = " + response);

				if (response != 0) {
					toastMessage("Got invalid response while doing query: " + response);
					return;
				}

				final ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

				for (final String resp : responseList) {
					Log.d(TAG, "resp = " + resp);
				}

				toastMessage("Item query done");

			}
		}).start();
	}

	protected void toastMessage(final String message) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(PaymentOneAPKActivity.this, message, 1500).show();
			}
		});

	}

	final ServiceConnection serviceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service) {

			Log.d(TAG, "com.nokia.example.paymentoneapk.PaymentOneActivity.onServiceConnected");

			mService.setService(PaymentOneAPKActivity.this, service);

			mapProductsSkus();

			checkIfBillingIsSupported();
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
		}
	};
}
