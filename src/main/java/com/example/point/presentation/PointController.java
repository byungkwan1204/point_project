package com.example.point.presentation;

import com.example.point.presentation.request.PointCreateRequest;
import com.example.point.presentation.request.PointUseCancelRequest;
import com.example.point.presentation.request.PointUseRequest;
import com.example.point.presentation.response.PointResponse;
import com.example.point.service.usecase.PointUsecase;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("points")
@RequiredArgsConstructor
public class PointController {

    private final PointUsecase pointUsecase;


    @PostMapping("save")
    public ResponseEntity<PointResponse> savePoint(@RequestBody @Valid PointCreateRequest request) throws Exception {
        return ResponseEntity.ok(pointUsecase.savePoint(request));
    }

    @PostMapping("save-cancel/{pointKey}")
    public ResponseEntity<PointResponse> saveCancelPoint(@PathVariable("pointKey") Long pointKey) throws Exception {
        return ResponseEntity.ok(pointUsecase.saveCancelPoint(pointKey));
    }

    @PostMapping("use")
    public ResponseEntity<List<PointResponse>> usePoint(@RequestBody @Valid PointUseRequest request) throws Exception {
        return ResponseEntity.ok(pointUsecase.usePoint(request));
    }

    @PostMapping("use-cancel")
    public ResponseEntity<List<PointResponse>> useCancelPoint(@RequestBody @Valid PointUseCancelRequest request) throws Exception {
        return ResponseEntity.ok(pointUsecase.useCancelPoint(request));
    }
}
