package com.example.myweatherapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Predictions extends AppCompatActivity {

    ListView lvPredictions;
    WeatherPredictionAdapter adapter;
    ArrayList<Forecast> predictionsList;
    SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       init();

    }
    public void init(){

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_predictions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sPref = getPreferences(MODE_PRIVATE);
        lvPredictions = findViewById(R.id.lvPredictionsList);
        predictionsList = new ArrayList<>();
        loadData();
        adapter = new WeatherPredictionAdapter(Predictions.this,predictionsList);
        lvPredictions.setAdapter(adapter);
    }
    public void loadData() {
        String cityName = sPref.getString("city_from_location", "");
        Toast.makeText(this, cityName, Toast.LENGTH_SHORT).show();
        String url = "https://api.weatherapi.com/v1/forecast.json?key=2d67c27fc92e49f88bc113407240307&q="+cityName+"&days=15&aqi=yes&alerts=yes";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray forecastDays = response.getJSONObject("forecast").getJSONArray("forecastday");

                            for (int i = 0; i < forecastDays.length(); i++) {
                                JSONObject dayObj = forecastDays.getJSONObject(i);
                                String date = dayObj.getString("date");
                                String condition = dayObj.getJSONObject("day").getJSONObject("condition").getString("text");
                                String iconURL = dayObj.getJSONObject("day").getJSONObject("condition").getString("icon");
                                String temperature = dayObj.getJSONObject("day").getString("avgtemp_c");

                                Forecast forecast = new Forecast(date, condition, iconURL, temperature);
                                predictionsList.add(forecast);
                            }
                            adapter.notifyDataSetChanged(); // Notify adapter of data change
                        } catch (JSONException e) {
                            Toast.makeText(Predictions.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Display the exact error message from Volley
                        Toast.makeText(Predictions.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

}