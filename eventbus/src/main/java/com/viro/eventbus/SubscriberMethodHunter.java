package com.viro.eventbus;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * the subscriber method hunter, find all of the subscriber's methods which
 * annotated with @Subscriber.
 */
public class SubscriberMethodHunter {

    /**
     * the event bus's subscriber's map
     */
    private Map<EventType, CopyOnWriteArrayList<Subscription>> mSubscriberMap;

    /**
     * @param subscriberMap
     */
    public SubscriberMethodHunter(Map<EventType, CopyOnWriteArrayList<Subscription>> subscriberMap) {
        mSubscriberMap = subscriberMap;
    }

    /**
     * 查找订阅对象中的所有订阅函数,订阅函数的参数只能有一个.找到订阅函数之后构建Subscription存储到Map中
     * 
     * @param subscriber 订阅对象
     */
    public void findSubscribeMethods(Object subscriber) {
        if (mSubscriberMap == null) {
            throw new NullPointerException("the mSubscriberMap is null. ");
        }
        Class<?> clazz = subscriber.getClass();
        // 查找类中符合要求的注册方法,直到Object类
        while (clazz != null && !isSystemClass(clazz.getName())) {
            final Method[] Methods = clazz.getDeclaredMethods();
            for (int i = 0; i < Methods.length; i++) {
                Method method = Methods[i];
                // 根据注解来解析函数
                Subscriber annotation = method.getAnnotation(Subscriber.class);
                if (annotation != null) {
                    // 获取方法参数
                    Class<?>[] paramsTypeClass = method.getParameterTypes();
                    // 订阅函数只支持一个参数
                    if (paramsTypeClass != null && paramsTypeClass.length == 1) {
                        Class<?> paramType = convertType(paramsTypeClass[0]);
                        EventType eventType = new EventType(paramType, annotation.tag());
                        TargetMethod subscribeMethod = new TargetMethod(method, eventType,
                                annotation.mode());
                        subscribe(eventType, subscribeMethod, subscriber);
                    }
                }
            }

            // 获取父类,以继续查找父类中符合要求的方法
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 按照EventType存储订阅者列表,这里的EventType就是事件类型,一个事件对应0到多个订阅者.
     * 
     * @param event 事件
     * @param method 订阅方法对象
     * @param subscriber 订阅者
     */
    private void subscribe(EventType event, TargetMethod method, Object subscriber) {
        CopyOnWriteArrayList<Subscription> subscriptionLists = mSubscriberMap.get(event);
        if (subscriptionLists == null) {
            subscriptionLists = new CopyOnWriteArrayList<Subscription>();
        }

        Subscription newSubscription = new Subscription(subscriber, method);
        if (subscriptionLists.contains(newSubscription)) {
            return;
        }

        subscriptionLists.add(newSubscription);
        // 将事件类型key和订阅者信息存储到map中
        mSubscriberMap.put(event, subscriptionLists);
    }

    /**
     * remove subscriber methods from map
     */
    public void removeMethodsFromMap(Object subscriber) {
        Iterator<CopyOnWriteArrayList<Subscription>> iterator = mSubscriberMap
                .values().iterator();
        while (iterator.hasNext()) {
            CopyOnWriteArrayList<Subscription> subscriptions = iterator.next();
            if (subscriptions != null) {
                List<Subscription> foundSubscriptions = new
                        LinkedList<Subscription>();
                Iterator<Subscription> subIterator = subscriptions.iterator();
                while (subIterator.hasNext()) {
                    Subscription subscription = subIterator.next();
                    // 获取引用
                    Object cacheObject = subscription.subscriber.get();
                    if (isObjectsEqual(cacheObject, subscriber)
                            || cacheObject == null) {
                        Log.d("", "### 移除订阅 " + subscriber.getClass().getName());
                        foundSubscriptions.add(subscription);
                    }
                }

                // 移除该subscriber的相关的Subscription
                subscriptions.removeAll(foundSubscriptions);
            }

            // 如果针对某个Event的订阅者数量为空了,那么需要从map中清除
            if (subscriptions == null || subscriptions.size() == 0) {
                iterator.remove();
            }
        }
    }

    private boolean isObjectsEqual(Object cachedObj, Object subscriber) {
        return cachedObj != null
                && cachedObj.equals(subscriber);
    }

    /**
     * if the subscriber method's type is primitive, convert it to corresponding
     * Object type. for example, int to Integer.
     * 
     * @param eventType origin Event Type
     * @return
     */
    private Class<?> convertType(Class<?> eventType) {
        Class<?> returnClass = eventType;
        if (eventType.equals(boolean.class)) {
            returnClass = Boolean.class;
        } else if (eventType.equals(int.class)) {
            returnClass = Integer.class;
        } else if (eventType.equals(float.class)) {
            returnClass = Float.class;
        } else if (eventType.equals(double.class)) {
            returnClass = Double.class;
        }

        return returnClass;
    }

    private boolean isSystemClass(String name) {
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.");
    }

}
