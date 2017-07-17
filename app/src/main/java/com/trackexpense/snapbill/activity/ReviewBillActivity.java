package com.trackexpense.snapbill.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.adapter.CategoryRecyclerViewAdapter;
import com.trackexpense.snapbill.adapter.CurrencyRecyclerViewAdapter;
import com.trackexpense.snapbill.adapter.RecyclerViewAdapter;
import com.trackexpense.snapbill.cache.CurrencyCache;
import com.trackexpense.snapbill.model.Bill;
import com.trackexpense.snapbill.model.BillAction;
import com.trackexpense.snapbill.ui.ProgressButton;
import com.trackexpense.snapbill.utils.Category;
import com.trackexpense.snapbill.utils.GUIUtils;
import com.trackexpense.snapbill.utils.Res;
import com.trackexpense.snapbill.utils.Utils;
import com.trackexpense.snapbill.utils.Validation;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.parceler.Parcels;

import java.util.Calendar;
import java.util.Currency;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Johev on 22-04-2017.
 */

public class ReviewBillActivity extends AppCompatActivity {

    public static final String EXTRA_BILL_ACTION = "extra-bill-actionTitle";
    public static final String EXTRA_BILL_PARCEL = "extra-bill";

    @BindView(R.id.bill_action)
    TextView billActionTextView;
    @BindView(R.id.input_merchant)
    EditText inputMerchant;
    @BindView(R.id.input_currency)
    TextView inputCurrency;
    @BindView(R.id.input_amount)
    EditText inputAmount;
    @BindView(R.id.layout_date)
    View layoutDate;
    @BindView(R.id.input_date)
    EditText inputDate;
    @BindView(R.id.input_category)
    EditText inputCategory;

    @BindView(R.id.save_button)
    ProgressButton saveButton;

    private CompositeSubscription mSubscriptions;

    private Bill bill;
    private BillAction billAction;
    private String actionTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_bill);
        ButterKnife.bind(this);

        mSubscriptions = new CompositeSubscription();
        checkExtras();
        initLayout();
    }

    private void checkExtras() {
        //create new default bill if not passed in intent
        this.bill = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_BILL_PARCEL));
        if (this.bill == null) {
            this.actionTitle = "New";
            this.bill = new Bill();
            billAction = BillAction.ADD;
        } else {
            billAction = (BillAction) getIntent().getSerializableExtra(EXTRA_BILL_ACTION);
            switch (billAction) {
                case EDIT:
                    this.actionTitle = "Edit";
                    break;
                case ADD:
                default:
                    billAction = BillAction.ADD;
                    this.actionTitle = "Review";
                    break;
            }
        }
    }

    private void initBillFields() {
        // merchant
        inputMerchant.setText(this.bill.getMerchant());
        // currency
        if (this.bill.getCurrencyInstance() != null)
            inputCurrency.setText(this.bill.getCurrencyInstance().getSymbol());
        // amount
        if (this.bill.getAmount() != null)
            inputAmount.setText(this.bill.getAmountFormatted());
        // date
        inputDate.setText(this.bill.getDateFormatted());
        // category
        inputCategory.setText(this.bill.getCategory().name());
    }

    private void initLayout() {
        initBillFields();
        billActionTextView.setText(this.actionTitle);

        inputMerchant.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    openAmountDialog(true);
                    return true;
                }
                return false;
            }
        });
        //onTabPressed on input_amount, open DatePickerDialog
        inputAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    openDatePicker(true);
                    return true;
                }
                return false;
            }
        });

        // if bill actionTitle - new, show keyboard on activity start
        if (this.bill.getBillAction() == BillAction.ADD) {
            showKeyboard(inputMerchant);
        }
    }

    @OnClick(R.id.input_amount)
    protected void onInputAmountClicked() {
        openAmountDialog(false);
    }

    private void openAmountDialog(boolean openDate) {
        String amount = this.bill.getAmount() == null ? "" : this.bill.getAmount().toString();
        showDialogInputConfirm(amount, "Amount", openDate);
    }

    private void showDialogInputConfirm(final String text, final String title, final boolean openDate) {
        final Dialog inputConfirmDialog = new Dialog(this, R.style.DialogSlideAnim);
        inputConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inputConfirmDialog.setCanceledOnTouchOutside(false);
        inputConfirmDialog.setContentView(R.layout.dialog_input);

        GUIUtils.roundedCornerDialog(inputConfirmDialog);

        TextView titleTextView = GUIUtils.getView(inputConfirmDialog, R.id.title_textview);
        titleTextView.setText(title);

        final EditText valueEditText = (EditText) inputConfirmDialog.findViewById(R.id.value_textview);
        valueEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        valueEditText.setText(text);

        ImageButton confirmButton = GUIUtils.getView(inputConfirmDialog, R.id.confirm_button);
        ImageButton clearButton = GUIUtils.getView(inputConfirmDialog, R.id.clear_button);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = valueEditText.getText().toString().trim();

                value = Utils.sanitizeAmount(value);
                if (!Validation.isNumber(value)) {
                    GUIUtils.showSnackBar(valueEditText, "Amount should be a number");
                    return;
                }

                setAmount(value);

                GUIUtils.hideKeyboard(ReviewBillActivity.this, valueEditText);
                inputConfirmDialog.dismiss();

                if (openDate)
                    openDatePicker(true);
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputConfirmDialog.dismiss();
            }
        });

        inputConfirmDialog.show();
        showKeyboard(valueEditText);
    }

    private void setAmount(String value) {
        this.bill.setAmount(Double.parseDouble(value));
        inputAmount.setText(this.bill.getAmountFormatted());
    }

    private void showKeyboard(final EditText editText) {
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                GUIUtils.showKeyboard(ReviewBillActivity.this, editText);
            }
        }, 500);
    }

    @OnClick(R.id.input_currency)
    protected void OnCurrencyClick() {
        final Dialog currencyDialog;
        currencyDialog = new Dialog(ReviewBillActivity.this);
        currencyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currencyDialog.setContentView(R.layout.dialog_currency);

        GUIUtils.roundedCornerDialog(currencyDialog);

        CurrencyRecyclerViewAdapter adapter = new CurrencyRecyclerViewAdapter(getApplicationContext());
        adapter.setSelectedItem(this.bill.getCurrencyInstance());
        adapter.setOnItemSelectedListener(new RecyclerViewAdapter.OnItemSelectedListener<Currency>() {
            @Override
            public void selected(Currency currency,int index) {
                CurrencyCache.setSelectedCurrency(currency);
                inputCurrency.setText(currency.getSymbol());
                bill.setCurrency(currency.getCurrencyCode());

                // to reformat amount on display
                if (!Utils.isNull(bill.getAmount())) {
                    setAmount(bill.getAmount().toString());
                }

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        currencyDialog.dismiss();
                    }
                });
            }

            @Override
            public void hold(Currency holdItem, int index) {

            }
        });

        RecyclerView currencyRecyclerView = GUIUtils.getView(currencyDialog, R.id.recycler_view);
        currencyRecyclerView.setHasFixedSize(true);
        currencyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        currencyRecyclerView.setAdapter(adapter);

        TextView titleTextView = (TextView) currencyDialog.findViewById(R.id.title_textview);
        titleTextView.setText("Currency");

        currencyDialog.show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @OnClick(R.id.input_date)
    protected void inputDateClicked() {
        openDatePicker(false);
    }

    protected void openDatePicker(final boolean openCategoryNext) {
        GUIUtils.hideKeyboard(this);

        // read bill date and set it as selected in datePickerDialog
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.bill.getDate());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        bill.setDate(calendar.getTime());
                        inputDate.setText(bill.getDateFormatted());
                    }
                }, year, month, day);
        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (openCategoryNext) {
                    openCategoryPicker();
                }
            }
        });
        datePickerDialog.setStyle(R.style.MyDatePickerStyle, 0);
        datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
    }

    @OnClick(R.id.input_category)
    protected void openCategoryPicker() {
        final Dialog categoryDialog = new Dialog(ReviewBillActivity.this);
        categoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        categoryDialog.setContentView(R.layout.dialog_currency);

        GUIUtils.roundedCornerDialog(categoryDialog);

        CategoryRecyclerViewAdapter adapter = new CategoryRecyclerViewAdapter(getApplicationContext());
        adapter.setSelectedItem(this.bill.getCategory());
        adapter.setOnItemSelectedListener(new RecyclerViewAdapter.OnItemSelectedListener<Category>() {
            @Override
            public void selected(Category category, int index) {
                bill.setCategory(category);
                inputCategory.setText(category.name());
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        categoryDialog.dismiss();
                    }
                });
            }

            @Override
            public void hold(Category holdItem, int index) {

            }
        });

        RecyclerView categoryRecyclerView = GUIUtils.getView(categoryDialog, R.id.recycler_view);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),
