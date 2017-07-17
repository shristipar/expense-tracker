package com.trackexpense.snapbill.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.cache.CurrencyCache;
import com.trackexpense.snapbill.ui.TextViewWithFont;
import com.trackexpense.snapbill.utils.GUIUtils;

import java.util.Currency;

/**
 * Created by lenovo on 22-Apr-17.
 */

public class CurrencyRecyclerViewAdapter extends RecyclerViewAdapter<Currency, CurrencyRecyclerViewAdapter.CurrencyRecyclerViewHolders> {

    private final static String TAG = CurrencyRecyclerViewAdapter.class.getSimpleName();

    public CurrencyRecyclerViewAdapter(Context context) {
        super(context, CurrencyCache.getCurrenciesSortedByCountry());
    }

    @Override
    public CurrencyRecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        int itemLayoutId;
        switch (viewType) {
            case 1:
                itemLayoutId = R.layout.card_currency_view_alt;
                break;
            case 0:
            default:
                itemLayoutId = R.layout.card_currency_view;
                break;
        }

        View layoutView = LayoutInflater.from(parent.getContext())
                .inflate(itemLayoutId, parent, false);
        return new CurrencyRecyclerViewHolders(layoutView);
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).equals(CurrencyCache.getSelectedCurrency()) ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(CurrencyRecyclerViewHolders holder, int position) {
        holder.currencySymbol.setText(itemList.get(position).getSymbol());
        holder.currencyDisplayName.setText(itemList.get(position).getDisplayName());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    public class CurrencyRecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextViewWithFont currencySymbol;
        public TextViewWithFont currencyDisplayName;
        public View rootView;
//        public CardView rootCardView;

        public CurrencyRecyclerViewHolders(View itemView) {
            super(itemView);

            rootView = itemView;

            currencySymbol = GUIUtils.getView(itemView, R.id.currency_symbol);
            currencyDisplayName = GUIUtils.getView(itemView, R.id.currency_display_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            setSelectedIndex(getAdapterPosition());
        }
    }
}
