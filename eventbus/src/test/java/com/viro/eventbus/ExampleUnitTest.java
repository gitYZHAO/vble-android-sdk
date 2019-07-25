package com.viro.eventbus;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        Class[] typeParam = new Class[0];
        try {
            ParameterizedType type = (ParameterizedType) SubClass.class
                    .getMethod("getValue", typeParam).getGenericReturnType();

            for (Type t : type.getActualTypeArguments()) {
                System.out.println(t);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    class SupperClass{}

    class SubClass extends SupperClass{
        public List<Map<Integer,String>> getValue(){
            return null;
        }
    }

}