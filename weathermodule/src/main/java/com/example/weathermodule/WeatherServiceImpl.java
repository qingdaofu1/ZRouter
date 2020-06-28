package com.example.weathermodule;

import android.content.Context;

import com.example.annotations.Route;

@Route(path = "/wetherservice/getinfo")
public class WeatherServiceImpl implements IWeatherService {
    @Override
    public String getWeatherInfo(String city) {
        return city + "今天天气挺好的";
    }

    @Override
    public void init(Context context) {

    }
}
