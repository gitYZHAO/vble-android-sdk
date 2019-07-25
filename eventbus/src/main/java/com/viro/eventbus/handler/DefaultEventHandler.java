package com.viro.eventbus.handler;

import com.viro.eventbus.Subscription;
import java.lang.reflect.InvocationTargetException;

/**
 * 事件在哪个线程post,事件的接收就在哪个线程
 */
public class DefaultEventHandler implements EventHandler {
    /**
     * handle the event
     * 
     * @param subscription
     * @param event
     */
    public void handleEvent(Subscription subscription, Object event) {
        if (subscription == null
                || subscription.subscriber.get() == null) {
            return;
        }
        try {
            // 执行
            subscription.targetMethod.invoke(subscription.subscriber.get(), event);
        } catch (IllegalArgumentException
                | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
