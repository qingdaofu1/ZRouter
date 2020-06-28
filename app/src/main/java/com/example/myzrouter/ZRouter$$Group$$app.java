package com.example.myzrouter;

import com.example.annotations.RouteType;
import com.example.myzrouter.Interface.IRouteGroup;
import com.example.zrouter.MainActivity;

import java.util.Map;

public class ZRouter$$Group$$app implements IRouteGroup {
    @Override
    public void loadInto(Map<String, RouteModel> atlas) {
        atlas.put("/main/activity", new RouteModel(RouteType.ACTIVITY,
                "/main/activity", MainActivity.class));
    }
}
