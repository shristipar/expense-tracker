package com.trackexpense.snapbill.model;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.query.Select;
import com.trackexpense.snapbill.cache.CurrencyCache;
import com.trackexpense.snapbill.network.request.BillRequest;
import com.trackexpense.snapbill.network.request.Request;
import com.trackexpense.snapbill.network.response.Response;
import com.trackexpense.snapbill.utils.Category;
import com.trackexpense.snapbill.utils.Utils;

import org.parceler.Parcel;

import java.util.Currency;
import java.util.Date;
import java.util.List;

/**
 * Created by Shristi on 26-04-2017.
 * <p>
 * used by Parceler, Retrofit
 */
@Parcel
public class Bill extends SugarRecord<Bill> {
    @SerializedName("_id")
    private String billId;
    @SerializedName("created_at")
    private Date createdAt;
    private String merchant;
    private Double amount;
    private String currency;
    private Date date;
    private Category category;
    private boolean pending;
    private BillAction billAction;

//    private static String TAG = Bill.class.getSimpleName();

    public Bill() {
        this.merchant = "";
        this.amount = null;
        this.currency = CurrencyCache.getSelectedCurrency().getCurrencyCode();
        this.date = new Date();
        this.category = Category.getDefault();
        this.createdAt = new Date();
        this.pending = false;
        this.billAction = BillAction.ADD;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getCurrency() {
        return currency;
    }

    public Currency getCurrencyInstance() {
        return CurrencyCache.fromCurrencyCode(this.getCurrency());
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getDate() {
        return date;
    }

    public String getDateFormatted() {
        return Utils.getInputDateFormat(this.getDate());
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAmountFormatted() {
        return CurrencyCache.format(this.getCurrencyInstance(),this.getAmount());
    }
    public String getAmountFormattedWithCurrency() {
        if (this.amount == null)
            return null;
        else {
            return this.getCurrencyInstance().getSymbol()
                    + " " + this.getAmountFormatted();
        }
    }

    public int getCategoryImageResId() {
        return category.getImageResId();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public void add(Context context) {
        this.setPending(true);
        this.setBillAction(BillAction.ADD);
        this.save();
        sync(context);
    }

    public void edit(Context context) {
        if(this.isPending() && this.getBillAction() != BillAction.ADD) {
            this.setBillAction(BillAction.EDIT);
        }
        this.setPending(true);
        this.save();
        sync(context);
    }

    public void delete(Context context) {
        // delete directly from local db if
        // its still pending for ADD in server
        if (this.isPending() && this.getBillAction() == BillAction.ADD) {
            this.delete();
        } else {
            this.setBillAction(BillAction.DELETE);
            this.setPending(true);
            this.save();
            this.sync(context);
        }
    }

    public void sync(Context context) {
        final Bill self = this;
        Request request;
        switch (this.getBillAction()) {
            case ADD:
                request = BillRequest.add(this);
                break;
            case EDIT:
                request = BillRequest.edit(this);
                break;
            case DELETE:
                request = BillRequest.delete(this);
                break;
            default:
                throw new IllegalStateException("Cannot sync unsupported Bill Action");
        }

        request.call(context, new Request.OnResponseListener<Response>() {
            @Override
            public void onSuccess(Response response) {
                self.setPending(false);
                switch (self.getBillAction()) {
                    case ADD:
                        self.setBillId(response.getMessage());
                        self.save();
                        break;
                    case DELETE:
                        self.delete();
                }
            }

            @Override
            public void onError(String err) {
                self.setPending(true);
                self.save();
            }
        });
    }

    public void syncAdd(Context context) {
        final Bill self = this;
        BillRequest.add(this).call(context, new Request.OnResponseListener<Response>() {
            @Override
            public void onSuccess(Response response) {
                self.setPending(false);
                self.setBillId(response.getMessage());
                self.save();
            }

            @Override
            public void onError(String err) {
                self.setPending(true);
                self.save();
            }
        });
    }

    private void syncDelete(Context context) {
        final Bill self = this;
        BillRequest.delete(this).call(context, new Request.OnResponseListener() {
            @Override
            public void onSuccess(Object response) {
                self.delete();
            }

            @Override
            public void onError(String err) {
//                self.setToBeDeleted(true);
                self.save();
            }
        });
    }

    public static List<Bill> getAll() {
        List<Bill> bills = Select.from(Bill.class)
                .orderBy("date desc").list();
        return bills;
    }

    public static List<Bill> getPending() {
        List<Bill> records = SugarRecord.find(Bill.class,
                "pending = ?", "1");
        return records;
    }

    public BillAction getBillAction() {
        return billAction;
    }

    public void setBillAction(BillAction billAction) {
        this.billAction = billAction;
    }
}