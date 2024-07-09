package com.example.myweatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WeatherPredictionAdapter extends ArrayAdapter<Forecast> {
    public WeatherPredictionAdapter(@NonNull Context context, @NonNull ArrayList<Forecast> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Forecast object = getItem(position);
        View view =convertView;
        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.single_lv_design,null,false);
        }

        TextView tvDate,tvTemp,tvCondition;
        ImageView ivConditionPic;

        tvDate = view.findViewById(R.id.tvForecastDate);
        tvTemp = view.findViewById(R.id.tvForecastTemperature);
        tvCondition = view.findViewById(R.id.tvForecastCondition);
        ivConditionPic = view.findViewById(R.id.ivForecastIcon);

        assert object != null;
        tvDate.setText(object.getDate());
        tvTemp.setText(object.getTemperature());
        tvCondition.setText(object.getCondition());
        Picasso.get().load("https:"+object.getIconURL()).into(ivConditionPic);

        return view;
    }
}
