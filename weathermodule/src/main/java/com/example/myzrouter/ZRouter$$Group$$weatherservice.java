package com.example.myzrouter;

import com.example.annotations.RouteType;
import com.example.myzrouter.Interface.IRouteGroup;
import com.example.weathermodule.WeatherServiceImpl;

import java.util.Map;

public class ZRouter$$Group$$weatherservice implements IRouteGroup {
    @Override
    public void loadInto(Map<String, RouteModel> atlas) {
        atlas.put("/wetherservice/getinfo", new RouteModel(RouteType.PROVIDER,
                "/wetherservice/getinfo", WeatherServiceImpl.class));
    }
}
