package com.viro.eventbus.handler;

import android.os.Handler;
import android.os.Looper;

import com.viro.eventbus.Subscription;

/**
 * 事件处理在UI线程,通过Handler将事件处理post到UI线程的消息队列
 */
public class UIThreadEventHandler implements EventHandler {

    /**
     * ui handler
     */
    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    /**
     * 
     */
    private DefaultEventHandler mEventHandler = new DefaultEventHandler();

    /**
     * @param subscription
     * @param event
     */
    public void handleEvent(final Subscription subscription, final Object event) {
        mUIHandler.post(new Runnable() {

            @Override
            public void run() {
                mEventHandler.handleEvent(subscription, event);
            }
        });
    }

}
