package com.trackexpense.snapbill.network.request;

import android.support.annotation.NonNull;

import com.trackexpense.snapbill.model.User;
import com.trackexpense.snapbill.network.RetrofitFactory;
import com.trackexpense.snapbill.network.routes.UserRoutes;

import rx.Observable;

/**
 * Created by sjjhohe on 12-Jun-17.
 */

public class UserRequest extends Request {
    private static final String TAG = UserRequest.class.getSimpleName();

    public UserRequest(Observable observable) {
        super(observable);
    }

    public static Request register(@NonNull User user) {
        return new UserRequest(RetrofitFactory
                        .getRetrofit()
                        .register(user));
    }

    public static Request login(@NonNull String email, @NonNull String password) {
        return new UserRequest(RetrofitFactory
                .getRetrofit(email, password)
                .login());
    }

    public static Request activate(@NonNull String email, @NonNull String token) {
        return new UserRequest(RetrofitFactory
                .getRetrofit()
                .activate(email,token));
    }

    public static Request isAuthorized(@NonNull String email, @NonNull String token) {
        return new UserRequest(RetrofitFactory
                .getRetrofit(UserRoutes.class,token)
                .isAuthorized(email));
    }
}
