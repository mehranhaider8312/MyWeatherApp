package com.example.myweatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    Context context;
    ArrayList<Weather> weathersList;

    public WeatherAdapter(Context context, ArrayList<Weather> weathersList) {
        this.context = context;
        this.weathersList = weathersList;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_forecast_design,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        Weather weather = weathersList.get(position);

        holder.tvTemperature.setText(weather.getTemperature()+"°C");
        Picasso.get().load("https:"+weather.getIcon()).into(holder.ivCondition);
        SimpleDateFormat inSDF = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat outSDF = new SimpleDateFormat("hh:mm aa");
        try{
            Date timestamp = inSDF.parse(weather.getTime());
            holder.tvTime.setText(outSDF.format(timestamp));
        }catch (ParseException exp)
        {
            Toast.makeText(context, exp.getMessage(), Toast.LENGTH_SHORT).show();
        }
        holder.tvTime.setText(weather.getTime());
        holder.tvWindSpeed.setText(weather.getWindSpeed()+"Km/h");

    }

    @Override
    public int getItemCount() {
        return weathersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTime,tvWindSpeed,tvTemperature;
        ImageView ivCondition;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTemperature = itemView.findViewById(R.id.tvSingleTemperature);
            tvWindSpeed = itemView.findViewById(R.id.tvSingleWindSpeed);
            tvTime = itemView.findViewById(R.id.tvSingleTime);
            ivCondition = itemView.findViewById(R.id.ivSingleCondition);
        }
    }
}
