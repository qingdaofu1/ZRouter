package com.example.annotations;

public enum RouteType {
    ACTIVITY(0, "android.app.Activity"),
    SERVICE(1, "android.app.Service"),
    PROVIDER(2, "com.example.myzrouter.IProvider");

    private int id;
    private String className;

    RouteType(int id, String className) {
        this.id = id;
        this.className = className;
    }
}
