package com.viro.eventbus.matchpolicy;

import com.viro.eventbus.EventType;
import java.util.List;

/**
 */
public interface MatchPolicy {
    List<EventType> findMatchEventTypes(EventType type, Object aEvent);
}
