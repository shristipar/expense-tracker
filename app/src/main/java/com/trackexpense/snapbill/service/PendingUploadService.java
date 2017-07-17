package com.trackexpense.snapbill.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.trackexpense.snapbill.model.Bill;

import java.util.List;

/**
 * Created by sjjhohe on 17-Jun-17.
 */

public class PendingUploadService extends IntentService {

    public PendingUploadService() {
        super("PendingUploadService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PendingUploadService(String name) {
        super(name);
    }

    private static boolean isRunning = false;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (isRunning)
            return;

        isRunning = true;
        Bundle extras = intent.getExtras();
        boolean isNetworkConnected = extras.getBoolean("isNetworkConnected");
        if (isNetworkConnected) {
            uploadPendingBills(getApplicationContext());
        }
    }

    private void uploadPendingBills(Context context) {
        List<Bill> bills = Bill.getPending();
        for (final Bill bill : bills) {
            bill.sync(context);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }
}
