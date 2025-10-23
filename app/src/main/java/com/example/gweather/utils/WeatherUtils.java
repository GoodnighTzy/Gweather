package com.example.gweather.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherUtils {
    public static void saveWeatherHistory(Context ctx, JSONObject obj) {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("WeatherHistory", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String existing = prefs.getString("history", "[]");
            JSONArray arr = new JSONArray(existing);

            JSONObject entry = new JSONObject();
            entry.put("city", obj.getString("name"));
            entry.put("temp", obj.getJSONObject("main").getDouble("temp"));
            entry.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));

            arr.put(entry);
            editor.putString("history", arr.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONArray getWeatherHistory(Context ctx) {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("WeatherHistory", Context.MODE_PRIVATE);
            return new JSONArray(prefs.getString("history", "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }
}
