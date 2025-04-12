package com.example.point.domain.event.trigger;

import com.example.point.domain.model.Point;
import com.example.point.domain.model.PointHistoryActionType;

public record PointHistoryEvent(

    Point point,
    int amount,
    PointHistoryActionType actionType,
    Long relatedHistoryKey,
    Long orderKey
) {
    public static PointHistoryEvent save(Point point, int amount) {
        return new PointHistoryEvent(point, amount, PointHistoryActionType.SAVE, null, null);
    }

    public static PointHistoryEvent reSave(Point point, int amount, Long relatedHistoryKey) {
        return new PointHistoryEvent(point, amount, PointHistoryActionType.SAVE, relatedHistoryKey, null);
    }

    public static PointHistoryEvent saveCancel(Point point, int amount, Long relatedHistoryKey) {
        return new PointHistoryEvent(point, amount, PointHistoryActionType.SAVE_CANCEL, relatedHistoryKey, null);
    }

    public static PointHistoryEvent use(Point point, int amount, Long orderKey) {
        return new PointHistoryEvent(point, amount, PointHistoryActionType.USE, null, orderKey);
    }

    public static PointHistoryEvent useCancel(Point point, int amount, Long relatedHistoryKey, Long orderKey) {
        return new PointHistoryEvent(point, amount, PointHistoryActionType.USE_CANCEL, relatedHistoryKey, orderKey);
    }

    public static PointHistoryEvent expired(Point point) {
        return new PointHistoryEvent(point, point.getRemainAmount(), PointHistoryActionType.EXPIRED, null ,null);
    }
}
