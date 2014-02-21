package com.nokia.example.paymentoneapk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

@SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "StaticNonFinalField"})
public final class PaymentOneAPKUtils {

	public static final  String  NOKIA_NIAP            = "com.nokia.payment.iapenabler";
	private static final String  TAG                   = PaymentOneAPKUtils.class.getCanonicalName();

	private static       boolean isNokiaIAPValueCached = false;
	private static       boolean isNokiaIAPInstalled   = false;

	private PaymentOneAPKUtils() {}

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
}
