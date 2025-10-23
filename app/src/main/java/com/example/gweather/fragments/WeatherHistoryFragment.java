package com.example.gweather.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gweather.R;
import com.example.gweather.utils.WeatherUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherHistoryFragment extends Fragment {
    LinearLayout historyLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather_history, container, false);
        historyLayout = v.findViewById(R.id.historyLayout);
        loadHistory();
        return v;
    }

    private void loadHistory() {
        JSONArray arr = WeatherUtils.getWeatherHistory(requireContext());
        historyLayout.removeAllViews();

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);
                TextView tv = new TextView(requireContext());
                tv.setText(obj.getString("time") + " - " + obj.getString("city") + ": " + obj.getDouble("temp") + "°C");
                historyLayout.addView(tv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
