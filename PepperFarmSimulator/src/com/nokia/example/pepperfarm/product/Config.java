/**
 * Copyright (c) 2014 Microsoft Mobile and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.product;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class Config {

    public static final String APPTITLE = "Pepperfarm";
    public static final String PEPPER_FARM_PRODUCTID = "1081412";
    public static final String PEPPER_SEEDS_PRODUCTID = "1081413";
    public static final String JALAPENO_PRODUCTID = "1081414";
    public static final String SWEET_BELL_PRODUCTID = "1081416";
    public static final String HABENERO_PRODUCTID = "1081417";
    public static final String PEPPER_SPRAY_PRODUCTID = "1081418";


    /**
     * Sets the title of application according to APPTITLE
     *
     * @param reference
     */
    public static void setVersiontitle(Activity reference) {
        try {
            PackageInfo pInfo = reference.getPackageManager().getPackageInfo(reference.getPackageName(), 0);
            String version_code = Integer.toString(pInfo.versionCode);

            PackageInfo iapInfo = reference.getPackageManager().getPackageInfo("com.nokia.payment.iapenabler", 0);
            String iap_versioncode = Integer.toString(iapInfo.versionCode);
            String iap_version = iapInfo.versionName.concat(".").concat(iap_versioncode);


            reference.setTitle(APPTITLE.concat(" 1.0.").concat(version_code).concat(" / ").concat(iap_version));

        } catch (NameNotFoundException ignored) {
        }
    }

}
