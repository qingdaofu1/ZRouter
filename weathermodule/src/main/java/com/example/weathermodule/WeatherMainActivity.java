package com.example.weathermodule;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.annotations.AutoWired;
import com.example.annotations.Route;
import com.example.myzrouter.ZRouter;


@Route(path = "/weather/weatheractivity")
public class WeatherMainActivity extends AppCompatActivity {

    @AutoWired(name = "map")
    public String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_main);
        ZRouter.getInstance().inject(this);
        findViewById(R.id.btn_jump_to1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZRouter.getInstance()
                        .build("/main/activity")
                        .withString("ok", "dddddddddddddd")
                        .navigation();
                finish();
            }
        });
        Toast.makeText(this, "get string extra is= " + msg, Toast.LENGTH_SHORT).show();
    }
}
