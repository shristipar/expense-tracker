package com.trackexpense.snapbill;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.trackexpense.snapbill.activity.RegisterActivity;
import com.trackexpense.snapbill.utils.Constants;

/**
 * Created by Johev on 07-03-2017.
 */

public class Config {

    public static final String BASE_URL = "http://192.168.1.10:3000/api/v1/";

    public static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName();
    }

    public static void startLandingActivity(Context context) {
        Prefs.putBoolean(Constants.PREF_LOGGED_IN, true);
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//        activity.overridePendingTransition(0,0);
//        activity.overridePendingTransition(R.anim.activity_in,R.anim.activity_out);
    }

    public static void startLoginActivity(Context context) {
        Prefs.putBoolean(Constants.PREF_LOGGED_IN, false);
        Intent intent = new Intent(context.getApplicationContext(),RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//        activity.overridePendingTransition(0,0);
//        activity.overridePendingTransition(R.anim.activity_in,R.anim.activity_out);
    }

    public static String getEmail() {
        return Prefs.getString(Constants.PREF_EMAIL,"");
    }
    public static String getToken() {
        return Prefs.getString(Constants.PREF_TOKEN,"");
    }
}
