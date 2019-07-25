package com.viro.eventbus.matchpolicy;

import com.viro.eventbus.EventType;

import java.util.LinkedList;
import java.util.List;

public class DefaultMatchPolicy implements MatchPolicy {

    @Override
    public List<EventType> findMatchEventTypes(EventType type, Object aEvent) {
        Class<?> eventClass = aEvent.getClass();
        List<EventType> result = new LinkedList<EventType>();
        while (eventClass != null) {
            result.add(new EventType(eventClass, type.tag));
            addInterfaces(result, eventClass, type.tag);
            eventClass = eventClass.getSuperclass();
        }

        return result;
    }

    /**
     * 获取该事件的所有接口类型
     * 
     * @param eventTypes 存储列表
     * @param eventClass 事件实现的所有接口
     */
    private void addInterfaces(List<EventType> eventTypes, Class<?> eventClass, String tag) {
        if (eventClass == null) {
            return;
        }
        Class<?>[] interfacesClasses = eventClass.getInterfaces();
        for (Class<?> interfaceClass : interfacesClasses) {
            if (!eventTypes.contains(interfaceClass)) {
                eventTypes.add(new EventType(interfaceClass, tag));
                addInterfaces(eventTypes, interfaceClass, tag);
            }
        }
    }

}
