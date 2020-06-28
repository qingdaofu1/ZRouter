package com.example.compilers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


public class BaseProcessor extends AbstractProcessor {
    public static final String PACKAGE_GENERATED_FILE = "com.example.myzrouter";
    Messager messager;
    Elements elementUtils;
    public Filer filer;
    Types types;
    public String moduleName;
    public static final String divider = "$$$";
    public static List<String> alreadyHandledModule = new ArrayList<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    public boolean isActivity(TypeMirror tm) {
        TypeElement activity = elementUtils.getTypeElement("android.app.Activity");
        if (activity == null) {
            return false;
        }

        return types.isSubtype(tm, activity.asType());
    }

    public boolean isProvider(TypeMirror tm) {
        TypeElement provider = elementUtils.getTypeElement("com.example.myzrouter.Interface.IProvider");
        if (provider == null) {
            return false;
        }
        return types.isSubtype(tm, provider.asType());
    }
}
