package com.example.myweatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class WeatherPredictionAdapter extends ArrayAdapter<Forecast> {

    List<Forecast> forecastList;
    Context context;

    public WeatherPredictionAdapter(Context context, List<Forecast> forecastList) {
        super(context, 0);
        this.context = context;
        this.forecastList = forecastList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.single_lv_design, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvForecastDate = convertView.findViewById(R.id.tvForecastDate);
            viewHolder.tvForecastTemperature = convertView.findViewById(R.id.tvForecastTemperature);
            viewHolder.tvForecastCondition = convertView.findViewById(R.id.tvForecastCondition);
            viewHolder.ivForecastIcon = convertView.findViewById(R.id.ivForecastIcon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Forecast forecast = forecastList.get(position);
        viewHolder.tvForecastDate.setText(forecast.getDate());
        viewHolder.tvForecastTemperature.setText(forecast.getTemperature());
        viewHolder.tvForecastCondition.setText(forecast.getCondition());

        // Load weather icon using Picasso
        Picasso.get().load(forecast.getIconURL()).into(viewHolder.ivForecastIcon);

        return convertView;
    }

    private static class ViewHolder {
        TextView tvForecastDate;
        TextView tvForecastTemperature;
        TextView tvForecastCondition;
        ImageView ivForecastIcon;
    }
}
