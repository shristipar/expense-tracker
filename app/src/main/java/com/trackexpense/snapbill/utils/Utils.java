package com.trackexpense.snapbill.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trackexpense.snapbill.network.response.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.adapter.rxjava.HttpException;

/**
 * Created by Johev on 31-03-2017.
 */

public class Utils {

    public static SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
    public static SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);

    public static String getInputDateFormat(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getInputDateFormat(calendar);
    }

    public static String getInputDateFormat(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH)
                + " - " + monthFormat.format(calendar.getTime())
                //+ " - " + calendar.getAllOrderBy(Calendar.MONTH)
                + " - " + calendar.get(Calendar.YEAR);
        //+ " ("+ dayFormat.format(calendar.getTime()) +")";
    }

    public static String getHttpErrorMessage(Throwable error) {
        Gson gson = new GsonBuilder().create();
        try {
            String errorBody = ((HttpException) error).response().errorBody().string();
            Response response = gson.fromJson(errorBody, Response.class);
            return response.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String sanitizeAmount(String text) {
        if (text == null)
            return null;
        else
            return text.replaceAll(",| ", "");
    }

    public static <T> boolean isListEmpty(List<T> list) {
        return list == null || list.size() == 0;
    }

    public static void copyProps(Object source, Object dest) {
        Method[] methods = source.getClass().getMethods();

        for (Method methodName : methods) {
            String sourceMethodName = methodName.getName();
            try {
                // getter methods
                if (sourceMethodName.startsWith("get")) {
                    String destMethodName = sourceMethodName.replaceFirst("get","set");
                    dest.getClass()
                            .getMethod(destMethodName,methodName.getReturnType())
                            .invoke(dest, methodName.invoke(source, null));
                } // is methods
                else if (sourceMethodName.startsWith("is")) {
                    String destMethod = sourceMethodName.replaceFirst("is", "set");
                    dest.getClass()
                            .getMethod(destMethod,methodName.getReturnType())
                            .invoke(dest, methodName.invoke(source, null));
                }

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isNull(Object object) {
        return object==null;
    }
}
