package com.meteor.admin.service.impl;

import com.meteor.admin.domain.assembler.MerchantApplyNotifyMqAssembler;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.service.IMerchantApplyNotifyService;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.message.UserEventMessage;
import com.meteor.mq.contract.message.UserMessageContract;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 *  商家申请审核结果通知服务实现类
 *
 * @author Programmer
 * @date 2026-01-30 21:44
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantApplyNotifyServiceImpl implements IMerchantApplyNotifyService {

    private final MqSender mqSender;
    private final MerchantApplyNotifyMqAssembler assembler;

    @Override
    public void notifyApproved(MerchantApply apply) {
        if (apply == null) {
            return;
        }
        UserEventMessage msg = assembler.toApprovedNotifyMessage(apply);
        sendNotify(apply, msg, "notifyApproved");
    }

    @Override
    public void notifyRejected(MerchantApply apply) {
        if (apply == null) {
            return;
        }
        UserEventMessage msg = assembler.toRejectedNotifyMessage(apply);
        sendNotify(apply, msg, "notifyRejected");
    }

    private void sendNotify(MerchantApply apply, UserEventMessage msg, String scene) {
        MqSendResult result = mqSender.sendAndWaitConfirm(
                UserMessageContract.Exchange.USER_MESSAGE,
                UserMessageContract.RoutingKey.USER_MESSAGE_CREATED,
                msg,
                UserMessageContract.CONFIRM_TIMEOUT
        );

        if (result == null) {
            log.error("{} sendAndWaitConfirm returned null, applyId={}, eventId={}",
                    scene, apply.getApplyId(), msg.getEventId());
            throw new BizException(CommonErrorCode.MQ_SEND_FAILED);
        }

        if (!result.isAck()) {
            log.error("{} MQ confirm failed, applyId={}, eventId={}",
                    scene, apply.getApplyId(), msg.getEventId());
            throw new BizException(CommonErrorCode.MQ_SEND_FAILED);
        }

        if (result.noRoute()) {
            log.warn("{} MQ NO_ROUTE, applyId={}, eventId={}",
                    scene, apply.getApplyId(), msg.getEventId());
        }
    }
}


