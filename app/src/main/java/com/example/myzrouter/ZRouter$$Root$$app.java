package com.example.myzrouter;

import com.example.myzrouter.Interface.IRouteGroup;
import com.example.myzrouter.Interface.IRouteRoot;

import java.util.Map;

public class ZRouter$$Root$$app implements IRouteRoot {
    @Override
    public void loadInto(Map<String, Class<? extends IRouteGroup>> routes) {
        routes.put("app", ZRouter$$Group$$app.class);
    }
}