//                CategoryRecyclerViewAdapter.NO_OF_COLUMNS);
//        billRecyclerView.setLayoutManager(gridLayoutManager);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        categoryRecyclerView.setHasFixedSize(true);
        categoryRecyclerView.setAdapter(adapter);

        TextView titleTextView = GUIUtils.getView(categoryDialog, R.id.title_textview);
        titleTextView.setText("Category");

        categoryDialog.show();
    }

    @OnClick(R.id.save_button)
    protected void onSave() {
        //validate all fields
        String merchant = GUIUtils.getText(inputMerchant);
        String date = GUIUtils.getText(inputDate);
        String category = GUIUtils.getText(inputCategory);

        if (Validation.isEmpty(merchant)) {
            GUIUtils.showInputError(inputMerchant, Res.getString(R.string.error_empty_input));
        } else if (this.bill.getAmount() == null) {
            GUIUtils.showInputError(inputAmount, Res.getString(R.string.error_empty_input));
//        } else if (!Validation.isNumber(amount)) {
//            GUIUtils.showInputError(inputAmount, Res.getString(R.string.error_not_number));
        } else if (Validation.isEmpty(date)) {
            GUIUtils.showInputError(inputDate, Res.getString(R.string.error_empty_input));
        } else if (Validation.isEmpty(category)) {
            GUIUtils.showInputError(inputCategory, Res.getString(R.string.error_empty_input));
        } else {
            bill.setMerchant(merchant);

            save();
        }
    }

    private void save() {
        switch (billAction) {
            case ADD:
                bill.add(getApplicationContext());
                break;
            case EDIT:
                bill.edit(getApplicationContext());
                Intent data = new Intent();
                data.putExtra(EXTRA_BILL_PARCEL, Parcels.wrap(bill));
                setResult(RESULT_OK, data);
        }

        finish();
    }
}
