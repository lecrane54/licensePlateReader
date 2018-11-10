package com.example.klefe.licenseplaterrecognizer;

import android.app.Application;

import com.google.firebase.FirebaseApp;

/**
 * Created by klefe on 11/8/18.
 */

public class MyApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
