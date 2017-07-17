package com.trackexpense.snapbill.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.model.Bill;
import com.trackexpense.snapbill.utils.GUIUtils;
import com.trackexpense.snapbill.utils.Res;
import com.trackexpense.snapbill.utils.Utils;

import java.util.List;

/**
 * Created by lenovo on 22-Apr-17.
 */

public class BillsRecyclerViewAdapter extends RecyclerViewAdapter<Bill, BillsRecyclerViewAdapter.RecyclerViewHolders> {

    private final static String TAG = BillsRecyclerViewAdapter.class.getSimpleName();

    private final static int altColor = Res.getColor(R.color.black_overlay);

    public BillsRecyclerViewAdapter(Context context, List<Bill> itemList) {
        super(context,itemList);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResId = R.layout.card_bill_view;
        View layoutView = LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false);
        return new RecyclerViewHolders(layoutView);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        Bill bill = itemList.get(position);
        holder.categoryImageView.setImageDrawable(Res.getDrawable(bill.getCategoryImageResId()));
        holder.merchantTextView.setText(bill.getMerchant());
        holder.dateTextView.setText(Utils.getInputDateFormat(bill.getDate()));
        holder.amountTextView.setText(bill.getAmountFormattedWithCurrency());
//        if (position % 2 != 0) {
//            holder.setAlternate();
//        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    protected class RecyclerViewHolders extends RecyclerView.ViewHolder
            implements View.OnClickListener,View.OnLongClickListener {

        public ImageView categoryImageView;
        public TextView merchantTextView;
        public TextView dateTextView;
        public TextView amountTextView;
        public CardView rootCardView;
        public View rootView;

        public RecyclerViewHolders(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            rootView = itemView;
            rootCardView = GUIUtils.getView(itemView,R.id.root_card_view);
            categoryImageView = GUIUtils.getView(itemView,R.id.category);
            merchantTextView = GUIUtils.getView(itemView,R.id.merchant);
            dateTextView = GUIUtils.getView(itemView,R.id.date);
            amountTextView = GUIUtils.getView(itemView,R.id.amount);
        }

        @Override
        public boolean onLongClick(View v) {
            if(itemSelectedListener!=null) {
                itemSelectedListener.hold(itemList.get(getAdapterPosition()),getAdapterPosition());
            }
            return false;
        }

        @Override
        public void onClick(View view) {
            setSelectedIndex(getAdapterPosition());
        }

        public void setAlternate() {
            if (rootCardView != null)
                rootCardView.setCardBackgroundColor(altColor);
        }
    }
}
