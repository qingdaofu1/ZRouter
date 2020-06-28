package com.example.myzrouter.Interface;

import com.example.myzrouter.RouteModel;

import java.util.Map;

public interface IProviderGroup {
    void loadInto(Map<String, RouteModel> providers);
}
