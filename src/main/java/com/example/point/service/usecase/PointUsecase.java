package com.example.point.service.usecase;

import com.example.point.presentation.request.PointCreateRequest;
import com.example.point.presentation.request.PointUseCancelRequest;
import com.example.point.presentation.request.PointUseRequest;
import com.example.point.presentation.response.PointResponse;
import java.util.List;

public interface PointUsecase {

    // 포인트 적립
    PointResponse savePoint(PointCreateRequest pointCreateRequest) throws Exception;

    // 포인트 적립 취소
    PointResponse saveCancelPoint(Long pointKey) throws Exception;

    // 포인트 사용
    List<PointResponse> usePoint(PointUseRequest pointUseRequest) throws Exception;

    // 포인트 사용 취소
    List<PointResponse> useCancelPoint(PointUseCancelRequest pointUseCancelRequest) throws Exception;

}
