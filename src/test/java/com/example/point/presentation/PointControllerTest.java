package com.example.point.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.point.ControllerTestSupport;
import com.example.point.domain.model.PointRewardType;
import com.example.point.domain.model.PointStatus;
import com.example.point.presentation.request.PointCreateRequest;
import com.example.point.presentation.request.PointUseCancelRequest;
import com.example.point.presentation.request.PointUseRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Sql("/sql/point-controller-test-data.sql")
@Transactional
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SpringBootTest
class PointControllerTest extends ControllerTestSupport {

    @DisplayName("포인트 적립 테스트")
    @Test
    void savePoint() throws Exception {

        // given
        Long userKey = 1L;
        int amount = 1000;
        PointRewardType rewardType = PointRewardType.MANUAL;

        PointCreateRequest request = new PointCreateRequest(userKey, amount, null, rewardType);

        // when & then
        mockMvc.perform(post("/points/save")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userKey").value(userKey))
            .andExpect(jsonPath("$.status").value(PointStatus.ACTIVE.name()))
            .andExpect(jsonPath("$.totalAmount").value(amount))
            .andExpect(jsonPath("$.remainAmount").value(amount))
            .andExpect(jsonPath("$.rewardType").value(rewardType.name()));
    }

    @DisplayName("포인트 적립 취소 테스트")
    @Test
    void saveCancelPoint() throws Exception {

        // given
        Long pointKey = 1L;

        // when & then
        mockMvc.perform(post("/points/save-cancel/{pointKey}", pointKey))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pointKey").value(pointKey))
            .andExpect(jsonPath("$.status").value(PointStatus.CANCELED.name()))
            .andExpect(jsonPath("$.remainAmount").value(0));
    }

    @DisplayName("포인트 사용 테스트")
    @Test
    void usePoint() throws Exception {

        // given
        Long userKey = 1L;
        Long orderKey = 1234L;
        int amount = 1300;

        PointUseRequest request = new PointUseRequest(userKey, orderKey, amount);

        // when & then
        mockMvc.perform(post("/points/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].remainAmount").value(0))
            .andExpect(jsonPath("$[1].remainAmount").value(200));
    }

    @DisplayName("포인트 사용 취소 테스트")
    @Test
    void useCancelPoint() throws Exception {

        // given
        Long userKey = 2L;
        Long orderKey = 1234L;

        PointUseCancelRequest request = new PointUseCancelRequest(userKey, orderKey);

        // when & then
        mockMvc.perform(post("/points/use-cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].userKey").value(userKey))
            .andExpect(jsonPath("$[0].remainAmount").value(500))
            .andExpect(jsonPath("$[1].userKey").value(userKey))
            .andExpect(jsonPath("$[1].remainAmount").value(100));
    }
}