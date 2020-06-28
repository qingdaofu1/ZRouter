package com.example.myzrouter;

import com.example.annotations.RouteType;
import com.example.myzrouter.Interface.IProviderGroup;
import com.example.weathermodule.WeatherServiceImpl;

import java.util.Map;

public class ZRouter$$Providers$$weathermodule implements IProviderGroup {
    @Override
    public void loadInto(Map<String, RouteModel> providers) {
        providers.put("com.example.weathermodule.IWeatherService", new RouteModel(RouteType.PROVIDER,
                "/wetherservice/getinfo", WeatherServiceImpl.class));
    }
}
