package com.meteor.ticketing.service.impl;

import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.ticketing.controller.dto.PressureStartDTO;
import com.meteor.ticketing.controller.vo.PressureStartVO;
import com.meteor.ticketing.service.IPressureTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *  压测任务服务实现
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-18 12:18
 */
@Service
@RequiredArgsConstructor
public class PressureTaskServiceImpl implements IPressureTaskService {
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public PressureStartVO start(PressureStartDTO req) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String taskId = "pt_" + ts + "_" + idGenerator.nextId();

        // TODO: Day1 我们会在这里：写 Redis meta/counter，并异步启动 Runner
        return new PressureStartVO(taskId, "RUNNING");
    }
}
