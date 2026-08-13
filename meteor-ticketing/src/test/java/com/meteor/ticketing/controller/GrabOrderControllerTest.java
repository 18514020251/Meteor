package com.meteor.ticketing.controller;

import com.meteor.api.enums.GrabOrderResultEnum;
import com.meteor.common.exception.GlobalExceptionHandler;
import com.meteor.satoken.context.LoginContext;
import com.meteor.ticketing.controller.vo.GrabOrderVO;
import com.meteor.ticketing.service.IGrabOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 *  抢单控制器测试类
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-13
 */
class GrabOrderControllerTest {

    private IGrabOrderService grabOrderService;
    private LoginContext loginContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        grabOrderService = mock(IGrabOrderService.class);
        loginContext = mock(LoginContext.class);

        GrabOrderController controller =
                new GrabOrderController(
                        grabOrderService,
                        loginContext
                );

        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @DisplayName("缺少 clientRequestId 时应返回参数错误")
    @Test
    void grabShouldRejectMissingClientRequestId() throws Exception {
        mockMvc.perform(
                post("/ticketing/order/grab")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "screeningId": 2001
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("客户端请求标识不能为空"));

        verifyNoInteractions(loginContext, grabOrderService);
    }

    @DisplayName("clientRequestId 超过64个字符时应返回参数错误")
    @Test
    void grabShouldRejectTooLongClientRequestId() throws Exception {
        String clientRequestId = "a".repeat(65);
        mockMvc.perform(
                        post("/ticketing/order/grab")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "screeningId": 2001,
                                      "clientRequestId": "%s"
                                    }
                                    """.formatted(clientRequestId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("客户端请求标识长度不能超过64"));

        verifyNoInteractions(loginContext, grabOrderService);
    }

    @DisplayName("合法 clientRequestId 时应正常进入抢票业务")
    @Test
    void grabShouldProceedWhenClientRequestIdIsValid() throws Exception {
        Long screeningId = 2001L;
        Long userId = 1001L;

        when(loginContext.currentLoginId())
                .thenReturn(userId);

        when(grabOrderService.grab(screeningId, userId))
                .thenReturn(GrabOrderVO.of(GrabOrderResultEnum.SUCCESS, "90001", 82L));

        mockMvc.perform(
                post("/ticketing/order/grab")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {
                                   "screeningId": 2001,
                                   "clientRequestId": "client-abc-123"
                                 }
                                 """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(loginContext).currentLoginId();

        verify(grabOrderService).grab(screeningId, userId);
    }
}
