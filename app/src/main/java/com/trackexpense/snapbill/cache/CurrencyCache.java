package com.trackexpense.snapbill.cache;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.pixplicity.easyprefs.library.Prefs;
import com.trackexpense.snapbill.MainActivity;
import com.trackexpense.snapbill.utils.Constants;
import com.trackexpense.snapbill.utils.Utils;
import com.trackexpense.snapbill.utils.Validation;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by Johev on 26-04-2017.
 */

public class CurrencyCache {
    private static List<Locale> topLocales;
    private static List<Currency> topCurrencies;

    private static List<Currency> otherCurrencies;
    //device default currency
    private static Currency defaultCurrency;

    private static HashMap<String, Currency> currencyCodeMaptoCurrecy = new HashMap<>();
    private static HashMap<Currency, Locale> currencyMaptoLocale = new HashMap<>();

    //user selected currency
    private static Currency selectedCurrency;

    private static List<Currency> currencyList;
    private static Context context;

    public static void setContext(Context context) {
        CurrencyCache.context = context;
        Locale defaultLocale = getCurrentLocale(context);
        defaultCurrency = Currency.getInstance(defaultLocale);

        topLocales = new ArrayList<>();
        topLocales.add(Locale.CHINA);
        topLocales.add(Locale.JAPAN);
        topLocales.add(Locale.UK);
        topLocales.add(Locale.US);
        topLocales.add(defaultLocale);

        Set<Currency> currencySet = new HashSet<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            Currency currency = getCurrencyFromLocale(locale);
            if (currency != null) {
                currencyCodeMaptoCurrecy.put(currency.getCurrencyCode(), currency);
                currencySet.add(currency);
            }
        }

        otherCurrencies = new ArrayList<>(currencySet);
        Collections.sort(otherCurrencies, new Comparator<Currency>() {
            @Override
            public int compare(Currency c1, Currency c2) {
                return c1.getDisplayName().compareTo(c2.getDisplayName());
            }
        });

        Set<Currency> topCurrencySet = new HashSet<>();
        for (Locale locale : topLocales) {
            Currency currency = getCurrencyFromLocale(locale);
            if (currency != null) {
                topCurrencySet.add(currency);
                currencyCodeMaptoCurrecy.put(currency.getCurrencyCode(), currency);
                otherCurrencies.remove(currency);
            }
        }
        topCurrencies = new ArrayList<>(topCurrencySet);

        currencyList = new ArrayList<>(topCurrencies);
        currencyList.addAll(otherCurrencies);


        // getAllOrderBy selectedCurrency from prefs
        // default value of selectedCurrency = defaultCurreny
        String selectedCurrencyCode = Prefs.getString(Constants.PREF_SELECTED_CURRENCY_CODE, null);
        if (selectedCurrencyCode == null) {
            selectedCurrency = defaultCurrency;
        } else {
            selectedCurrency = fromCurrencyCode(selectedCurrencyCode);
        }
    }

    public static Currency getSelectedCurrency() {
        return selectedCurrency;
    }

    public static void setSelectedCurrency(Currency currency) {
        Prefs.putString(Constants.PREF_SELECTED_CURRENCY_CODE, currency.getCurrencyCode());
        selectedCurrency = currency;
    }

    public static Currency getCurrencyFromLocale(Locale locale) {
        try {
            Currency currency = Currency.getInstance(locale);
            currencyMaptoLocale.put(currency, locale);
            return currency;
        } catch (Exception e) {
            Log.e("CurrencyCache", "currency not found for locale: " + locale.getDisplayName());
        }

        return null;
    }

    private static Locale getLocal(Currency currency) {
        return currencyMaptoLocale.get(currency);
    }

    public static List<Currency> getCurrenciesSortedByCountry() {
        List<Currency> ret = new ArrayList<>();
        ret.add(selectedCurrency);
        ret.addAll(currencyList);
        return ret;
    }

    public static Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    public static Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    public static Currency fromCurrencyCode(String currencyCode) {
        return currencyCodeMaptoCurrecy.get(currencyCode);
    }

    public static String format(Currency currency, String amount) {
        return amount == null ? null : format(currency, Double.parseDouble(amount));
    }

    public static String format(Currency currency, Double amount) {
        if (amount == null || currency == null)
            return null;

        NumberFormat numberFormat = NumberFormat
                .getNumberInstance(getLocal(currency));

//        numberFormat.setCurrency(getSelectedCurrency());
//        Log.e("CURRENCY", getSelectedCurrency().getCurrencyCode());

        return numberFormat.format(amount);
    }
}
