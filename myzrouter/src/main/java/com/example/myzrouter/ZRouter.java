package com.example.myzrouter;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.example.annotations.RouteType;
import com.example.myzrouter.Interface.IAutoWiredInject;
import com.example.myzrouter.Interface.IProvider;
import com.example.myzrouter.Interface.IRouteGroup;
import com.example.myzrouter.Interface.IProviderGroup;
import com.example.myzrouter.Interface.IRouteRoot;
import com.example.myzrouter.Utils.ClassUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ZRouter {
    private static final String TAG = "ZRouter";
    private static ZRouter instance = null;
    public static final String DOT = ".";
    public static final String ROUTE_ROOT_PAKCAGE = "com.example.myzrouter";
    public static final String SDK_NAME = "ZRouter";
    public static final String SEPARATOR = "$$$";
    public static final String SUFFIX_ROOT = "Root";
    public static final String SUFFIX_PROVIDERS = "Providers";
    public static final String SUFFIX_AUTOWIRED = "AutoWired";

    private static Context mContext;

    static Map<String, Class<? extends IRouteGroup>> routeGroup = new HashMap<>();
    static Map<String, RouteModel> routes = new HashMap<>();

    static Map<String, RouteModel> providerGroup = new HashMap<>();
    static Map<Class<? extends IProvider>, IProvider> providers = new HashMap<>();
    private static Handler mMainHandler;


    private ZRouter() {
    }

    public static ZRouter getInstance() {
        if (instance == null) {
            synchronized (ZRouter.class) {
                if (instance == null) {
                    instance = new ZRouter();
                }
            }
        }
        return instance;
    }

    /**
     * 检索所有特定包路径下的类，并收集类名为固定规则的类并分类存储到不同的集合中。
     * com.example.myzrouter.ZRouter$$Root------>所有Route类集合
     * com.example.myzrouter.ZRouter$$Provider----->所有Provider集合
     *
     * @param application
     */
    public static void init(Application application) {
        mContext = application;
        mMainHandler = new Handler(Looper.getMainLooper());
        Set<String> allRouteInfo = collectAllRouteInfo(application, "com.example.myzrouter");
        Log.d(TAG, "init: allRouteInfo size=" + allRouteInfo.size());
        classifyRouteType(allRouteInfo);
    }

    private static Set<String> collectAllRouteInfo(Application application, String packageName) {
        try {
            return ClassUtils.getFileNameByPackageName(application, packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void classifyRouteType(Set<String> routerMap) {
        try {
            for (String className : routerMap) {
                if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                    //com.example.myzrouter.ZRouter$$Root    检测时root类型开头的类名，创建类实例并将map传入routeGroup
                    //routeGroup中包含了所有module中，所有在此包名路径下的类
                    ((IRouteRoot) (Class.forName(className).getConstructor().newInstance())).loadInto(routeGroup);
                    Log.d(TAG, "classifyRouteType: routeGroup size = " + routerMap.size());
                } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_PROVIDERS)) {
                    //com.example.myzrouter.ZRouter$$Provider 检测providers类型开头的类。 创建类实例并将map传入providerGroup
                    ((IProviderGroup) (Class.forName(className).getConstructor().newInstance())).loadInto(providerGroup);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    public RouteManager build(String path) {
        return new RouteManager(path);
    }

    public <T> T navigation(Class<? extends T> serviceClass) {
        if (serviceClass == null) {
            return null;
        }
        String name = serviceClass.getName();
        for (String s : providerGroup.keySet()) {
            Log.d(TAG, "navigation: name =" + name + " \n" + "s=" + s);
            if (name.equals(s)) {
                RouteModel routeModel = providerGroup.get(s);
                IProvider instance = routeModel.getProvider();
                if (instance == null) {
                    Class aClass = routeModel.getaClass();
                    try {
                        IProvider provider = (IProvider) aClass.getConstructor().newInstance();
                        routeModel.setProvider(provider);
                        instance = provider;
                        routeModel.setProvider(instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return (T) routeModel.getProvider();
            }
        }
        return null;
    }

    public Object navigation(Context context, String path, Bundle mBundle) {
        // TODO: 2020/5/30 Arouter在同一包内的path还可以分组，这里暂不处理
//        String groupName = getGroupName(path);

        if (routes != null && routes.size() == 0) {
            collectAllGroupRouteModel();
        }
        RouteModel routeModel = routes.get(path);
        if (routeModel == null) {
            Log.e(TAG, "navigation: found no Route info about " + path);
            return null;
        }
        Class routeClass = routeModel.getaClass();
        RouteType routeType = routeModel.getRouteType();
        switch (routeType) {
            case ACTIVITY:
                final Intent intent = new Intent(mContext, routeClass);
                intent.putExtras(mBundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mContext.startActivity(intent);
                    }
                });
                break;
            case SERVICE:
                break;
            case PROVIDER:
                if (providers != null && providers.size() == 0) {
                    collectAllProviderRouteModel();
                }
                IProvider instance = providers.get(routeClass);
                if (null == instance) {
                    IProvider provider = null;
                    try {
                        provider = (IProvider) routeClass.getConstructor().newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    provider.init(mContext);
                    providers.put(routeClass, provider);
                    instance = provider;
                }
                routeModel.setProvider(instance);
                Log.d(TAG, "navigation: path="+path+"  instance="+instance);
                return routeModel.getProvider();
            default:
                break;
        }

        return null;
    }

    private void collectAllGroupRouteModel() {
        try {
            for (Map.Entry<String, Class<? extends IRouteGroup>> group : routeGroup.entrySet()) {
                Log.d(TAG, "collectAllGroupRouteModel: routeGroup path=" + group.getKey());
                Class<? extends IRouteGroup> groupClass = group.getValue();
                if (groupClass != null) {
                    groupClass.getConstructor().newInstance().loadInto(routes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void collectAllProviderRouteModel() {
        try {
            for (Map.Entry<String, RouteModel> iProvider : providerGroup.entrySet()) {
                RouteModel routeModel = iProvider.getValue();
                Class providerClass = routeModel.getaClass();
                Object o = providerClass.getConstructor().newInstance();
                providers.put(providerClass, (IProvider) o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从path从获取group名，即组名。    其中/main/MainActivity 路径的组名是main
     *
     * @param path
     * @return
     */
    private String getGroupName(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("Extract the default group failed, the path must be start with '/' and contain more than 2 '/'!");
        }

        try {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(defaultGroup)) {
                throw new IllegalArgumentException("Extract the default group failed! There's nothing between 2 '/'!");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void inject(Object object) {
        //获取所有AutoWired的类, 此类在对象当前目录查找
        Set<String> allAutoWiredInfo = collectAllRouteInfo((Application) mContext, object.getClass().getPackage().getName());
        classifyAutoWired(allAutoWiredInfo, object);
    }

    //调用AutoWired注解变量的生成类，使其赋值
    private static void classifyAutoWired(Set<String> allAutoWiredInfo, Object object) {
        try {
            for (String className : allAutoWiredInfo) {
                Log.d(TAG, "classifyAutoWired: className=" + className);
                String s = object.getClass().getName() + SEPARATOR + SDK_NAME + SEPARATOR + SUFFIX_AUTOWIRED;
                Log.d(TAG, "classifyAutoWired: s=" + s);
                Log.d(TAG, "classifyAutoWired: ---------------------------");
                if (s.equals(className)) {
                    //routeGroup中包含了所有module中，所有在此包名路径下的类
                    Log.d(TAG, "classifyAutoWired: class for name>>>>>>" + s);
                    ((IAutoWiredInject) (Class.forName(className).getConstructor().newInstance())).inject(object);
                    break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Be sure execute in main thread.
     *
     * @param runnable code
     */
    private void runInMainThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mMainHandler.post(runnable);
        } else {
            runnable.run();
        }
    }
}
