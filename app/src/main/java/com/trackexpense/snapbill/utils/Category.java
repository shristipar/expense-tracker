package com.trackexpense.snapbill.utils;

import com.trackexpense.snapbill.R;

/**
 * Created by Johev on 30-04-2017.
 */

public enum Category {
    Food(1, R.drawable.ic_restaurant_menu_black_48dp),
    Travel(2, R.drawable.ic_drive_eta_black_48dp),
    Shopping(3, R.drawable.ic_shopping_cart_black_48dp),
    Groceries(4, R.drawable.ic_shopping_basket_black_48dp),
    Rent(5, R.drawable.ic_hotel_black_48dp),
    Electricity(6, R.drawable.ic_power_black_48dp),
    Mobile(7, R.drawable.ic_smartphone_black_48dp),
    Gas(8, R.drawable.ic_local_gas_station_black_48dp),
    Other(9, R.drawable.ic_dashboard_black_48dp);

    private int id;
    private int imageResId;

    Category(int id, int imageResId) {
        this.id = id;
        this.imageResId = imageResId;
    }

    public int getImageResId() {
        return imageResId;
    }

    public static Category getDefault() {
        return Food;
    }
}
