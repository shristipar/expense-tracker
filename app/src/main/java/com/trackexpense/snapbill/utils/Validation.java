package com.trackexpense.snapbill.utils;

import android.text.TextUtils;
import android.util.Patterns;

import org.w3c.dom.Text;

/**
 * Created by Johev on 02-05-2017.
 */

public class Validation {

    public static boolean isEmpty(String value) {
        return TextUtils.isEmpty(value);
    }

    public static boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email)
                && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String pass) {
        return pass.length()>=6 && pass.length()<=30;
    }

    public static boolean isTokenValid(String token) {
        return token.length()==6 && TextUtils.isDigitsOnly(token);
    }

    public static boolean isNumber(String s) {
        return !isEmpty(s) && s.matches("[-+]?\\d*\\.?\\d*");
    }
}
