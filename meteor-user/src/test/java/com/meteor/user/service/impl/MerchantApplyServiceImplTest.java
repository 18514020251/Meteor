package com.meteor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.user.domain.dto.MerchantApplyDTO;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.mapper.MerchantApplyMapper;
import com.meteor.user.mq.publisher.MerchantApplyEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 商家申请服务测试类
 *
 * @author Programmer
 * @date 2026-01-21
 */
@ExtendWith(MockitoExtension.class)
class MerchantApplyServiceImplTest {

    @Mock
    private MerchantApplyMapper merchantApplyMapper;

    @Mock
    private MerchantApplyEventPublisher eventPublisher;

    @InjectMocks
    private MerchantApplyServiceImpl merchantApplyService;

    /**
     * 测试提交商家申请 - 用户已存在未处理申请
     * 预期：抛出OPERATION_NOT_ALLOWED异常
     */
    @Test
    void apply_should_throw_exception_when_pending_apply_exists() {
        // 模拟merchantApplyMapper.exists方法返回true（存在未处理申请）
        Mockito.doReturn(true)
               .when(merchantApplyMapper).exists(Mockito.any(LambdaQueryWrapper.class));
        
        // 创建DTO对象
        MerchantApplyDTO dto = new MerchantApplyDTO();
        dto.setShopName("Test Shop");
        dto.setApplyReason("Test application");
        
        Long userId = 1L;
        
        BizException ex = assertThrows(
                BizException.class,
                () -> merchantApplyService.apply(userId, dto)
        );

        assertEquals(
                CommonErrorCode.OPERATION_NOT_ALLOWED.getCode(),
                ex.getCode()
        );
    }


    /**
     * 测试提交商家申请 - 正常提交
     * 预期：提交成功，插入申请记录并发布事件
     */
    @Test
    void apply_should_submit_successfully_when_no_existing_apply() {
        // 模拟merchantApplyMapper.exists方法返回false（不存在未处理或已通过申请）
        Mockito.doReturn(false)
               .when(merchantApplyMapper).exists(Mockito.any(LambdaQueryWrapper.class));
        
        // 模拟merchantApplyMapper.insert方法返回1（插入成功）
        Mockito.doReturn(1)
               .when(merchantApplyMapper).insert(Mockito.any(MerchantApply.class));
        
        // 模拟eventPublisher.publishCreated方法
        Mockito.doNothing()
               .when(eventPublisher).publishCreated(Mockito.any(MerchantApply.class));
        
        // 创建DTO对象
        MerchantApplyDTO dto = new MerchantApplyDTO();
        dto.setShopName("Test Shop");
        dto.setApplyReason("Test application");
        
        Long userId = 1L;
        
        // 执行申请操作
        merchantApplyService.apply(userId, dto);
        
        // 验证merchantApplyMapper.insert方法被调用
        Mockito.verify(merchantApplyMapper, Mockito.times(1)).insert(Mockito.any(MerchantApply.class));
        
        // 验证eventPublisher.publishCreated方法被调用
        Mockito.verify(eventPublisher, Mockito.times(1)).publishCreated(Mockito.any(MerchantApply.class));
    }
}
