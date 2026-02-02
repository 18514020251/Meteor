package com.meteor.ticketing.service.impl;

import com.meteor.ticketing.domain.entity.HotRank;
import com.meteor.ticketing.mapper.HotRankMapper;
import com.meteor.ticketing.service.IHotRankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 场次热度榜 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
public class HotRankServiceImpl extends ServiceImpl<HotRankMapper, HotRank> implements IHotRankService {

}
