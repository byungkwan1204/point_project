package com.example.point.service.usecase;

import com.example.point.presentation.request.PointUseCancelRequest;
import com.example.point.presentation.request.PointCreateRequest;
import com.example.point.presentation.request.PointUseRequest;
import com.example.point.domain.model.Point;
import java.util.List;

public interface PointUsecase {

    // 포인트 적립
    Point savePoint(PointCreateRequest pointCreateRequest);

    // 포인트 적립 취소
    Point cancelSavePoint(Long pointKey);

    // 포인트 사용
    List<Point> usePoint(PointUseRequest pointUseRequest);

    // 포인트 사용 취소
    List<Point> cancelUsePoint(PointUseCancelRequest pointUseCancelRequest);

}
