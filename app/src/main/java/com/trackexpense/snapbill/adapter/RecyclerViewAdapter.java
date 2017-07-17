package com.trackexpense.snapbill.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.trackexpense.snapbill.model.Bill;

import java.util.List;

/**
 * Created by sjjhohe on 16-Jun-17.
 */

public abstract class RecyclerViewAdapter<T, U extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<U> {
    protected OnItemSelectedListener itemSelectedListener;
    protected Context context;
    protected List<T> itemList;

    protected T selectedItem;

    public RecyclerViewAdapter(Context context, List<T> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void removed(T item) {
        int index = itemList.indexOf(item);
        itemList.remove(index);
        notifyItemRemoved(index);
    }

    public void updated(T oldItem, T newItem) {
        int index = itemList.indexOf(oldItem);
        if(index==-1) {
            throw new IllegalStateException("oldItem not in list");
        }

        itemList.set(index,newItem);
        notifyItemChanged(index);
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(T selectedItem) {
        if (itemList.indexOf(selectedItem) == -1) {
            throw new IllegalStateException("SelectedItem not in itemList");
        }

        this.selectedItem = selectedItem;
    }

    protected void setSelectedIndex(int index) {
        setSelectedItem(itemList.get(index));
        callSelectedListener(index);
        this.notifyDataSetChanged();
    }

    private void callSelectedListener(int selectedIndex) {
        if (this.itemSelectedListener != null) {
            itemSelectedListener.selected(this.selectedItem, selectedIndex);
        }
    }

    public interface OnItemSelectedListener<T> {
        public void selected(T selectedItem, int index);

        public void hold(T holdItem, int index);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.itemSelectedListener = listener;
    }
}
