/**
 * Copyright (c) 2014 Microsoft Mobile and/or its subsidiary(-ies).
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.pepperfarm.client;

import android.app.Application;


public class Pepperfarm extends Application {

    static Pepperfarm pepperfarm;

    public Pepperfarm() {
        pepperfarm = this;
    }

    public static Pepperfarm getContext() {
        return pepperfarm;
    }

}
