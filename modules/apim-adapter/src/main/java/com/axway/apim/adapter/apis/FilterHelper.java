package com.axway.apim.adapter.apis;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

public class FilterHelper {

    private FilterHelper(){

    }

    public static void setFilter(String name, List<NameValuePair> filters) {
        String op = "eq";
        if(name.startsWith("*") || name.endsWith("*")) {
            op = "like";
            name = name.replace("*", "");
        }
        filters.add(new BasicNameValuePair("field", "name"));
        filters.add(new BasicNameValuePair("op", op));
        filters.add(new BasicNameValuePair("value", name));
    }
}
