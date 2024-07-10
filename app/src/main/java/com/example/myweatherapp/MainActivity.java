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
    Button btnCuisine;
    FloatingActionButton fabForecast;
    SharedPreferences sPref;
    public static String STR_LESS_THAN_0_SUGGESTION ;
    public static String STR_0_TO_20_SUGGESTION;
    public static String STR_20_TO_30_SUGGESTION;
    public static String STR_30_TO_40_SUGGESTION;
    public static String STR_40_TO_50_SUGGESTION;
    public final static String FILENAME="MY_FOOD";


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
        btnCuisine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView time, temperature, windSpeed, food;
                AlertDialog.Builder foodAlert = new AlertDialog.Builder(MainActivity.this);
                foodAlert.setTitle("Food to try in this Weather");
                View alertView = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.food_suggestion_view, null, false);
                time = alertView.findViewById(R.id.tvAlertTime);
                temperature = alertView.findViewById(R.id.tvAlertTemperature);
                windSpeed = alertView.findViewById(R.id.tvAlertWindSpeed);
                food = alertView.findViewById(R.id.tvAlertFood);

                time.setText(weatherArrayList.get(0).getTime());
                temperature.setText(weatherArrayList.get(0).getTemperature());
                windSpeed.setText(weatherArrayList.get(0).getWindSpeed());
                double temp = Double.parseDouble(weatherArrayList.get(0).getTemperature());
                if (temp < 0) {
                    food.setText(STR_LESS_THAN_0_SUGGESTION);
                } else if (temp > 0 && temp < 20) {
                    food.setText(STR_0_TO_20_SUGGESTION);
                } else if (temp > 20 && temp < 30) {
                    food.setText(STR_20_TO_30_SUGGESTION);
                } else if (temp > 30 && temp < 40) {
                    food.setText(STR_30_TO_40_SUGGESTION);
                } else if (temp > 40 && temp < 50) {
                    food.setText(STR_40_TO_50_SUGGESTION);
                }
                foodAlert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AlertDialog.Builder suggestionAlert = new AlertDialog.Builder(MainActivity.this);
                        suggestionAlert.setTitle("My Food Suggestion Dialoge box");
                        View suggestionView = LayoutInflater.from(MainActivity.this)
                                .inflate(R.layout.food_suggestion_update_design, null, false);
                        EditText etOne = suggestionView.findViewById(R.id.etLessThan0);
                        etOne.setText("for Temperature less than Zero : "+STR_LESS_THAN_0_SUGGESTION);

                        EditText etTwo = suggestionView.findViewById(R.id.et0To20);
                        etTwo.setText("for Temperature less than Zero : "+STR_0_TO_20_SUGGESTION);

                        EditText etThree = suggestionView.findViewById(R.id.et20To30);
                        etThree.setText("for Temperature less than Zero : "+STR_20_TO_30_SUGGESTION);

                        EditText etFour = suggestionView.findViewById(R.id.et30To40);
                        etFour.setText("for Temperature less than Zero : "+STR_30_TO_40_SUGGESTION);

                        EditText etFive = suggestionView.findViewById(R.id.et40To50);
                        etFive.setText("for Temperature less than Zero : "+STR_40_TO_50_SUGGESTION);
                        suggestionAlert.setView(suggestionView);

                        suggestionAlert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(TextUtils.isEmpty(STR_0_TO_20_SUGGESTION)){
                                    etOne.setError("Must Write something here");
                                    return;
                                }
                                if(TextUtils.isEmpty(STR_0_TO_20_SUGGESTION)){
                                    etTwo.setError("Must Write something here");
                                    return;
                                }
                                if(TextUtils.isEmpty(STR_20_TO_30_SUGGESTION)){
                                    etThree.setError("Must Write something here");
                                    return;
                                }
                                if(TextUtils.isEmpty(STR_30_TO_40_SUGGESTION)){
                                    etFour.setError("Must Write something here");
                                    return;
                                }
                                if(TextUtils.isEmpty(STR_40_TO_50_SUGGESTION)){
                                    etFive.setError("Must Write something here");
                                    return;
                                }
                                STR_LESS_THAN_0_SUGGESTION=etOne.getText().toString().trim();
                                STR_0_TO_20_SUGGESTION=etTwo.getText().toString().trim();
                                STR_20_TO_30_SUGGESTION=etThree.getText().toString().trim();
                                STR_30_TO_40_SUGGESTION=etFour.getText().toString().trim();
                                STR_40_TO_50_SUGGESTION=etFive.getText().toString().trim();
                                writeOnFile();
                            }
                        }).show();
                    }
                });
                foodAlert.setView(alertView);
                foodAlert.show();
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
        btnCuisine = findViewById(R.id.btnCuisine);
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

     public void writeOnFile()
    {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try{
            fw = new FileWriter("MY_FOOD", true);
            bw = new BufferedWriter(fw);
            bw.write(STR_LESS_THAN_0_SUGGESTION+"\n"+STR_0_TO_20_SUGGESTION+"\n"+STR_20_TO_30_SUGGESTION+
                    "\n"+STR_30_TO_40_SUGGESTION+"\n"+STR_40_TO_50_SUGGESTION);
            bw.newLine();
        }
        catch(IOException e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally{
            if(bw != null)
            {
                try {
                    bw.close();
                } catch (IOException ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();                }
            }
            if(fw != null){
                try {
                    fw.close();
                } catch (IOException ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();                }
            }
        }
    }
    public void readFromFile()
    {
        FileReader fr = null;
        BufferedReader br = null;
        try{
            fr = new FileReader("MY_FOOD");
            br = new BufferedReader(fr);
            String line;
            while((line = br.readLine())!=null)
            {
                String[] arr = line.split("\n");
                STR_LESS_THAN_0_SUGGESTION = arr[0];
                STR_0_TO_20_SUGGESTION = arr[1];
                STR_20_TO_30_SUGGESTION = arr[2];
                STR_30_TO_40_SUGGESTION = arr[3];
                STR_40_TO_50_SUGGESTION = arr[5];
            }
        }catch(IOException ex)
        {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();        }
        finally{
            if(br!= null)
            {
                try {
                    br.close();
                } catch (IOException ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            if(fr != null){
                try {
                    fr.close();
                } catch (IOException ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



}