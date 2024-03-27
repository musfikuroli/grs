package com.grs.api.model;

import java.lang.reflect.Method;
import java.util.List;


public class EventSubscriber {

    private String beanName;

    private List<Method> methods;

    public EventSubscriber(String beanName, List<Method> methods) {
        this.beanName = beanName;
        this.methods = methods;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }
}
