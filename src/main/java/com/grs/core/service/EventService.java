package com.grs.core.service;

import com.grs.api.annotation.EventListener;
import com.grs.api.annotation.EventPublisher;
import com.grs.api.model.EventSubscriber;
import com.grs.utils.ContextUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;

@Service
@Aspect
public class EventService implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private WeakHashMap<String, List<EventSubscriber>> eventListeners = new WeakHashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        eventListeners.clear();
        WeakHashMap<String, List<Method>> methodMap = ContextUtils.findServicesWithMethodAnnotation(applicationContext, EventListener.class);
        for (Map.Entry<String, List<Method>> entry : methodMap.entrySet()) {
            for (Method method : entry.getValue()) {
                String[] events = method.getAnnotation(EventListener.class).value();
                for (String event : events) {
                    subscribe(event, entry.getKey());
                }
            }
        }
    }

    public synchronized void subscribe(String event, String... listenerNames) {
        List<EventSubscriber> subscribers = new ArrayList<>();
        for (String listenerName : listenerNames) {
            Object bean = applicationContext.getBean(listenerName);
            Class<?> listenerType = bean.getClass();
            if (Advised.class.isAssignableFrom(listenerType)) {
                listenerType = ((Advised) bean).getTargetSource().getTargetClass();
            }
            Method[] methods = listenerType.getMethods();
            List<Method> subscribedMethods = new ArrayList<>();
            for (Method method : methods) {
                if (method.isAnnotationPresent(EventListener.class)) {
                    if (Arrays.asList(method.getAnnotation(EventListener.class).value()).contains(event)) {
                        subscribedMethods.add(method);
                    }
                }
            }
            EventSubscriber eventSubscriber = new EventSubscriber(listenerName, subscribedMethods);
            subscribers.add(eventSubscriber);
        }
        getListenersForEvent(event, true).addAll(subscribers);
    }

    public void unSubscribe(String event, String... listenerNames) {
        getListenersForEvent(event, false).removeAll(Arrays.asList(listenerNames));
    }

    private List<EventSubscriber> getListenersForEvent(String value, boolean create) {
        List<EventSubscriber> listeners = eventListeners.get(value);
        if (listeners == null && create) {
            listeners = new ArrayList<>();
            eventListeners.put(value, listeners);
        }
        return listeners;
    }

    public void publish(String event, Object... args) {
        List<EventSubscriber> listenersForEvent = getListenersForEvent(event, false);
        if (listenersForEvent != null) {
            for (EventSubscriber listener : listenersForEvent) {
                Object bean = applicationContext.getBean(listener.getBeanName());
                for (Method method : listener.getMethods()) {
                    try {
                        method.invoke(bean, args);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Around(value = "@annotation(eventPublisher)")
    public Object handleBroadcast(ProceedingJoinPoint pjp, EventPublisher eventPublisher) throws Throwable {
        Object retVal = pjp.proceed();
        String[] events = eventPublisher.value();
        for (String event : events) {
            publish(event, retVal);
        }
        return retVal;
    }
}
