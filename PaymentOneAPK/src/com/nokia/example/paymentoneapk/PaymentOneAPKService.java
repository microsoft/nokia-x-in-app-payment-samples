/**
 * Copyright (c) 2014 Nokia Corporation and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.paymentoneapk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.vending.billing.IInAppBillingService;
import com.nokia.payment.iap.aidl.INokiaIAPService;

import java.util.List;

@SuppressWarnings("StaticNonFinalField")
public class PaymentOneAPKService {

	public static final  String NOKIA_NIAP = "com.nokia.payment.iapenabler";
	private static final String TAG        = PaymentOneAPKService.class.getCanonicalName();

	private IInAppBillingService googleIABService = null;
	private INokiaIAPService     nokiaIAPService  = null;

	private static boolean isNokiaIAPValueCached = false;
	private static boolean isNokiaIAPInstalled   = false;

	private boolean useGoogleBilling = false;

	public static boolean isNokiaNIAPAvailable(final Context context) {

		if (isNokiaIAPValueCached) {
			return isNokiaIAPInstalled;
		}

		final PackageManager packageManager = context.getPackageManager();
		final List<PackageInfo> allPackages = packageManager.getInstalledPackages(0);

		isNokiaIAPValueCached = true;

		for (final PackageInfo packageInfo : allPackages) {

			if (NOKIA_NIAP.equals(packageInfo.packageName)) {

				Log.d(TAG, "Nokia IAP found");

				isNokiaIAPInstalled = true;

				return true;

			}
		}

		isNokiaIAPInstalled = false;

		return false;
	}

	public int isBillingSupported(final int apiVersion, final String packageName, final String type)
		throws RemoteException {

		return useGoogleBilling
			   ? googleIABService.isBillingSupported(apiVersion, packageName, type)
			   : nokiaIAPService.isBillingSupported(apiVersion, packageName, type);
	}

	public Bundle getBuyIntent(final int apiVersion, final String packageName, final String sku,
		final String type, final String developerPayload) throws RemoteException {

		return useGoogleBilling
			   ? googleIABService.getBuyIntent(apiVersion, packageName, sku, type, developerPayload)
			   : nokiaIAPService.getBuyIntent(apiVersion, packageName, sku, type, developerPayload);
	}

	public Bundle getSkuDetails(final int apiVersion, final String packageName, final String type,
		final Bundle skusBundle) throws RemoteException {

		return useGoogleBilling
			   ? googleIABService.getSkuDetails(apiVersion, packageName, type, skusBundle)
			   : nokiaIAPService.getProductDetails(apiVersion, packageName, type, skusBundle);

	}

	public int consumePurchase(final int apiVersion, final String packageName, final String purchaseToken)
		throws RemoteException {

		return useGoogleBilling
			   ? googleIABService.consumePurchase(apiVersion, packageName, purchaseToken)
			   : nokiaIAPService.consumePurchase(apiVersion, packageName, null, purchaseToken);
	}

	public void mapProducts(final int apiVersion, final String packageName, final Bundle productMap)
		throws RemoteException {

		if (!useGoogleBilling) {
			nokiaIAPService.setProductMappings(apiVersion, packageName, productMap);
		}

	}

	public void useGoogleIAB(final IInAppBillingService service) {
		useGoogleBilling = true;

		googleIABService = service;
	}

	public void useNokiaIAP(final INokiaIAPService service) {
		useGoogleBilling = false;

		nokiaIAPService = service;
	}

	public void setService(final Context context, final IBinder service) {

		if (PaymentOneAPKService.isNokiaNIAPAvailable(context)) {
			useNokiaIAP(INokiaIAPService.Stub.asInterface(service));

		} else {
			useGoogleIAB(IInAppBillingService.Stub.asInterface(service));

		}

	}

	public Intent getServiceIntent(final Context context) {
		return PaymentOneAPKService.isNokiaNIAPAvailable(context)
			   ? new Intent("com.nokia.payment.iapenabler.InAppBillingService.BIND")
			   : new Intent("com.android.vending.billing.InAppBillingService.BIND");
	}
}
