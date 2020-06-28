package com.example.compilers;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;

public class CompilerRouteModel {
    Map<String, TypeElement> map = new HashMap<>();

    public CompilerRouteModel() {
    }

    /**
     * 放入path String和TypeElement的map，这些都是需要创建的方法索引
     *
     * @param path        路径
     * @param typeElement
     */
    public void putElement(String path, TypeElement typeElement) {
        map.put(path, typeElement);
    }

    public Map<String, TypeElement> getMap(){
        return map;
    }
}
