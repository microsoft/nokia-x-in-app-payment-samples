/**
 * Copyright (c) 2014 Microsoft Mobile and/or its subsidiary(-ies).
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

import java.util.ArrayList;

import static com.nokia.example.paymentoneapk.PaymentOneAPKUtils.getErrorMessage;

@SuppressWarnings("MethodOnlyUsedFromInnerClass")
public class PaymentOneAPKActivity extends Activity implements ServiceConnection {

	private static final String TAG = PaymentOneAPKActivity.class.getCanonicalName();

	public static final int TOAST_DURATION = 1500;
	public static final int API_VERSION = 3;

	private final PaymentOneAPKService mService = new PaymentOneAPKService();

	private final ArrayList<String> productSkus = new ArrayList<String>(10);

	private Button buyButton = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Log.d(TAG, "PaymentOneAPKActivity.onCreate");

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

		bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d(TAG, "PaymentOneAPKActivity.onActivityResult");

		Log.d(TAG, String.format("requestCode = %d", requestCode));
		Log.d(TAG, String.format("resultCode = %d", resultCode));

		toastMessage("Item purchased");

		consumeTestProduct();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindService(this);
	}

	@Override
	public void onServiceConnected(final ComponentName name, final IBinder service) {
		Log.d(TAG, "PaymentOneAPKActivity.onServiceConnected");

		mService.setService(this, service);

		mapProductsSkus();

		checkIfBillingIsSupported();
	}

	@Override
	public void onServiceDisconnected(final ComponentName name) {
		mService.clearService();
	}

	private void mapProductsSkus() {

		productSkus.add("android.test.purchased");

		final Bundle productMappings = new Bundle();
		productMappings.putString("1023608", "android.test.purchased");

		try {
			mService.mapProducts(API_VERSION, getPackageName(), productMappings);
		} catch (final RemoteException e) {
			Log.e(TAG, "error while mapping product skus", e);
		}

	}

	public void launchPurchase(final String sku, final String itemType, final int requestCode,
		final String extraData) {

		Log.d(TAG, "PaymentOneAPKActivity.launchPurchase");

		try {
			final Bundle buyIntentBundle = mService.getBuyIntent(API_VERSION, getPackageName(), sku, itemType, extraData);

			final int response = buyIntentBundle.getInt("RESPONSE_CODE");

			if (response != PaymentOneAPKUtils.RESULT_OK) {

				Log.e(TAG, String.format("error while buying. response=%s", getErrorMessage(response)));
				toastMessage(String.format("Got an error while buying: %s", getErrorMessage(response)));

				return;
			}

			final PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

			Log.d(TAG, String.format("pendingIntent = %s", pendingIntent));

			startIntentSenderForResult(
				pendingIntent.getIntentSender(), requestCode, new Intent(), 0, 0, 0
			);

		} catch (final RemoteException e) {
			Log.e(TAG, "error while buying", e);
			toastMessage("Got an exception while buying");
		} catch (final IntentSender.SendIntentException e) {
			Log.e(TAG, "error while buying", e);
			toastMessage("Got an exception while buying");
		}
	}

	public void consumeTestProduct() {
		Log.d(TAG, "PaymentOneAPKActivity.consumeTestProduct");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mService.consumePurchase(
						API_VERSION, getPackageName(), String.format("inapp:%s:android.test.purchased", getPackageName())
					);

				} catch (final RemoteException e) {
					Log.e(TAG, "error while consuming", e);
					toastMessage("Got an exception while consuming");
					return;
				}

				toastMessage("Item consumed");

			}
		}).start();
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void checkIfBillingIsSupported() {
		Log.d(TAG, "PaymentOneAPKActivity.checkIfBillingIsSupported");

		final int result;
		try {
			result = mService.isBillingSupported(API_VERSION, getPackageName(), "inapp");

		} catch (final RemoteException e) {
			Log.e(TAG, "error while isBillingSupported", e);
			toastMessage("Got an exception while consuming");

			return;
		}

		if (result != PaymentOneAPKUtils.RESULT_OK) {
			toastMessage(String.format("Billing is not supported: %s", getErrorMessage(result)));

			Log.e(TAG, String.format("result = %d : %s", result, getErrorMessage(result)));

			return;
		}

		queryProductDetails();

		buyButton.setEnabled(true);
	}

	private void queryProductDetails() {
		Log.d(TAG, "PaymentOneAPKActivity.queryProductDetails");

		final Bundle querySkus = new Bundle();

		querySkus.putStringArrayList("ITEM_ID_LIST", productSkus);

		new Thread(new Runnable() {
			@SuppressWarnings("CollectionDeclaredAsConcreteClass")
			@Override
			public void run() {

				final Bundle skuDetails;
				try {

					skuDetails = mService.getSkuDetails(API_VERSION, getPackageName(), "inapp", querySkus);

				} catch (final RemoteException e) {
					Log.e(TAG, "query error", e);
					return;
				}

				final int response = skuDetails.getInt("RESPONSE_CODE");
				Log.d(TAG, String.format("response = %d", response));

				if (response != PaymentOneAPKUtils.RESULT_OK) {

					toastMessage(
						String.format("Got invalid response while doing query: %s", getErrorMessage(response))
					);

					return;
				}

				final ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

				for (final String resp : responseList) {
					Log.d(TAG, String.format("resp = %s", resp));
				}

				toastMessage("Item query done");

			}
		}).start();
	}

	protected void toastMessage(final String message) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(PaymentOneAPKActivity.this, message, TOAST_DURATION).show();
			}
		});

	}

	@Override
	public String toString() {
		return String.format("PaymentOneAPKActivity{mService=%s, productSkus=%s, buyButton=%s}",
			mService,
			productSkus,
			buyButton);
	}
}
