package com.viro.eventbus;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * 订阅者对象,包含订阅者和目标方法
 */
public class Subscription {
    /**
     * 订阅者对象
     */
    public Reference<Object> subscriber;
    /**
     * 接受者的方法
     */
    public Method targetMethod;
    /**
     * 执行事件的线程模型
     */
    public ThreadMode threadMode;
    /**
     * 事件类型
     */
    public EventType eventType;

    /**
     * @param subscriber
     * @param targetMethod
     */
    public Subscription(Object subscriber, TargetMethod targetMethod) {
        this.subscriber = new WeakReference<Object>(subscriber);
        this.targetMethod = targetMethod.method;
        this.threadMode = targetMethod.threadMode;
        this.eventType = targetMethod.eventType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subscriber == null) ? 0 : subscriber.hashCode());
        result = prime * result + ((targetMethod == null) ? 0 : targetMethod.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Subscription other = (Subscription) obj;
        if (subscriber.get() == null) {
            if (other.subscriber.get() != null)
                return false;
        } else if (!subscriber.get().equals(other.subscriber.get()))
            return false;
        if (targetMethod == null) {
            return other.targetMethod == null;
        } else if (!targetMethod.equals(other.targetMethod))
            return false;
        return true;
    }

}
