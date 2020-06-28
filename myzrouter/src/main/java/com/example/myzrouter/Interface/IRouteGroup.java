package com.example.myzrouter.Interface;

import com.example.myzrouter.RouteModel;

import java.util.Map;

public interface IRouteGroup {
    void loadInto(Map<String, RouteModel> atlas);
}
