package com.meteor.admin.service;

import com.meteor.admin.controller.vo.OnlineUserVO;
import com.meteor.common.domain.PageResult;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-08 22:00
 */
public interface IUserCacheService {

    PageResult<OnlineUserVO> pageOnlineUsers(int pageNum, int pageSize);
}
