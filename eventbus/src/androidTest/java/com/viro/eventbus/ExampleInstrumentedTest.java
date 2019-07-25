package com.viro.eventbus;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    final private String TAG = "EventBusTest";
    EventBus eb;

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.viro.eventbus.test", appContext.getPackageName());

        eb = EventBus.getDefault();
        eb.register(this);

        Log.d(TAG,"Post a Message in Thread Name:[" + Thread.currentThread().getName() + "]");
        eb.post("hello");

    }

    @Subscriber(mode = ThreadMode.ASYNC)
    public void methodASYNC(String s) {
        Log.d(TAG,"Thread Name:[" + Thread.currentThread().getName() +
                "], Call methodASYNC: " + s);
    }
}
