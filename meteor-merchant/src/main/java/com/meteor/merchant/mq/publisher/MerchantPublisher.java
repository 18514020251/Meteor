package com.meteor.merchant.mq.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.meteor.api.model.MerchantMqFailureEntity;
import com.meteor.common.constants.MqConstants;
import com.meteor.common.enums.system.ModuleEnum;
import com.meteor.common.enums.system.mq.MessageStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.merchant.service.IMerchantMqFailMsgService;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.UserDeactivatedMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 商家相关的 MQ 发布实现
 * 插入失败时，将记录存储在 merchant_mq_fail_msg 表中供后续补偿重试。
 *
 * @author Programmer
 * @date 2026-02-01 15:59
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MerchantPublisher {

    private final MqSender mqSender;
    private final IMerchantMqFailMsgService merchantMqFailMsgService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public void publishUserDeactivatedEvent(UserDeactivatedMessage msg) {
        // 参数校验
        if (msg == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "msg is null");
        }
        if (msg.getUserId() == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "userId is null");
        }
        if (msg.getTimestamp() == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "timestamp is null");
        }

        try {
            MqSendResult result = mqSender.sendAndWaitConfirm(
                    MerchantApplyContract.Exchange.USER_EVENT,
                    MerchantApplyContract.RoutingKey.USER_DEACTIVATED,
                    msg,
                    MerchantApplyContract.CONFIRM_TIMEOUT
            );

            if (!result.isAck()) {
                throw new BizException(CommonErrorCode.MQ_SEND_FAILED);
            }

            log.debug("MQ sent: user.deactivated, userId={}, ts={}",
                    msg.getUserId(), msg.getTimestamp());

        } catch (Exception ex) {
            log.error("MQ send failed", ex);
            saveMqFailure(msg, ex);
        }
    }

    /**
     * 消息发送失败保存记录到 merchant_mq_fail_msg 表
     *
     * @param msg 消息体
     * @param ex  异常信息
     */
    private void saveMqFailure(UserDeactivatedMessage msg, Exception ex) {
        try {
            MerchantMqFailureEntity failMsg = buildFailureEntity(msg, ex);
            merchantMqFailMsgService.save(failMsg);

            log.info("Saved failed MQ message to database, msgId={}, userId={}",
                    failMsg.getMsgId(), msg.getUserId());

        } catch (Exception dbEx) {
            log.error("Failed to save MQ failure record: userId={}, error={}",
                    msg.getUserId(), dbEx.getMessage(), dbEx);
        }
    }

    private MerchantMqFailureEntity buildFailureEntity(UserDeactivatedMessage msg, Exception ex) {

        String payloadJson = safeSerialize(msg);

        LocalDateTime now = LocalDateTime.now();

        MerchantMqFailureEntity failMsg = new MerchantMqFailureEntity();
        failMsg.setMsgId("user_deactivated_" + msg.getUserId());
        failMsg.setModuleName(ModuleEnum.MERCHANT);
        failMsg.setBizId(msg.getUserId());
        failMsg.setExchangeName(MerchantApplyContract.Exchange.USER_EVENT);
        failMsg.setRoutingKey(MerchantApplyContract.RoutingKey.USER_DEACTIVATED);
        failMsg.setPayload(payloadJson);
        failMsg.setStatus(MessageStatusEnum.PENDING);
        failMsg.setRetryCnt(MqConstants.DEFAULT_RETRY_COUNT);
        failMsg.setNextRetryTime(now.plusMinutes(MqConstants.DEFAULT_NEXT_RETRY_TIME));
        failMsg.setLastError(ex.getMessage());
        failMsg.setCreateTime(now);
        failMsg.setUpdateTime(now);
        failMsg.setTopic("user_deactivated");

        return failMsg;
    }

    private String safeSerialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message to JSON", e);
            return "{\"error\":\"serialize_failed\"}";
        }
    }
}
