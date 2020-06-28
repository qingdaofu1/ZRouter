package com.example.myzrouter;

import android.os.Bundle;

import com.example.annotations.RouteType;
import com.example.myzrouter.Interface.IRouteGroup;
import com.example.myzrouter.RouteModel;
import com.example.weathermodule.WeatherMainActivity;

import java.util.Map;

public class ZRouter$$Group$$weathermodule implements IRouteGroup {
    @Override
    public void loadInto(Map<String, RouteModel> atlas) {
        atlas.put("/weather/weatheractivity", new RouteModel(RouteType.ACTIVITY,
                "/weather/weatheractivity", WeatherMainActivity.class));
    }
}
