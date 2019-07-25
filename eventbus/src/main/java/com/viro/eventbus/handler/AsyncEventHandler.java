package com.viro.eventbus.handler;

import android.os.Handler;
import android.os.HandlerThread;

import com.viro.eventbus.Subscription;


/**
 * 事件的异步处理,将事件的处理函数执行在子线程中
 */
public class AsyncEventHandler implements EventHandler {

    /**
     * 事件分发线程
     */
    DispatcherThread mDispatcherThread;

    /**
     * 事件处理器
     */
    EventHandler mEventHandler = new DefaultEventHandler();

    public AsyncEventHandler() {
        mDispatcherThread = new DispatcherThread(AsyncEventHandler.class.getSimpleName());
        mDispatcherThread.start();
    }

    /**
     * 将订阅的函数执行在异步线程中
     * 
     * @param subscription
     * @param event
     */
    public void handleEvent(final Subscription subscription, final Object event) {
        mDispatcherThread.post(new Runnable() {

            @Override
            public void run() {
                mEventHandler.handleEvent(subscription, event);
            }
        });
    }

    /**
     */
    class DispatcherThread extends HandlerThread {

        /**
         * 关联了AsyncExecutor消息队列的Handler
         */
        protected Handler mAsyncHandler;

        /**
         * @param name
         */
        public DispatcherThread(String name) {
            super(name);
        }

        /**
         * @param runnable
         */
        public void post(Runnable runnable) {
            if (mAsyncHandler == null) {
                throw new NullPointerException("mAsyncHandler == null, please call start() first.");
            }

            mAsyncHandler.post(runnable);
        }

        @Override
        public synchronized void start() {
            super.start();
            mAsyncHandler = new Handler(this.getLooper());
        }

    }

}
