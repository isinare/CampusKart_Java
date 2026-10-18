package com.campuskart.server;

import java.net.URLDecoder;
import java.util.ArrayList;

// ★ RUBRIC: Collections (1M) | Asg 5: form fields kept in TWO PARALLEL ArrayLists
//            (a HashMap is not in the lab manual, so simple lists are used instead)
//            Asg 4 : wrapper classes Integer.parseInt / Double.parseDouble convert the text values
public class FormData {

    private final ArrayList<String> names = new ArrayList<String>();    // ★ Asg 5: ArrayList
    private final ArrayList<String> values = new ArrayList<String>();

    public FormData(String encoded) {
        if (encoded == null || encoded.length() == 0) {
            return;
        }
        String[] pairs = encoded.split("&");     // "a=1&b=2" -> ["a=1", "b=2"]
        for (int i = 0; i < pairs.length; i++) {
            int equals = pairs[i].indexOf('=');
            String name = equals < 0 ? pairs[i] : pairs[i].substring(0, equals);
            String value = equals < 0 ? "" : pairs[i].substring(equals + 1);
            names.add(decode(name));
            values.add(decode(value));
        }
    }

    public String get(String name) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(name)) {
                return values.get(i);
            }
        }
        return "";                               // missing field -> empty string
    }

    // ★ RUBRIC: Collections | Asg 4: WRAPPER classes turn form text into numbers.
    // A bad value throws NumberFormatException, which the Router catches (Asg 14).
    public int getInt(String name) {
        return Integer.parseInt(get(name).trim());
    }

    public double getDouble(String name) {
        return Double.parseDouble(get(name).trim());
    }

    private String decode(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8");
        } catch (Exception e) {
            return text;                         // if decoding fails, use the raw text
        }
    }
}
