package com.trackexpense.snapbill.activity;

import android.app.Activity;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.pixplicity.easyprefs.library.Prefs;
import com.trackexpense.snapbill.Config;
import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.cache.CurrencyCache;
import com.trackexpense.snapbill.network.response.Response;
import com.trackexpense.snapbill.network.request.Request;
import com.trackexpense.snapbill.network.request.UserRequest;
import com.trackexpense.snapbill.utils.Constants;
import com.trackexpense.snapbill.utils.Res;

import rx.subscriptions.CompositeSubscription;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import static com.trackexpense.snapbill.utils.Validation.isEmpty;

/**
 * Created by sjjhohe on 13-May-17.
 */

public class SplashActivity extends Activity implements Request.OnResponseListener<Response> {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private CompositeSubscription mSubscriptions;

    private static boolean appClosed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCalligraphy();
        initPrefs();
        initRes();
        initCurrencyCache();

        mSubscriptions = new CompositeSubscription();
        checkLogin();
    }

    private void initCurrencyCache() {
        CurrencyCache.setContext(this);
    }

    private void initRes() {
        Res.setContext(this);
    }

    private void initPrefs() {
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(Config.getDefaultSharedPreferencesName(getApplicationContext()))
                .setUseDefaultSharedPreference(true)
                .build();
    }

    private void initCalligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    private void checkLogin() {
        if (Prefs.contains(Constants.PREF_EMAIL) &&
                Prefs.contains(Constants.PREF_TOKEN)) {
            String email = Prefs.getString(Constants.PREF_EMAIL, "");
            String token = Prefs.getString(Constants.PREF_TOKEN, "");
            if (!isEmpty(email) || !isEmpty(token)) {
                UserRequest.isAuthorized(email, token).call(this, this);
            } else {
//                Config.startLandingActivity(this);
                Config.startLoginActivity(this);
            }
        } else {
//            Config.startLandingActivity(this);
            Config.startLoginActivity(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        appClosed = true;
    }

    @Override
    public void onSuccess(Response response) {
        if (!appClosed) {
            Prefs.putString(Constants.PREF_NAME, response.getMessage());
            Prefs.putString(Constants.PREF_EMAIL, response.getMessage());
            Prefs.putString(Constants.PREF_TOKEN, response.getToken());

            Config.showToastMessage(this, "Welcome\n" + Prefs.getString(Constants.PREF_TOKEN, ""));
            Config.startLandingActivity(this);
        }
    }

    @Override
    public void onError(String err) {
        Config.showToastMessage(this, err);

        boolean isLoggedIn = Prefs.getBoolean(Constants.PREF_LOGGED_IN,false);
        if(isLoggedIn) {
            Config.startLandingActivity(this);
        } else {
            Config.startLoginActivity(this);
        }
    }
}
