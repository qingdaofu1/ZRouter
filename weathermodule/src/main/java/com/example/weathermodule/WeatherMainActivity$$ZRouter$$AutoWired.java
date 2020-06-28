package com.example.weathermodule;

import com.example.myzrouter.Interface.IAutoWiredInject;

public class WeatherMainActivity$$ZRouter$$AutoWired implements IAutoWiredInject {
    @Override
    public void inject(Object object) {
        WeatherMainActivity substitute = (WeatherMainActivity) object;
        substitute.msg = substitute.getIntent().getStringExtra("map");
    }
}
