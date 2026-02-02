package com.meteor.ticketing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.meteor.common.constants.ticketing.TicketingConstants;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.common.enums.ticketing.VersionEnum;
import com.meteor.ticketing.controller.enums.SaleModeEnum;
import com.meteor.ticketing.controller.enums.ScreeningStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 电影场次表
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("screening")
@Schema(description="Screening对象")
public class Screening implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "场次ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "商家ID")
    private Long merchantId;

    @Schema(description = "电影ID")
    private Long movieId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "开售时间")
    private LocalDateTime saleStartTime;

    @Schema(description = "停售时间")
    private LocalDateTime saleEndTime;

    @Schema(description = "场次状态")
    private ScreeningStatusEnum status;

    @Schema(description = "销售模式")
    private SaleModeEnum saleMode;

    @Schema(description = "基础价格(分)")
    private Integer basePrice;

    @Schema(description = "最小价格(分)")
    private Integer minPrice;

    @Schema(description = "最大价格(分)")
    private Integer maxPrice;

    @Schema(description = "总票数")
    private Integer totalTickets;

    @Schema(description = "可用票数")
    private Integer availableTickets;

    @Schema(description = "已售票数")
    private Integer soldTickets;

    @Schema(description = "热度分(展示用)")
    private Long hotScore;

    @Schema(description = "版本号(防超卖)")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "是否删除 0=否 1=是")
    private Integer deleted;

    public Screening initForCreate(Long operatorId, LocalDateTime now) {
        this.setMinPrice(this.getBasePrice());
        this.setMaxPrice(this.getBasePrice());
        this.setAvailableTickets(this.getTotalTickets());
        this.setSoldTickets(TicketingConstants.DEFAULT_TICKETING_SOLD_TICKETS);
        this.setHotScore(TicketingConstants.DEFAULT_TICKETING_HOT_RANK);
        this.setVersion(VersionEnum.INIT.getValue());
        this.setDeleted(DeleteStatus.NORMAL.getCode());

        this.setCreateBy(operatorId);
        this.setUpdateBy(operatorId);
        this.setCreateTime(now);
        this.setUpdateTime(now);
        return this;
    }

}
