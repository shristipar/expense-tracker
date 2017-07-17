package com.trackexpense.snapbill.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ViewAnimator;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.pixplicity.easyprefs.library.Prefs;
import com.trackexpense.snapbill.Config;
import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.network.response.Response;
import com.trackexpense.snapbill.model.User;
import com.trackexpense.snapbill.network.request.Request;
import com.trackexpense.snapbill.network.request.UserRequest;
import com.trackexpense.snapbill.ui.EditTextWithFont;
import com.trackexpense.snapbill.ui.ProgressButton;
import com.trackexpense.snapbill.utils.Constants;
import com.trackexpense.snapbill.utils.GUIUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.trackexpense.snapbill.activity.RegisterActivity.State.ACTIVATION;
import static com.trackexpense.snapbill.activity.RegisterActivity.State.REGISTER;
import static com.trackexpense.snapbill.utils.Validation.isEmailValid;
import static com.trackexpense.snapbill.utils.Validation.isEmpty;
import static com.trackexpense.snapbill.utils.Validation.isPasswordValid;
import static com.trackexpense.snapbill.utils.Validation.isTokenValid;



/**
 * Created by Johev on 02-05-2017.
 */

public class RegisterActivity extends AppCompatActivity implements Request.OnResponseListener<Response> {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    CallbackManager callbackManager;

    @BindView(R.id.input_email)
    protected EditTextWithFont inputEmail;
    @BindView(R.id.input_pass)
    protected EditTextWithFont inputPass;
    @BindView(R.id.input_repass)
    protected EditTextWithFont inputRepass;
    @BindView(R.id.input_token)
    protected EditTextWithFont inputToken;

    @BindView(R.id.layout_email)
    protected View layoutEmail;
    @BindView(R.id.layout_pass)
    protected View layoutPass;
    @BindView(R.id.layout_repass)
    protected View layoutRepass;
    @BindView(R.id.layout_token)
    protected View layoutToken;

    @BindView(R.id.title_switcher)
    ViewAnimator titleSwitcher;

    @BindView(R.id.change_button)
    ViewAnimator changeButton;

    @BindView(R.id.submit_button)
    ProgressButton submitButton;

    @BindColor(R.color.bg_medium_sea_green)
    int colorBgMediumSeaGreen;
    @BindColor(R.color.bg_urobilin)
    int colorBgUrobilin;
    @BindColor(R.color.bg_fiery_rose)
    int colorBgFieryRose;

    @BindView(R.id.root_view)
    protected View rootView;

    @BindView(R.id.login_button)
    protected LoginButton loginButton;

    private CompositeSubscription mSubscriptions;

    State currState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        mSubscriptions = new CompositeSubscription();

        initState(State.LOGIN);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void initState(State state) {
        currState = state;

        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.e(TAG, Profile.getCurrentProfile().getFirstName() + "\n"
                        + Profile.getCurrentProfile().getId() + "\n"
                        + Profile.getCurrentProfile().getLinkUri() + "\n"
                        + Profile.getCurrentProfile().getProfilePictureUri(90, 90) + "\n");



                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    String email = object.getString("email");

                                    Log.e("TAG", email);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "cancel");

