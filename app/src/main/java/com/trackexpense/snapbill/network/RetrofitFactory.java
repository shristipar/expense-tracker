package com.trackexpense.snapbill.network;

import android.content.Context;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trackexpense.snapbill.Config;
import com.trackexpense.snapbill.network.routes.UserRoutes;
import com.trackexpense.snapbill.utils.Constants;
import com.trackexpense.snapbill.utils.Utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;


/**
 * Created by Johev on 02-05-2017.
 */

public class RetrofitFactory {

    private static final String TAG = RetrofitFactory.class.getSimpleName();

    public static UserRoutes getRetrofit() {

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        return new Retrofit.Builder()
                .baseUrl(Config.BASE_URL)
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory
                        .create(getGson()))
                .build()
                .create(UserRoutes.class);

    }

    public static UserRoutes getRetrofit(String email, String password) {

        String credentials = email + ":" + password;
        final String basic = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder()
                        .addHeader("Authorization", basic)
                        .method(original.method(), original.body());

                return chain.proceed(builder.build());
            }
        });


        return getRetrofit(UserRoutes.class, httpClient);
    }

    /**
     * Retrofit interface used for token authorisation
     *
     * @param interfaceClass
     * @param token
     * @param <T>
     * @return
     */
    public static <T> T getRetrofit(Class<T> interfaceClass, final String token) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder()
                        .addHeader("x-access-token", token)
                        .method(original.method(), original.body());
                return chain.proceed(builder.build());
            }
        });

        return getRetrofit(interfaceClass, httpClient);
    }

    /**
     * Common Helper method used to create given retrofit interface with given HttpClient
     *
     * @param interfaceClass
     * @param httpClient
     * @param <T>
     * @return
     */
    private static <T> T getRetrofit(Class<T> interfaceClass, OkHttpClient.Builder httpClient) {
        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        return new Retrofit.Builder()
                .baseUrl(Config.BASE_URL)
                .client(httpClient.build())
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory
                        .create(getGson()))
                .build()
                .create(interfaceClass);
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
    }

    public static boolean isNetworkError(Throwable error) {
        return !(error instanceof HttpException);
    }

    public static String handleDefaultErrors(Context context, Throwable error) {
        String msg = Utils.getHttpErrorMessage(error);
        switch (msg) {
            case Constants.ERR_INVALID_TOKEN:
                Config.startLoginActivity(context);
                return null;
        }

        return msg;
    }

    public static void handleError(Throwable throwable, Context context, com.trackexpense.snapbill.network.request.Request.OnResponseListener onResponseListener) {
        // handle default cases
        if (throwable instanceof HttpException) {
            String err = handleDefaultErrors(context, throwable);
            if (err != null) {
                // pass to response listener if not handled
                onResponseListener.onError(err);
            }
        } else {
            onResponseListener.onError("Network Error");
        }
    }

}