package com.example.myzrouter.Interface;

import java.util.Map;

public interface IRouteRoot {
    void loadInto(Map<String, Class<? extends IRouteGroup>> routes);
}
