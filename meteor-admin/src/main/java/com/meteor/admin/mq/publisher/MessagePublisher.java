package com.meteor.admin.mq.publisher;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.mq.support.AdminMqFailMsgBuilder;
import com.meteor.admin.service.IMqFailMsgService;
import com.meteor.api.model.AdminMqFailureEntity;
import com.meteor.common.enums.system.ModuleEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.message.UserEventMessage;
import com.meteor.mq.contract.message.UserMessageContract;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 *  商家申请消息发布者
 *
 *
 *  @author Programmer
 *  @date 2026-02-12 18:14
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final MqSender mqSender;
    private final IMqFailMsgService mqFailMsgService;
    private final AdminMqFailMsgBuilder failMsgBuilder;

    public void sendNotify(MerchantApply apply, UserEventMessage msg, String scene) {


        MqSendResult result = mqSender.sendAndWaitConfirm(
                UserMessageContract.Exchange.USER_MESSAGE,
                //"我还是故意不存在,测试发送失败",
                UserMessageContract.RoutingKey.USER_MESSAGE_CREATED,
                msg,
                UserMessageContract.CONFIRM_TIMEOUT
        );

        if (result == null || !result.isAck() || result.noRoute()) {

            String error;

            if (result == null) {
                error = "MQ sendAndWaitConfirm returned null";
            } else if (result.noRoute()) {
                error = "MQ NO_ROUTE";
            } else {
                error = "MQ confirm failed";
            }
            log.error("{} MQ send failed, applyId={}, eventId={}, error={}",
                    scene, apply.getApplyId(), msg.getEventId(), error);

            AdminMqFailureEntity failMsg = failMsgBuilder.build(
                    new AdminMqFailMsgBuilder.BuildCmd(
                            "message_" + apply.getApplyId(),
                            ModuleEnum.APPLY,
                            apply.getApplyId(),
                            UserMessageContract.Exchange.USER_MESSAGE,
                            UserMessageContract.RoutingKey.USER_MESSAGE_CREATED,
                            "user_event_message",
                            msg,
                            error
                    )
            );

            mqFailMsgService.save(failMsg);

            throw new BizException(CommonErrorCode.MQ_SEND_FAILED);
        }
    }
}
