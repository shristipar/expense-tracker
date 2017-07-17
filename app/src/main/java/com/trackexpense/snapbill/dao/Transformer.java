package com.trackexpense.snapbill.dao;

import com.trackexpense.snapbill.model.Bill;
import com.trackexpense.snapbill.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sjjhohe on 14-Jun-17.
 */

public class Transformer<T,U> {
    /*public static <T,U> U transform(T src, Class<U> uClass) {
        U dest = null;
        try {
            dest = uClass.newInstance();
            Utils.copyProps(src,dest);
            return dest;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T,U> List<U> transform(List<T> srcList, Class<U> uClass) {
        List<U> destList = new ArrayList<>();
        for(T src: srcList) {
            destList.add(transform(src,uClass));
        }
        return destList;
    }*/
}
