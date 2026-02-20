package com.meteor.ticketing.service;

import com.meteor.ticketing.controller.dto.PressureStartDTO;
import com.meteor.ticketing.controller.vo.PressureStartVO;

/**
 *  压测任务服务
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-18 12:14
 */
public interface IPressureTaskService {
    PressureStartVO start(PressureStartDTO req);
}
