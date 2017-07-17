package com.trackexpense.snapbill.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.adapter.BillsRecyclerViewAdapter;
import com.trackexpense.snapbill.cache.FontCache;
import com.trackexpense.snapbill.model.Bill;
import com.trackexpense.snapbill.model.BillAction;
import com.trackexpense.snapbill.network.request.BillRequest;
import com.trackexpense.snapbill.network.request.Request;
import com.trackexpense.snapbill.network.response.BillsResponse;
import com.trackexpense.snapbill.utils.GUIUtils;
import com.trackexpense.snapbill.utils.Utils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by sjjhohe on 05-Jun-17.
 */

public class AllBillsActivity extends AppCompatActivity {

    @BindView(R.id.bill_recycler_view)
    RecyclerView billRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;

    List<Bill> adapterBills;

    private final String TAG = AllBillsActivity.class.getSimpleName();

    private BillsRecyclerViewAdapter adapter;

    private final static int EDIT_REQUEST_CODE = 101;
    private static Bill oldBill;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_bills_coordinate);
        ButterKnife.bind(this);

        initLayout();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                getBills();
            }
        });
    }

    private void initLayout() {
        initCoordinateLayout();
        initRecyclerView();
    }

    private void initCoordinateLayout() {
        toolbar.setTitle("Your Bills");

        Typeface font = FontCache.get("moon-bold", AllBillsActivity.this);
        collapsingToolbarLayout.setCollapsedTitleTypeface(font);
        collapsingToolbarLayout.setExpandedTitleTypeface(font);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setElevation(0);
    }

    //    @Background
    private void getBills() {
        List<Bill> bills = Bill.getAll();
        if (Utils.isListEmpty(bills)) {
            requestForAllBills();
        } else {
            showBillsAtTop(bills);
        }
    }

    private void requestForAllBills() {
        adapterBills.clear();
        BillRequest.getAll()
                .call(this, new Request.OnResponseListener<BillsResponse>() {
                    @Override
                    public void onSuccess(BillsResponse response) {
                        processResponse(response);
                    }

                    @Override
                    public void onError(String err) {
                        Log.e(TAG, err);
                        GUIUtils.showSnackBar(toolbar, err);
                    }
                });
    }

    /*private void requestForAllBillsAfter(Date createdAt) {
        BillRequest.getAfter(createdAt)
                .call(this, new Request.OnResponseListener<BillsResponse>() {
                    @Override
                    public void onSuccess(BillsResponse response) {
                        processResponse(response);
                    }

                    @Override
                    public void onError(String err) {

                    }
                });
    }*/

    private void processResponse(BillsResponse response) {
        List<Bill> bills = response.getBills();
        for (Bill bill : bills) {
            bill.setPending(false);
            bill.save();
        }

        showBillsAtTop(bills);
    }

    private void showBillsAtTop(List<Bill> bills) {
        adapterBills.addAll(0, bills);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void initRecyclerView() {
        adapterBills = new ArrayList<>();
        adapter = new BillsRecyclerViewAdapter(getApplicationContext(), adapterBills);
//        CategoryRecyclerViewAdapter adapter = new CategoryRecyclerViewAdapter(getApplicationContext());
        adapter.setOnItemSelectedListener(new BillsRecyclerViewAdapter.OnItemSelectedListener<Bill>() {
            @Override
            public void selected(Bill selectedItem, int index) {

            }

            @Override
            public void hold(Bill holdItem, int index) {
                showEditDeleteDialog(holdItem,index);
//                GUIUtils.showSnackBar(toolbar, Utils.getInputDateFormat(holdItem.getCreatedAt()));
            }
        });

        billRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        billRecyclerView.setHasFixedSize(true);
        billRecyclerView.setAdapter(adapter);
    }

    private void showEditDeleteDialog(final Bill selectedBill, final int index) {
        String[] options = new String[]{"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog_Theme);
//        builder.setTitle("You want to?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        goToEditBill(selectedBill);
                        break;
                    case 1:
                        deleteBill(selectedBill);
                        break;
                }
            }
        });
        builder.show();
    }

    private void deleteBill(Bill selectedBill) {
        selectedBill.delete(this);
        adapter.removed(selectedBill);
    }

    private void goToEditBill(Bill selectedBill) {
        oldBill = selectedBill;
        Intent intent = new Intent(getApplicationContext(), ReviewBillActivity.class);
        intent.putExtra(ReviewBillActivity.EXTRA_BILL_PARCEL, Parcels.wrap(selectedBill));
        intent.putExtra(ReviewBillActivity.EXTRA_BILL_ACTION, BillAction.EDIT);
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bill bill = Parcels.unwrap(data.getParcelableExtra(ReviewBillActivity.EXTRA_BILL_PARCEL));
                    if (!Utils.isNull(bill)) {
                        adapter.updated(oldBill,bill);
                    }
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}