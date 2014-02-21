package com.nokia.example.paymentoneapk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

@SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "StaticNonFinalField"})
public final class PaymentOneAPKUtils {

	public static final int RESULT_OK                  = 0;// - success
	public static final int RESULT_USER_CANCELED       = 1;// - user pressed back or canceled a dialog
	public static final int RESULT_BILLING_UNAVAILABLE = 3;// - this billing API version is not supported for the type requested or billing is otherwise impossible (for example there is no SIM card inserted)
	public static final int RESULT_ITEM_UNAVAILABLE    = 4;// - requested ProductID is not available for purchase
	public static final int RESULT_DEVELOPER_ERROR     = 5;// - invalid arguments provided to the API
	public static final int RESULT_ERROR               = 6;// - Fatal error during the API action
	public static final int RESULT_ITEM_ALREADY_OWNED  = 7;// - Failure to purchase since item is already owned
	public static final int RESULT_ITEM_NOT_OWNED      = 8;// - Failure to consume since item is not owned
	public static final int RESULT_NO_SIM_CARD         = 9;

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

	public static String getErrorMessage(int error) {
		switch (error) {
			case RESULT_OK:
				return "Ok";
			case RESULT_USER_CANCELED:
				return "User canceled";
			case RESULT_BILLING_UNAVAILABLE:
				return "Billing unavailable";
			case RESULT_ITEM_UNAVAILABLE:
				return "Item unavailable";
			case RESULT_DEVELOPER_ERROR:
				return "Developer error";
			case RESULT_ERROR:
				return "Error";
			case RESULT_ITEM_ALREADY_OWNED:
				return "Item already owned";
			case RESULT_ITEM_NOT_OWNED:
				return "Item not owned";
			case RESULT_NO_SIM_CARD:
				return "No sim card";
			default:
				return String.format("Unknown error code:%d", error);
		}
	}
}
