package com.example.myweatherapp;

public class Forecast {
    String date;
    String condition;
    String iconURL;
    String temperature;

    public Forecast(String date, String condition, String iconURL, String temperature) {
        this.date = date;
        this.condition = condition;
        this.iconURL = iconURL;
        this.temperature = temperature;
    }

    public Forecast() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}
