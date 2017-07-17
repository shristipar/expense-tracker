package com.trackexpense.snapbill.network.request;

import android.content.Context;

import com.trackexpense.snapbill.network.RetrofitFactory;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by sjjhohe on 12-Jun-17.
 */

public class Request {
    public final static CompositeSubscription subscriptions;

    static {
        subscriptions = new CompositeSubscription();
    }

    private Observable observable;

    public Request(Observable observable) {
        this.observable = observable;
    }

    public static <T> void call(Observable<T> observable, Action1 actionOnSuccess, Action1 actionOnError) {
        subscriptions.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(actionOnSuccess, actionOnError));
    }

    public void call(Context context, OnResponseListener responseListener) {
        subscriptions.add(observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(Request.getActionOnSuccess(responseListener),
                        Request.getActionOnError(context, responseListener)));
    }

    private static <T> Action1<T> getActionOnSuccess(final OnResponseListener responseListener) {
        return new Action1<T>() {
            @Override
            public void call(T response) {
                responseListener.onSuccess(response);
            }
        };
    }

    private static Action1 getActionOnError(final Context context, final OnResponseListener onResponseListener) {
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                RetrofitFactory.handleError(throwable, context, onResponseListener);
            }
        };
    }

    public interface OnResponseListener<T> {
        void onSuccess(T response);

        void onError(String err);
    }
}
