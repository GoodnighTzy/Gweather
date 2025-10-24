package com.example.gweather.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gweather.R;
import com.example.gweather.utils.WeatherUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CurrentWeatherFragment extends Fragment {
    private TextView tvCity, tvTemp, tvSunrise, tvSunset ;
    private ImageView ivWeatherIcon;
    private EditText etCity;
    private Button btnSearch;

    private static final String API_KEY = "4eb431a578964ce05c1c32ff98d6fd72";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_current_weather, container, false);

        tvCity = v.findViewById(R.id.tvCity);
        tvTemp = v.findViewById(R.id.tvTemp);
        tvSunrise = v.findViewById(R.id.tvSunrise);
        tvSunset = v.findViewById(R.id.tvSunset);
        ivWeatherIcon = v.findViewById(R.id.ivWeatherIcon);
        etCity = v.findViewById(R.id.etCity);
        btnSearch = v.findViewById(R.id.btnSearch);


        fetchWeather("Tagaytay");


        btnSearch.setOnClickListener(v1 -> {
            String city = etCity.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeather(city);
            } else {
                Toast.makeText(requireContext(), "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }



    private void fetchWeather(String city) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                "&units=metric&appid=" + API_KEY;

        new Thread(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                InputStreamReader reader;

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new InputStreamReader(conn.getInputStream());
                } else {
                    reader = new InputStreamReader(conn.getErrorStream());
                }

                BufferedReader br = new BufferedReader(reader);
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);
                br.close();


                JSONObject obj = new JSONObject(response.toString());
                requireActivity().runOnUiThread(() -> displayWeather(obj));

                WeatherUtils.saveWeatherHistory(requireContext(), obj);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }



    private void displayWeather(JSONObject obj) {
        try {
            String city = obj.optString("name", "Unknown City");
            String country = obj.optJSONObject("sys") != null
                    ? obj.getJSONObject("sys").optString("country", "Unknown")
                    : "Unknown";

            double temp = obj.optJSONObject("main") != null
                    ? obj.getJSONObject("main").optDouble("temp", 0.0)
                    : 0.0;

            long sunrise = 0, sunset = 0;
            int timezoneOffset = obj.optInt("timezone", 0); // offset in seconds

            if (obj.has("sys")) {
                JSONObject sys = obj.getJSONObject("sys");
                sunrise = sys.optLong("sunrise", 0);
                sunset = sys.optLong("sunset", 0);
            }


            Date sunriseDate = new Date((sunrise + timezoneOffset) * 1000L);
            Date sunsetDate = new Date((sunset + timezoneOffset) * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            String sunriseTime = sunrise > 0 ? sdf.format(sunriseDate) : "N/A";
            String sunsetTime = sunset > 0 ? sdf.format(sunsetDate) : "N/A";


            String condition = "clear";
            if (obj.has("weather") && obj.getJSONArray("weather").length() > 0) {
                condition = obj.getJSONArray("weather").getJSONObject(0).getString("main");
            }


            long currentUtcMillis = System.currentTimeMillis();
            long localTimeMillis = currentUtcMillis + (timezoneOffset * 1000L);
            Calendar localCal = Calendar.getInstance();
            localCal.setTimeInMillis(localTimeMillis);
            int localHour = localCal.get(Calendar.HOUR_OF_DAY);


            int iconRes;
            String conditionLower = condition.toLowerCase();

            if (conditionLower.contains("rain")) {
                iconRes = R.drawable.ic_rain;
            } else if (conditionLower.contains("clear")) {
                if (localHour >= 18 || localHour < 6) {
                    iconRes = R.drawable.ic_moon;
                } else {
                    iconRes = R.drawable.ic_sun;
                }
            } else {

                if (localHour >= 18 || localHour < 6) {
                    iconRes = R.drawable.ic_moon;
                } else {
                    iconRes = R.drawable.ic_sun;
                }
            }


            tvCity.setText(city + ", " + country);
            tvTemp.setText(String.format(Locale.getDefault(), "%.1f°C", temp));
            tvSunrise.setText("Sunrise: " + sunriseTime);
            tvSunset.setText("Sunset: " + sunsetTime);
            ivWeatherIcon.setImageResource(iconRes);

        } catch (Exception e) {
            e.printStackTrace();
            tvCity.setText("City not found");
            tvTemp.setText("--°C");
            tvSunrise.setText("Sunrise: --:--");
            tvSunset.setText("Sunset: --:--");
        }
    }





}
