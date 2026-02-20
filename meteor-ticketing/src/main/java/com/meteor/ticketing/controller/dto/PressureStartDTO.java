package com.meteor.ticketing.controller.dto;



import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 压测创建请求
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-18 12:13
 */
@Data
public class PressureStartDTO {

    @NotNull
    private Scene scene = Scene.TICKETING_GRAB;

    @NotNull
    @Min(1)
    private Long screeningId;

    @NotNull
    @Min(1)
    @Max(20000)
    private Integer concurrency;

    /**
     * 总请求数
     */
    @NotNull
    @Min(1)
    @Max(5_000_000)
    private Integer totalRequests;

    /**
     * ramp-up 秒数：逐步升压，演示更稳；默认 5 秒
     */
    @Min(0)
    @Max(300)
    private Integer rampUpSeconds = 5;

    /**
     * 单请求超时：默认 2000ms
     */
    @NotNull
    @Min(200)
    @Max(60_000)
    private Integer timeoutMs = 2000;

    /**
     * 指标采样周期（写 Redis / SSE 推送的间隔），默认 1000ms
     */
    @NotNull
    @Min(200)
    @Max(5_000)
    private Integer tickMs = 1000;

    public enum Scene {
        TICKETING_GRAB
    }
}
