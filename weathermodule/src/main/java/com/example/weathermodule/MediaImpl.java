package com.example.weathermodule;

import android.content.Context;

import com.example.annotations.Route;


@Route(path = "/wetherservice_group2/getsinger")
public class MediaImpl implements IMediaService {
    @Override
    public String getArtister() {
        return "周杰伦";
    }

    @Override
    public void init(Context context) {

    }
}
