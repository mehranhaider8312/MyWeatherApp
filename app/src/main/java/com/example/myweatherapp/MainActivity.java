package com.example.myweatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RelativeLayout rlHome;
    ProgressBar pbLoading;
    TextView tvCityName,tvTemperature,tvCondition;
    TextInputEditText etCity;
    ImageView ivBack,ivIcon,ivSearch;
    RecyclerView rvForecasts;
    ArrayList<Weather> weatherArrayList;
    WeatherAdapter adapter;
    LocationManager locationManager;
    int PERMISSION_CODE = 1;
    String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        
    }
    public void init()
    {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                ,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

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
        adapter = new WeatherAdapter(this,weatherArrayList);
        rvForecasts.setAdapter(adapter);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        assert location != null;
        cityName = getCityName(location.getLatitude(),location.getLongitude());

        getWeatherInfo(cityName);

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = etCity.getText().toString().trim();
                if(TextUtils.isEmpty(city))
                {
                    etCity.setError("City Name cannot be Empty");
                    return;
                }

                tvCityName.setText(city);
                getWeatherInfo(city);
            }
        });




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Provide Permissions First....", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double lat, double lang)
    {
        String cityName = "Null";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(lat,lang,10);

            assert addresses != null;
            for(Address adr : addresses){
                if(adr!=null){
                    String city = adr.getLocality();
                    if(city!=null && city.isEmpty()){
                        cityName = city;
                    }else{
                        Toast.makeText(this, "City Entered Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch(IOException exp) {
            Toast.makeText(this, exp.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=&q="+cityName+"&days=1&aqi=yes&alerts=yes";
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
                    tvTemperature.setText(temperature+"°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:"+conditionIcon).into(ivIcon);
                    tvCondition.setText(condition);
                    if(isDay==1){
                        //morning
                        Picasso.get().load("https://www.freepik.com/free-photo/3d-maple-tree-against-sunset-sky_4498736.htm#fromView=search&page=1&position=4&uuid=feb3ea1d-47fa-47de-aa28-06ec615b1254").into(ivBack);
                    }
                    else{
                        //night
                        Picasso.get().load("https://www.freepik.com/free-photo/3d-tree-against-moon-night-sky_3336334.htm#query=night%20wallpaper&position=3&from_view=keyword&track=ais_user&uuid=7a70ccaf-9956-450c-b15c-72268d65f62c").into(ivBack);
                    }
                JSONObject forcastOBJ = response.getJSONObject("forecast");
                    JSONObject forcastDay = forcastOBJ.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourlyForecastArray = forcastDay.getJSONArray("hour");

                    for(int i=0;i<hourlyForecastArray.length();i++){
                        JSONObject hourObj = hourlyForecastArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temp = hourObj.getString("");
                        String time = hourObj.getString("time");
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Enter a Valid city Name", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
