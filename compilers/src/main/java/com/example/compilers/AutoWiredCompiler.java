package com.example.compilers;

import com.example.annotations.AutoWired;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("com.example.annotations.AutoWired")
public class AutoWiredCompiler extends BaseProcessor {
    private Map<TypeElement, List<Element>> autoWiredMemberInParent = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Map<String, String> options = processingEnv.getOptions();
        String arouter_module_name = options.get("AROUTER_MODULE_NAME");
        messager.printMessage(Diagnostic.Kind.NOTE, "module name is " + arouter_module_name);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (alreadyHandledModule.contains(moduleName)) {
            return false;
        }
        alreadyHandledModule.add(moduleName);

        //收集所有AutoWired信息及包裹类信息
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoWired.class);
        for (Element element : elements) {
            VariableElement variableElement = (VariableElement) element;
            AutoWired annotation = element.getAnnotation(AutoWired.class);
            String autoWiredName = annotation.name();
            TypeElement parentElement = (TypeElement) variableElement.getEnclosingElement();
            if (autoWiredMemberInParent.get(parentElement) == null) {
                List<Element> autoWiredMembers = new ArrayList<>();
                autoWiredMemberInParent.put(parentElement, autoWiredMembers);
            }
            autoWiredMemberInParent.get(parentElement).add(element);
        }

        /*public class MainActivity$$ZRouter$$AutoWired implements IAutoWiredInject {
            @Override
            public void inject(Object object) {
                MainActivity substitute = (MainActivity)object;
                ((MainActivity)object).extra = substitute.getIntent().getStringExtra("ok");
                substitute.weatherService = (IWeatherService) ZRouter.getInstance().build("/wetherservice/getinfo").navigation();
            }
        }*/
        for (Map.Entry<TypeElement, List<Element>> typeElementListEntry : autoWiredMemberInParent.entrySet()) {
            TypeElement parent = typeElementListEntry.getKey();
            List<Element> autoWiredElement = typeElementListEntry.getValue();
            //获取含有AutoWired成员的类名，每个类生成一个文件
            Name parentSimpleName = parent.getSimpleName();
            messager.printMessage(Diagnostic.Kind.NOTE, "parentSimpleName=" + parentSimpleName);
            Name parentQualifiedName = parent.getQualifiedName();
            TypeMirror parentType = parent.asType();
//            @Override
//            public void inject(Object object) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("inject")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addAnnotation(Override.class)
                    .addParameter(Object.class, "object")
                    .addStatement("$T substitute = ($T)object", parentType, parentType);
            for (Element element : autoWiredElement) {
                messager.printMessage(Diagnostic.Kind.NOTE, "elment="+element.asType().toString());
                VariableElement variableElement = (VariableElement) element;
                //判断变量类型是否是provider类型
                if (isProvider(element.asType())) {
                    addProviderStatement(variableElement, methodSpecBuilder);
                } else if (isActivity(parent.asType())) {
                    //判断包裹类是否是activity，是则认为内部autowired变量为intent支持的类型
                    addIntentStatement(parent, variableElement, methodSpecBuilder);
                } else {
                    messager.printMessage(Diagnostic.Kind.NOTE, "此变量不是intent类型也不是provider类型" + element.getSimpleName());
                }
            }
//            public class MainActivity$$ZRouter$$AutoWired implements IAutoWiredInject {
            String fileName = parentSimpleName + divider + "ZRouter" + divider + "AutoWired";
            TypeElement autoWiredTypeElement = elementUtils.getTypeElement(PACKAGE_GENERATED_FILE + ".Interface.IAutoWiredInject");
            TypeSpec typeSpec = TypeSpec.classBuilder(fileName)
                    .addSuperinterface(ClassName.get(autoWiredTypeElement))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodSpecBuilder.build())
                    .build();

            JavaFile javaFile = JavaFile.builder(parentQualifiedName.toString().replace("." + parentSimpleName.toString(), ""), typeSpec)
                    .build();
            try {
                messager.printMessage(Diagnostic.Kind.NOTE, "开始写AutoWired文件！");
                javaFile.writeTo(filer);
                messager.printMessage(Diagnostic.Kind.NOTE, "写开始写AutoWired文件结束了！！");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return false;
    }

    private void addProviderStatement(VariableElement variableElement, MethodSpec.Builder methodSpecBuilder) {
//        substitute.weatherService = (IWeatherService) ZRouter.getInstance().build("/wetherservice/getinfo").navigation();
        ClassName ZRouterClass = ClassName.get(PACKAGE_GENERATED_FILE, "ZRouter");
        AutoWired annotation = variableElement.getAnnotation(AutoWired.class);
        String name = annotation.name();
        String simpleName = variableElement.getSimpleName().toString();
        TypeMirror typeMirror = variableElement.asType();
        TypeName typeName = ClassName.get(typeMirror);
        if (!"".equals(name)) {
            methodSpecBuilder.addStatement("substitute." + simpleName + " = ($T) $T.getInstance().build($S).navigation()", typeName, ZRouterClass, name);
        } else {
            methodSpecBuilder.addStatement("substitute." + simpleName + " = ($T) $T.getInstance().navigation($T.class)", typeName, ZRouterClass, typeName);
        }
    }

    private void addIntentStatement(TypeElement parent, VariableElement variableElement, MethodSpec.Builder methodSpecBuilder) {
        AutoWired annotation = variableElement.getAnnotation(AutoWired.class);
        String name = annotation.name();
        String simpleName = variableElement.getSimpleName().toString();
        TypeMirror typeMirror = variableElement.asType();
        TypeName typeName = ClassName.get(typeMirror);
//        ((MainActivity)object).extra = substitute.getIntent().getStringExtra("ok");
        TypeElement string = elementUtils.getTypeElement("java.lang.String");
        TypeElement anInt = elementUtils.getTypeElement("java.lang.Integer");
        TypeElement aBoolean = elementUtils.getTypeElement("java.lang.Boolean");
        if (types.isSameType(typeMirror, string.asType())) {
            methodSpecBuilder.addStatement("(($T)object)." + simpleName + " = substitute.getIntent().getStringExtra($S)",parent, name);
        } else if (types.isSameType(typeMirror, anInt.asType())) {
            methodSpecBuilder.addStatement("(($T)object)." + simpleName + " = substitute.getIntent().getIntExtra($S)",parent, name);
        } else if (types.isSameType(typeMirror, aBoolean.asType())) {
            methodSpecBuilder.addStatement("(($T)object)." + simpleName + " = substitute.getIntent().getBooleanExtra($S)",parent, name);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "不支持此类型，这里只是示例，只支持string， boolean，int 三种类型");
        }
    }

}
