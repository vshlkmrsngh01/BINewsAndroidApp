/*
 * Created by Bharat Idea on 03/08/21, 3:25 PM
 *  Copyright (c) 2021 . All rights reserved.
 *  Last modified 03/08/21, 3:25 PM
 */

package com.newsof.bharatidea;

import android.app.Application;

import com.onesignal.OneSignal;

public class BIApp extends Application {

    private static final String ONESIGNAL_APP_ID = "4d02dfc1-67fe-40f3-8eef-1969c8827157";

    @Override
    public void onCreate() {
        super.onCreate();


        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
    }
}
