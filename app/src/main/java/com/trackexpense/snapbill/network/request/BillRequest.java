package com.trackexpense.snapbill.network.request;

import android.support.annotation.NonNull;

import com.pixplicity.easyprefs.library.Prefs;
import com.trackexpense.snapbill.model.Bill;
import com.trackexpense.snapbill.network.RetrofitFactory;
import com.trackexpense.snapbill.network.routes.BillRoutes;
import com.trackexpense.snapbill.utils.Constants;
import com.trackexpense.snapbill.utils.Utils;

import java.util.Date;

import rx.Observable;

/**
 * Created by sjjhohe on 12-Jun-17.
 */

public class BillRequest extends Request {
    private static final String TAG = BillRequest.class.getSimpleName();
    private static String email;
    private static String token;
    private static BillRoutes routes;

    static {
        email = Prefs.getString(Constants.PREF_EMAIL, "");
        token = Prefs.getString(Constants.PREF_TOKEN, "");
        routes = RetrofitFactory.getRetrofit(BillRoutes.class, token);
    }

    public BillRequest(Observable observable) {
        super(observable);
    }

    public static Request getAll() {
        return new BillRequest(routes.getAll(email));
    }

    public static Request add(@NonNull Bill bill) {
        return new BillRequest(routes.add(email, bill));
    }

    public static Request getAfter(@NonNull Date date) {
        return new BillRequest(routes.getAfter(email, date));
    }

    public static Request delete(@NonNull Bill bill) {
        return new BillRequest(routes.delete(email, bill.getBillId()));
    }

    public static Request edit(Bill bill) {
        Observable observable;
        if (Utils.isNull(bill.getId())) {
            observable = routes.add(email, bill);
        } else {
            observable = routes.edit(email, bill.getBillId(), bill);
        }
        return new BillRequest(observable);
    }
}