                Config.showToastMessage(RegisterActivity.this, "cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "onerror");
                Config.showToastMessage(RegisterActivity.this, "onerror");
            }
        });

        switch (currState) {
            case REGISTER:
                layoutToken.setVisibility(View.GONE);
                break;
            case LOGIN:
                rootView.setBackgroundColor(colorBgUrobilin);

                layoutRepass.setVisibility(View.GONE);
                layoutToken.setVisibility(View.GONE);

                titleSwitcher.setDisplayedChild(1);
                changeButton.setDisplayedChild(1);

                submitButton.setTextColor(colorBgUrobilin);
                break;
            case ACTIVATION:
                rootView.setBackgroundColor(colorBgMediumSeaGreen);

                layoutEmail.setVisibility(View.GONE);
                layoutPass.setVisibility(View.GONE);
                layoutRepass.setVisibility(View.GONE);
                layoutToken.setVisibility(View.VISIBLE);

                titleSwitcher.setDisplayedChild(2);
                changeButton.setDisplayedChild(2);

                submitButton.setTextColor(colorBgFieryRose);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager != null)
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.submit_button)
    protected void onSubmit() {
        if (loading)
            return;

        clearError();

        if (currState == ACTIVATION) {
            String email = inputEmail.getText().toString().trim();
            String token = inputToken.getText().toString().trim();

            if (isEmpty(token)) {
                GUIUtils.showInputError(inputToken, "should not be empty");
            } else if (!isTokenValid(token)) {
                GUIUtils.showInputError(inputToken, "Sorry, its invalid");
            }

            activate(email, token);
        } else {
            String email = inputEmail.getText().toString().trim();
            String pass = inputPass.getText().toString().trim();
            String repass = inputRepass.getText().toString().trim();

            if (isEmpty(email)) {
                GUIUtils.showInputError(inputEmail, "should not be empty");
            } else if (!isEmailValid(email)) {
                GUIUtils.showInputError(inputEmail, "doesn't look valid");
            } else if (isEmpty(pass)) {
                GUIUtils.showInputError(inputPass, "should not be empty");
            } else if (!isPasswordValid(pass)) {
                GUIUtils.showInputError(inputPass, "must be within 6-60 chars");
            }
            //register
            else if (currState == REGISTER) {
                if (!repass.equals(pass)) {
                    GUIUtils.showInputError(inputRepass, "Password doesn't match");
                } else {
                    User user = new User();
                    user.setName(email);
                    user.setEmail(email);
                    user.setPassword(pass);

                    register(user);
                }
            }
            //login
            else {
                login(email, pass);
            }
        }
    }

    private void register(User user) {
        startProgress();
        UserRequest.register(user).call(this,this);
    }

    private void activate(String email, String token) {
        startProgress();
        UserRequest.activate(email,token).call(this,this);
    }

    private void login(String email, String pass) {
        startProgress();
        UserRequest.login(email,pass).call(this,this);
    }

    private boolean loading = false;

    private void startProgress() {
        loading = true;
        submitButton.startProgress();
        changeButton.setVisibility(View.INVISIBLE);
    }

    private void stopProgress() {
        changeButton.setVisibility(View.VISIBLE);
        submitButton.stopProgress();
        loading = false;
    }

    @Override
    public void onSuccess(Response response) {
        stopProgress();
        switch (currState) {
            case REGISTER:
                Config.showToastMessage(this, response.getMessage());
                setActivationState();
                break;
            case LOGIN:
            case ACTIVATION:
                Prefs.putString(Constants.PREF_EMAIL, response.getMessage());
                Prefs.putString(Constants.PREF_TOKEN, response.getToken());

                Config.showToastMessage(this, "Welcome\n"
                        + Prefs.getString(Constants.PREF_EMAIL,"")
                        + Prefs.getString(Constants.PREF_TOKEN, ""));
                Config.startLandingActivity(this);
        }
    }

    @Override
    public void onError(String err) {
        stopProgress();
        GUIUtils.showSnackBar(submitButton,err);
    }

    @OnClick(R.id.change_button)
    protected void toggleState() {
        if (currState == ACTIVATION)
            return;

        clearError();
        clearValues();

        titleSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        titleSwitcher.setInAnimation(this, R.anim.fade_in);

        changeButton.setOutAnimation(this, android.R.anim.fade_out);
        changeButton.setInAnimation(this, R.anim.fade_in);

        currState = currState.toggle();

        switch (currState) {
            case LOGIN:
                GUIUtils.animateColorOfView(rootView, "backgroundColor",
                        colorBgMediumSeaGreen, colorBgUrobilin, 1000);

                layoutRepass.setVisibility(View.GONE);

                titleSwitcher.showNext();
                changeButton.showNext();
                submitButton.animateTextColor(colorBgUrobilin, 1000);
                break;
            case REGISTER:
                GUIUtils.animateColorOfView(rootView, "backgroundColor",
                        colorBgUrobilin, colorBgMediumSeaGreen, 1000);

                layoutRepass.setVisibility(View.VISIBLE);

                titleSwitcher.showPrevious();
                changeButton.showPrevious();
                submitButton.animateTextColor(colorBgMediumSeaGreen, 1000);
        }
    }

    private void setActivationState() {
        GUIUtils.animateColorOfView(rootView, "backgroundColor",
                currState == REGISTER ? colorBgMediumSeaGreen : colorBgUrobilin,
                colorBgFieryRose, 1000);

        layoutEmail.setVisibility(View.GONE);
        layoutPass.setVisibility(View.GONE);
        layoutRepass.setVisibility(View.GONE);
        layoutToken.setVisibility(View.VISIBLE);

        titleSwitcher.setDisplayedChild(2);
        changeButton.setDisplayedChild(2);

        submitButton.animateTextColor(colorBgFieryRose, 1000);

        inputToken.requestFocus();

        currState = ACTIVATION;
    }

    private void clearError() {
        inputEmail.setError(null);
        inputPass.setError(null);
        inputRepass.setError(null);
    }

    private void clearValues() {
        inputEmail.setText("");
        inputPass.setText("");
        inputRepass.setText("");
    }

    enum State {
        REGISTER,
        LOGIN,
        ACTIVATION;

        public State toggle() {
            return this == REGISTER ? LOGIN : REGISTER;
        }
    }
}
