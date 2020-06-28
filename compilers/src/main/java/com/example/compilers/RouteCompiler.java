package com.example.compilers;

import com.example.annotations.Route;
import com.example.annotations.RouteType;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("com.example.annotations.Route")
public class RouteCompiler extends BaseProcessor {
    //存储groupName 与对应生成的类的集合
    Map<String, String> groupFileMap = new HashMap<>();
    CompilerRouteModel providerMap;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Map<String, String> options = processingEnv.getOptions();
        moduleName = options.get("AROUTER_MODULE_NAME");
        messager.printMessage(Diagnostic.Kind.NOTE, "module name is " + moduleName);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (alreadyHandledModule.contains(moduleName)) {
            return false;
        }
        alreadyHandledModule.add(moduleName);
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Route.class);
        Map<String, CompilerRouteModel> routeMap = new HashMap<>();
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            Route annotation = typeElement.getAnnotation(Route.class);
            String path = annotation.path();
            messager.printMessage(Diagnostic.Kind.NOTE, "path is " + path);

            //path = "/weather/weatheractivity"  获取GroupName   此例为weather
            String[] split = path.split("/");
            if (split.length < 3) {
                messager.printMessage(Diagnostic.Kind.NOTE, "the path is incorrect, need two \\");
                return false;
            }
            String groupName = split[1];
            CompilerRouteModel compilerRouteModel = routeMap.get(groupName);
            if (compilerRouteModel == null) {
                compilerRouteModel = new CompilerRouteModel();
                routeMap.put(groupName, compilerRouteModel);
            }
            //同一group的model的集合
            compilerRouteModel.putElement(path, typeElement);
        }


        createGroupFiles(routeMap);
        createProviderFile(providerMap);
        createRootFile(groupFileMap);
        return false;
    }

    private void createProviderFile(CompilerRouteModel providerMap) {
       /* public class ZRouter$$Provider$$weathermodule implements IProviderGroup {
            @Override
            public void loadInto(Map<String, CompilerRouteModel> providers) {
                providers.put("com.example.weathermodule.IWeatherService", new CompilerRouteModel(RouteType.PROVIDER,
                        "/wetherservice/getinfo", WeatherServiceImpl.class));
            }
        }*/

        ClassName map = ClassName.get("java.util", "Map");
        ClassName string = ClassName.get("java.lang", "String");
        ClassName routeModel = ClassName.get(PACKAGE_GENERATED_FILE, "RouteModel");

        MethodSpec.Builder builder = MethodSpec.methodBuilder("loadInto")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(ParameterizedTypeName.get(map, string, routeModel), "providers");
        if (providerMap != null) {
            Map<String, TypeElement> provids = providerMap.getMap();
            for (Map.Entry<String, TypeElement> stringTypeElementEntry : provids.entrySet()) {
                String path = stringTypeElementEntry.getKey();
                TypeElement typeElement = stringTypeElementEntry.getValue();
                Name qualifiedName = typeElement.getQualifiedName();
                List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
                String interfaceStr = "";
                if (interfaces != null && interfaces.size() > 0) {
                    interfaceStr = interfaces.get(0).toString();
                    messager.printMessage(Diagnostic.Kind.NOTE, " ssssss=" + interfaceStr);
                }
                builder.addStatement("providers.put($S,new RouteModel($T.$L, $S, $T.class))",
                        interfaceStr, RouteType.class, RouteType.PROVIDER.toString(), path, typeElement.asType());
            }
        }
        String fileName = "ZRouter" + divider + "Providers" + divider + moduleName;
        TypeElement typeProviderGroup = elementUtils.getTypeElement("com.example.myzrouter.Interface.IProviderGroup");
        TypeSpec groupFileSpec = TypeSpec.classBuilder(fileName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(typeProviderGroup))
                .addMethod(builder.build())
                .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_GENERATED_FILE, groupFileSpec)
                .build();

        try {
            messager.printMessage(Diagnostic.Kind.NOTE, "开始写Providers文件！");
            javaFile.writeTo(filer);
            messager.printMessage(Diagnostic.Kind.NOTE, "写Providers文件结束了！！");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createGroupFiles(Map<String, CompilerRouteModel> routeMap) {
        TypeElement typeRouteGroup = elementUtils.getTypeElement("com.example.myzrouter.Interface.IRouteGroup");
        groupFileMap.clear();
        // 为每个groupname创建一个类
        for (Map.Entry<String, CompilerRouteModel> stringGroupRouteModelEntry : routeMap.entrySet()) {
            String groupName = stringGroupRouteModelEntry.getKey();
            CompilerRouteModel compilerRouteModel = stringGroupRouteModelEntry.getValue();
            MethodSpec groupMethodSpec = createGroupMethodSpec(compilerRouteModel.getMap());
            if (groupMethodSpec == null) {
                return;
            }
            String fileName = "ZRouter" + divider + "Group" + divider + groupName;

            TypeSpec groupFileSpec = TypeSpec.classBuilder(fileName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(typeRouteGroup))
                    .addMethod(groupMethodSpec)
                    .build();

            JavaFile javaFile = JavaFile.builder(PACKAGE_GENERATED_FILE, groupFileSpec)
                    .build();

            try {
                messager.printMessage(Diagnostic.Kind.NOTE, "开始写Group文件！");
                javaFile.writeTo(filer);
                messager.printMessage(Diagnostic.Kind.NOTE, "写Group文件结束了！！");
            } catch (IOException e) {
                e.printStackTrace();
            }
            groupFileMap.put(groupName, fileName);
        }

    }

    /**
     * @param groupFileMap
     */
    private void createRootFile(Map<String, String> groupFileMap) {
        /*public class ZRouter$$Root$$app implements IRouteRoot {
            @Override
            public void loadInto(Map<String, Class<? extends IRouteGroup>> routes) {
                routes.put("app", ZRouter$$Group$$app.class);
            }
        }*/

        //Map<Integer, ? extends T> map
        //addParameter(ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(Integer.class), WildcardTypeName.subtypeOf(TypeVariableName.get("T"))), "map")
        TypeElement typeRouteGroup = elementUtils.getTypeElement("com.example.myzrouter.Interface.IRouteGroup");
        TypeElement typeRouteRoot = elementUtils.getTypeElement("com.example.myzrouter.Interface.IRouteRoot");
        ClassName map = ClassName.get("java.util", "Map");
        ClassName string = ClassName.get("java.lang", "String");
        //Class<? extends IRouteGroup>
        ParameterizedTypeName parameterizedTypeNameClass = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(typeRouteGroup)));
        //Map<String, Class<? extends IRouteGroup>>
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(map, string, parameterizedTypeNameClass);
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("loadInto")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterizedTypeName, "routes")
                .returns(TypeName.VOID);

        for (Map.Entry<String, String> stringClassEntry : groupFileMap.entrySet()) {
            methodSpecBuilder.addStatement("routes.put($S,$T.class)",
                    stringClassEntry.getKey(), ClassName.get(PACKAGE_GENERATED_FILE, stringClassEntry.getValue()));
        }
        TypeSpec rootFileSpec = TypeSpec.classBuilder("ZRouter" + divider + "Root" + divider + moduleName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(typeRouteRoot))
                .addMethod(methodSpecBuilder.build())
                .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_GENERATED_FILE, rootFileSpec)
                .build();
        try {
            messager.printMessage(Diagnostic.Kind.NOTE, "开始写Root文件！");
            javaFile.writeTo(filer);
            messager.printMessage(Diagnostic.Kind.NOTE, "写Root文件结束了！！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据<path, typeElement>集合生成方法体
     *
     * @param pathMap
     * @return
     */
    private MethodSpec createGroupMethodSpec(Map<String, TypeElement> pathMap) {
        RouteType routeType = null;
        ClassName map = ClassName.get("java.util", "Map");
        ClassName string = ClassName.get("java.lang", "String");
        ClassName routeModel = ClassName.get(PACKAGE_GENERATED_FILE, "RouteModel");

        MethodSpec.Builder loadIntoMethodBuilder = MethodSpec.methodBuilder("loadInto")
                //Map<String, CompilerRouteModel> atlas
                .addParameter(ParameterizedTypeName.get(map, string, routeModel), "atlas")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.VOID);
        //从pathMap 生成方法体
        for (Map.Entry<String, TypeElement> stringTypeElementEntry : pathMap.entrySet()) {
            String path = stringTypeElementEntry.getKey();
            TypeElement typeElement = stringTypeElementEntry.getValue();
            TypeMirror tm = typeElement.asType();
            boolean assignableFromActivity = isActivity(tm);
            boolean assignableFromProvider = isProvider(tm);
            if (assignableFromActivity) {
                routeType = RouteType.ACTIVITY;
            } else if (assignableFromProvider) {
                routeType = RouteType.PROVIDER;
                if (providerMap == null) {
                    providerMap = new CompilerRouteModel();
                }
                providerMap.putElement(path, typeElement);
            }
            if (routeType == null) {
                messager.printMessage(Diagnostic.Kind.NOTE, "咱不支持此类型的文件 path=" + path);
                return null;
            }
            //        @Override
//        public void loadInto(Map<String, CompilerRouteModel> atlas) {
//            atlas.put("/main/activity", new RouteModel(RouteType.ACTIVITY,
//                    "/main/activity", MainActivity.class));
//        }
            loadIntoMethodBuilder.addStatement("atlas.put($S,new RouteModel($T.$L, $S, $T.class))",
                    path, RouteType.class, routeType.toString(), path, typeElement.asType());
        }
        return loadIntoMethodBuilder.build();
    }
}
