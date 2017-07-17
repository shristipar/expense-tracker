package com.trackexpense.snapbill.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.utils.Category;
import com.trackexpense.snapbill.utils.GUIUtils;
import com.trackexpense.snapbill.utils.Res;

import java.util.Arrays;

/**
 * Created by lenovo on 22-Apr-17.
 */

public class CategoryRecyclerViewAdapter extends RecyclerViewAdapter<Category, CategoryRecyclerViewAdapter.RecyclerViewHolders> {
    private final static String TAG = CategoryRecyclerViewAdapter.class.getSimpleName();

    private final static int altColor = Res.getColor(R.color.black_overlay);

    public CategoryRecyclerViewAdapter(Context context) {
        super(context, Arrays.asList(Category.values()));
        setSelectedItem(Category.getDefault());
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResId;
        switch (viewType) {
            case 1:
                layoutResId = R.layout.card_category_view_selected;
                break;
            case 0:
            default:
                layoutResId = R.layout.card_category_view;
        }

        View layoutView = LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false);
        return new RecyclerViewHolders(layoutView);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        Category item = itemList.get(position);
        holder.categoryName.setText(item.name());
        holder.categoryImg.setImageResource(item.getImageResId());
//        if (position % 2 != 0) {
//            holder.setAlternate();
//        }
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).equals(selectedItem)? 1 : 0;
//        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView categoryName;
        public ImageView categoryImg;
        public View rootView;

        public RecyclerViewHolders(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

//            itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
//            rootView = (CardView) itemView.findViewById(R.id.card_view);
//            rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
//            rootView.setOnClickListener(this);

            rootView = GUIUtils.getView(itemView, R.id.root_view);

            categoryName = (TextView) itemView.findViewById(R.id.category_name);
            categoryImg = (ImageView) itemView.findViewById(R.id.category_img);
        }

        @Override
        public void onClick(View view) {
            setSelectedIndex(getAdapterPosition());
        }

        public void setAlternate() {
            if (rootView != null)
                rootView.setBackgroundColor(altColor);
        }
    }
}
