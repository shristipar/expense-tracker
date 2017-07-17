package com.trackexpense.snapbill.network.response;

import com.trackexpense.snapbill.model.Bill;

import java.util.List;

/**
 * Created by sjjhohe on 12-Jun-17.
 */

public class BillsResponse {
    private List<Bill> bills;

    public List<Bill> getBills() {
        return bills;
    }
}
