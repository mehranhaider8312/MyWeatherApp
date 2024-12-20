package com.example.myweatherapp;

import static android.graphics.Insets.add;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RelativeLayout rlHome;
    ProgressBar pbLoading;
    TextView tvCityName, tvTemperature, tvCondition;
    TextInputEditText etCity;
    ImageView ivBack, ivIcon, ivSearch;
    RecyclerView rvForecasts;
    ArrayList<Weather> weatherArrayList;
    WeatherAdapter adapter;
    LocationManager locationManager;
    int PERMISSION_CODE = 1;
    String cityName;
    FloatingActionButton fabForecast;
    SharedPreferences sPref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                , WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        init();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                cityName = getCityName(location.getLatitude(), location.getLongitude());
                getWeatherInfo(cityName);
            } else {
                // If location is null, request location updates
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        }
        getWeatherInfo(cityName);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString("city_from_location",cityName);
        editor.apply();
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = etCity.getText().toString().trim();
                if (TextUtils.isEmpty(city)) {
                    etCity.setError("City Name cannot be Empty");
                    return;
                }
                cityName = city;
                tvCityName.setText(city);
                getWeatherInfo(city);
            }
        });

        fabForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Predictions.class));
                finish();
            }
        });
    }

    private final android.location.LocationListener locationListener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            cityName = getCityName(location.getLatitude(), location.getLongitude());
            getWeatherInfo(cityName);
            // Remove the location
            locationManager.removeUpdates(this);
        }
    };

    public void init() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rlHome = findViewById(R.id.rlHome);
        pbLoading = findViewById(R.id.pbLoading);
        tvCityName = findViewById(R.id.tvCityName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        etCity = findViewById(R.id.etCity);
        ivBack = findViewById(R.id.ivBack);
        ivIcon = findViewById(R.id.ivIcon);
        ivSearch = findViewById(R.id.ivSearch);
        rvForecasts = findViewById(R.id.rvForecasts);
        weatherArrayList = new ArrayList<>();
        adapter = new WeatherAdapter(this, weatherArrayList);
        rvForecasts.setAdapter(adapter);
        fabForecast = findViewById(R.id.fabPredicition);
        sPref = getSharedPreferences("mylocationfile",MODE_PRIVATE);
        //readFromFile();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Provide Permissions First....", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double lat, double lng) {
        String cityName = "Unknown";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (address.getLocality() != null) {
                    cityName = address.getLocality();
                } else {
                    Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return cityName;
    }



    private void getWeatherInfo(String cityName) {
        String url = "https://api.weatherapi.com/v1/forecast.json?key=2d67c27fc92e49f88bc113407240307&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        tvCityName.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pbLoading.setVisibility(View.GONE);
                rlHome.setVisibility(View.VISIBLE);
                weatherArrayList.clear();
                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    tvTemperature.setText(temperature + "°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    String iconUrl = "https:" + conditionIcon;
                    Picasso.get().load(iconUrl).into(ivIcon);
                    tvCondition.setText(condition);

                    if (isDay == 1) {
                        ivBack.setImageResource(R.drawable.day_back);
                    } else {
                        ivBack.setImageResource(R.drawable.night_back);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastDay = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourlyForecastArray = forecastDay.getJSONArray("hour");

                    for (int i = 0; i < hourlyForecastArray.length(); i++) {
                        JSONObject hourObj = hourlyForecastArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temp = hourObj.getString("temp_c");
                        String windSpeed = hourObj.getString("wind_kph");
                        String hourIconUrl = hourObj.getJSONObject("condition").getString("icon");
                        weatherArrayList.add(new Weather(time, temp, hourIconUrl, windSpeed));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}