package com.example.myzrouter;

import com.example.annotations.RouteType;
import com.example.myzrouter.Interface.IProvider;

public class RouteModel {
    private RouteType routeType;
    private String path;
    private Class aClass;
    private IProvider provider;

    public RouteModel(RouteType routeType, String path, Class aClass) {
        this.routeType = routeType;
        this.path = path;
        this.aClass = aClass;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public String getPath() {
        return path;
    }

    public Class getaClass() {
        return aClass;
    }

    public IProvider getProvider() {
        return provider;
    }

    public void setProvider(IProvider provider) {
        this.provider = provider;
    }
}
