package com.example.zrouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.annotations.AutoWired;
import com.example.annotations.Route;
import com.example.myzrouter.ZRouter;
import com.example.weathermodule.IMediaService;
import com.example.weathermodule.IWeatherService;

@Route(path = "/main/activity")
public class MainActivity extends AppCompatActivity {
    @AutoWired(name = "/wetherservice/getinfo")
    IWeatherService weatherService;

    @AutoWired(name="ok")
    public String extra;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ZRouter.getInstance().inject(this);
        findViewById(R.id.btn_jump_app2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZRouter.getInstance()
                        .build("/weather/weatheractivity")
                        .withString("map", "hello kitty")
                        .navigation();
                finish();
            }
        });
        Toast.makeText(this, "get extra = " + extra, Toast.LENGTH_SHORT).show();

        findViewById(R.id.btn_getweather).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = 1;
                //方式1
//                String weatherInfo = weatherService.getWeatherInfo("上海");
//                 Toast.makeText(MainActivity.this, weatherInfo, Toast.LENGTH_SHORT).show();
                //方式2
//                IWeatherService weatherService1 = (IWeatherService) ZRouter.getInstance()
//                        .build("/wetherservice/getinfo")
//                        .navigation();
//                String weatherInfo1 = weatherService1.getWeatherInfo("北京");
//                Toast.makeText(MainActivity.this, weatherInfo1, Toast.LENGTH_SHORT).show();
                //方式3
                IWeatherService weatherService2  = ZRouter.getInstance()
                        .navigation(IWeatherService.class);
                String weatherInfo2 = weatherService2.getWeatherInfo("杭州");
                Toast.makeText(MainActivity.this, weatherInfo2, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_getsinger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMediaService mediaService = (IMediaService) ZRouter.getInstance()
                        .build("/wetherservice_group2/getsinger")
                        .navigation();
                Toast.makeText(MainActivity.this, " singer is "+mediaService.getArtister(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
