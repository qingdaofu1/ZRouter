package com.example.zrouter;

import com.example.myzrouter.Interface.IAutoWiredInject;
import com.example.myzrouter.ZRouter;
import com.example.weathermodule.IWeatherService;

public class MainActivity$$ZRouter$$AutoWired implements IAutoWiredInject {
    @Override
    public void inject(Object object) {
        MainActivity substitute = (MainActivity)object;
        ((MainActivity)object).extra = substitute.getIntent().getStringExtra("ok");
        substitute.weatherService = (IWeatherService) ZRouter.getInstance().build("/wetherservice/getinfo").navigation();
    }
}
