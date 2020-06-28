package com.example.weathermodule;


import com.example.myzrouter.Interface.IProvider;

public interface IWeatherService extends IProvider {
    String getWeatherInfo(String city);
}
