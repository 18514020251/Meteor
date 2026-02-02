package com.meteor.ticketing.controller.dto.screening;

import com.meteor.ticketing.controller.enums.SaleModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;


/**
 *  新增电影场次 DTO
 *
 * @author Programmer
 * @date 2026-02-02 10:58
 */
@Data
public class ScreeningCreateDTO {


    @Schema(description = "电影ID", example = "1001")
    @NotNull(message = "电影ID不能为空")
    private Long movieId;

    @Schema(description = "开场时间", example = "2026-02-10T19:30:00")
    @NotNull(message = "开场时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "散场时间", example = "2026-02-10T21:30:00")
    private LocalDateTime endTime;

    @Schema(description = "开售时间", example = "2026-02-01T10:00:00")
    @NotNull(message = "开售时间不能为空")
    private LocalDateTime saleStartTime;

    @Schema(description = "停售时间", example = "2026-02-10T19:30:00")
    private LocalDateTime saleEndTime;


    @Schema(description = "基础票价（分）", example = "4900")
    @NotNull(message = "基础票价不能为空")
    @Min(value = 1, message = "票价必须大于 0")
    private Integer basePrice;

    @Schema(description = "总票数", example = "120")
    @NotNull(message = "总票数不能为空")
    @Min(value = 1, message = "总票数必须大于 0")
    private Integer totalTickets;


    @Schema(description = "销售模式")
    @NotNull(message = "销售模式不能为空")
    private SaleModeEnum saleMode;

}