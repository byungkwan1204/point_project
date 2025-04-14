package com.example.point.domain.event.trigger;

import com.example.point.domain.model.PointHistoryActionType;

public record PointHistoryEvent(

    Long pointKey,
    int amount,
    PointHistoryActionType actionType,
    Long relatedHistoryKey,
    Long orderKey
) {

    public static PointHistoryEvent save(Long pointKey, int amount) {
        return new PointHistoryEvent(pointKey, amount, PointHistoryActionType.SAVE, null, null);
    }

    public static PointHistoryEvent reSave(Long pointKey, int amount, Long relatedHistoryKey) {
        return new PointHistoryEvent(pointKey, amount, PointHistoryActionType.SAVE, relatedHistoryKey, null);
    }

    public static PointHistoryEvent saveCancel(Long pointKey, int amount, Long relatedHistoryKey) {
        return new PointHistoryEvent(pointKey, amount, PointHistoryActionType.SAVE_CANCEL, relatedHistoryKey, null);
    }

    public static PointHistoryEvent use(Long pointKey, int amount, Long orderKey) {
        return new PointHistoryEvent(pointKey, amount, PointHistoryActionType.USE, null, orderKey);
    }

    public static PointHistoryEvent useCancel(Long pointKey, int amount, Long relatedHistoryKey, Long orderKey) {
        return new PointHistoryEvent(pointKey, amount, PointHistoryActionType.USE_CANCEL, relatedHistoryKey, orderKey);
    }

    public static PointHistoryEvent expired(Long pointKey, int amount) {
        return new PointHistoryEvent(pointKey, amount, PointHistoryActionType.EXPIRED, null ,null);
    }
}
