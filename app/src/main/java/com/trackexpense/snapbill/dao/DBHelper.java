package com.trackexpense.snapbill.dao;

import com.orm.SugarRecord;
import com.orm.query.Select;

import java.util.List;

/**
 * Created by sjjhohe on 13-Jun-17.
 */

public class DBHelper {

    private final static String TAG = DBHelper.class.getSimpleName();

    /*public static <M, R extends SugarRecord<R>> R save(M model, Class<R> rClass) {
        R record = Transformer.transform(model, rClass);
        record.save();
        return record;
    }

    public static <M, R extends SugarRecord<R>> void save(List<M> models, Class<R> rClass) {
        List<R> records = Transformer.transform(models, rClass);
        SugarRecord.saveInTx(records);
    }*/

    public static <R extends SugarRecord<R>> List<R> getAllOrderBy(Class<R> recordClass, String orderBy) {
        List<R> list = Select.from(recordClass)
                .orderBy(orderBy)
                .list();
        return list;
    }
}
