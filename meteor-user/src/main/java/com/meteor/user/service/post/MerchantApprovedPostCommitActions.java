package com.meteor.user.service.post;

import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import com.meteor.mq.contract.merchant.MerchantContract;
import com.meteor.mq.core.MqSender;
import com.meteor.satoken.context.LoginContext;
import com.meteor.user.service.cache.IUserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 商家审核通过后的副作用处理
 * 包括清缓存、踢下线和通知 merchant 模块
 * @author Programmer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantApprovedPostCommitActions {

    private final IUserCacheService userCacheService;
    private final LoginContext loginContext;
    private final MqSender mqSender;

    /**
     * 审核通过后的后续操作：清缓存、踢下线、发送MQ通知
     * 失败策略：失败只告警，不影响数据库已提交的事实
     */
    public void onApproved(MerchantApplyReviewedMessage message) {
        Long userId = message.getUserId();

        try {
            userCacheService.evictUserAll(userId);
            loginContext.kickout(userId);
        } catch (Exception e) {
            log.warn("post-commit evict/kickout failed, userId={}", userId, e);
        }

        try {
            mqSender.send(
                    MerchantContract.Exchange.MERCHANT,
                    MerchantContract.RoutingKey.MERCHANT_ACTIVATED,
                    message
            );
            log.info("post-commit published merchant activated, applyId={}, userId={}",
                    message.getApplyId(), userId);
        } catch (Exception e) {
            log.warn("post-commit publish merchant activated failed (non-critical), applyId={}, userId={}",
                    message.getApplyId(), message.getUserId(), e);
        }
    }
}
