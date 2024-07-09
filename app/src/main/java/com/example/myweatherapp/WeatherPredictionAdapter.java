package com.example.myweatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class WeatherPredictionAdapter extends ArrayAdapter<Forecast> {

    Context context;

    public WeatherPredictionAdapter(Context context, ArrayList<Forecast> forecastList) {
        super(context, 0);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Toast.makeText(context, ""+position, Toast.LENGTH_SHORT).show();
        TextView tvForecastDate,
        tvForecastTemperature,
        tvForecastCondition;
        ImageView ivForecastIcon;

        View v = convertView;

        if (convertView == null) {
            v = LayoutInflater.from(context).inflate(R.layout.single_lv_design, parent, false);
        }
        tvForecastDate        = v.findViewById(R.id.tvForecastDate);
        tvForecastTemperature = v.findViewById(R.id.tvForecastTemperature);
        tvForecastCondition   = v.findViewById(R.id.tvForecastCondition);
        ivForecastIcon        = v.findViewById(R.id.ivForecastIcon);

        Forecast forecast = getItem(position);
        assert forecast != null;
        tvForecastDate.setText(forecast.getDate());
        tvForecastTemperature.setText(forecast.getTemperature());
        tvForecastCondition.setText(forecast.getCondition());

        // Load weather icon using Picasso
        Picasso.get().load(forecast.getIconURL()).into(ivForecastIcon);

        return v;
    }


}
